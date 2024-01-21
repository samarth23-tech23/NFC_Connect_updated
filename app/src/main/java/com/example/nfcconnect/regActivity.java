package com.example.nfcconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Call;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.io.IOException;
import java.util.Base64; // Import Base64
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class regActivity extends AppCompatActivity implements TextWatcher {

    EditText reguName, regemail, regPass, regnPass;
    Button rButton;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    String encPassword, encNFCpass, name, emailId, aesKeyString; // Store the AES key as a string
    SecretKey aesKey;

    OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        reguName = findViewById(R.id.uName);
        regemail = findViewById(R.id.eId);
        rButton = findViewById(R.id.regbtn2);
        regPass = findViewById(R.id.pId);
        regnPass = findViewById(R.id.npId);

        // Generate the AES key once in the onCreate method
        try {
            aesKey = generateAESKey();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(regActivity.this, "AES Key generation error", Toast.LENGTH_SHORT).show();
        }

        rButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = reguName.getText().toString();
                emailId = regemail.getText().toString();
                String password = regPass.getText().toString();
                String NFC_Password = regnPass.getText().toString();

                if (name.length() != 0 && emailId.length() != 0 && password.length() != 0 && NFC_Password.length() != 0) {
                    try {
                        // Use the same AES key for both password and NFC password encryption
                        encPassword = encryptAES(password, aesKey);
                        encNFCpass = encryptAES(NFC_Password, aesKey);

                        rootNode = FirebaseDatabase.getInstance();
                        reference = rootNode.getReference("lockSystems"); // Root node for lock systems

                        // Create a unique identifier for each lock system
                        String lockSystemId = generateLockSystemId(name);

                        // Create a map to store the user data
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("emailId", emailId);
                        userData.put("encPassword", encPassword);
                        userData.put("encNFCpass", encNFCpass);
                        userData.put("aesKey", encodeAESKeyToString(aesKey)); // Store the AES key as a Base64 string

                        // Add user data under the lock system node
                        reference.child(lockSystemId).child("users").child(name).setValue(userData);

                        showLockSystemIdDialog(lockSystemId);

                        Toast.makeText(regActivity.this, "Successfully Registered with Name " + name, Toast.LENGTH_SHORT).show();

                        // Create a request body for the HTTP request to the Raspberry Pi
                        RequestBody formbody4 = new FormBody.Builder()
                                .add("name", name)
                                .add("email", emailId)
                                .add("password", password)
                                .add("nfc_password", NFC_Password)
                                .add("aes_key", encodeAESKeyToString(aesKey))
                                .build();

                        // Create an HTTP request to register on the Raspberry Pi
                        Request request3 = new Request.Builder()
                                .url("https://full-honeybee-joint.ngrok-free.app/register")
                                .post(formbody4)
                                .build();

                        Toast.makeText(regActivity.this, "Sending request for Register", Toast.LENGTH_SHORT).show();

                        // Execute the HTTP request asynchronously
                        okHttpClient.newCall(request3).enqueue(new Callback() {
                            @Override
                            public void onFailure(okhttp3.Call call, IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Socket response error", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(regActivity.this, "Waiting for response ...", Toast.LENGTH_SHORT).show();
                                        // Handle the response from the Raspberry Pi
                                        try {
                                            String responseText = response.body().string();
                                            // Assuming that responseText is the UID you want to associate with the user
                                            if (!responseText.isEmpty()) {
                                                // Push the UID into the user's data in the Firebase Realtime Database
                                                DatabaseReference userReference = reference.child(name); // Assuming 'name' is the user's name
                                                userReference.child("uid").setValue(responseText);
                                                Toast.makeText(getApplicationContext(), " Recived: " + responseText, Toast.LENGTH_LONG).show();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(regActivity.this, "Encryption error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(regActivity.this, "Fill all the required fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        regPass.addTextChangedListener(this);
    }

    private void showLockSystemIdDialog(String lockSystemId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lock System ID");
        builder.setMessage("Your Lock System ID is: " + lockSystemId);
        builder.setPositiveButton("Copy to Clipboard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Copy the lock system ID to the clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Lock System ID", lockSystemId);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(regActivity.this, "Lock System ID copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("OK", null);
        builder.show();
    }

    private SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // Use AES-128
        return keyGenerator.generateKey();
    }

    private String encryptAES(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(encryptedBytes); // Convert the bytes to a Base64-encoded string
        }
        return null;
    }

    private String encodeAESKeyToString(SecretKey secretKey) {
        // Convert the SecretKey to a Base64-encoded string
        byte[] keyBytes = secretKey.getEncoded();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(keyBytes);
        }
        return null;
    }

    private String generateLockSystemId(String userName) {
        // Generate a lock system ID based on the user's name and a random number
        int randomNumber = new Random().nextInt(100000); // Adjust the range as needed
        return userName + "_" + randomNumber;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // This method is called before the text is changed
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // This method is called when the text is changed
    }

    @Override
    public void afterTextChanged(Editable editable) {
        // This method is called after the text has changed
        // Perform the operation when the password text is modified
    }
}
