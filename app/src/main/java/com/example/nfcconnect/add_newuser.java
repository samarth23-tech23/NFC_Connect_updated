package com.example.nfcconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class add_newuser extends AppCompatActivity {

    EditText reguName, regemail, regPass, regnPass;
    Button addBtn;
    DatabaseReference reference;
    String encPassword, encNFCpass, name, emailId, aesKeyString, systemId;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_newuser);

        reguName = findViewById(R.id.uName);
        regemail = findViewById(R.id.eId);
        regPass = findViewById(R.id.pId);
        regnPass = findViewById(R.id.npId);
        addBtn = findViewById(R.id.addbtn2);

        sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
        systemId = sharedPreferences.getString("systemId", "");

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = reguName.getText().toString();
                emailId = regemail.getText().toString();
                String password = regPass.getText().toString();
                String NFC_Password = regnPass.getText().toString();

                if (name.length() != 0 && emailId.length() != 0 && password.length() != 0 && NFC_Password.length() != 0) {
                    try {
                        // Generate a new AES key for each user
                        SecretKey aesKey = generateAESKey();

                        // Use the same AES key for both password and NFC password encryption
                        encPassword = encryptAES(password, aesKey);
                        encNFCpass = encryptAES(NFC_Password, aesKey);

                        reference = FirebaseDatabase.getInstance().getReference("lockSystems").child(systemId).child("users");

                        // Create a map to store the user data
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("emailId", emailId);
                        userData.put("encPassword", encPassword);
                        userData.put("encNFCpass", encNFCpass);
                        userData.put("aesKey", encodeAESKeyToString(aesKey)); // Store the AES key as a Base64 string

                        // Add user data under the lock system node
                        reference.child(name).setValue(userData);

                        Toast.makeText(add_newuser.this, "User added successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(add_newuser.this, "Encryption error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(add_newuser.this, "Fill all the required fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // Use AES-128
        return keyGenerator.generateKey();
    }

    private String encryptAES(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT); // Convert the bytes to a Base64-encoded string
        }
        return null;
    }

    private String encodeAESKeyToString(SecretKey secretKey) {
        // Convert the SecretKey to a Base64-encoded string
        byte[] keyBytes = secretKey.getEncoded();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return android.util.Base64.encodeToString(keyBytes, android.util.Base64.DEFAULT);
        }
        return null;
    }
}
