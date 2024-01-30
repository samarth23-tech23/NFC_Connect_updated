package com.example.nfcconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class reset extends AppCompatActivity {
    String name, oldP, newP, getPassword, txtResponse2, username, encNFCpass, combinedUP, pass, systemId;
    TextView oldpass, newpass, uname;
    Button chkBtn, resetBtn;

    FirebaseDatabase rootNode;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    SharedPreferences sharedPreferences;
    OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        Intent intent = getIntent();
        name = intent.getStringExtra("username");
        oldpass = findViewById(R.id.oldPass);
        newpass = findViewById(R.id.newPass);
        chkBtn = findViewById(R.id.chkBtn);
        resetBtn = findViewById(R.id.resetBtn);
        uname = findViewById(R.id.uName2);

        newpass.setVisibility(View.INVISIBLE);

        // Retrieve systemId from SharedPreferences
        sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
        systemId = sharedPreferences.getString("systemId", "");

        chkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = uname.getText().toString();
                oldP = oldpass.getText().toString();

                // Database fetching
                reference.child("lockSystems").child(systemId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(username)) {
                            getPassword = snapshot.child(username).child("encNFCpass").getValue(String.class);

                            // Decrypt the stored password using AES
                            try {
                                String aesKey = snapshot.child(username).child("aesKey").getValue(String.class);
                                pass = decryptAES(getPassword, aesKey);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(reset.this, "Decryption error", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (pass.equals(oldP)) {
                                Toast.makeText(reset.this, "Password Matched", Toast.LENGTH_SHORT).show();
                                newpass.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(reset.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(reset.this, "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPass = newpass.getText().toString();
                final String[] encryptedPass = {null}; // Store encryptedPass in a final array

                // Database fetching
                reference.child("lockSystems").child(systemId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(username)) {
                            try {
                                String aesKey = snapshot.child(username).child("aesKey").getValue(String.class);
                                encryptedPass[0] = encryptAES(newPass, aesKey);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(reset.this, "Encryption error", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            combinedUP = username + ":" + encryptedPass[0];
                            Toast.makeText(reset.this, combinedUP, Toast.LENGTH_LONG).show();

                            // For resetting, send combinedUP to the server (e.g., via HTTP request)
                            // Note: The server URL should be updated based on your requirements
                            RequestBody formbody3 = new FormBody.Builder()
                                    .add("resetpassword", combinedUP)
                                    .add("activity", "reset")
                                    .build();

                            Request request3 = new Request.Builder()
                                    .url("https://full-honeybee-joint.ngrok-free.app/reset")  // Update with your server URL
                                    .post(formbody3)
                                    .build();

                            Toast.makeText(reset.this, "Sending request for Reset..", Toast.LENGTH_SHORT).show();

                            okHttpClient.newCall(request3).enqueue(new Callback() {
                                @Override
                                public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                txtResponse2 = response.body().string();
                                                Toast.makeText(reset.this, "Waiting for response ..", Toast.LENGTH_SHORT).show();
                                                Toast.makeText(reset.this, txtResponse2, Toast.LENGTH_LONG).show();
                                                // Update the Firebase database with the new NFC password
                                                reference.child("lockSystems").child(systemId).child("users").child(username).child("encNFCpass").setValue(encryptedPass[0]);

                                                Toast.makeText(reset.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Socket response error", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                        } else {
                            Toast.makeText(reset.this, "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    private String decryptAES(String encryptedText, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        byte[] decodedKey = Base64.getDecoder().decode(key);
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private String encryptAES(String plainText, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        byte[] decodedKey = Base64.getDecoder().decode(key);
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
