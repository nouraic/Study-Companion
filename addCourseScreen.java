package com.oneplusplus.christopher.studycompanion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class addCourseScreen extends AppCompatActivity
{

    private User user;
    private ListView listview;
    private ArrayList<String> offeredNames = new ArrayList();
    private ArrayList<String> offeredIDs = new ArrayList();

    private Spinner searchChoice;
    private String searchMethod;
    private ListView listView;
    private ProgressBar loadSpinner;

    private ArrayAdapter<String> adapter; //adapter for the course names
    private ArrayAdapter<String> adapter2;//adapter for the course codes (IDs)

    private ProgressDialog dialog;

    private DatabaseReference drefOfferedCourses = FirebaseDatabase.getInstance().getReference().child("Courses");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course_screen); // This line of code crashes the program

        dialog = new ProgressDialog(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadSpinner = (ProgressBar) findViewById(R.id.loadingCoursesBar);
        listView = (ListView) findViewById(R.id.offeredCoursesView);
        //creates the options in the dropdown menu for how the user wants to search for their courses
        searchChoice = findViewById(R.id.searchOptionID);
        String[] choices = new String[]{"Course Name", "Course ID"};
        ArrayAdapter<String> choiceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, choices);
        searchChoice.setAdapter(choiceAdapter);

        //things to setup listview adapter
        listview = (ListView) findViewById(R.id.offeredCoursesView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 ,offeredNames);

        //things to setup listview for offeredIDs
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, offeredIDs);

        loadSpinner.setVisibility(View.VISIBLE);
        syncCourses();
        initOnClickActions();
    }

    private void initOnClickActions() {
        //add a course button
        Button addCourse = findViewById((R.id.addButton));
        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            //this will send the result back to course overview so that
            public void onClick(View view) {
                TextView text = findViewById(R.id.courseNameID);

                String newCourseName;
                String newCourseID="";
                String inputToVerify="";
               //get the name of the course to add
               if(searchMethod.equals("Course Name")){  //user searched by course Name so that is in the search box

                    newCourseName = text.getText().toString();
                    if(verifyCourse(newCourseName) != -1) {
                        //newCourseID = the same index as newCourseName but in offeredIds
                        newCourseID = offeredIDs.get(offeredNames.indexOf(newCourseName));
                        inputToVerify = newCourseName;
                    }
               }
               else {   //user searched by course id so that is in the search box
                   newCourseID = text.getText().toString();
                   //newCourseName = the same index as newCourseID but in offeredNames
                   newCourseName = offeredNames.get(offeredIDs.indexOf(newCourseID));
                   inputToVerify = newCourseID;
               }
               //newCourse = newCourseID+" "+newCourseName;



               if(verifyCourse(inputToVerify) != -1 ) {    //verify the course exists  before adding it
                   Intent intent = new Intent();
                   intent.putExtra("COURSE_ID", newCourseID);
                   intent.putExtra("COURSE_NAME", newCourseName);
                   setResult(RESULT_OK, intent);
                   finish();
               }
               else {
                   TextView errorMessage = findViewById(R.id.invalidClassText);
                   errorMessage.setText("Error: Could not find that course");
               }
           }

        });

        //listview item listeners
        listview.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            TextView text = findViewById(R.id.courseNameID);

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String course = (String) listview.getItemAtPosition(i);
                text.setText(course);
                TextView text = findViewById(R.id.invalidClassText);
                text.setText("");
            }
        });

        EditText courseName = findViewById(R.id.courseNameID);
        courseName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {

                if(searchMethod.equals("Course Name")) {
                    adapter.getFilter().filter(s.toString());
                    //clear the error message if the previous course entered was not found
                    TextView text = findViewById(R.id.invalidClassText);
                    text.setText("");
                }
                else {
                    adapter2.getFilter().filter(s.toString());
                    //clear the error message if the previous course entered was not found
                    TextView text = findViewById(R.id.invalidClassText);
                    text.setText("");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //do nothing
            }
        });

        //on change listener when user changes how they want to search for a course
        searchChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = adapterView.getItemAtPosition(i);
                searchMethod = item.toString();

                if(searchMethod.equals("Course Name")) {
                    listView.setAdapter(adapter);
                }
                else {
                    listView.setAdapter(adapter2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });
    }

    //adds all of the currently offered courses to the array list of offered courses
    //then adds it to the list view using the adapter
    private void syncCourses()
    {

        drefOfferedCourses.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                offeredNames.clear();
                offeredIDs.clear();

                //for all existing courses get the course name and the course code
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    try {
                        String course = postSnapshot.child("Name").getValue().toString();
                        String id = postSnapshot.child("ID").getValue().toString();
                        offeredNames.add(course);
                        offeredIDs.add(id);
                    } catch (Exception e){};
                }
                listView.setAdapter(adapter);   //by default set adapter to show the course codes
                loadSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
    }); // e
}

    //returns the index of the course of -1 if not found
    private int verifyCourse(String course) {
        //if the user searched by course name check the offeredName array list
        if(searchMethod.equals("Course Name")) {
            return offeredNames.indexOf(course);
        }
        //else check the offeredIDs to verify the course does exist
        return offeredIDs.indexOf(course);
    }

}
