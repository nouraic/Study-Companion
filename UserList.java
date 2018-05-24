package com.oneplusplus.christopher.studycompanion;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class UserList extends ArrayAdapter<User> {
    private Activity context;
    List<User> userList;

    public UserList(Activity context, List<User> userList) {
        super(context, R.layout.layout_user_list, userList);
        this.context = context;
        this.userList = userList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_user_list, null, true);

        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        TextView textViewGenre = (TextView) listViewItem.findViewById(R.id.textViewEmail);

        User users = userList.get(position);
        textViewName.setText(users.getUserName());
        textViewGenre.setText(users.getUserEmail());

        return listViewItem;
    }
}
