package com.example.todooo.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooo.Adapter.RecycleViewCalendarAdapter;
import com.example.todooo.LoginActivity;
import com.example.todooo.Model.Task;
import com.example.todo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shrikanthravi.collapsiblecalendarview.data.Day;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class CalendarFragment extends Fragment {
    View view;

    CollapsibleCalendar collapsibleCalendar;
    TextView tv;
    ImageView ivBack2Today;

    private RecyclerView rcvCalendar;
    private RecycleViewCalendarAdapter rcvViewCalendarAdapter;

    List<Task> taskList;
    List<Task> todayTask;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    String today;

    private final static String TAG = "Calendar Fragment";
    String UID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calendar, container, false);
        // Get current user ID
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            UID = currentUser.getUid();
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID + "/task");

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        today = checkLessThan10(day) + "/" + checkLessThan10(month) + "/" + year;

        rcvCalendar = view.findViewById(R.id.recycleViewCalendar);
        rcvViewCalendarAdapter = new RecycleViewCalendarAdapter(view.getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext(), RecyclerView.VERTICAL, false);
        rcvCalendar.setLayoutManager(linearLayoutManager);
        getData(today);
        rcvViewCalendarAdapter.setData(taskList, UID);
        rcvCalendar.setAdapter(rcvViewCalendarAdapter);

        collapsibleCalendar = view.findViewById(R.id.calendarView);
        collapsibleCalendar.getTodayItemPosition();
        collapsibleCalendar.setSelected(true);
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                Day day = collapsibleCalendar.getSelectedDay();
                today = checkLessThan10(day.getDay()) + "/" + checkLessThan10(day.getMonth() + 1) + "/" + day.getYear();
                Log.d(TAG, "Selected Day: " + today);
                getData(today);
            }

            @Override
            public void onItemClick(@NonNull View view) {

            }

            @Override
            public void onDataUpdate() {

            }

            @Override
            public void onMonthChange() {

            }

            @Override
            public void onWeekChange(int i) {

            }
        });

        ivBack2Today = view.findViewById(R.id.ivBack2Today);
        ivBack2Today.setOnClickListener(v -> {
            Calendar calendar2 = Calendar.getInstance();
            int year1 = calendar2.get(Calendar.YEAR);
            int month1 = calendar2.get(Calendar.MONTH);
            int day1 = calendar2.get(Calendar.DAY_OF_MONTH);
            int year2 = collapsibleCalendar.getYear();
            int month2 = collapsibleCalendar.getMonth();
            int prevNext = (year1 - year2) * 12 + (month1 - month2);
            if (prevNext < 0) {
                prevNext = -prevNext;
                for (int i = 0; i < prevNext; i++) {
                    collapsibleCalendar.prevMonth();
                }
            } else {
                for (int i = 0; i < prevNext; i++) {
                    collapsibleCalendar.prevMonth();
                }
            }
            collapsibleCalendar.expand(1);
            Day d = new Day(year1, month1, day1);
            collapsibleCalendar.select(d);
        });

        return view;
    }

    public String checkLessThan10(int i) {
        String s;
        if (i < 10) {
            s = "0" + i;
        } else {
            s = "" + i;
        }
        return s;
    }

    public void getData(String today) {
        taskList = new ArrayList<>();
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Task task = snapshot.getValue(Task.class);
                if (task != null) {
                    if(taskList != null && taskList.contains(task)){
                        return;
                    }
                    if (task.getDueDate() != null && task.getDueDate().equals(today) && !task.isComplete()) {
                        taskList.add(task);
                        Log.d(TAG, "onChildAdded: added +1" );
                    }
                }
                rcvViewCalendarAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Task task = snapshot.getValue(Task.class);
                if (task == null || taskList == null || taskList.isEmpty()) {
                    return;
                }
                boolean inList = false;
                for (int i = 0; i < taskList.size(); i++) {
                    if (task.getID().equals(taskList.get(i).getID())){
                        if(!task.isComplete() && task.getDueDate() != null && task.getDueDate().equals(today)) {
                            taskList.set(i, task);
                        }
                        if(task.isComplete() || task.getDueDate() == null ||
                                (task.getDueDate() != null && !task.getDueDate().equals(today))){
                            taskList.remove(task);
                        }
                        inList = true;
                        break;
                    }
                }
                if (!inList && task.getDueDate() != null && task.getDueDate().equals(today) && !task.isComplete()) {
                    taskList.add(task);
                    Log.d(TAG, "onChildChange: added +1" );
                }
                rcvViewCalendarAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Task task = snapshot.getValue(Task.class);
                if (task == null || taskList == null || taskList.isEmpty()) {
                    return;
                }
                for (int i = 0; i < taskList.size(); i++) {
                    if (task.getID().equals(taskList.get(i).getID())) {
                        taskList.remove(i);
                        break;
                    }
                }
                rcvViewCalendarAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rcvViewCalendarAdapter.setData(taskList, UID);
        rcvCalendar.setAdapter(rcvViewCalendarAdapter);
    }
}