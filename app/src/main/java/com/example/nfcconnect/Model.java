package com.example.nfcconnect;

public class Model {
    String Time,Date,User;
    public Model(){
    }

    public Model(String time, String date, String user) {
        Time = time;
        Date = date;
        User = user;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }
}
