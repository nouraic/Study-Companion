package com.oneplusplus.christopher.studycompanion;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class deleteAccount extends AppCompatActivity {
    private TextView promptUser;
    private Button no;
    private Button yes;
    private ProgressDialog dialog; // constantly displays to the user
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // gets the current user using the application
    String userID = currentUser.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        dialog = new ProgressDialog(this);
        promptUser = (TextView)findViewById(R.id.textView3);
        no = (Button)findViewById(R.id.button2);
        yes = (Button)findViewById(R.id.yes);


        // check if the user presses the yes or no button

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClickedNo(view);
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                userClickedYes(view);
            }
        });

    } // end of onCreate

    // delete the users account from firebase
    public void userClickedYes(View v) {
        final View view = v;

        if(isNetworkAvailable()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        DatabaseReference currentUserInfo = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
                        currentUserInfo.removeValue();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
        else {
            Toast.makeText(deleteAccount.this, "Cannot connect to the internet",Toast.LENGTH_LONG).show();
        }
    }

    // return the user to courseOverView
    public void userClickedNo(View view)
    {
        Intent sendUserToDifferentScreen = new Intent(view.getContext(), CourseOverviewActivity.class);
        startActivity(sendUserToDifferentScreen);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
