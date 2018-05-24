package com.oneplusplus.christopher.studycompanion;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StudyGroupsTab extends Fragment implements OnClickListener{

    private ListView listview;
    private FloatingActionButton createGroupButton;
    private List<StudyGroup> groupList;
    private String courseID;
    private Date onCreateTime;
    DatabaseReference drefGetStudyGroups;

    InternetTime time = new InternetTime();

    private ValueEventListener listener;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tabstudygroups, container, false);
        courseID = (String) getArguments().getString("COURSE_ID");// get the current course

        Task timeTask = new Task();
        try {
            onCreateTime = timeTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        listview = rootView.findViewById(R.id.cStudyGroupList);                           // list of groups
        createGroupButton = (FloatingActionButton) rootView.findViewById(R.id.ccreateStudyGroupButton);                     // button to create group

        groupList = new ArrayList<>();                                                               // array list of groups

        drefGetStudyGroups = FirebaseDatabase.getInstance().getReference().child("Courses").child(courseID).child("Study Groups");

        listener = drefGetStudyGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupList.clear();                                                     // clear previous group
                for (DataSnapshot studygroupSnapshot : dataSnapshot.getChildren()) {
                    StudyGroup group = studygroupSnapshot.getValue(StudyGroup.class);  // for each datashot create StudyGroup object
                    try {
                        if (manageStudyGroup(group)) {
                            groupList.add(group);                                      // add to array list
                            listview.setOnItemClickListener(onListClick);
                        }
                    } catch (Exception e) {
                       e.printStackTrace();
                    }
                }
                try {
                    StudyGroupList adapter = new StudyGroupList(getActivity(), groupList);
                    listview.setAdapter(adapter);
                } catch(Exception e) { Log.d("ERROR", "HERE"); }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });

        createGroupButton.setOnClickListener(this);                                    // on click listener for group creation popup

        return rootView;
    }

    // IF A GROUP IN THE LIST IS CLICKED
    private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String current = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString(); // get email of the current user
            String groupOwner = groupList.get(position).getOwner().toString();                  // get email of owner of group

            if(current.equals(groupOwner)) {        // if the current users email is the same as the owners
                if(isNetworkAvailable()) {
                    popup(position);                    // allow them to edit
                } else { Toast.makeText(getContext(), "Cannot connect to the internet",Toast.LENGTH_LONG).show();}
            }
            else // ADD TOAST HERE INSTEAD
                Toast.makeText(getContext(), "You are not the owner of this group",Toast.LENGTH_LONG).show();

        }
    };

    // if create study group button is clicked
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        if(v == createGroupButton) {
            if(isNetworkAvailable()) {
                popup(null);        // show popup
            } else { Toast.makeText(getContext(), "Cannot connect to the internet",Toast.LENGTH_LONG).show();}
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean manageStudyGroup(StudyGroup group) throws ParseException, ExecutionException, InterruptedException {

        Date groupTime = stringToDate(group.getDate(), group.getTime());

        // if the group time has passed an hour over current time
        if(groupTime.getTime() < onCreateTime.getTime()+3600000L) {
            drefGetStudyGroups.removeEventListener(listener);
            if(group.getReoccur().equals("Once")) {                          // check if group only meets once
                drefGetStudyGroups.child(group.getName()).removeValue();   // remove group
                drefGetStudyGroups.addValueEventListener(listener);
                return false;                                           // return false to prevent adding group
            }
            else {
                // get a week from the current group date (group date - 1 hour + a week) and update it in firebase
                Date newDate = new Date(groupTime.getTime()-3600000L + 604800000L);
                SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
                drefGetStudyGroups.child(group.getName()).child("date").setValue(sdf.format(newDate));     // update in firebase
                drefGetStudyGroups.addValueEventListener(listener);
            }
        }
        else {                                                          // group time has not passed
            return true;                                                // therefore return true to add the group
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Date stringToDate(String date, String time) throws ParseException {
        String timestampString = date + " " + time;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("EST"));
        Date timestamp = sdf.parse(timestampString);
        return timestamp;
    }

    // STUDY GROUP CREATION MENU
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void popup(final Integer position) {
        final View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_layout, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);                                  // popup properties
        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);      // popup properties

        final EditText name;            // name
        final TextView dateText;        // date
        final TextView error;           // error if the selected date is old
        final TextView timeText;        // time
        final EditText location;        // location
        final Button create;            // create button
        final Button delete;            // delete button
        final CheckBox reoccur;         // reoccur check
        final Date timeStamp;

        // define components of popup
        name = popupView.findViewById(R.id.cname);
        dateText = (TextView) popupView.findViewById(R.id.cdate);
        timeText = (TextView) popupView.findViewById(R.id.ctime);
        create = (Button) popupView.findViewById(R.id.ccreate);
        location = popupView.findViewById(R.id.clocation);
        error = (TextView) popupView.findViewById(R.id.cerror);
        delete = (Button) popupView.findViewById(R.id.cdelete);
        delete.setVisibility(View.GONE);
        reoccur = (CheckBox) popupView.findViewById(R.id.creoccur);

        // IF BEING EDITED, GET THE VALUES OF THE GROUP TO FILL CONTENTS
        if(position!=null) {
            String oldName = groupList.get(position).getName();
            name.setText(groupList.get(position).getName());
            dateText.setText(groupList.get(position).getDate());
            timeText.setText(groupList.get(position).getTime());
            location.setText(groupList.get(position).getLocation());
            if(groupList.get(position).getReoccur().equals("Weekly")) {
                reoccur.setChecked(true);                               // if weekly check off weekly
            }
            else {
                reoccur.setChecked(false);                              // if once make sure check box is empty
            }
            delete.setVisibility(View.VISIBLE);                         // allow user to delete group
            // ask user if they are sure they want to delete group
            // https://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-on-android
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    drefGetStudyGroups.child(groupList.get(position).getName()).removeValue();
                                    popupWindow.dismiss();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Are you sure you want to delete this study group?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            });
            create.setText("SAVE CHANGES");
        }

        // DATE PICKER
        // reference https://github.com/mitchtabian/DatePickerDialog/blob/master/DatePickerDialog/app/src/main/java/tabian/com/datepickerdialog/MainActivity.java
        dateText.setOnClickListener(new View.OnClickListener() {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            Date timeStamp;
            String monthDayYear;
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        year = selectedYear;
                        month = 1 + selectedMonth;
                        day = selectedDay;
                        monthDayYear = month + "/" + day + "/" + year;                  // creating string of desired date format
                        Log.d("DATE", monthDayYear);
                        dateText.setText(monthDayYear);                                 // set date text
                        timeStamp = new Date(year, month, day, hour, minute, 0);
                    }
                }, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        hour = selectedHour;
                        minute = selectedMinute;
                        Date time = null;
                        try {
                            time = new SimpleDateFormat("HH:mm").parse(hour+":"+minute);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        String timeToString = new SimpleDateFormat("hh:mm a").format(time); // format time
                        timeText.setText(timeToString);                                             // set text to time
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        // TIME PICKER
        // trigger when the user clicks the create button to create a new study group
        create.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    try {
                        Task timeTask = new Task();
                        Date todayDate = onCreateTime;
                        if (checkForValidGroup(todayDate)) {         // check if the group is valid (time/date)
                            if (position != null) {
                                drefGetStudyGroups.child(groupList.get(position).getName()).removeValue();// if edited remove old name
                            }
                            Log.d("TAG", "REMOVING EVENT LISTENER");
                            drefGetStudyGroups.removeEventListener(listener);
                            // write/update study group information to firebase
                            String studyName = name.getText().toString();
                            drefGetStudyGroups.child(studyName).child("name").setValue(name.getText().toString());
                            drefGetStudyGroups.child(studyName).child("date").setValue(dateText.getText().toString());
                            drefGetStudyGroups.child(studyName).child("time").setValue(timeText.getText().toString());
                            drefGetStudyGroups.child(studyName).child("location").setValue(location.getText().toString());
                            drefGetStudyGroups.child(studyName).child("owner").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
                            if (reoccur.isChecked()) {
                                drefGetStudyGroups.child(studyName).child("reoccur").setValue("Weekly");
                            } else {
                                drefGetStudyGroups.child(studyName).child("reoccur").setValue("Once");
                            }
                            popupWindow.dismiss();
                            drefGetStudyGroups.addValueEventListener(listener);
                        } else {
                            error.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Log.d("EXCEPTION", e.toString());
                    }
                } else { Toast.makeText(getContext(), "Cannot connect to the internet",Toast.LENGTH_LONG).show();}
            }


            // FUNCTION TO CHECK IF GROUP IS VALID TO CREATE
            boolean checkForValidGroup(Date todayDate) throws ParseException {
                // if name is not entered then error
                if(name.getText().toString().equals("") || name.getText().toString().equals(null)) {
                    error.setText("Please input a study group name");
                    return false;
                }
                /*
                // for future use
                // if the name is already taken then error
                else if(!checkNameValid(name.getText().toString())) {
                    error.setText("This study group name has already been taken");
                    return false;
                }
                */
                // if location is not entered then error
                else if(location.getText().toString().equals("") || location.getText().toString().equals(null)) {
                    error.setText("Please input a location of where the group should meet");
                    return false;
                }
                // if date and or time is not entered then error
                else if(dateText.getText().toString().equals("CHOOSE A DATE AND A TIME") || timeText.getText().toString().equals("SELECT TIME")) {
                    error.setText("Please select a time and date");
                    error.setVisibility(View.VISIBLE);
                    return false;
                }
                // if date/time is an hour older than the current time then error
                try {
                    if (stringToDate(dateText.getText().toString(), timeText.getText().toString()).getTime() - 3600000 < todayDate.getTime()) {
                      error.setText("Please select a valid time and date");
                        return false;
                    }
                }catch(Exception e) { error.setText("Please select a valid time and date"); return false; }

                return true;
            }

            // for future use
            /*
            // go through all study group names to check if new name is
            boolean checkNameValid(String studyGroupName) {
                for(int i=0; i<groupList.size();i++) {
                    if(groupList.get(i).getName().equals(studyGroupName)) {
                        return false;
                    }
                }
                return true;
            }
            */

        });

        popupWindow.showAsDropDown(popupView, 0, 0);
    }

    // get global time
    public class Task extends AsyncTask<Date, Date, Date> {
        @Override
        protected Date doInBackground(Date... date) {
            return time.fetchCurrentInternetTime();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}