package com.oneplusplus.christopher.studycompanion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class CreatePostActivity extends AppCompatActivity {

    Button postButton;
    EditText subject;
    EditText question;
    TextView title;
    CheckBox anonCheckBox;
    Boolean anon = false;   //true or false if user wants to be aonymous
    String subjectStr;
    String questionStr;
    String postIDStr;
    String courseIDStr;
    String[] tempStrArr;
    String titleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        postButton = findViewById(R.id.postQuestionButton);
        subject = findViewById(R.id.subjectText);
        question = findViewById(R.id.questionText);
        anonCheckBox = findViewById(R.id.anonCheckBox);
        title = findViewById(R.id.textView5);

        subjectStr = getIntent().getStringExtra("SUBJECT");
        tempStrArr = subjectStr.split(" - ");
        subjectStr = tempStrArr[0];
        titleName = getIntent().getStringExtra("TITLE");

        questionStr = getIntent().getStringExtra("QUESTION");
        postIDStr = getIntent().getStringExtra("POSTID");
        courseIDStr = getIntent().getStringExtra("COURSE_ID");

        subject.setText(subjectStr);
        question.setText(questionStr);
        title.setText(titleName);

        initOnClickActions();

    }

    private void initOnClickActions() {
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputFieldsValid()) {
                    String subj = subject.getText().toString();
                    String ques = question.getText().toString();

                    Intent intent = new Intent();
                    intent.putExtra("SUBJECT", subj);
                    intent.putExtra("QUESTION", ques);
                    intent.putExtra("POSTID", postIDStr);
                    intent.putExtra("COURSE_ID", courseIDStr);
                    if (anon) {
                        intent.putExtra("ANON", "TRUE");
                    } else {
                        intent.putExtra("ANON", "FALSE");
                    }
                    Log.d("FORUM", "ANON is : " + anon);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        anonCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                anon = b;
            }
        });

    }



    private boolean inputFieldsValid() {
        return true;
    }

}
