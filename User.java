package com.oneplusplus.christopher.studycompanion;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class User implements  Parcelable {

    public String name;
    public String email;
    public String id;
    private ArrayList<String> courses = new ArrayList<>();  //holds the course IDs
    private ArrayList<String> courseNames = new ArrayList<>();//holds the actual name of the array
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference courseRoot = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid());//.child("courses");


    public User() {

    }

    public User(String _name, String _email, String uid) {
        name = _name;
        email = _email;
        id = uid;
    }

    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();
        id = in.readString();
    }


    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    public String getUserName() {
        return name;
    }

    public String getUserEmail() {
        return email;
    }

    public String getUserID() {
        return id;
    }

    public ArrayList<String> getCourses() { return courses; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(id);
    }

    //functions to add and drop courses
    public void addCourse(String newCourse, String newCourseName ) {
        courses.add(newCourse); //adds it to the local array list
        //adds it to the database for this user
        courseRoot.child("courses").child(newCourse).child("CourseID").setValue(newCourse);
        courseRoot.child("courses").child(newCourse).child("CourseName").setValue(newCourseName);
    }

    public void removeCourse(String toRemove ) {
        int index = courses.indexOf(( toRemove ));
        courses.remove( index );
    }

    public void syncCourses() {
        courses.add("TEST");

        courseRoot.child("courses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //take in an array list of courses and sets it to the user's local arraylist
    public void setUserCourses(ArrayList<String> userCourses ) {
        courses = userCourses;
    }
}