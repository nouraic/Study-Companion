package com.oneplusplus.christopher.studycompanion;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ForgotPassword extends AppCompatActivity {

    //stores all of the users in the database's emails
    private ArrayList<String> userEmails = new ArrayList<>();
    private DatabaseReference drefUserEmails = FirebaseDatabase.getInstance().getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initOnClickActions();

    }

    //gets all the user's email addressed and checks if the email the user typed in exists
    //if it does it sends the email if not sets prompt that the email was not found
    private void emailAction() {
        drefUserEmails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userEmails.clear();
                Log.d("COURSES", "HERE: ");

                for(DataSnapshot index : dataSnapshot.getChildren()) {
                    if(index.child("UserInfo").child("Email").getValue() != null) {
                        String userEmail = index.child("UserInfo").child("Email").getValue().toString();
                        userEmails.add(userEmail.toLowerCase());
                        Log.d("COURSES", "Adding Email: "+userEmail);

                    }
                }
                //get the email in the box
                EditText emailView = findViewById(R.id.emailTextView);
                String emailText = emailView.getText().toString();
                //verify it exists and take action
                if(emailExists(emailText) ){
                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailText)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("COURSES", "EMAIL Sent: ");
                                    }
                                }
                            });
                    // go to login -- need to get it to verify user
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

                    //toast the user that they are expecting an email to verify their account
                    Toast.makeText(ForgotPassword.this, "Password reset link has been sent " +
                            "please check your email.",Toast.LENGTH_LONG).show();

                    startActivity(intent);  //bring them back to the log in screen
                    finish();
                }
                else {
                    TextView userFound = findViewById(R.id.userFoundText);
                    userFound.setText("No user with that email found.");
                    Log.d("COURSES", "Email not found: ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //returns true if the email was found false otherwise
    private boolean emailExists(String email) {
        email = email.toLowerCase();
        if(userEmails.indexOf(email) == -1){
            return false;
        }
        return true;
    }

    private void initOnClickActions() {
        Button sendEmail = findViewById(R.id.resetEmailButton);

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()) {
                    emailAction();
                }
                else { Toast.makeText(ForgotPassword.this, "Cannot connect to the internet",Toast.LENGTH_LONG).show();}

            }
        });

        EditText emailView = findViewById(R.id.emailTextView);
        emailView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                TextView userFound = findViewById(R.id.userFoundText);
                userFound.setText("");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
