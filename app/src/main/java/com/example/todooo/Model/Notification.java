package com.example.todooo.Model;

public class Notification {
    long dueDate;
    long reminder;
    String ID;
    TypeNotifications type;
    String title;
    String desc;

    public Notification() {
    }

    public Notification(long dueDate, long reminder, String ID, TypeNotifications type, String title, String desc) {
        this.dueDate = dueDate;
        this.reminder = reminder;
        this.ID = ID;
        this.type = type;
        this.title = title;
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public long getReminder() {
        return reminder;
    }

    public void setReminder(long reminder) {
        this.reminder = reminder;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public TypeNotifications getType() {
        return type;
    }

    public void setType(TypeNotifications type) {
        this.type = type;
    }
}
