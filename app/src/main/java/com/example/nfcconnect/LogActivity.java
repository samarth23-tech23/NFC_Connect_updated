package com.example.nfcconnect;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
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
    String name, systemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        btn = findViewById(R.id.setBtn);
        tv1 = findViewById(R.id.output);

        // Retrieve systemId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);
        systemId = sharedPreferences.getString("systemId", "");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("username");
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("deviceLogs").child(systemId).child(name);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        StringBuilder sb = new StringBuilder();
                        for (DataSnapshot logSnapshot : dataSnapshot.getChildren()) {
                            String date = logSnapshot.child("Date").getValue(String.class);
                            String time = logSnapshot.child("Time").getValue(String.class);

                            sb.append("Username: ").append(name).append("\n");
                            sb.append("Date: ").append(date).append("\n");
                            sb.append("Time: ").append(time).append("\n\n");
                        }

                        // Display the logs in the TextView
                        if (sb.length() > 0) {
                            tv1.setText(sb.toString());
                        } else {
                            tv1.setText("No logs found for the user: " + name);
                        }
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
