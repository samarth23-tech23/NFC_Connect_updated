// File: users.java

package com.example.nfcconnect;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class users extends AppCompatActivity {

    private TextView outputTextView;
    private String systemId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        outputTextView = findViewById(R.id.output);

        // Retrieve systemId from SharedPreferences or your session management
        sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
        systemId = sharedPreferences.getString("systemId", "");

        // Call the method to fetch and display users
        fetchAndDisplayUsers();
    }

    // Method to fetch and display users from Firebase
    private void fetchAndDisplayUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("lockSystems")
                .child(systemId)
                .child("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder usersData = new StringBuilder();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Assuming the user data structure has a "name" field
                    String userName = userSnapshot.child("name").getValue(String.class);

                    usersData.append("Name: ").append(userName).append("\n");
                }

                // Display the user data in the TextView
                outputTextView.setText(usersData.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    // Add any other methods or functionality as needed
}
