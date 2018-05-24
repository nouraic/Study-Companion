package com.oneplusplus.christopher.studycompanion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UsersPosts extends AppCompatActivity {
    private final int POST_CODE = 1;    //this wont be used in this class, reserves code for ForumTab
    private final int REPLY_CODE = 2;
    private final int EDIT_CODE = 3;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ValueEventListener listener;
    private DatabaseReference usersPostsRoot;
    private DatabaseReference reportedPostsRoot;
    private DatabaseReference userNameRoot;
    private String userID;
    private LinearLayout forumContainer;
    private View view;
    private String currentUsersName;
    private InternetTime InetTime;
    private ProgressBar progressBar;
    private TextView showIfNoPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_posts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

//        setSupportActionBar(toolbar);

        userID = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid()).getKey().toString();
        usersPostsRoot = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid()).child("UsersPosts");
        reportedPostsRoot = FirebaseDatabase.getInstance().getReference().child("Admin").child("Reports");

        userNameRoot = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid()).child("UserInfo").child("Name");
        InetTime = new InternetTime();  //declare new internet time, this gets the time from a server instead of the user's phone

        getUsersName();
        forumContainer = findViewById(R.id.userPostsContainer);
        progressBar = findViewById(R.id.userPostsSpinner);
        progressBar.setVisibility(View.VISIBLE);
        showIfNoPosts = findViewById(R.id.showIfNoPosts);
        drawUsersPosts();
    }

    //handles all of the activity results (add post, edit post, reply )
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == REPLY_CODE) { //reply to a post request returned
            if(resultCode == RESULT_OK) {
                if (isNetworkAvailable()) {
                    String responseID = new SimpleDateFormat("yy:MM:dd HH:mm:ss").format(new Date());
                    String postID = data.getStringExtra("POST_ID");
                    String courseID = data.getStringExtra("COURSE_ID");
                    //String replyTo = data.getStringExtra("REPLY_TO");
                    String reply = data.getStringExtra("REPLY");

                    usersPostsRoot.removeEventListener(listener);

                    DatabaseReference postRoot = FirebaseDatabase.getInstance().getReference()
                            .child("Courses").child(courseID).child("Forum").child(postID).child("Replies");

                    postRoot.child(responseID).setValue(currentUsersName + " " + reply);

                    usersPostsRoot.addValueEventListener(listener);
                } else { Toast.makeText(this, "Cannot connect to the internet", Toast.LENGTH_LONG).show();}
            }
        }

        if(requestCode == EDIT_CODE) {  //user wants to edit a post
            if(resultCode == RESULT_OK) {
                if(isNetworkAvailable()) {
                    String subject = data.getStringExtra("SUBJECT");
                    String question = data.getStringExtra("QUESTION");
                    String postID = data.getStringExtra("POSTID");
                    String courseID = data.getStringExtra("COURSE_ID");

                    usersPostsRoot.removeEventListener(listener);

                    DatabaseReference postRoot = FirebaseDatabase.getInstance().getReference()
                            .child("Courses").child(courseID).child("Forum").child(postID);

                    postRoot.child("Subject").setValue(subject + " ⇊ " + currentUsersName + " (EDITED)");
                    postRoot.child("Question").setValue(question);

                    usersPostsRoot.addValueEventListener(listener);
                } else { Toast.makeText(this, "Cannot connect to the internet", Toast.LENGTH_LONG).show();}
        }
        }
    }

    //function to draw to the screen all of the posts this user has made
    private void drawUsersPosts() {
        Log.d("USERPOSTS", "Drawing User Posts");
        //fetch the data in the usersPost directory
        listener = usersPostsRoot.addValueEventListener(new ValueEventListener() {
            @Override
            //every time the data in their location is changed
            public void onDataChange(DataSnapshot dataSnapshot) {
                forumContainer.removeAllViews();
                int counter = 0;
                for(DataSnapshot index : dataSnapshot.getChildren()) {  //for all of their posts
                    try {   //try to get the post id and course id
                        String courseID = index.child("Course ID").getValue().toString();
                        String postID = index.child("Post ID").getValue().toString();

                        //create the post for this particular user post
                        createForumPost(courseID, postID, counter);
                        counter++;
                    }catch (Exception e){};
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //listens once to forum directory for this course to fetch the post at location [courseID][postID]
    private void createForumPost(final String courseID, final String postID, final int counter){
        //Log.d("OnDataChange", "CourseID: "+courseID+" PostID: "+postID);
        Log.d("USERPOSTS", "Creating Forum Post");
        DatabaseReference coursesRoot = FirebaseDatabase.getInstance().getReference()
                .child("Courses").child(courseID).child("Forum").child(postID);

        //grab the database information
        coursesRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String subject = dataSnapshot.child("Subject").getValue().toString();
                    String question = dataSnapshot.child("Question").getValue().toString();
                    String timeStamp = dataSnapshot.child("Time Stamp").getValue().toString();

                    Log.d("OnDataChange", "Subject: "+subject+" Question "+question);

                    makePostView(subject, question, timeStamp, postID, counter, courseID);
                }catch (Exception e){};
                progressBar.setVisibility(View.GONE);

                if(forumContainer.getChildCount() > 0) {
                    showIfNoPosts.setVisibility(View.GONE);
                }
                else {
                    showIfNoPosts.setVisibility(View.VISIBLE);
                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //creates the text views for the user's post
    private void makePostView(final String Subject, final String question, final String timeStamp, final String postID, final int counter, final String courseID) {
        Log.d("USERPOSTS", "Making Post View");

        String split[] = Subject.split(" ⇊ ");
        final String subject = split[0];

        forumContainer.setBackgroundColor(Color.parseColor("#1DA1F2"));

        //decide the background color for the post
        int backgroundColor = Color.parseColor("#E8E8E8");
        if( (counter % 2) == 0){
            backgroundColor = Color.WHITE;
        }

        //final container to return
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(backgroundColor);
        container.setTag(postID+" "+courseID);

        //this will house all the user options for each post
        LinearLayout bottomToolBar = new LinearLayout(this);
        bottomToolBar.setOrientation(LinearLayout.HORIZONTAL);

        //layout params for the time stamp container
        android.widget.LinearLayout.LayoutParams timeStampLayout =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeStampLayout.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        timeStampLayout.gravity = Gravity.END;

        //layout params for the button icons
        RelativeLayout.LayoutParams iconButtons = new RelativeLayout.LayoutParams(100, 100);
        iconButtons.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
        iconButtons.width = pxToDp(40);
        iconButtons.height = pxToDp(40);

        //layout prarms for the subject container
        android.widget.LinearLayout.LayoutParams subjectLayout =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        subjectLayout.width = pxToDp(300);

        //layout params for buttons
        android.widget.LinearLayout.LayoutParams buttons =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //container to house the timestamp and the subject
        LinearLayout titleContainer = new LinearLayout(this);
        titleContainer.setOrientation(LinearLayout.HORIZONTAL);

        //fields for timestamp and subject
        TextView timeStampView = new TextView(this);
        TextView subjectView = new TextView(this);

        //SUBJECT SETUP
        subjectView.setTextSize(18);
        subjectView.setText(subject);
        subjectView.setTypeface(null, Typeface.BOLD);
        subjectView.setLayoutParams(subjectLayout);

        //TIME STAMP SETUP
        timeStampView.setText(timeStamp);
        timeStampView.setLayoutParams(timeStampLayout);

        //Add title and timestamp to the title container
        titleContainer.addView(subjectView);
        titleContainer.addView(timeStampView);

        //container to house the question
        LinearLayout questionContainer = new LinearLayout(this);
        questionContainer.setOrientation(LinearLayout.HORIZONTAL);

        //fields for question
        TextView q = new TextView(this);    //put a bold Q: before the question
        TextView questionView = new TextView(this);

        //QUESTION SETUP
        q.setText("Q: ");
        q.setTypeface(null, Typeface.BOLD);
        q.setTextSize(18);

        questionView.setGravity(Gravity.LEFT);
        questionView.setText(question);
        questionView.setTextSize(17);
        Log.d("USERPOSTS","QuestionView: "+questionView.getText());

        //add Q: and question to the question container
        questionContainer.addView(q);
        questionContainer.addView(questionView);

        /* //OLD REPLY BUTTON SETUP
        TextView reply = new TextView(this);    //will act as a button
        reply.setText("Reply:");
        reply.setTextColor(Color.parseColor("#0000FF"));
        reply.setLayoutParams(buttons);
        */
        ImageButton reply = new ImageButton(this);
        reply.setAdjustViewBounds(true);
        reply.setBackgroundColor(Color.TRANSPARENT);
        reply.setLayoutParams(iconButtons);
        reply.setImageResource(R.drawable.comment_button);

        bottomToolBar.addView(reply);


        //set onclick listener for the reply button
        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UsersPosts.this , ReplyPostActivity.class);
                intent.putExtra("POST_ID", postID);
                intent.putExtra("COURSE_ID",courseID);
                startActivityForResult(intent, 2);
            }
        });

        //only gets added if the user is the author of the post
        ImageButton delete = new ImageButton(this);    //will act as a button has to be declared here
        ImageButton edit = new ImageButton(this);     //act as button has to be declared here

        //spacer view makes things look nicer spaced out
        TextView spacer = new TextView(this);

        //Container to house the replies
        LinearLayout replyContainer = makeReplyContainer(courseID, postID);

        //add all Main sub-containers to the Main container to add to the view
        container.addView(titleContainer);
        container.addView(questionContainer);
        container.addView(replyContainer);
       // container.addView(reply);

        /* //OLD DELETE BUTTON SETUP
        delete.setText("DELETE ");
        delete.setTextColor(Color.parseColor("#FF0000"));
        delete.setGravity(Gravity.END);
        */
        delete.setAdjustViewBounds(true);
        delete.setBackgroundColor(Color.TRANSPARENT);
        delete.setLayoutParams(iconButtons);
        delete.setImageResource(R.drawable.delete_button);



        //set onclick listener for delete button
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //prompt user if they want to delete, Y= delete N= dismiss
                deletePostPrompt(courseID, postID);
            }
        });

        //spacer between delete and edit buttons
        TextView newSpacer = new TextView(this);

        /*//OLD EDIT BUTTON SETUP
        edit.setText("EDIT POST ");
        edit.setTextColor(Color.parseColor("#00FF00"));
        edit.setGravity(Gravity.END);
        */
        edit.setAdjustViewBounds(true);
        edit.setBackgroundColor(Color.TRANSPARENT);
        edit.setLayoutParams(iconButtons);
        edit.setImageResource(R.drawable.edit_icon);

        TextView spacer1 = new TextView(this);
        TextView spacer2 = new TextView(this);
        TextView spacer3 = new TextView(this);

        spacer1.setLayoutParams(iconButtons);
        spacer2.setLayoutParams(iconButtons);
        spacer3.setLayoutParams(iconButtons);

        bottomToolBar.addView(spacer1);
        bottomToolBar.addView(spacer2);
        bottomToolBar.addView(edit);
        bottomToolBar.addView(spacer3);
        bottomToolBar.addView(delete);

        //set onclick listener for edit button
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editPost(courseID, postID, subject, question);
            }
        });

        //add elements to the container to be returned as one view later

        /*
        container.addView(edit);
        container.addView(newSpacer);
        container.addView(delete);
        container.addView(spacer);*/
        container.addView(bottomToolBar);


        //then add container to the Main view
        //pushing to index 0 makes most recent posts at the top of the feed
        Log.d("USERPOSTS","Adding to the Main container");
        Log.d("USERPOSTS", "Subject: "+subjectView.getText()+" Question: "+questionView.getText());
        Log.d("USERPOSTS", "Children "+forumContainer.getChildCount());
       // forumContainer.removeAllViews();
        forumContainer.addView(container, 0);
    }

    //returns a view of all the replies for this post
    private LinearLayout makeReplyContainer(String courseID, String postID) {
        Log.d("USERPOSTS", "Making a reply container");
        final LinearLayout container = new LinearLayout(this);    //the container to return
        container.setOrientation(LinearLayout.VERTICAL);

        TextView r = new TextView(this);    //textview to say "Replies"
        r.setText("Replies:");
        r.setTypeface(null, Typeface.BOLD);
        container.addView(r);

        //get the directory of the course this post resides in
        DatabaseReference postRoot = FirebaseDatabase.getInstance().getReference()
                .child("Courses").child(courseID).child("Forum").child(postID).child("Replies");
        postRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot index : dataSnapshot.getChildren()) {
                    try {
                        String text = index.getValue().toString();
                        TextView reply = new TextView(UsersPosts.this);
                        reply.setText("↳"+text);
                        container.addView(reply);
                    }catch (Exception e){};
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return container;
    }

    //called by delete Post prompt returns true or false if user wants to remove post
    private void deletePostPrompt(final String courseID, final String postID) {
        //build a yes no alert to ask the user if they want to remove this post
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Post");
        builder.setMessage("Are you sure you want to remove this post?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("USERPOSTS", "Delete Button Action");
                deletePost(courseID, postID);
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

    //removes the post from forum and from admin reported posts if it was reported
    //called by deletePost Prompt
    private void deletePost(String courseID, String postID) {
        //get the directory of the course this post resides in
        DatabaseReference postRoot = FirebaseDatabase.getInstance().getReference()
                .child("Courses").child(courseID).child("Forum").child(postID);

        postRoot.removeValue();
        reportedPostsRoot.child(courseID+" "+postID).removeValue();
        usersPostsRoot.child(postID+" "+courseID).removeValue();

        //LinearLayout toRemove = get
        //forumContainer.rem
        //forumContainer.removeAllViews();
        //drawUsersPosts();
    }

    //function to get called when the user wants to edit one of their posts
    private void editPost(String courseID, String postID, String subject, String question) {
        Intent intent = new Intent(this, CreatePostActivity.class);

        //extras to make CreatePost act as way to edit post
        intent.putExtra("SUBJECT",subject);
        intent.putExtra("QUESTION", question);
        intent.putExtra("POSTID", postID);
        intent.putExtra("COURSE_ID", courseID);
        intent.putExtra("TITLE", "Edit Your Post");
        startActivityForResult(intent, EDIT_CODE);
    }

    //function to convert pixel value into density pixel value
    //used so pixel spacing is consistent across devices
    private int pxToDp(int px){
        int conversion;
        float dp = getResources().getDisplayMetrics().density;
        conversion =  (int) Math.ceil(px * dp);
        return conversion;
    }

    //syncs from firebase the current user's name
    private void getUsersName() {
        userNameRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUsersName = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
