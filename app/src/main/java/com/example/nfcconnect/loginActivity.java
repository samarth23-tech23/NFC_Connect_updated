package com.example.nfcconnect;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class loginActivity extends AppCompatActivity {
    EditText mUsername, mPassword, mSystemId;
    Button mLoginBtn;
    FirebaseDatabase rootNode;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    String getPassword, pass, uName, systemId;

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
            finish(); // Finish LoginActivity to prevent going back
        }

        setContentView(R.layout.activity_login);
        mUsername = findViewById(R.id.l_uName);
        mPassword = findViewById(R.id.l_password);
        mSystemId = findViewById(R.id.systemId);  // Added field for system ID
        mLoginBtn = findViewById(R.id.loginBtn);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String l_password = mPassword.getText().toString();
                uName = mUsername.getText().toString();
                systemId = mSystemId.getText().toString();  // Get the system ID from the input field

                if (uName.isEmpty() || l_password.isEmpty() || systemId.isEmpty()) {
                    Toast.makeText(loginActivity.this, "Please enter both username, password, and system ID!", Toast.LENGTH_SHORT).show();
                    return;
                }

                reference.child("lockSystems").child(systemId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean userFound = false;

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String currentUsername = userSnapshot.getKey();
                            if (currentUsername != null && currentUsername.equals(uName)) {
                                userFound = true;
                                getPassword = userSnapshot.child("encPassword").getValue(String.class);
                                String getKey = userSnapshot.child("aesKey").getValue(String.class);

                                try {
                                    // Decode the AES key from Base64
                                    byte[] decodedKey = Base64.getDecoder().decode(getKey);
                                    SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

                                    // Decrypt the stored password using AES
                                    pass = decryptAES(getPassword, secretKey);

                                    if (l_password.equals(pass)) {
                                        // Passwords match, user is logged in successfully
                                        // Save the session and FCM token in shared preferences
                                        saveSessionAndToken(uName, systemId);

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
                                break; // No need to continue iterating once the user is found
                            }
                        }

                        if (!userFound) {
                            Toast.makeText(loginActivity.this, "User not found in the specified lock system!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    private String decryptAES(String encryptedText, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decode the Base64-encoded encrypted text
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

            // Decrypt the password
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // Convert decrypted bytes to String
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "Decryption Error";
        }
    }

    private void saveSessionAndToken(String username, String systemId) {
        // Save the session in shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("st", username);
        editor.putString("systemId", systemId); // Store the systemId in shared preferences
        editor.apply();

        // Obtain the FCM token and log it
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();

                        // Log the FCM token
                        Log.d(TAG, "FCM Token: " + fcmToken);

                        // Save the FCM token in shared preferences
                        SharedPreferences fcmPreferences = getSharedPreferences("fcm", MODE_PRIVATE);
                        SharedPreferences.Editor fcmEditor = fcmPreferences.edit();
                        fcmEditor.putString("fcmToken", fcmToken);
                        fcmEditor.apply();
                    } else {
                        Log.e(TAG, "Error getting FCM token: " + task.getException());
                        // Handle the error if needed
                    }
                });
    }

}
