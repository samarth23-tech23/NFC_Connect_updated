package com.example.nfcconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Homepage extends AppCompatActivity {
    TextView Tmonth, Tday, Tyear, tname;
    // Add any other variables you need here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is logged in using shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
        String username = sharedPreferences.getString("st", "");

        if (username.isEmpty()) {
            // User is not logged in, go to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish Homepage to prevent going back
        }

        setContentView(R.layout.activity_homepage);

        Tmonth = findViewById(R.id.month);
        Tday = findViewById(R.id.date);
        Tyear = findViewById(R.id.year);
        tname = findViewById(R.id.tvName);
        // Initialize other views as needed

        Date currentTime = Calendar.getInstance().getTime();

        // Format the date using SimpleDateFormat with desired format
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM, dd, yyyy", Locale.US);
        String formattedDate = sdf.format(currentTime);

        String[] splitDate = formattedDate.split(",");

        tname.setText("Welcome back, " + username);
        Tmonth.setText(splitDate[0].trim()); // Month
        Tday.setText(splitDate[1].trim());   // Day
        Tyear.setText(splitDate[2].trim());  // Year
    }

    public void logout(View c) {
        // Clear user session and go to MainActivity
        SharedPreferences preferences = getSharedPreferences("session", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish(); // Finish Homepage to prevent going back
    }

    public void reset(View v) {
        Intent intent = new Intent(Homepage.this, reset.class);
        startActivity(intent);
    }

    public void checkUsers(View v) {
        Intent intent = new Intent(Homepage.this, users.class);
        startActivity(intent);
    }

    public void log(View r) {
        Intent intent = new Intent(getApplicationContext(), LogActivity.class);
        intent.putExtra("username", tname.getText().toString().replace("Welcome back, ", ""));
        startActivity(intent);
    }
}
