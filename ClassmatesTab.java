package com.oneplusplus.christopher.studycompanion;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ClassmatesTab extends Fragment implements View.OnClickListener{

    private Switch onlinetoggleButton;
    private ListView listview;
    private String courseName;
    private ProgressBar spinner;

    private List<User> userList; // list of users

    //firebase references
    FirebaseAuth firebaseAuth;
    DatabaseReference drefGetCurrentUserName;
    DatabaseReference drefSetUserOnline;
    DatabaseReference drefGetUsersOnline;
    DatabaseReference drefGetUserSavedState;

    private String nameVariable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        courseName = (String) getArguments().getString("COURSE_ID");        // get course name from previous tab

        View rootView = inflater.inflate(R.layout.tabclassmates, container, false);

        // initialize online toggle button and add listener
        onlinetoggleButton = (Switch) rootView.findViewById(R.id.conlinetoggle);
        onlinetoggleButton.setOnClickListener(this);

        // loading spinner
        spinner = rootView.findViewById(R.id.loadingClassmates);

        // create array of users
        firebaseAuth = FirebaseAuth.getInstance();
        userList = new ArrayList<>();
        listview = (ListView) rootView.findViewById(R.id.clist);
        listview.setVisibility(View.GONE);

        // set firebase references
        drefGetCurrentUserName = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid()).child("UserInfo").child("Name");
        drefGetUserSavedState = FirebaseDatabase.getInstance().getReference().child("Courses").child(courseName).child("Users Online").child(FirebaseAuth.getInstance().getUid()).child("Name");

        final UserList adapter =  new UserList(getActivity(), userList);
        listview.setAdapter(adapter);

        // get all users online everytime a new user goes online
        drefGetUsersOnline = FirebaseDatabase.getInstance().getReference().child("Courses").child(courseName).child("Users Online");
        drefGetUsersOnline.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                spinner.setVisibility(View.VISIBLE);
                userList.clear();                                   // clear array
                adapter.notifyDataSetChanged();
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {       // get each user
                    User user = userSnapshot.getValue(User.class);                  // create user
                    try {
                        if(!user.getUserEmail().equals(firebaseAuth.getCurrentUser().getEmail())) { // if user is not current user
                            userList.add(user);                                                     // add them to array
                        }
                        else if(user.getUserEmail().equals(firebaseAuth.getCurrentUser().getEmail())) {
                            onlinetoggleButton.setChecked(true);                                    // set the toggle button
                            listview.setVisibility(View.VISIBLE);                                   // make list of users appear
                        }
                    } catch (Exception e) { }
                }
                listview.setOnItemClickListener(onListClick);
                spinner.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        return rootView;
    }

    // Open default email client and create email
    private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { userList.get(position).getUserEmail() });
            intent.putExtra(Intent.EXTRA_SUBJECT, "Study Companion - Looking to Study?");
            intent.putExtra(Intent.EXTRA_TEXT, "Hi "+ userList.get(position).getUserName()+",");
            startActivity(Intent.createChooser(intent, ""));
        }
    };

    // set the users status to online
    void setUserOnline() {
        drefSetUserOnline = FirebaseDatabase.getInstance().getReference().child("Courses").child(courseName).child("Users Online");  // firebase reference

        drefGetCurrentUserName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nameVariable = dataSnapshot.getValue(String.class);
                // if user is online then set them online and make the list viewable
                if(onlinetoggleButton.isChecked()) {                                                                                                    // when the user is online
                    drefSetUserOnline.child(firebaseAuth.getCurrentUser().getUid()).child("name").setValue(nameVariable);                               // set user name
                    drefSetUserOnline.child(firebaseAuth.getCurrentUser().getUid()).child("email").setValue(firebaseAuth.getCurrentUser().getEmail());  // set user email
                    listview.setVisibility(View.VISIBLE);                                                                                               // make list visible
                }
                // if user is offline remove them and make the list hidden
                else { // when the user is offline
                    drefSetUserOnline.child(firebaseAuth.getCurrentUser().getUid()).removeValue();                                                      // remove user
                    listview.setVisibility(View.GONE);                                                                                                  // hide list
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // if the toggle button is clicked set user to online / offline
    @Override
    public void onClick(View view) {
        if(isNetworkAvailable()) {
            if (view == onlinetoggleButton) {
                setUserOnline();
            }
        } else {
            onlinetoggleButton.setChecked(false);                           // set the toggle button
            Toast.makeText(getContext(), "Cannot connect to the internet",Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}



