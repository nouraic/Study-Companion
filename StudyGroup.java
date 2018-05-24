package com.oneplusplus.christopher.studycompanion;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class StudyGroup {
    public String name;
    public String date;
    public String time;
    public String location;
    public String owner;
    public String reoccur;
    //public String numMembers;

    public StudyGroup(){

    }

    public StudyGroup(String name, String date, String time, String location, String owner, String reoccur, int hour, int minute) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.location = location;
        this.owner = owner;
        this.reoccur = reoccur;
        //this.numMembers = numMembers;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() { return time; }

    public String getLocation() {
        return location;
    }

    public String getOwner() { return owner; }

    public String getReoccur() { return reoccur; }

    //public String getNumMembers() { return numMembers;}
}

