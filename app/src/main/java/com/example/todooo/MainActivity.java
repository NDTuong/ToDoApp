package com.example.todooo;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.example.todo.R;
import com.example.todooo.Adapter.FragmentAdapter;
import com.example.todooo.Alarm.AlarmReceiver;
import com.example.todooo.CustomDialog.BottomSheetAddTaskDialog;
import com.example.todooo.Fragment.TaskFragment;
import com.example.todooo.Model.Notification;
import com.example.todooo.Model.Task;
import com.example.todooo.Model.TypeNotifications;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ViewPager2 viewPager2;
    FragmentAdapter fragmentAdapter;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    String UID;
    FloatingActionButton fabAddTask;

    AlarmManager alarmManager;

    private final static String TAG = "MainActivity";
    public static int count = 1;

    Calendar today;
    List<Task> updateTask;
    List<Long> listNotificationTime1;
    List<Long> listNotificationTime2;
    List<Notification> listNotifications1;
    List<Notification> listNotifications2;
    List<Notification> listNotifications3;

    @SuppressLint("SimpleDateFormat")
    final static SimpleDateFormat DUE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    @SuppressLint("SimpleDateFormat")
    final static SimpleDateFormat REMINDER_FORMAT = new SimpleDateFormat("dd/MM/yyyy - HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        // [CHECK ĐĂNG NHẬP CHƯA?]
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            UID = currentUser.getUid();
        }
        // [KẾT THÚC CHECK ĐĂNG NHẬP CHƯA?]
        mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);


        viewPager2 = findViewById(R.id.viewPager2);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        FragmentManager fm = getSupportFragmentManager();
        fragmentAdapter = new FragmentAdapter(fm, getLifecycle());
        viewPager2.setAdapter(fragmentAdapter);

        bottomNavigationView.setBackground(null);
        bottomNavigationView.getMenu().getItem(3).setEnabled(false);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.icCalendar:
                        viewPager2.setCurrentItem(0);
                        break;
                    case R.id.icTask:
                        viewPager2.setCurrentItem(1);
                        break;
                    case R.id.icSettings:
                        viewPager2.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });

        fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> {
            BottomSheetAddTaskDialog bottomSheet = new BottomSheetAddTaskDialog();
            bottomSheet.show(getSupportFragmentManager(),
                    "ModalBottomSheet");
        });

        //LOAD DATA TASK FROM FIREBASE AND CREATE NOTIFICATIONS

        today = Calendar.getInstance();
//        today.set(Calendar.MILLISECOND, 0);
//        today.set(Calendar.SECOND, 0);
//        today.set(Calendar.MINUTE, 0);
//        today.set(Calendar.HOUR_OF_DAY, 0);
        mDatabase.child("task").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateTask = new ArrayList<>();
                count = 1;
//                mDatabase.child("notificationsList").removeValue();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Task task = snap.getValue(Task.class);
                    String dueDate = task.getDueDate();
                    if (dueDate != null) {
                        int checkDueDate = compareDueDateWith2Day(dueDate);
                        Log.d(TAG, "CHECK DUEDATE: " + checkDueDate);
                        switch (checkDueDate) {
                            case -1:
                                if (task.getRepeat() != null) {
                                    switch (task.getRepeat().getType()) {
                                        case DAILY:
                                            String newDueDate = updateDueDateTask(task.getRepeat().getRepeatEvery(), dueDate, Calendar.DATE);
                                            task.setDueDate(newDueDate);
//                                            Log.d(TAG, "DAILY UPDATE: " + task.getDueDate());
                                            updateTask.add(task);
                                            break;
                                        case MONTHLY:
                                            String newDueDate2 = updateDueDateTask(task.getRepeat().getRepeatEvery(), dueDate, Calendar.MONTH);
                                            task.setDueDate(newDueDate2);
                                            updateTask.add(task);
                                            break;
                                        case YEARLY:
                                            String newDueDate3 = updateDueDateTask(task.getRepeat().getRepeatEvery(), dueDate, Calendar.YEAR);
                                            task.setDueDate(newDueDate3);
                                            updateTask.add(task);
                                            break;
                                        case WEEKLY:
                                            String newDueDate4 = updateDueDateTaskWeekly(task.getRepeat().getRepeatEvery(), dueDate, Calendar.DATE, task.getRepeat().getRepeatOnWeek());
                                            task.setDueDate(newDueDate4);
                                            Log.d(TAG, "DAILY UPDATE: " + task.getDueDate());
                                            updateTask.add(task);
                                            break;
                                    }
                                }
                                break;
                            case 1:
                            default:
                        }
                        Notification notification = new Notification();
                        notification.setID(task.getID());
                        notification.setDueDate(convertStr2TimeInMil1(task.getDueDate()));
                        notification.setTitle(task.getTitle());
                        notification.setDesc(task.getDescriptions());
                        if (task.getReminder() != null && task.getReminder().getTimeReminder() != null) {
                            notification.setReminder(convertStr2TimeInMil2(task.getReminder().getTimeReminder()));
                            notification.setType(task.getReminder().getTypeNotify());
                        } else {
                            notification.setReminder(0);
                            notification.setType(TypeNotifications.NOTIFICATIONS);
                        }
                        mDatabase.child("notificationsList").child(task.getID()).setValue(notification);

                    }
                }
                if (updateTask != null) {
                    for (Task t : updateTask) {
                        String taskID = t.getID();
                        mDatabase.child("task").child(taskID).setValue(t);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase.child("notificationsList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                count = 1;
                List<Notification> _listNotificationsRe = new ArrayList<>();
                List<Notification> _listNotificationsDu = new ArrayList<>();

                NotificationManagerCompat.from(MainActivity.this).cancelAll();

                long now = today.getTimeInMillis();
                for(DataSnapshot snap : snapshot.getChildren()){
                    Notification notification = snap.getValue(Notification.class);
                    if(notification!=null){
                        if(notification.getDueDate() != 0 && notification.getDueDate() >= now){
                            _listNotificationsDu.add(notification);
                        }
                        if(notification.getReminder() != 0 && notification.getReminder() >= now){
                            _listNotificationsRe.add(notification);
                        }
                    }
                }

                Collections.sort(_listNotificationsDu, (o1, o2) -> {
                    // ## Ascending order
                    return (o1.getDueDate() < o2.getDueDate()) ? -1 : ((o1.getDueDate() == o2.getDueDate()) ? 0 :1 );
                });

                Collections.sort(_listNotificationsRe, new Comparator<Notification>(){
                    public int compare(Notification o1, Notification o2) {
                        // ## Ascending order
                        return (o1.getReminder() < o2.getReminder()) ? -1 : ((o1.getReminder() == o2.getReminder()) ? 0 :1 );
                    }
                });
                Log.d(TAG, "DUELIST: " + _listNotificationsDu);
                if(_listNotificationsDu != null && _listNotificationsDu.size() > 0){
                    for(int i = 0; i<_listNotificationsDu.size(); i++){
                        if(_listNotificationsDu.get(i).getDueDate() <= _listNotificationsDu.get(0).getDueDate()) {
                            String title = _listNotificationsDu.get(i).getTitle();
                            String desc = _listNotificationsDu.get(i).getDesc();
                            int type = 0;
                            String id = _listNotificationsDu.get(i).getID();
                            long time = _listNotificationsDu.get(i).getDueDate();
                            createAlarm(title, desc, type, time, id);
                            Log.d(TAG, "DUE: " + title);
                        }
                    }
                }

                if(_listNotificationsRe != null && _listNotificationsRe.size() > 0){
                    for(int i = 0; i<_listNotificationsRe.size(); i++) {
                        if (_listNotificationsRe.get(i).getReminder() <= _listNotificationsRe.get(0).getReminder()) {
                            String title = _listNotificationsRe.get(i).getTitle();
                            String desc = _listNotificationsRe.get(i).getDesc();
                            int type = 0;
                            if (_listNotificationsRe.get(i).getType() == TypeNotifications.ALARM) {
                                type = 1;
                            }
                            String id = _listNotificationsRe.get(i).getID();
                            long time = _listNotificationsRe.get(i).getReminder();
                            createAlarm(title, desc, type, time, id);
                            Log.d(TAG, "RE: " + title);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private long convertStr2TimeInMil2(String timeReminder) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(Objects.requireNonNull(REMINDER_FORMAT.parse(timeReminder)));
            return  cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private void createAlarm(String title, String desc, int type, long time,String id){
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("TITLE", title);
        alarmIntent.putExtra("DESC", desc);
        alarmIntent.putExtra("TYPE", type);
        alarmIntent.putExtra("ID", id);
        alarmIntent.putExtra("CODE", count);
        Log.d(TAG, "COUNT: " + count);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,count, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        count++;

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Todo";
            String description = "Notifications of Todo app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("todo_app_notifications", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int compareDueDateWith2Day(String strDate){
        String strToday = DUE_DATE_FORMAT.format(today.getTime());
        String[] arrToday = strToday.split("/");
        String[] arrDueDate = strDate.split("/");
        int year1 = Integer.parseInt(arrToday[2]);
        int month1 = Integer.parseInt(arrToday[1]);
        int day1 = Integer.parseInt(arrToday[0]);
        int year2 = Integer.parseInt(arrDueDate[2]);
        int month2 = Integer.parseInt(arrDueDate[1]);
        int day2 = Integer.parseInt(arrDueDate[0]);
        Log.d(TAG, "DAILY UPDATE: " + day2 + "-" + month2 + "-" + year2 + "||" + day1 + "-" + month1 + "-" + year1 );
        int sub = (year2 - year1)*12 + month2 - month1;
        if(sub < 0){
            return -1; //nho hon today
        }
        if(sub > 0){ return 1;} // lon hon today
        int subDay = day2 - day1;
        if(subDay > 0) { return  1;}
        if(subDay < 0){return -1;}
        return 0;
    }

    private String updateDueDateTask(int i, String day, int calendarAdd){
        Calendar c = Calendar.getInstance();

        try {
            c.setTime(Objects.requireNonNull(DUE_DATE_FORMAT.parse(day)));
            while (c.getTimeInMillis() < (today.getTimeInMillis())) {
                c.add(calendarAdd, i);
            }
            return DUE_DATE_FORMAT.format(c.getTime());

        } catch (Exception e){
            return null;
        }
    }

    private String updateDueDateTaskWeekly(int i, String day, int calendarAdd, List<Integer> onWeekDay){
        Calendar c = Calendar.getInstance();

        try {
            c.setTime(Objects.requireNonNull(DUE_DATE_FORMAT.parse(day)));
            int toAdd = i - 1;
            for(int ii = 0;; ii+=toAdd) {
                c.add(Calendar.WEEK_OF_YEAR, ii);
                int dow = 0;
                while (dow != 1) {
                    c.add(calendarAdd, 1);
                    dow = c.get(Calendar.DAY_OF_WEEK);
                    if (onWeekDay.contains(dow - 1) && (c.getTimeInMillis() >= today.getTimeInMillis())) {
                        return DUE_DATE_FORMAT.format(c.getTime());
                    }
                }
            }
        } catch (Exception e){
            return null;
        }
    }

    private long convertStr2TimeInMil1(String date) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(Objects.requireNonNull(DUE_DATE_FORMAT.parse(date)));
            cal.set(Calendar.HOUR_OF_DAY, 16);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 24);
            return  cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}