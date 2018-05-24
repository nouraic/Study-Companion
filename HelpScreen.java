package com.oneplusplus.christopher.studycompanion;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class HelpScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView link = (TextView) findViewById(R.id.guideLink);
        String linkText = "https://docs.google.com/document/d/1-Te88DH2Dkh_04_C3uwesInRvgBiAs4d7LqjdYNPMgU/edit?usp=sharing";

        link.setText(Html.fromHtml("<a href=\"https://docs.google.com/document/d/1-Te88DH2Dkh_04_C3uwesInRvgBiAs4d7LqjdYNPMgU/edit?usp=sharing\">here</a> "));
        link.setMovementMethod(LinkMovementMethod.getInstance());

    }

}
