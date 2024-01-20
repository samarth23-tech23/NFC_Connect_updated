package com.example.nfcconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class loginActivity extends AppCompatActivity {
    EditText mUsername, mPassword;
    Button mLoginBtn;
    FirebaseDatabase rootNode;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    String getPassword, pass, uName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already logged in using shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
        String username = sharedPreferences.getString("st", "");

        if (!username.isEmpty()) {
            // User is already logged in, redirect to Homepage
            Intent intent = new Intent(this, Homepage.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish(); // Finish loginActivity to prevent going back
        }

        setContentView(R.layout.activity_login);
        mUsername = findViewById(R.id.l_uName);
        mPassword = findViewById(R.id.l_password);
        mLoginBtn = findViewById(R.id.loginBtn);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String l_password = mPassword.getText().toString();
                uName = mUsername.getText().toString();

                if (uName.isEmpty() || l_password.isEmpty()) {
                    Toast.makeText(loginActivity.this, "Please enter both username and password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                reference.child("app").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(uName)) {
                            getPassword = snapshot.child(uName).child("encPassword").getValue(String.class);
                            String getKey = snapshot.child(uName).child("aesKey").getValue(String.class);
                            Toast.makeText(loginActivity.this, getKey, Toast.LENGTH_SHORT).show();

                            try {
                                // Decode the AES key from Base64
                                byte[] decodedKey;
                                decodedKey = Base64.getDecoder().decode(getKey);
                                SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

                                // Decrypt the stored password using AES
                                pass = decryptAES(getPassword, secretKey);

                                if (l_password.equals(pass)) {
                                    // Passwords match, user is logged in successfully
                                    // Save the session in shared preferences
                                    SharedPreferences sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("st", uName);
                                    editor.apply();

                                    Toast.makeText(loginActivity.this, "Successfully logged in!", Toast.LENGTH_SHORT).show();

                                    // Opening new Activity
                                    Intent intent = new Intent(loginActivity.this, Homepage.class);
                                    intent.putExtra("username", uName);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(loginActivity.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(loginActivity.this, "Decryption error", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(loginActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    private String decryptAES(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
