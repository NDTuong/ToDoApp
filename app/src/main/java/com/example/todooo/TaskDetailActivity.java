package com.example.todooo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todo.R;
import com.example.todooo.Adapter.ItemSubTaskAdapter;
import com.example.todooo.CustomDialog.RepeatSettingDialog;
import com.example.todooo.Lib.CustomDateTimePicker;
import com.example.todooo.Model.Reminder;
import com.example.todooo.Model.Repeat;
import com.example.todooo.Model.Task;
import com.example.todooo.Model.TypeNotifications;
import com.example.todooo.Model.TypeRepeat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskDetailActivity extends AppCompatActivity {

    TextView tvTaskName;
    ImageView back;
    ScrollView scrollViewSubTask;
    Button btnSave, btnDelete;
    EditText noteInput;
    Spinner addTag;

    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    String UID;
    String taskID;

    Task taskDetail;
    ItemSubTaskAdapter itemSubTaskAdapter;
    RecyclerView rcvSubTask;
    Reminder reminder = new Reminder();
    Repeat repeat = new Repeat();
    int mPriority = 3;
    int day, month, year;
    boolean hasDueDate = false;

    // checkSize: dùng để kiểm tra số lượng sub-task giúp giới hạn height của layout
    int checkSize = 0;
    List<String> subtask;

    List<String> tagList;
    List<String> tagListKey;
    ArrayAdapter arrayAdapter;

    String ADD_NEW_TASK;
    String NO_TAG;
    String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        NO_TAG = getResources().getString(R.string.no_tag);
        ADD_NEW_TASK = getResources().getString(R.string.add_new_tag);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            UID = currentUser.getUid();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);

        //Get the bundle
        Bundle bundle = getIntent().getExtras();
        taskID = bundle.getString("TaskID").toString();

        back = findViewById(R.id.ivBack);
        tvTaskName = findViewById(R.id.tvTaskName);
        rcvSubTask = findViewById(R.id.listSubTask);
        scrollViewSubTask = findViewById(R.id.scrollViewSubTask);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        noteInput = findViewById(R.id.noteInput);

        // phần view set reminder duedate
        LinearLayout llDueDate = findViewById(R.id.llDueDate);
        TextView tvDueDate = findViewById(R.id.tvDueDate);
        TextView tvShowDueDate = findViewById(R.id.tvShowDueDate);
        ImageView ivDueDate = findViewById(R.id.ivDueDate);

        // phần view set reminder date + time
        LinearLayout llReminder = findViewById(R.id.llReminder);
        TextView tvReminder = findViewById(R.id.tvReminder);
        TextView tvShowReminder = findViewById(R.id.tvShowReminder);
        ImageView ivReminder = findViewById(R.id.ivReminder);

        // phần view set reminder type
        LinearLayout llReminderType = findViewById(R.id.llReminderType);
        TextView tvReminderType = findViewById(R.id.tvReminderType);
        TextView tvShowReminderType = findViewById(R.id.tvShowReminderType);
        ImageView ivReminderType = findViewById(R.id.ivReminderType);

        // phần view set repeat
        LinearLayout llRepeat = findViewById(R.id.llRepeat);
        TextView tvRepeat = findViewById(R.id.tvRepeat);
        TextView tvShowRepeat = findViewById(R.id.tvShowRepeat);
        ImageView ivRepeat = findViewById(R.id.ivRepeat);

        // phần view set priority
        LinearLayout llPriority = findViewById(R.id.llPriority);
        TextView tvPriority = findViewById(R.id.tvPriority);
        TextView tvShowPriority = findViewById(R.id.tvShowPriority);
        ImageView ivPriority = findViewById(R.id.ivPriority);
        ImageView ivShowPriority = findViewById(R.id.ivShowPriority);

        // SUB-TASK
        LinearLayout llAddSubTask = findViewById(R.id.llAddSubTask);
        itemSubTaskAdapter = new ItemSubTaskAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rcvSubTask.setLayoutManager(linearLayoutManager);
        llAddSubTask.setOnClickListener(v -> showAddSubTaskDialog(taskID));

        //BACK
        back.setOnClickListener(v -> finish());

        // CLICK TASK NAME
        tvTaskName.setOnClickListener(v -> showEditTaskNameDialog(taskID, taskDetail.getTitle() ));

        // CLICK REMINDER
        llReminder.setOnClickListener(v -> {
            if (!hasDueDate) { return; }
            showDateTimePicker(tvShowReminder, tvShowReminderType);
        });

        // CLICK REMINDER TYPE
        llReminderType.setOnClickListener(v -> {
            if (!hasDueDate) { return; }
            showPopUpMenu(this, v, R.menu.menu_type_notify, tvShowReminderType);
        });

        // CLICK REPEAT
        llRepeat.setOnClickListener(v -> {
            if (!hasDueDate) { return; }
            final RepeatSettingDialog repeatSettingDialog = new RepeatSettingDialog(this);
            repeatSettingDialog.setRepeat(taskDetail.getRepeat());
            repeatSettingDialog.setPositiveButton(v1 -> {
//                Toast.makeText(getContext(), "DONE" + repeatSettingDialog.getRepeat().toString(), Toast.LENGTH_SHORT).show();
                if(repeatSettingDialog.getRepeat().getType() == TypeRepeat.NO_REPEAT){
                    tvShowRepeat.setText(getResources().getString(R.string.no));
                } else {
                    tvShowRepeat.setText(getResources().getString(R.string.yes));
                }
                repeat = repeatSettingDialog.getRepeat();
//                Log.d(TAG, "check repeat set: " + repeatSettingDialog.getRepeat().toString());
                repeatSettingDialog.dismiss();
            });
            repeatSettingDialog.setNegativeButton(v12 -> {
                Toast.makeText(this, "CANCEL", Toast.LENGTH_SHORT).show();
                repeatSettingDialog.dismiss();
            });
            repeatSettingDialog.show();
        });

        // CLICK PRIORITY
        llPriority.setOnClickListener(v -> {
            if (!hasDueDate) { return; }
            showPopUpMenu(this, v, R.menu.menu_priority, tvShowPriority, ivShowPriority);
        });

        // CLICK DUE DATE
        Calendar myCalendar= Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = (view, _year, _month, _day) -> {
            hasDueDate = true;
            year = _year;
            month = _month;
            day = _day;
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH,month);
            myCalendar.set(Calendar.DAY_OF_MONTH,day);
            String dueDate = checkLessThan10(day) + "/" + checkLessThan10(month + 1) + "/" + year;
            tvShowDueDate.setText(dueDate);
            changeColorImageView(ivReminder, R.color.primary_color);
            changeColorImageView(ivReminderType, R.color.primary_color);
            changeColorImageView(ivRepeat, R.color.primary_color);
            changeColorImageView(ivPriority, R.color.primary_color);

            changeColorTextView(tvReminder, R.color.black);
            changeColorTextView(tvReminderType, R.color.black);
            changeColorTextView(tvRepeat, R.color.black);
            changeColorTextView(tvPriority, R.color.black);
            changeColorTextView(tvShowReminder, R.color.black);
            changeColorTextView(tvShowReminderType, R.color.black);
            changeColorTextView(tvShowRepeat, R.color.black);
            changeColorTextView(tvShowPriority, R.color.black);
        };
        llDueDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(v.getContext(),date, year, month, day);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no_date), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        tvShowDueDate.setText(R.string.no);
                        hasDueDate = false;
                        changeColorImageView(ivReminder, R.color.primary_color_tint40);
                        changeColorImageView(ivReminderType, R.color.primary_color_tint40);
                        changeColorImageView(ivRepeat, R.color.primary_color_tint40);
                        changeColorImageView(ivPriority, R.color.primary_color_tint40);

                        changeColorTextView(tvReminder, R.color.grey700);
                        changeColorTextView(tvReminderType, R.color.grey700);
                        changeColorTextView(tvRepeat, R.color.grey700);
                        changeColorTextView(tvPriority, R.color.grey700);
                        changeColorTextView(tvShowReminder, R.color.grey700);
                        changeColorTextView(tvShowReminderType, R.color.grey700);
                        changeColorTextView(tvShowRepeat, R.color.grey700);
                        changeColorTextView(tvShowPriority, R.color.grey700);
                    }
                }
            });
            dialog.show();
        });

        // SPINNER ADD TAG
        addTag = findViewById(R.id.addTagSpinner);
        tagList = new ArrayList<>();
        tagList.add(getResources().getString(R.string.no_tag));
        getData();
        arrayAdapter = new ArrayAdapter<>(this, R.layout.my_spinner, tagList);
        addTag.setAdapter(arrayAdapter);
//        addTag.setSelection(0);
        addTag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(addTag.getSelectedItem().toString().equals(ADD_NEW_TASK)){
                            openAddNewTag(mDatabase, addTag);
                            return;
                        }
//                        newTask.setTag(addTag.getSelectedItem().toString());
//                        Log.d(TAG, "Selected TAG: " + addTag.getSelectedItem().toString());
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });


        btnSave.setOnClickListener(v -> {
            taskDetail.setSubTask(subtask);
            taskDetail.setReminder(reminder);
            taskDetail.setRepeat(repeat);
            taskDetail.setPriority(mPriority);
            if(hasDueDate) {
                String dueDate = checkLessThan10(day) + "/" + checkLessThan10(month + 1) + "/" + year;
                taskDetail.setDueDate(dueDate);
            } else {
                taskDetail.setDueDate(null);
            }
            taskDetail.setHasDueDate(hasDueDate);

            String note = noteInput.getText().toString().trim();
            taskDetail.setDescriptions(note);
            String taskName = tvTaskName.getText().toString();
            taskDetail.setTitle(taskName);
            taskDetail.setTag(addTag.getSelectedItem().toString());
            mDatabase.child("task").child(taskID).setValue(taskDetail);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            mDatabase.child("task").child(taskID).removeValue();
            finish();
        });

        mDatabase.child("task").child(taskID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                taskDetail = snapshot.getValue(Task.class);
                if (taskDetail != null) {
                    String taskName = taskDetail.getTitle();
                    if (taskDetail.getSubTask() != null) {
                        subtask = taskDetail.getSubTask();
                    } else {
                        subtask = new ArrayList<>();
                    }

                    if (subtask != null) {
                        if (subtask.size() > 5 && checkSize == 0) {
                            ViewGroup.LayoutParams layoutParams = scrollViewSubTask.getLayoutParams();
                            layoutParams.height = 540;
                            scrollViewSubTask.setLayoutParams(layoutParams);
                            checkSize += 1;
                        }
                        itemSubTaskAdapter.setData(subtask);
                        rcvSubTask.setAdapter(itemSubTaskAdapter);
                    }

                    tvTaskName.setText(taskName);

                    if(taskDetail.getReminder() != null){
                        reminder = taskDetail.getReminder();
                    }
                    if(taskDetail.getRepeat() != null){
                        repeat = taskDetail.getRepeat();
                    }


                    hasDueDate = taskDetail.isHasDueDate();
                    if (!hasDueDate) {
                        changeColorImageView(ivReminder, R.color.primary_color_tint40);
                        changeColorImageView(ivReminderType, R.color.primary_color_tint40);
                        changeColorImageView(ivRepeat, R.color.primary_color_tint40);
                        changeColorImageView(ivPriority, R.color.primary_color_tint40);

                        changeColorTextView(tvReminder, R.color.grey700);
                        changeColorTextView(tvReminderType, R.color.grey700);
                        changeColorTextView(tvRepeat, R.color.grey700);
                        changeColorTextView(tvPriority, R.color.grey700);
                        changeColorTextView(tvShowReminder, R.color.grey700);
                        changeColorTextView(tvShowReminderType, R.color.grey700);
                        changeColorTextView(tvShowRepeat, R.color.grey700);
                        changeColorTextView(tvShowPriority, R.color.grey700);
                        year = myCalendar.get(Calendar.YEAR);
                        month = myCalendar.get(Calendar.MONTH);
                        day = myCalendar.get(Calendar.DAY_OF_MONTH);
                    }
                    if (hasDueDate) {
                        tvShowDueDate.setText(taskDetail.getDueDate());
                        String[] sDueDate = taskDetail.getDueDate().split("/");
                        day = Integer.parseInt(sDueDate[0]);
                        month = Integer.parseInt(sDueDate[1]) - 1;
                        year = Integer.parseInt(sDueDate[2]);
                        if (taskDetail.getReminder() != null) {
                            tvShowReminder.setText(taskDetail.getReminder().getTimeReminder());
                            if (taskDetail.getReminder().getTypeNotify() != null) {
                                switch (taskDetail.getReminder().getTypeNotify()) {
                                    case NOTIFICATIONS:
                                        tvShowReminderType.setText(getResources().getString(R.string.notification));
                                        break;
                                    default:
                                        tvShowReminderType.setText(getResources().getString(R.string.alarm));
                                        break;
                                }
                            }
                        }
                    }
                    if (taskDetail.getRepeat() != null) {
                        if (taskDetail.getRepeat().getType() != TypeRepeat.NO_REPEAT) {
                            tvShowRepeat.setText(getResources().getString(R.string.yes));
                        } else {
                            tvShowRepeat.setText(getResources().getString(R.string.no));
                        }
                    }

                    switch (taskDetail.getPriority()) {
                        case 1:
                            tvShowPriority.setText(getResources().getString(R.string.high));
                            ivShowPriority.setBackground(getResources().getDrawable(R.drawable.ic_circle_1));
                            break;
                        case 2:
                            tvShowPriority.setText(getResources().getString(R.string.medium));
                            ivShowPriority.setBackground(getResources().getDrawable(R.drawable.ic_circle_2));
                            break;
                        default:
                            tvShowPriority.setText(getResources().getString(R.string.normal));
                            ivShowPriority.setBackground(getResources().getDrawable(R.drawable.ic_circle_3));
                            break;
                    }
                    if (taskDetail.getDescriptions() != null) {
                        noteInput.setText(taskDetail.getDescriptions());
                    }
                    if(taskDetail.getTag() != null){
                        tag = taskDetail.getTag();
                    }
                    if(taskDetail.getTag() == null){
                        tag = NO_TAG;
                    }
                    int spinnerPosition = arrayAdapter.getPosition(tag);
                    addTag.setSelection(spinnerPosition);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // SHOW POPUP MENU 2
    private void showPopUpMenu(Context context, View view, int menu, TextView tv, ImageView iv) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            tv.setText(item.getTitle());
            String priority = tv.getText().toString().trim();
            String normal = context.getResources().getString(R.string.normal);
            String medium = context.getResources().getString(R.string.medium);
            String high = context.getResources().getString(R.string.high);
            if (priority.equals(normal)) {
                changeBackgroundDrawable(iv,R.drawable.ic_circle_3);
                mPriority = 3;
            }
            if (priority.equals(medium)) {
                changeBackgroundDrawable(iv,R.drawable.ic_circle_2);
                mPriority = 2;
            }
            if (priority.equals(high)) {
                changeBackgroundDrawable(iv,R.drawable.ic_circle_1);
                mPriority = 1;
            }
            return false;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();
    }

    private void showEditTaskNameDialog(String taskID, String _taskName) {
        final Dialog dialog = createDialog(R.layout.dialog_input_tag);

        EditText taskName = dialog.findViewById(R.id.tagInput);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);

        taskName.setText(_taskName);

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String newTaskName = taskName.getText().toString().trim();
            if (!TextUtils.isEmpty(newTaskName)) {
                tvTaskName.setText(newTaskName);
                dialog.dismiss();
            } else {
                Toast.makeText(this, R.string.add_new_task_name_error, Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();
    }

    private void showAddSubTaskDialog(String taskID) {
        final Dialog dialog = createDialog(R.layout.dialog_input_tag);

        EditText taskName = dialog.findViewById(R.id.tagInput);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String newTaskName = taskName.getText().toString().trim();
            if (!TextUtils.isEmpty(newTaskName)) {
                subtask.add(newTaskName);
                if (subtask.size() > 5 && checkSize == 0) {
                    ViewGroup.LayoutParams layoutParams = scrollViewSubTask.getLayoutParams();
                    layoutParams.height = 540;
                    scrollViewSubTask.setLayoutParams(layoutParams);
                    checkSize += 1;
                }
                // focus vào item ở cuối list và thông báo dataset change để hiển thị
                rcvSubTask.smoothScrollToPosition(subtask.size() - 1);
                itemSubTaskAdapter.notifyDataSetChanged();
                itemSubTaskAdapter.setData(subtask);
                rcvSubTask.setAdapter(itemSubTaskAdapter);
                dialog.dismiss();
            } else {
                Toast.makeText(this, R.string.add_new_tag_error_empty, Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();
    }

    private Dialog createDialog(int menu) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(menu);

        Window window = dialog.getWindow();
        if (window == null) {
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttr = window.getAttributes();
        windowAttr.gravity = Gravity.CENTER;
        window.setAttributes(windowAttr);
        dialog.setCancelable(true);
        return dialog;
    }

    // HÀM SET HEIGHT CHO SCROLLVIEW SUB-TASK
    public void setWidthScrollView() {
        if (checkSize == 1) {
            ViewGroup.LayoutParams layoutParams = scrollViewSubTask.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            checkSize = 0;
            scrollViewSubTask.setLayoutParams(layoutParams);
        }
    }

    public void changeBackgroundDrawable(ImageView iv, int drawable) {
        iv.setBackground((getResources().getDrawable(drawable)));
    }

    // HÀM THAY ĐỔI MÀU TEXT
    public void changeColorTextView(TextView tv, int color) {
        tv.setTextColor(getResources().getColor(color));
    }

    // HÀM THAY ĐỔI MÀU CÁC IMAGE VIEW
    public void changeColorImageView(ImageView iv, int color) {
        Drawable drawable = iv.getBackground();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, getResources().getColor(color));
        iv.setBackground(drawable);
    }

    // HÀM SHOW DATETIMEPICKER
    private void showDateTimePicker(TextView tvShowReminderSet, TextView tvShowReminderType) {
        CustomDateTimePicker customDateTimePicker = new CustomDateTimePicker(this, new CustomDateTimePicker.ICustomDateTimeListener() {
            @Override
            public void onSet(@NotNull Dialog dialog, @NotNull Calendar calendar,
                              @NotNull Date date, int year, @NotNull String monthFullName,
                              @NotNull String monthShortName, int monthNumber, int day,
                              @NotNull String weekDayFullName, @NotNull String weekDayShortName,
                              int hour24, int hour12, int min, int sec, @NotNull String AM_PM, boolean isClear) {
                String dDay, mMonth, hHour, mMinute;
                dDay = checkLessThan10(day);
                mMonth = checkLessThan10(monthNumber + 1);
                hHour = checkLessThan10(hour24);
                mMinute = checkLessThan10(min);
                String dateAndTime = dDay + "/" + mMonth + "/" + year + " - " + hHour + ":" + mMinute;
                if (isClear) {
                    setTextUseResource(tvShowReminderSet, R.string.default_no_dialog_schedule);
                    reminder.setTimeReminder(null);
                    tvShowReminderType.setText(getString(R.string.notification));
                    reminder.setTypeNotify(TypeNotifications.NOTIFICATIONS);
                } else {
                    tvShowReminderSet.setText(dateAndTime);
                    reminder.setTimeReminder(dateAndTime);
                }
//                Log.d(TAG, "Reminder time set: " + reminder.getTimeReminder());
            }

            @Override
            public void onCancel() {
            }
        });
        customDateTimePicker.set24HourFormat(true);
        customDateTimePicker.showDialog();
    }
    // HÀM KIỂM TRA GIÁ TRỊ INT < 10 HAY KHÔNG
    // NẾU NHỎ HƠN TRẢ VỀ STRING 0 + INT
    public String checkLessThan10(int i) {
        String s;
        if (i < 10) { s = "0" + i; }
        else { s = "" + i; }
        return s;
    }
    // HÀM SET TEXT VIEW
    public void setTextUseResource(TextView tv, int stringID){
        tv.setText(getResources().getString(stringID));
    }

    // SHOW POPUPMENU
    private void showPopUpMenu(Context context, View view, int menu, TextView tv) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            String typeNotify = item.getTitle().toString();
            if (typeNotify.equals(context.getResources().getString(R.string.notification))) {
                reminder.setTypeNotify(TypeNotifications.NOTIFICATIONS);
            }
            if (typeNotify.equals(context.getResources().getString(R.string.alarm))) {
                reminder.setTypeNotify(TypeNotifications.ALARM);
            }
            tv.setText(typeNotify);
            return false;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();

    }

    private void getData() {
        tagList = new ArrayList<>();
        tagListKey = new ArrayList<>();
        tagList.add(NO_TAG);
        tagListKey.add("0");
        mDatabase.child("tag").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String tagName = snapshot.getValue(String.class);
                if(tagList != null && tagList.contains(ADD_NEW_TASK)){
                    tagList.remove(ADD_NEW_TASK);
                }
                if(tagName != null){
                    tagList.add(tagName);
                    tagListKey.add(snapshot.getKey());
//                    newTask.setTag(tagName);
                }
                tagList.add(ADD_NEW_TASK);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String tagName = snapshot.getValue(String.class);
                String key = snapshot.getKey();
                int i = tagListKey.indexOf(key);
                if(tagName != null){
                    tagList.set(i, tagName);
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String tagName = snapshot.getValue(String.class);
                String key = snapshot.getKey();
                if(tagName == null || tagList == null || tagName.isEmpty()){
                    return;
                }
                tagList.remove(tagName);
                tagListKey.remove(key);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(tagList.size() == 1){
            tagList.add(ADD_NEW_TASK);
        }
    }

    private void openAddNewTag(DatabaseReference mDatabase, Spinner spinner) {
        final Dialog dialog = createDialog(R.layout.dialog_input_tag);

        EditText tagInput = dialog.findViewById(R.id.tagInput);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);

        btnCancel.setOnClickListener(v -> {
            spinner.setSelection(0);
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String newTagName = tagInput.getText().toString().trim();
            if(!TextUtils.isEmpty(newTagName)) {
                if(newTagName.length() > 50){
                    Toast.makeText(this,R.string.add_new_tag_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(tagList.contains(newTagName)){
                    spinner.setSelection(spinner.getAdapter().getCount() - 2);
                    Toast.makeText(this,R.string.add_new_tag_error_2,Toast.LENGTH_SHORT).show();
                } else {
                    String key = mDatabase.push().getKey();
                    mDatabase.child("tag").child(key).setValue(newTagName);
                    spinner.setSelection(spinner.getAdapter().getCount() - 1);
                }
                dialog.dismiss();

            } else {
                Toast.makeText(this,R.string.add_new_tag_error_empty,Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();
    }
}