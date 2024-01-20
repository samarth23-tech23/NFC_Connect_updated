package com.example.nfcconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class contactUs extends AppCompatActivity {
    EditText mailId, message, name,phoneno;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        mailId = findViewById(R.id.emailaddress);
        message = findViewById(R.id.msg);
        name = findViewById(R.id.myname);
        phoneno = findViewById(R.id.phoneNo);
        btnSend = findViewById(R.id.sendBtn);

        btnSend.setOnClickListener(new View.OnClickListener() {

            String mail_txt = mailId.getText().toString();
            String msg_txt = message.getText().toString();
            String name_txt = name.getText().toString();
            String phone_txt = phoneno.getText().toString();
            public void onClick(View view) {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "www.samarthamodakr@gmail.com"});
                email.putExtra(Intent.EXTRA_TEXT, msg_txt);

//need this to prompts email client only
                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email client :"));
            }
        });

    }
}



            /*
            public void onClick(View view) {
                String mail_txt = mailId.getText().toString();
                String msg_txt = message.getText().toString();
                String name_txt = name.getText().toString();
                String phone_txt = phoneno.getText().toString();

                String mailto = "mailto:www.samarthamodkar@gmail.com" +

                        "&body=" + Uri.parse(msg_txt);
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));
                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(contactUs.this, "Error to open email app", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
*/

/*
 btnSend.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String mailto = "mailto:www.samarthamodkar@gmail.com" +
                    "?cc=" +
                    "&subject=" + Uri.encode("your subject") +
                    "&body=" + Uri.encode("your mail body");
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailto));

            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Error to open email app", Toast.LENGTH_SHORT).show();
            }
        }
    });

 */
