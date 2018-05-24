package com.oneplusplus.christopher.studycompanion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class ForumTab extends Fragment{
    private final int POST_CODE = 1;
    private final int REPLY_CODE = 2;
    private final int EDIT_CODE = 3;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference userNameRoot;
    private String userID;
    private DatabaseReference courseForumRoot;
    private DatabaseReference usersPostsRoot;
    private DatabaseReference reportedPostsRoot;
    private LinearLayout forumContainer;
    private FloatingActionButton addPost;
    private String courseName;
    private String courseID;
    private ArrayList<String>forumPosts = new ArrayList<>();
    private ValueEventListener listener;
    private String pID;
    private String currentUsersName;
    private static Context mContext;
    private InternetTime inetTime;

    private boolean userChoice; //used for creating yes no dialogs

    private ProgressBar loadingSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tabforum, container, false);

        //trying to draw the menu bar

        courseID = (String) getArguments().getString("COURSE_ID");
        courseName = (String) getArguments().getString("COURSE_NAME");

        Log.d("ADDCOURSE","CourseID "+courseID+", CourseName "+courseName);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //the root to the admin location of all reported posts
        reportedPostsRoot = FirebaseDatabase.getInstance().getReference().child("Admin").child("Reports");

        inetTime = new InternetTime();  //declare new internet time, this gets the time from a server instead of the user's phone

        userNameRoot = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid()).child("UserInfo").child("Name");
        userID = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid()).getKey().toString();

        //path to this class forum
        courseForumRoot = FirebaseDatabase.getInstance().getReference().child("Courses").child(courseID).child("Forum");
        usersPostsRoot = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid()).child("UsersPosts");
        forumContainer = rootView.findViewById(R.id.forumContainer);
        addPost = rootView.findViewById(R.id.submitPostButton);
        loadingSpinner = rootView.findViewById(R.id.forumSpinner);
        loadingSpinner.setVisibility(View.GONE);

        //ViewPager pager = rootView.findViewById(R.id.contain)

        //show the loading spinner when the page starts up, dismisses when the forum is done loading
        loadingSpinner.setVisibility(View.VISIBLE);
        getUsersName();
        getForumPosts();
        initOnClickActions();
        main();

        return rootView;
    }

    // add code here
    void main() {

    }

    private void initOnClickActions() {
        //add a post button listener
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FORUM", "Add post: ");
                Intent intent = new Intent(getContext(), CreatePostActivity.class);

                //all blank because these will be used when this intent is used to edit existing post
                intent.putExtra("SUBJECT","");
                intent.putExtra("QUESTION", "");
                intent.putExtra("POSTID", "");
                intent.putExtra("TITLE", "Create New Post");
                startActivityForResult(intent, POST_CODE);
            }
        });
    }

    //handles all of the activity results (add post, edit post, reply )
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        String userNameToShow = currentUsersName;
        if(requestCode == POST_CODE) {  //post in forum request returned
            if(resultCode == RESULT_OK) {
                if(isNetworkAvailable()) {
                    Date worldTime = inetTime.fetchCurrentInternetTime();  //get the internet time
                    String anon = data.getStringExtra("ANON");
                    String subject = data.getStringExtra("SUBJECT");
                    String question = data.getStringExtra("QUESTION");
                    //post id, uses the time post was created to the second to make unique id for it
                    //String postId = new SimpleDateFormat("yy:MM:dd HH:mm:ss").format(new java.util.Date());
                    String postId = new SimpleDateFormat("yy:MM:dd HH:mm:ss").format(worldTime);

                    //timeStamp doesn't show the seconds, this is used to display when item was posted in forum
                    //String timeStamp = new SimpleDateFormat("MM/dd/yy hh:mm a").format(new java.util.Date());
                    String timeStamp = new SimpleDateFormat("MM/dd/yy hh:mm a").format(worldTime);

                    if (anon.equals("TRUE")) {
                        userNameToShow = "Anonymous";
                        Log.d("COURSES", "User is anonymous");
                    } else {
                        userNameToShow = currentUsersName;
                    }

                    //adds the post to the database under this course's location
                    courseForumRoot.removeEventListener(listener); //stop listening to prevent null pointers


                    courseForumRoot.child(postId).child("Subject").setValue(subject + " ⇊ " + userNameToShow);
                    courseForumRoot.child(postId).child("Question").setValue(question);
                    courseForumRoot.child(postId).child("Time Stamp").setValue(timeStamp);
                    courseForumRoot.child(postId).child("Post ID").setValue(postId);
                    courseForumRoot.child(postId).child("Author").setValue(userID);
                    courseForumRoot.child(postId).child("Reports").setValue("0");

                    courseForumRoot.addValueEventListener(listener);    //resume listening

                    //adds the post id and course id for this post. This is for when the user goes to
                    //the my posts page UserPosts.java can find their posts in their directory
                    usersPostsRoot.child(postId + " " + courseID).child("Post ID").setValue(postId);
                    usersPostsRoot.child(postId + " " + courseID).child("Course ID").setValue(courseID);
                } else { Toast.makeText(getContext(), "Cannot connect to the internet", Toast.LENGTH_LONG).show();}
            }
        }

        if(requestCode == REPLY_CODE) { //reply to a post request returned
            if(resultCode == RESULT_OK) {
                if(isNetworkAvailable()) {
                    String responseID = new SimpleDateFormat("yy:MM:dd HH:mm:ss").format(inetTime.fetchCurrentInternetTime());
                    String postID = data.getStringExtra("POST_ID");
                    //String replyTo = data.getStringExtra("REPLY_TO");
                    String reply = data.getStringExtra("REPLY");

                    //stop listening to prevent null pointers
                    courseForumRoot.removeEventListener(listener);
                    courseForumRoot.child(postID).child("Replies").child(responseID).setValue(currentUsersName + ": " + reply);
                    courseForumRoot.addValueEventListener(listener);    //resume listening after data stored
                } else { Toast.makeText(getContext(), "Cannot connect to the internet", Toast.LENGTH_LONG).show(); }
            }
        }

        if(requestCode == EDIT_CODE) {  //user wants to edit a post
            if(resultCode == RESULT_OK) {
                if(isNetworkAvailable()) {
                    String subject = data.getStringExtra("SUBJECT");
                    String question = data.getStringExtra("QUESTION");
                    String postID = data.getStringExtra("POSTID");

                    courseForumRoot.removeEventListener(listener); //stop listening to prevent null pointers

                    courseForumRoot.child(postID).child("Subject").setValue(subject + " ⇊ " + currentUsersName + " (EDITED)");
                    courseForumRoot.child(postID).child("Question").setValue(question);

                    courseForumRoot.addValueEventListener(listener);    //resume listening
                } else { Toast.makeText(getContext(), "Cannot connect to the internet", Toast.LENGTH_LONG).show(); }
            }
        }
    }

    //gets all of the forum posts for this class and builds all the views for the forum
    private void getForumPosts() {
        listener = courseForumRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                forumContainer.removeAllViews();
                int counter = 0;
                ArrayList<String>replies = new ArrayList<>();
                replies.clear();

                for(DataSnapshot index : dataSnapshot.getChildren()) {
                    //get the question subject timestamp
                    try {
                        String question = index.child("Question").getValue().toString();
                        String subject = index.child("Subject").getValue().toString();
                        String timeStamp = index.child("Time Stamp").getValue().toString();
                        String postID = index.child("Post ID").getValue().toString();
                        String author = index.child("Author").getValue().toString();
                        String numReports = index.child("Reports").getValue().toString();

                        makeForumViews(subject, question, timeStamp, counter, postID, author, numReports);
                        counter++;
                    } catch (Exception e){};
                }
                loadingSpinner.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //function that creates all of the views to add to the screen for each forum post
    private void makeForumViews(final String subj, final String question, String timeStamp, int counter, final String postID, String author, final String numReports){
        //sets the background color to blue to separate the posts
        forumContainer.setBackgroundColor(Color.parseColor("#1DA1F2"));

        //split the subject of format Subject - Name to get two strings the subject and the name
        String[] temp = subj.split(" ⇊ ");
        final String subject = temp[0];
        String name = temp[1];


        android.widget.LinearLayout.LayoutParams timeStampLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //timeStampLayout.width = 300;
        timeStampLayout.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        timeStampLayout.gravity = Gravity.END;
        android.widget.LinearLayout.LayoutParams subjectLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        subjectLayout.width = pxToDp(300);
        //subjectLayout.gravity = Gravity.LEFT;
        //android.widget.TextView.LayoutParams buttonParams = new TextView.LayoutP
        android.widget.LinearLayout.LayoutParams buttons = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //layout params for the button icons
        RelativeLayout.LayoutParams iconButtons = new RelativeLayout.LayoutParams(100, 100);
        iconButtons.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
        iconButtons.width = pxToDp(40);
        iconButtons.height = pxToDp(40);

        //decide the background color for the post
        int backgroundColor = Color.parseColor("#E8E8E8");
        if( (counter % 2) == 0){
            backgroundColor = Color.WHITE;
        }


        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(8);

        //container to add everything to and then add this to screen
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(backgroundColor);

        //this will house all the user options for each post
        LinearLayout bottomToolBar = new LinearLayout(getContext());
        bottomToolBar.setOrientation(LinearLayout.HORIZONTAL);

        TextView nameView = new TextView(getContext());
        nameView.setText(name);
        nameView.setTextSize(19);
        nameView.setTypeface(null, Typeface.BOLD);
        nameView.setLayoutParams(subjectLayout);

        //container to house the timestamp and the subject
        LinearLayout titleContainer = new LinearLayout(getContext());
        titleContainer.setOrientation(LinearLayout.HORIZONTAL);

        //fields for timestamp and subject
        TextView timeStampView = new TextView(getContext());
        TextView subjectView = new TextView(getContext());

        //SUBJECT SETUP
        subjectView.setTextSize(17);
        subjectView.setText("Sub: "+subject);
        subjectView.setTypeface(null, Typeface.BOLD);
        subjectView.setLayoutParams(subjectLayout);


        //TIME STAMP SETUP
        timeStampView.setText(timeStamp);
        timeStampView.setLayoutParams(timeStampLayout);

        //Add authors name and timestamp to the title container
        titleContainer.addView(nameView);
        titleContainer.addView(timeStampView);

        //container to house the question
        LinearLayout questionContainer = new LinearLayout((getContext()));
        questionContainer.setOrientation(LinearLayout.HORIZONTAL);

        //fields for question
        TextView q = new TextView(getContext());    //put a bold Q: before the question
        TextView questionView = new TextView(getContext());
        questionView.setTextIsSelectable(true);

        //QUESTION SETUP
        q.setText("Q: ");
        q.setTypeface(null, Typeface.BOLD);
        q.setTextSize(18);

        questionView.setGravity(Gravity.LEFT);
        questionView.setText(question);
        questionView.setTextSize(17);

        //add Q: and question to the question container
        questionContainer.addView(q);
        questionContainer.addView(questionView);

        /* //OLD REPLY BUTTON SETUP
        TextView reply = new TextView(getContext());    //will act as a button
        reply.setText("Reply:");
        reply.setTextColor(Color.parseColor("#0000FF"));
        reply.setLayoutParams(buttons);
        */
        ImageButton reply = new ImageButton(getContext());
        reply.setAdjustViewBounds(true);
        reply.setBackgroundColor(Color.TRANSPARENT);
        reply.setLayoutParams(iconButtons);
        reply.setImageResource(R.drawable.comment_button);

        bottomToolBar.addView(reply);


        //only gets added if the user is the author of the post
        ImageButton delete = new ImageButton(getContext());    //will act as a button has to be declared here
        ImageButton edit = new ImageButton(getContext());     //act as button has to be declared here

        //only gets added if the user isn't the author of a post
        TextView report = new TextView((getContext())); //will act as a report button

        //spacer view makes things look nicer spaced out
        TextView spacer = new TextView(getContext());

        //Container to house the replies
        LinearLayout replyContainer = makeReplyContainer(postID);

        //add all sub-containers to the Main container to add to the view
        container.addView(titleContainer);
        container.addView(subjectView);
        container.addView(questionContainer);
        container.addView(replyContainer);
        container.addView(new TextView(getContext()));  //add a spacer (no format)
        container.addView(bottomToolBar);


        //CREATE DELETE AND EDIT BUTTONS ONLY IF THIS USER WAS THE AUTHOR
        try {   //because some old posts dont have an author
            if( userID.equals(author) ) {   //only add these buttons if this user was the author

                /*//OLD DELETE BUTTON SETUP
                delete.setText("DELETE ");
                delete.setTextColor(Color.parseColor("#FF0000"));
                //delete.setGravity(Gravity.END);
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
                        deletePostPrompt(postID);
                    }
                });

                //spacer between delete and edit buttons
                TextView newSpacer = new TextView(getContext());
                newSpacer.setLayoutParams(iconButtons);
                bottomToolBar.addView(newSpacer);

                /* //OLD EDIT BUTTON SETUP
                edit.setText("EDIT POST ");
                edit.setTextColor(Color.parseColor("#00FF00"));
                //edit.setGravity(Gravity.END);
                */

                //ImageButton edit = new ImageButton(getContext());
                edit.setAdjustViewBounds(true);
                edit.setBackgroundColor(Color.TRANSPARENT);
                edit.setLayoutParams(iconButtons);
                edit.setImageResource(R.drawable.edit_icon);


                //set onclick listener for edit button
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editPost(postID, subject, question);
                    }
                });

                //add elements to the container to be returned as one view later
                bottomToolBar.addView(edit);
                //container.addView(newSpacer);
                bottomToolBar.addView(delete);
            }
            else if( ! userID.equals(author) ) {    //if the current user isn't the author
                //create the report button for the post
                report.setText("Report Post ");
                report.setTextColor(Color.parseColor("#FF0000"));
                report.setGravity(Gravity.END);

                //create on click listener for the report button
                report.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reportPost(postID, numReports, subject, question);
                    }
                });

                //add element to the container to be returned as one view later
                container.addView((report));
            }
        }catch (Exception e){};

        //container.addView(spacer);

        //set onclick listener for the reply button
        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ReplyPostActivity.class);
                intent.putExtra("POST_ID", postID);
                startActivityForResult(intent, 2);
            }
        });

        //then add container to the Main view
        //pushing to index 0 makes most recent posts at the top of the feed
        TextView endSpacer = new TextView(getContext());
        endSpacer.setHeight(pxToDp(45));
        spacer.setHeight(pxToDp(5));
        forumContainer.addView(spacer,0);
        forumContainer.addView(container, 0);
        forumContainer.addView(endSpacer);
    }//end of makeForumViews


    //returns a container of all replies for certain post
    private LinearLayout makeReplyContainer(String postID){
        final LinearLayout repliesContainer = new LinearLayout(getContext());
        repliesContainer.setOrientation(LinearLayout.VERTICAL);

        TextView r = new TextView(getContext());    //textview to say "Replies"
        r.setText("Replies:");
        r.setTypeface(null, Typeface.BOLD);
        repliesContainer.addView(r);


        DatabaseReference courseForumRepliesRoot = courseForumRoot.child(postID).child("Replies");
        courseForumRepliesRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getContext();
                for(DataSnapshot index : dataSnapshot.getChildren()){
                    try {
                        String text = index.getValue().toString();
                        TextView reply = new TextView(getContext());
                        reply.setTextIsSelectable(true);
                        reply.setText("↳"+text);
                        repliesContainer.addView(reply);
                    }
                    catch (Exception e) {};
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return repliesContainer;
    }

    //called by delete Post prompt returns true or false if user wants to remove post
    private void deletePostPrompt(final String postID) {
        //build a yes no alert to ask the user if they want to remove this post
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Remove Post");
        builder.setMessage("Are you sure you want to remove this post?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePost(postID);
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
    private void deletePost(String postID) {
        courseForumRoot.child(postID).removeValue();
        reportedPostsRoot.child(courseID+" "+postID).removeValue();
        usersPostsRoot.child(postID+" "+courseID).removeValue();
    }

    //function to get called when the user wants to edit one of their posts
    private void editPost(String postID, String subject, String question) {
        Intent intent = new Intent(getContext(), CreatePostActivity.class);

        //extras to make CreatePost act as way to edit post
        intent.putExtra("SUBJECT",subject);
        intent.putExtra("QUESTION", question);
        intent.putExtra("POSTID", postID);
        intent.putExtra("TITLE", "Edit Your Post");
        startActivityForResult(intent, EDIT_CODE);
    }

    //Function to report a post in the forum. Checks with the database to get the list of users who have already
    //reported this post, if the current user has already reported it prompt them that they can't report again
    //otherwise it will report the post and send the post's information to the admin section of the database
    //if a post gets reported 3 times it auto deletes
    private void reportPost(final String postID, final String nReports, final String subject, final String question ){
        //Get all of the users who have reported this post to see if current user has already reported it
        DatabaseReference ref = courseForumRoot.child(postID).child("ReportedBy");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<String> users = new ArrayList<String>();  //array of users who reported this post
                //store all the users who reported this post
                for(DataSnapshot index : dataSnapshot.getChildren()){
                    users.add(index.getValue().toString());
                }
                //if user has not reported this post already
                if( ! userAlreadyReported(postID, users)){
                    //ask the user to confirm to report the post
                    reportPostPrompt(nReports, postID, subject, question);
                }
                else {
                    Log.d("Reports","User has reported this post");
                    //diaplay an "OK" dialog letting the user know they already reported this post
                    String title = "Report Post";
                    String message = "You have already reported this post.";

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(title);
                    builder.setMessage(message);

                    //user YES action
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do nothing
            }
        });
    }

    //returns true or false if the user has already reported the post
    private boolean userAlreadyReported(String postID, ArrayList<String> users) {
        Log.d("Reports","Checking if user already reported post");

        if(users.indexOf(userID) != -1){    //if the user was found in the array
            return true;
        }
        return false;   //user wasn't found
    }

    //function to aks the user if they want to report the post if they do, function reports the post
    //otherwise dismiss the prompt
    private void reportPostPrompt(final String nReports, final String postID, final String subject, final String question) {
        String title = "Report Post";
        String message = "Are you sure you want to report this post? Reporting this post confirms " +
                "that this post is either inappropriate, offensive, or irrelevant in some way. " +
                "If this is the case please tap YES to report, otherwise tap NO";

        //build the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);

        //user YES action
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //report the post
                int numReports = Integer.parseInt(nReports);
                numReports++; //increment the number of times post has been reported

                //if its been reported 3 or more times now, auto delete the post
                if( numReports >= 3) {
                    //deletes post from forum and admin section
                    deletePost(postID);
                }
                //else store the incremented value and add this post to the admin reported posts
                //so admin can see it has been reported
                else {
                    //update the forum post values
                    courseForumRoot.child(postID).child("Reports").setValue(numReports);
                    courseForumRoot.child(postID).child("ReportedBy").child(userID).setValue(userID);

                    //update the reported posts for the admin
                    reportedPostsRoot.child(courseID+" "+postID).child("Subject").setValue(subject);
                    reportedPostsRoot.child(courseID+" "+postID).child("Question").setValue(question);
                    reportedPostsRoot.child(courseID+" "+postID).child("CourseName").setValue(courseID);
                    reportedPostsRoot.child(courseID+" "+postID).child("NumReports").setValue(numReports);
                    reportedPostsRoot.child(courseID+" "+postID).child("PostID").setValue(postID);
                }
            }
        });

        //usr NO action
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    //syncs from firebase the current user's name
    private void getUsersName() {
        userNameRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    currentUsersName = dataSnapshot.getValue().toString();
                } catch(Exception e) {};
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //function to convert pixel value into density pixel value
    //used so pixel spacing is consistent across devices
    private int pxToDp(int px){
        int conversion;
        float dp = getResources().getDisplayMetrics().density;
        conversion =  (int) Math.ceil(px * dp);
        return conversion;
    }

    //used to get the timestamp from the internet rather than the user's device, this prevents things
    //being off because the user's phone had the wrong time, allows for consistency
    private Date getInternetTime()  {
        Date time;

        String TIME_SERVER = "time-a.nist.gov";
        NTPUDPClient timeClient = new NTPUDPClient();

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(TIME_SERVER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        TimeInfo timeInfo = null;
        try {
            timeInfo = timeClient.getTime(inetAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
       // NtpV3Packet message = timeInfo.getMessage();
        long returnTime = timeInfo.getReturnTime();
        //long serverTime = message.getTransmitTimeStamp().getTime();
        time = new Date(returnTime);
        return time;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}