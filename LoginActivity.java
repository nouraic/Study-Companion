package com.oneplusplus.christopher.studycompanion;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText email;
    private EditText pass;
    private Button logButton;
    private TextView createaccountButton;
    private TextView forgotPassword;
    private TextView loggingInView;
    private ProgressBar loggingInSpinner;
    private View view;
    private TextView prompt;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        view = findViewById(android.R.id.content);

        firebaseAuth = FirebaseAuth.getInstance();

        email = (EditText)findViewById(R.id.cemail);
        pass = (EditText)findViewById(R.id.cpass);
        logButton = (Button)findViewById(R.id.clogButton);
        createaccountButton = (TextView)findViewById(R.id.ccreateAccountButton);
        forgotPassword = (TextView)findViewById(R.id.forgotPasswordButton);
        loggingInView = (TextView)findViewById(R.id.loggingInText);
        loggingInSpinner = (ProgressBar)findViewById(R.id.loginSpinner);
        prompt = findViewById(R.id.badLoginPrompt);

        loggingInView.setVisibility(view.GONE);
        loggingInSpinner.setVisibility(view.GONE);

        logButton.setOnClickListener(this);
        createaccountButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);

        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            Log.d("CURRENT USER", FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
            if (auth.getCurrentUser() != null) {
                Intent intent = new Intent(LoginActivity.this, CourseOverviewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }catch(Exception e) {};


    }

    //function to log the user in, checks first to see if the email they entered has been verified
    //as well as their password matches the password for that email
    public void login() {
        final String emailString = email.getText().toString();
        final String passwordString = pass.getText().toString();//pass.getText().toString();

        //verify fields aren't empty
        if( (emailString.length() > 0) && (passwordString.length() > 0) ) {
            firebaseAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        checkIfEmailVerified();
                    }
                    else {
                        prompt.setText("Error: Bad Username or Password");
                    }
                }
            });
        }

    }

    //function to check if this user's email has been already verified
    private void checkIfEmailVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user.isEmailVerified()) {    //if account is verified log the user in
            finish();
            showLoadItems();
            Intent intent = new Intent(LoginActivity.this, CourseOverviewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            prompt.setText("Account has not been verified yet");
            FirebaseAuth.getInstance().signOut();
        }
    }

    private void showLoadItems() {
        loggingInView.setVisibility(view.VISIBLE);
        loggingInSpinner.setVisibility(view.VISIBLE);
        email.setVisibility(view.GONE);
        pass.setVisibility(view.GONE);
        logButton.setVisibility(view.GONE);
        createaccountButton.setVisibility(view.GONE);
        forgotPassword.setVisibility(view.GONE);
        prompt.setVisibility(view.GONE);
    }

    @Override
    public void onClick(View view) {
        if(view == logButton) {
            if(isNetworkAvailable()) {
                login();
            } else { Toast.makeText(LoginActivity.this, "Cannot connect to the internet", Toast.LENGTH_LONG).show();}
        }

        if(view == createaccountButton) {
            startActivity(new Intent(getApplicationContext(),CreateAccountActivity.class));
        }

        if(view == forgotPassword){
            startActivity(new Intent(getApplicationContext(),ForgotPassword.class));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
