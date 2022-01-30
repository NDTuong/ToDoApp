package com.example.todooo.Model;

import java.util.List;

public class Repeat {
    TypeRepeat type;
    int repeatEvery;
    List<Integer> repeatOnWeek;

    public Repeat() {
    }

    public Repeat(TypeRepeat type, int repeatEvery, List<Integer> repeatOnWeek) {
        this.type = type;
        this.repeatEvery = repeatEvery;
        this.repeatOnWeek = repeatOnWeek;
    }

    public TypeRepeat getType() {
        return type;
    }

    public void setType(TypeRepeat type) {
        this.type = type;
    }

    public int getRepeatEvery() {
        return repeatEvery;
    }

    public void setRepeatEvery(int repeatEvery) {
        this.repeatEvery = repeatEvery;
    }

    public List<Integer> getRepeatOnWeek() {
        return repeatOnWeek;
    }

    public void setRepeatOnWeek(List<Integer> repeatOnWeek) {
        this.repeatOnWeek = repeatOnWeek;
    }

    @Override
    public String toString() {
        return "Repeat{" +
                "type=" + type +
                ", repeatEvery=" + repeatEvery +
                ", repeatOnWeek=" + repeatOnWeek +
                '}';
    }
}
