package com.oneplusplus.christopher.studycompanion;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class StudyGroupList extends ArrayAdapter<StudyGroup> {
    private Activity context;
    List<StudyGroup> groupList;

    public StudyGroupList(Activity context, List<StudyGroup> groupList) {
        super(context, R.layout.layout_studygroup_list, groupList);
        this.context = context;
        this.groupList = groupList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_studygroup_list, null, true);

        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewEmail);
        TextView textViewDate = (TextView) listViewItem.findViewById(R.id.textViewDate);
        TextView textViewTime = (TextView) listViewItem.findViewById(R.id.textViewTime);
        TextView textViewLocation = (TextView) listViewItem.findViewById(R.id.textViewLocation);
        TextView textViewReoccur = (TextView) listViewItem.findViewById(R.id.textViewReoccur);

        StudyGroup groups = groupList.get(position);
        //Log.d("TEST", String.valueOf(userList.get(2)));
        textViewName.setText(groups.getName());
        textViewDate.setText(groups.getDate());
        textViewTime.setText(groups.getTime());
        textViewLocation.setText(groups.getLocation());
        textViewReoccur.setText(groups.getReoccur());

        return listViewItem;
    }
}
