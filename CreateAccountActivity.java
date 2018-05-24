package com.oneplusplus.christopher.studycompanion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText signupname;
    private EditText signupemail;
    private EditText signuppass;
    private EditText signuppass2;
    private Button createaccountButton;

    //stores all of the users in the database's emails to see if there is already an account with this email
    private ArrayList<String> userEmails = new ArrayList<>();
    private DatabaseReference drefUserEmails = FirebaseDatabase.getInstance().getReference().child("users");

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Log.d("TOAST", "Toasting");

        // DEVELOPER CODE
        firebaseAuth = FirebaseAuth.getInstance();          // initialize firebase


        signupname = findViewById(R.id.csignupname);
        signupemail = findViewById(R.id.csignupemail);
        signuppass =  findViewById(R.id.csignuppass);
        signuppass2 =  findViewById(R.id.csignuppass2);
        createaccountButton = findViewById(R.id.ccreateaccountButton);
        createaccountButton.setOnClickListener(this);
        signupname.requestFocus();

        setOutOfFocusListeners();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {    //when the user creates an account
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    // NOTE: this Activity should get onpen only when the user is not signed in, otherwise
                    // the user will receive another verification email.
                    sendVerificationEmail();
                } else {
                    // User is signed out
                }
            }
        };

    }

    //function to send the user a verification email if they are able to create an account
    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    //email got sent
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
                    finish();
                }
                else {
                    overridePendingTransition(0,0);
                    finish();
                    overridePendingTransition(0,0);
                    startActivity(getIntent());
                }
            }
        });
    }

    public void createAccount() {
        Log.d("COURSES", "CREATE ACCT RUNNING ");
        final String emailString = signupemail.getText().toString();
        final String passwordString = signuppass.getText().toString();
        final String password2String = signuppass2.getText().toString();
        final String nameString = signupname.getText().toString();

        if (allFieldsValid(nameString, emailString, passwordString, password2String)) {
            firebaseAuth.createUserWithEmailAndPassword(emailString, passwordString)
                    .addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //If task was successful
                            if (task.isSuccessful()) {
                                Log.d("Courses", "Making acct");
                                // GET NEW USERS' ID AND STORE INFORMATION INTO FIREBASE
                                String userID = firebaseAuth.getUid();
                                DatabaseReference currentUserID = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("UserInfo");
                                currentUserID.child("Name").setValue(nameString);
                                currentUserID.child("Email").setValue(emailString);


                                //Send the user the verification email
                                FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();

                                //log out the user, so they are only able to log back in after their email has been verified
                                FirebaseAuth.getInstance().signOut();

                                // go to login -- need to get it to verify user
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

                                //toast the user that they are expecting an email to verify their account
                                Toast.makeText(CreateAccountActivity.this, "Account Created. Please check your email " +
                                                                                        "for a link to verify your account.",Toast.LENGTH_LONG).show();
                                startActivity(intent);  //bring them back to the log in screen
                            } else {
                                //user account was not created
                                Log.d("CREATEACCOUNT","Create account not successful");
                            }

                        }
                    });
        }
    }

    // ONCLICK FUNCTION - THIS FUNCTION IS USED TO HANDLE MULTIPLE ONCLICK ELEMENTS
    @Override
    public void onClick(View view) {
        if (view == createaccountButton) {
            Log.d("CREATEACCOUNT","Create account button pressed");
            if(isNetworkAvailable()) {
                createAccount();
            } else { Toast.makeText(CreateAccountActivity.this, "Cannot connect to the internet",Toast.LENGTH_LONG).show(); }
        }
    }

    //on focus listeners for input boxes used so to create prompt if the email or passwords are not
    //valid
    private void setOutOfFocusListeners() {
        //email verification
        signupemail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                String email = signupemail.getText().toString();

                if (!email.equals("")) {//email field not empty
                    if (!hasFocus) {
                        if (!isValidEmail(email) || emailExists(email)) {
                            TextView text = findViewById(R.id.emailValidPrompt);
                            text.setTextColor(Color.parseColor("#FF0000"));
                            text.setText("X");
                        } else if (isValidEmail(email)) {
                            TextView text = findViewById(R.id.emailValidPrompt);
                            text.setTextColor(Color.parseColor("#00FF00"));
                            text.setText("✔");
                        }
                    }
                }
            }
        });

        //password verification
        signuppass.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = signuppass.getText().toString();

                if (!password.equals("")) { //password field not empty
                    if (!isValidPassword(password))
                    {
                        TextView text = findViewById(R.id.pass1Prompt);
                        text.setTextColor(Color.parseColor("#FF0000"));
                        text.setText("X");
                    } else if (isValidPassword(password))
                    {
                        TextView text = findViewById(R.id.pass1Prompt);
                        text.setTextColor(Color.parseColor("#00FF00"));
                        text.setText("✔");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        //password verification
        signuppass2.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = signuppass.getText().toString();
                String pass1 = signuppass.getText().toString();
                String pass2 = signuppass2.getText().toString();

                if (!pass2.equals("")) { //pass 2 field not empty
                    if (!pass1.equals(pass2)) {
                        TextView text = findViewById(R.id.pass2Prompt);
                        text.setTextColor(Color.parseColor("#FF0000"));
                        text.setText("Passwords Don't Match");
                    } else {
                        TextView text = findViewById(R.id.pass2Prompt);
                        text.setTextColor(Color.parseColor("#00FF00"));
                        text.setText("Passwords Match");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //checks if the password given matches the regular expression
    private boolean isValidPassword(String userPassword) {
        //Regex for password
        Pattern password = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
        Matcher m = password.matcher(userPassword);

        if (m.find()) { //matched the regex
            return true;
        }
        return false;
    }

    //checks if the email given matched the regular expression
    private boolean isValidEmail(String userEmail) {
        //Regex for email
        Pattern email = Pattern.compile("^[A-Za-z]+((@Merrimack.edu)|(@merrimack.edu))$");
        Matcher m = email.matcher(userEmail);

        if (m.find()) { //matched the regex
            return true;
        }
        return false;
    }

    private boolean allFieldsValid(String name, String email, String pass1, String pass2) {
        boolean validName =  true;
        boolean validEmail = isValidEmail(email);
        boolean validPass1 = isValidPassword(pass1);
        boolean validPass2 = pass1.equals(pass2);

        if(name.equals("")){ validName = false; };

        if (validName && validEmail && validPass1 && validPass2) {
            if(!emailExists(email)) {
                return true;
            }
        }
        return false;
    }

    //returns true if the email was found false otherwise
    private boolean emailExists(String email) {
        if(userEmails.indexOf(email) == -1){
            return false;
        }
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
