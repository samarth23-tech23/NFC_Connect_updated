package com.example.nfcconnect;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogActivity extends AppCompatActivity {
    Button btn;
    TextView tv1;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        btn = findViewById(R.id.setBtn);
        tv1 = findViewById(R.id.output);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("username");
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("raspberry pi/Logs");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        StringBuilder sb = new StringBuilder();
                        for (DataSnapshot uidSnapshot : dataSnapshot.getChildren()) {
                            // Loop through the auto-generated UID nodes
                            for (DataSnapshot nameSnapshot : uidSnapshot.getChildren()) {
                                String date = nameSnapshot.child("Date").getValue(String.class);
                                String time = nameSnapshot.child("Time").getValue(String.class);
                                String user = nameSnapshot.child("User").getValue(String.class);

                                // Check if the user matches the provided username
                                if (user != null && user.equals(name)) {
                                    sb.append("Date: ").append(date).append("\n");
                                    sb.append("Time: ").append(time).append("\n");
                                    sb.append("User: ").append(user).append("\n");
                                }
                            }
                        }

                        // Display the filtered logs in the TextView
                        tv1.setText(sb.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle onCancelled if needed
                        Log.e(TAG, "Database Error: " + databaseError.getMessage());
                        Toast.makeText(LogActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }
}
