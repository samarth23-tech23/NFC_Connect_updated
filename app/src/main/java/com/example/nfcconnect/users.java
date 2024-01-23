package com.example.nfcconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

public class users extends AppCompatActivity {
    Button btn;
    TextView tv1;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


    }
    public void viewUsers(View v) {
        Intent intent = new Intent(users.this, add_newuser.class);
        startActivity(intent);

    }
}