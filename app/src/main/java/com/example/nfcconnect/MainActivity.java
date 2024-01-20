package com.example.nfcconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is logged in using shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
        String username = sharedPreferences.getString("st", "");

        if (!username.isEmpty()) {
            // User is logged in, go to Homepage
            Intent intent = new Intent(this, Homepage.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish(); // Finish MainActivity to prevent going back
        }

        setContentView(R.layout.activity_main);
    }

    public void loginButton(View v){
        Toast.makeText(this, "Login yourself", Toast.LENGTH_SHORT).show();
        Intent login = new Intent(this, loginActivity.class);
        startActivity(login);
    }

    public void regButton(View w){
        Toast.makeText(this, "Register yourself", Toast.LENGTH_SHORT).show();
        Intent register = new Intent(this, regActivity.class);
        startActivity(register);
    }

    public void aboutUsBtn(View e){
        startActivity(new Intent(this, AboutUs.class));
    }

    public void button_contactUs(View z){
        Intent contact_us = new Intent(this, contactUs.class);
        startActivity(contact_us);
    }
}
