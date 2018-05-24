package com.oneplusplus.christopher.studycompanion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ReplyPostActivity extends AppCompatActivity {

    private Button replyButton;
    private EditText responseText;
    private String postID;
    private String courseID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        replyButton = findViewById(R.id.replyButton);
        responseText = findViewById(R.id.replyTextField);

        postID = getIntent().getStringExtra("POST_ID");
        courseID = getIntent().getStringExtra("COURSE_ID");

        initOnClickActions();
    }

    private void initOnClickActions() {
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputFieldsValid()){
                    String response = responseText.getText().toString();

                    Intent intent = new Intent();
                    intent.putExtra("POST_ID", postID);
                    intent.putExtra("COURSE_ID", courseID);
                    intent.putExtra("REPLY", response);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private boolean inputFieldsValid() {
        String response = responseText.getText().toString();
        if(response.length() < 1){
            return false;
        }
        return true;
    }
}
