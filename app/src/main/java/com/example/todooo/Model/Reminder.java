package com.example.todooo.Model;

public class Reminder {
    String timeReminder;
    TypeNotifications typeNotify;

    public Reminder() {
    }

    public Reminder(String timeReminder, TypeNotifications typeNotify) {
        this.timeReminder = timeReminder;
        this.typeNotify = typeNotify;
    }

    public String getTimeReminder() {
        return timeReminder;
    }

    public void setTimeReminder(String timeReminder) {
        this.timeReminder = timeReminder;
    }

    public TypeNotifications getTypeNotify() {
        return typeNotify;
    }

    public void setTypeNotify(TypeNotifications typeNotify) {
        this.typeNotify = typeNotify;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "timeReminder='" + timeReminder + '\'' +
                ", typeNotify=" + typeNotify +
                '}';
    }
}
