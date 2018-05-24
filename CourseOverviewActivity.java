package com.oneplusplus.christopher.studycompanion;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class CourseOverviewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private User user;
    final private ArrayList<String> userCourses = new ArrayList<>();
    final private ArrayList<String> userCourseNames = new ArrayList<>();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference courseRoot;

    private Button testButton;
    private String testButtonText;
    String courseName;
    private ProgressDialog dialog;
    private Button addCourse;
    private ProgressBar spinner;
    private View view;
    private InternetTime inetTime;
    private Date temp;


    protected void onCreate(Bundle savedInstanceState) {
        // GENERATED CODE
        //------------------------------------------------------------------------------------------
        super.onCreate(savedInstanceState);
        view = findViewById(android.R.id.content);
        setContentView(R.layout.activity_course_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Course Overview");
        setSupportActionBar(toolbar);

        FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
        if (curruser != null) {
            user = new User(); //first instance of the user
            courseRoot = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid()).child("courses");

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();


            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            //------------------------------------------------------------------------------------------
            spinner = findViewById(R.id.loadingCoursesSpinner);
            spinner.setVisibility(View.VISIBLE);

            // DEVELOPER CODE
            syncUserCourses();
            initOnClickActions();
        } else {
            Intent intent = new Intent(this, LoginActivity.class );
            startActivity(intent);
            finish();
        }
    }

    // TESTING FUNCTION
    public void testFunc(String courseName){
        //startActivity(new Intent(getApplicationContext(),ForumActivity.class));
        Intent intent = new Intent(getApplicationContext(), ForumActivity.class);
        intent.putExtra("COURSE_NAME", courseName);
        startActivity(intent);
    }

    // ONCLICK FUNCTION - THIS FUNCTION IS USED TO HANDLE MULTIPLE ONCLICK ELEMENTS
    @Override
    public void onClick(View view) {
        //if(view == testButton) {
        //testFunc();             // just a function call to test
        //}
    }

    // GENERATED CODE FROM HERE DOWN
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.course_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    //nav bar listeners
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.acctManagement) {
            Intent intent = new Intent(this, AccountManagementActivity.class);
            startActivity(intent);
        }
        else if( id == R.id.usersPosts) {   //user navigates to their posts
            Intent intent = new Intent(this, UsersPosts.class );
            startActivity(intent);
        }
        else if(id==R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class );
            startActivity(intent);
            finish();
        }
        else if(id==R.id.help) {
            Intent intent = new Intent(this, HelpScreen.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Call the class to open Account Management
    public void openAccountManagementActivity() {
        Intent intent = new Intent(this, AccountManagementActivity.class);
        startActivity(intent);
    }

    //function to set up all of the buttons for the user's classes
    private void createClassButtons() {
        if( userCourses.size() == 0 ) {  //user has not yet added courses
            TextView text = findViewById(R.id.displayIfNoCourses);
            text.setText(("You are not currently enrolled in any courses " +
                    "tap add course below to begin"));
            spinner.setVisibility(View.GONE);
        }
        else {
            TextView text = findViewById(R.id.displayIfNoCourses);
            text.setText((""));
            //where to insert the course buttons
            LinearLayout layout = findViewById(R.id.courseHolder);

            //for all the user's courses create buttons for them
            for (int i = 0; i < userCourses.size(); i++) {
                final Button tempClass = new Button(this);
                tempClass.setText((userCourseNames.get(i))+"\n"+userCourses.get(i));    //the name of the course
                tempClass.setTag(userCourses.get(i));           //the course ID
                tempClass.setBackgroundColor(Color.parseColor("#1DA1F2"));
                tempClass.setTextColor(Color.WHITE);
                final TextView tempClassSeperator = new TextView(this);             // add space between buttons
                tempClassSeperator.setBackgroundColor(Color.WHITE);                         // set background to white
                tempClassSeperator.setText("    ");                                         // set text to create valid spacing


                //create onclick listener for the new button
                tempClass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ForumActivity.class);
                        intent.putExtra("COURSE_ID", tempClass.getTag().toString());
                        intent.putExtra("COURSE_NAME", tempClass.getText());
                        startActivity(intent);
                    }
                });
                //create on long click listener to delete the course
                tempClass.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Button b = (Button) view;
                        String toRemove = (String) b.getTag();
                        buildAlert(toRemove);

                        return false;
                    }
                });
                //add the button to the view
                layout.addView(tempClass);
                layout.addView(tempClassSeperator);         // add button seperator
            }
            TextView spacer = new TextView(this);
            layout.addView(spacer);
            spinner.setVisibility(View.GONE);
        }
    }


    private void initOnClickActions()
    {
        //add a course button
        addCourse = findViewById((R.id.addCourseButton));

        dialog = new ProgressDialog(this);

        addCourse.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), addCourseScreen.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    //function that gets called when addCourseScreen returns which course should be added
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Button tempClass = new Button(this);
        LinearLayout layout = findViewById(R.id.courseHolder); //where to insert the course button
        layout.invalidate();

        //add a course request code
        if(requestCode == 1 ) {
            if(resultCode == RESULT_OK) {
                if(isNetworkAvailable()) {
                    //holds the string of either the courseName or courseID - depends on current search
                    final String courseName = data.getStringExtra("COURSE_NAME");
                    final String courseID = data.getStringExtra("COURSE_ID");


                    if (userCourses.indexOf(courseID) == -1) {    //course to add doesn't already has a button for it
                        user.addCourse(courseID, courseName);    //add course to the user's array
                        updateLocalUserCourses();   //update the stored array of user's courses
                    }
                }
                else { Toast.makeText(CourseOverviewActivity.this, "Cannot connect to the internet", Toast.LENGTH_LONG).show();}
            }
        }//end of add a course request
    }

    //updates the local variable that stores all of the user's courses likely don't need anymore
    public void updateLocalUserCourses() {
        //userCourses = user.getCourses();//{"HIS2410", "MTH 1100", "CSC1610", "ENG1010"};
    }

    //function that grabs the courses information and sets onchange listeners for the data
    public void syncUserCourses() {
        courseRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinearLayout layout = findViewById(R.id.courseHolder);
                layout.removeAllViews();
                userCourses.clear();
                userCourseNames.clear();

                //get all courses in the snapshot of the database
                for(DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    try {
                        String course = courseSnapshot.child("CourseID").getValue().toString();
                        String courseName = courseSnapshot.child("CourseName").getValue().toString();
                        userCourses.add(course);
                        userCourseNames.add(courseName);
                    }catch(Exception e){};
                }
                //this call has to be here.. after onDataChange array list becomes 0? no clue why - asynchronous stuff maybe
                createClassButtons();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //builds the dialog YES NO for when the user wants to delete a course
    //on yes deletes the course, no dismisses the dialog
    private void buildAlert(final String courseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Course");
        builder.setMessage("Are you sure you want to delete "+courseName+"?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                courseRoot.child(courseName).removeValue();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
