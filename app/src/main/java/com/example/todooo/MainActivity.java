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
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.todo.R;
import com.example.todooo.Adapter.FragmentAdapter;
import com.example.todooo.Alarm.AlarmReceiver;
import com.example.todooo.CustomDialog.BottomSheetAddTaskDialog;
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
    public static int count = 0;

    Calendar today;
    List<Task> updateTask = new ArrayList<>();
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
//        LocaleHelper.setLocale(MainActivity.this, "en");
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        // [CHECK ĐĂNG NHẬP CHƯA?]
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
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
                switch (item.getItemId()){
                    case R.id.icCalendar:
                        viewPager2.setCurrentItem(0);
                        break;
                    case R.id.icTask:
                        viewPager2.setCurrentItem(1);
                        break;
                    case R.id.icSettings:
                        viewPager2.setCurrentItem(2) ;
                        break;
                }
                return true;
            }
        });

        fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetAddTaskDialog bottomSheet = new BottomSheetAddTaskDialog();
                bottomSheet.show(getSupportFragmentManager(),
                        "ModalBottomSheet");
            }
        });

        //LOAD DATA TASK FROM FIREBASE AND CREATE NOTIFICATIONS
//        createAlarm("Noti 1", "note...", 0, 10000);
//        createAlarm("Noti 2", "note2...", 0, 10000);
//        createAlarm("Noti 4", "note4...", 0, 10000);
//        createAlarm("Noti 3", "note3...", 0, 15000);
//        createAlarm("Noti 5", "note5...", 0, 10000);

        today = Calendar.getInstance();
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);
        mDatabase.child("task").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mDatabase.child("notificationsList").removeValue();
                for(DataSnapshot snap : snapshot.getChildren()){
                    Task task = snap.getValue(Task.class);
                    String dueDate = task.getDueDate();
                    if(dueDate != null){
                        int checkDueDate = compareDueDateWith2Day(dueDate);
                        Log.d(TAG, "CHECK DUEDATE: " + checkDueDate);
                        switch (checkDueDate){
                            case -1:
                                if(task.getRepeat() != null){
                                    switch (task.getRepeat().getType()){
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
                                Notification notification = new Notification();
                                notification.setID(task.getID());
                                notification.setDueDate(convertStr2TimeInMil1(task.getDueDate()));
                                notification.setTitle(task.getTitle());
                                notification.setDesc(task.getDescriptions());
                                if(task.getReminder() != null && task.getReminder().getTimeReminder() != null){
                                    notification.setReminder(convertStr2TimeInMil2(task.getReminder().getTimeReminder()));
                                    notification.setType(task.getReminder().getTypeNotify());
                                } else {
                                    notification.setReminder(0);
                                    notification.setType(TypeNotifications.NOTIFICATIONS);
                                }
                                mDatabase.child("notificationsList").child(task.getID()).setValue(notification);
                        }

                    }
                }
                if(updateTask != null){
                    for(Task t : updateTask){
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
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: vao");
                listNotificationTime1 = new ArrayList<>();
                listNotifications1 = new ArrayList<>();
                listNotifications2 = new ArrayList<>();
                listNotifications3 = new ArrayList<>();
                List<Notification> listNotifications4 = new ArrayList<>();
                List<Notification> listNotifications5 = new ArrayList<>();
                listNotificationTime2 = new ArrayList<>();
                long minTime1 = 164418840000000L;
                long minTime2 = 164418840000000L;
                long now = today.getTimeInMillis();
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.cancel(NOTIFICATION_ID);
                NotificationManagerCompat.from(MainActivity.this).cancelAll();
                for(DataSnapshot snap : snapshot.getChildren()){
                    Notification notification = snap.getValue(Notification.class);
                    assert notification != null;
                    if(notification.getReminder() != 0 && notification.getReminder() >= now){
                        if(notification.getReminder() < minTime2){
                            minTime2 = notification.getReminder();
                            listNotificationTime2.clear();
                            listNotifications2.clear();
                            listNotificationTime2.add(notification.getReminder());
                            listNotifications2.add(notification);
                        }
                        if(notification.getReminder() == minTime2){
                            listNotificationTime2.add(notification.getReminder());
                            listNotifications2.add(notification);
                        }
                    }
                    if(notification.getDueDate() != 0 && notification.getDueDate() >= now){
                        if(notification.getDueDate() < minTime1){
                            minTime1 = notification.getDueDate();
                            listNotificationTime1.clear();
                            listNotifications1.clear();
                            listNotificationTime1.add(notification.getDueDate());
                            listNotifications1.add(notification);
                        }
                        if(notification.getDueDate() == minTime2){
                            listNotificationTime1.add(notification.getDueDate());
                            listNotifications1.add(notification);
                        }
                    }
                    if(notification.getDueDate() < now && notification.getReminder() < now){
                        listNotifications3.add(notification);
                    }
                    if(notification.getDueDate() < now){
                        listNotifications4.add(notification);
                    }
                    if(notification.getReminder() < now){
                        listNotifications5.add(notification);
                    }
                }
                Log.d(TAG, "CHECK LIST NOTIFY 1" + listNotificationTime1.toString());
                Log.d(TAG, "CHECK LIST NOTIFY 2" + listNotificationTime2.toString());
                if(listNotificationTime1 != null && listNotificationTime1.size() > 0){
                    for(int i = 0; i<listNotificationTime1.size(); i++){
                        String title = listNotifications1.get(i).getTitle();
                        String desc = listNotifications1.get(i).getDesc();
                        int type = 0;
                        if(listNotifications1.get(i).getType() == TypeNotifications.ALARM){
                            type = 1;
                        }
                        String id = listNotifications1.get(i).getID();
                        long time = listNotificationTime1.get(i);
//                        createAlarm(title, desc, type, time, id);
                    }
                }
                if(listNotificationTime2 != null && listNotificationTime2.size() > 0){
                    for(int i = 0; i<listNotificationTime2.size(); i++){
                        String title = listNotifications2.get(i).getTitle();
                        String desc = listNotifications2.get(i).getDesc();
                        int type = 0;
                        if(listNotifications2.get(i).getType() == TypeNotifications.ALARM){
                            type = 1;
                        }
                        String id = listNotifications2.get(i).getID();
                        long time = listNotificationTime2.get(i);
//                        createAlarm(title, desc, type, time, id);
                    }
                }

                if(listNotifications3 != null && listNotifications3.size() > 0){
                    for(int i = 0; i<listNotificationTime2.size(); i++){
                        mDatabase.child("notificationsList").child(listNotifications3.get(i).getID()).removeValue();
                    }
                }
                if(listNotifications3 != null && listNotifications3.size() > 0){
                    for(int i = 0; i<listNotificationTime2.size(); i++){
                        mDatabase.child("notificationsList").child(listNotifications3.get(i).getID()).removeValue();
                    }
                }
                if(listNotifications3 != null && listNotifications3.size() > 0){
                    for(int i = 0; i<listNotificationTime2.size(); i++){
                        mDatabase.child("notificationsList").child(listNotifications3.get(i).getID()).removeValue();
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
//        long cal = Calendar.getInstance().getTimeInMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,count, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Alarm rings continuously until toggle button is turned off
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        count++;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Test Main Activity";
            String description = "Testing...";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("todo_app_notifications",name,importance);
            channel.setDescription(description);
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
            cal.set(Calendar.HOUR_OF_DAY, 9);
            today.set(Calendar.MILLISECOND, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MINUTE, 50);
            return  cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}