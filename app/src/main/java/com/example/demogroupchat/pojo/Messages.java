package com.example.demogroupchat.pojo;

public class Messages {
    private String from, message, type, time;
    private boolean isseen;

    public Messages(String from, String message, String type, String time,boolean isseen) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
        this.isseen = isseen;
    }

    public boolean getIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public Messages() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


}
