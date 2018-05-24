package com.oneplusplus.christopher.studycompanion;

import android.app.ProgressDialog;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class changePassword extends AppCompatActivity {
    EditText userEntersNewPassword;
    EditText userConfirmsPassword;
    FirebaseAuth auth;
    Button changeButton;
    TextView requirements;
    private String firstPass;
    private String secondPass;
    private TextView oldPassValid;
    private TextView newPass1Valid;
    private TextView newPass2Valid;
    private TextView oldPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        userEntersNewPassword = (EditText) findViewById(R.id.new_password);
        userConfirmsPassword = (EditText) findViewById(R.id.new_password2);


        //fields to show user if their input is valid or not
        oldPassValid = findViewById(R.id.oldPassValid);
        newPass1Valid = findViewById(R.id.newPass1Valid);
        newPass2Valid = findViewById(R.id.newPass2Valid);
        oldPassword = findViewById(R.id.oldPassField);


        requirements = (TextView) findViewById(R.id.promptUser);

        changeButton = (Button)findViewById(R.id.ChangePassword);
        auth = FirebaseAuth.getInstance();

        setFeedBackListeners();

        // when the user clicks the change password button then call the change() function
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if(isNetworkAvailable()) {
                    firstPass = userEntersNewPassword.getText().toString();
                    secondPass = userConfirmsPassword.getText().toString();
                    if (firstPass.equals(secondPass)) {                      // check if passwords match
                        if (!isValidPassword(firstPass)) {                   // check if password fits credentials
                            Toast.makeText(changePassword.this, "Error Please make sure you have typed in the correct information",Toast.LENGTH_LONG).show();
                        } else {
                            change();                                   // change password
                        }
                    } else {                                                  // if passwords do not match
                        Toast.makeText(changePassword.this, "Please enter your old password",Toast.LENGTH_LONG).show();
                    }
                } else { Toast.makeText(changePassword.this, "Cannot connect to the internet",Toast.LENGTH_LONG).show(); }
            }
        });
    }

    //checks if the password given matches the regular expression
    private boolean isValidPassword(String userPassword) {
        //Regex for password
        Pattern password = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
        Matcher m = password.matcher(userPassword);
        if (m.find()) {
            return true;
        }
        return false;
    }

    //listeners to check text fields and let the user know if their input is valid or not
    private void setFeedBackListeners() {
        //Check if the users new password meets requirements as they type
        userEntersNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currentInput = userEntersNewPassword.getText().toString();
                if(! isValidPassword(currentInput)){
                    newPass1Valid.setTextColor(Color.parseColor("#FF0000"));
                    newPass1Valid.setText("X");
                }
                else if ( isValidPassword(currentInput )){
                    newPass1Valid.setTextColor(Color.parseColor("#00FF00"));
                    newPass1Valid.setText("✔");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //do nothing
            }
        });

        //Check if the users confirmation password matches their first password
        userConfirmsPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currentInput = userConfirmsPassword.getText().toString();
                if(! currentInput.equals(userEntersNewPassword.getText().toString())){
                    newPass2Valid.setTextColor(Color.parseColor("#FF0000"));
                    newPass2Valid.setText("X");
                }
                else if ( currentInput.equals(userEntersNewPassword.getText().toString())){
                    newPass2Valid.setTextColor(Color.parseColor("#00FF00"));
                    newPass2Valid.setText("✔");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //do nothing
            }
        });

    }

    //function to update their password
    public void change() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String usersEmail = user.getEmail();
        String usersPassword = oldPassword.getText().toString();
        final String newPass = userEntersNewPassword.getText().toString();

        if(usersPassword.length() < 1){
            usersPassword = "null@merrimack.edu";
        }

        AuthCredential credential = EmailAuthProvider
                .getCredential(usersEmail, usersPassword);

        //check first that the passwords the user entered are valid and match
        if(allPasswordsValid() && !(usersPassword.length() < 1) ) {
            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("PASSWORD", "Password updated");
                                            Toast.makeText(changePassword.this, "Password Changed", Toast.LENGTH_LONG).show();
                                            // return user to Main menu
                                            Intent sendUserToDifferentScreen = new Intent(changePassword.this, CourseOverviewActivity.class);
                                            startActivity(sendUserToDifferentScreen);
                                        } else {
                                            Log.d("PASSWORD", "Error password not updated");
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(changePassword.this, "Failed to authenticate make sure you typed in your old password correctly",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(changePassword.this, "Error Please make sure you have typed in the correct information",Toast.LENGTH_LONG).show();
        }

    }

    //returns true if all the passwords are valid and false otherwise
    private  boolean allPasswordsValid() {
        String pass1 = userEntersNewPassword.getText().toString();
        String pass2 = userConfirmsPassword.getText().toString();
        //if the first password is valid
        if( isValidPassword(pass1) && pass1.equals(pass2)){
            return true;
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}