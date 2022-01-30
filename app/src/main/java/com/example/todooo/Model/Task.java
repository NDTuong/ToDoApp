package com.example.todooo.Model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Task implements Serializable {

    String title;
    String descriptions;
    String tag;
    String dueDate;
    Reminder reminder;
    Repeat repeat;
    int priority;
    String ID;
    boolean isComplete;
    List<String> subTask;
    String timeComplete;
    boolean hasDueDate;

    public Task() {
    }

    public Task(String title, String descriptions, String tag, String dueDate, Reminder reminder, Repeat repeat,
                int priority, String ID, boolean isComplete, List<String> subTask, String timeComplete,
                boolean hasDueDate) {
        this.title = title;
        this.descriptions = descriptions;
        this.tag = tag;
        this.dueDate = dueDate;
        this.reminder = reminder;
        this.repeat = repeat;
        this.priority = priority;
        this.ID = ID;
        this.isComplete = isComplete;
        this.subTask = subTask;
        this.timeComplete = timeComplete;
        this.hasDueDate = hasDueDate;
    }

    public boolean isHasDueDate() {
        return hasDueDate;
    }

    public void setHasDueDate(boolean hasDueDate) {
        this.hasDueDate = hasDueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    public Repeat getRepeat() {
        return repeat;
    }

    public void setRepeat(Repeat repeat) {
        this.repeat = repeat;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public List<String> getSubTask() {
        return subTask;
    }

    public void setSubTask(List<String> subTask) {
        this.subTask = subTask;
    }

    public String getTimeComplete() {
        return timeComplete;
    }

    public void setTimeComplete(String timeComplete) {
        this.timeComplete = timeComplete;
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", descriptions='" + descriptions + '\'' +
                ", tag=" + tag +
                ", dueDate='" + dueDate + '\'' +
                ", reminder=" + reminder +
                ", repeat=" + repeat +
                ", priority=" + priority +
                ", ID='" + ID + '\'' +
                ", isComplete=" + isComplete +
                ", subTask=" + subTask +
                ", timeComplete='" + timeComplete + '\'' +
                ", hasDueDate=" + hasDueDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return ID.equals(task.ID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }
}
