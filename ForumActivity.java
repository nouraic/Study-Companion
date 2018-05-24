package com.oneplusplus.christopher.studycompanion;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

public class ForumActivity extends AppCompatActivity {

    private SectionsPageAdapter spa;
    private ViewPager pager;
    private String courseName;
    private String courseID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        //update the name of the toolbar based on what class it is
        courseName = getIntent().getStringExtra("COURSE_NAME");
        courseID = getIntent().getStringExtra("COURSE_ID");

        Log.d("ADDCOURSE", "Forum Activity: Course Name: "+courseName+" Course ID: "+courseID);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(courseID+" "+courseName);

        spa = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        pager = findViewById(R.id.container);
        setupViewPager(pager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }

    private void setupViewPager(ViewPager viewPager) {

        Bundle bundle = new Bundle();
        bundle.putString("COURSE_ID", courseID);
        bundle.putString("COURSE_NAME", courseName);

        ForumTab forum = new ForumTab();
        forum.setArguments(bundle);

        Bundle bundle2 = new Bundle();
        bundle2.putString("COURSE_ID", courseID);
        ClassmatesTab classmates = new ClassmatesTab();
        classmates.setArguments(bundle2);

        Bundle bundle3 = new Bundle();
        bundle3.putString("COURSE_ID", courseID);
        StudyGroupsTab studyGroups = new StudyGroupsTab();
        studyGroups.setArguments(bundle3);

        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(forum, "FORUM");
        adapter.addFragment(classmates, "CLASSMATES");
        adapter.addFragment(studyGroups, "STUDY GROUPS");
        viewPager.setAdapter(adapter);
    }
}
