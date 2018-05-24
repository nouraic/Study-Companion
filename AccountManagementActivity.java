package com.oneplusplus.christopher.studycompanion;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AccountManagementActivity extends AppCompatActivity
{
    // Change password setting

    private Button changePassword; // Button declared that when clicked allows the user to change
    // their passwordd

    private Button deleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings); // Buttons in the activity_settings.xml file

        changePassword = (Button)findViewById(R.id.change_Password); // references the button inside
        // the activity_settings.xml

        deleteAccount = (Button)findViewById(R.id.delete_Account);

        changePassword.setOnClickListener(new View.OnClickListener() // checks if user clicks the changePassword button
        {
            @Override public void onClick(View v) // if the user clicks the button then this function
                    // brings the user to the change password class
            {
                openChangePassword();
                // enters the class to change password
            }
        }); // end of click listener

        deleteAccount.setOnClickListener(new View.OnClickListener() // checks if user clicks the changePassword button
        {
            @Override public void onClick(View v) // if the user clicks the button then this function
            // brings the user to the change password class
            {
                deleteAccount();

                // enters the class to delete the users account
            }
        }); // end of click listener

    } // end of first override

    public void deleteAccount()
    {
        Intent intent = new Intent(this, deleteAccount.class);
        startActivity(intent); // opens the activity for the Act Management class
    }

    public void openChangePassword()
    {
        Intent intent = new Intent(this, changePassword.class);
        startActivity(intent); // opens the activity for the Act Management class
    }

}
