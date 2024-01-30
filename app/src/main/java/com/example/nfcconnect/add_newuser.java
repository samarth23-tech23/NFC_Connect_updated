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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class add_newuser extends AppCompatActivity {

    EditText reguName, regemail, regPass, regnPass;
    Button addBtn;
    DatabaseReference reference;
    String encPassword, encNFCpass, name, emailId, aesKeyString, systemId;
    SharedPreferences sharedPreferences;

    private OkHttpClient okHttpClient = new OkHttpClient();

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

                        // Request and receive code
                        String combinedUP = name + ":" + encNFCpass;

                        // Create a request body for the HTTP request to the Raspberry Pi
                        RequestBody formbody4 = new FormBody.Builder()
                                .add("registerpassword", combinedUP)
                                .add("activity", "adduser")
                                .add("device_id", systemId)
                                .build();

                        // Create an HTTP request to register on the Raspberry Pi
                        Request request3 = new Request.Builder()
                                .url("https://full-honeybee-joint.ngrok-free.app/register")
                                .post(formbody4)
                                .build();

                        // Set the timeout for the HTTP request
                        okHttpClient.newBuilder()
                                .readTimeout(120, TimeUnit.SECONDS)
                                .writeTimeout(120, TimeUnit.SECONDS)
                                .connectTimeout(120, TimeUnit.SECONDS)
                                .build();

                        Toast.makeText(add_newuser.this, "Sending request for Register", Toast.LENGTH_SHORT).show();

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
                                        Toast.makeText(add_newuser.this, "Waiting for response ...", Toast.LENGTH_SHORT).show();
                                        // Handle the response from the Raspberry Pi
                                        try {
                                            String responseText = response.body().string();
//                                         //   Toast.makeText(add_newuser.this, responseText, Toast.LENGTH_SHORT).show();
                                            // Assuming that responseText is the UID you want to associate with the user
                                            if (!responseText.equals("done")) {
                                                // Poll until the expected response is received or timeout occurs
                                                long startTime = System.currentTimeMillis();
                                                pollForFinalResponse(startTime, request3);
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

    private void pollForFinalResponse(long startTime, Request request) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String finalResponseText = "";
                    while (!finalResponseText.equals("done")) {
                        // Execute the HTTP request synchronously to get the updated response
                        Response finalResponse = okHttpClient.newCall(request).execute();
                        finalResponseText = finalResponse.body().string();

                        // Check if the timeout has occurred
                        if (System.currentTimeMillis() - startTime > 120 * 1000) {
                            break;
                        }

                        Thread.sleep(1000); // Sleep for 1 second before checking again
                    }

                    // Now process the final response on the main thread
                    processFinalResponse(finalResponseText);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void processFinalResponse(String finalResponseText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(add_newuser.this, finalResponseText, Toast.LENGTH_SHORT).show();
                // Handle the response as needed
            }
        });
    }
}
