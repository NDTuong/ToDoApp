package com.example.todooo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todo.R;
import com.example.todooo.Adapter.ItemSubTaskAdapter;
import com.example.todooo.Model.Task;
import com.example.todooo.Model.TypeRepeat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    // checkSize: dùng để kiểm tra số lượng sub-task giúp giới hạn height của layout
    int checkSize = 0;
    List<String> subtask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            UID = currentUser.getUid();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID + "/task");

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

        back.setOnClickListener(v -> finish());
        tvTaskName.setOnClickListener(v -> showEditTaskNameDialog(taskID, taskDetail.getTitle() ));

        itemSubTaskAdapter = new ItemSubTaskAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rcvSubTask.setLayoutManager(linearLayoutManager);

        btnSave.setOnClickListener(v -> {
            taskDetail.setSubTask(subtask);

            mDatabase.child(taskID).setValue(taskDetail);
        });

        mDatabase.child(taskID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskDetail = snapshot.getValue(Task.class);

                String taskName = taskDetail.getTitle();
                subtask = taskDetail.getSubTask();

                if(subtask != null){
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

                if(!taskDetail.isHasDueDate()){
                    changeColorImageView(ivReminder, R.color.primary_color_tint40);
                    changeColorImageView(ivReminderType, R.color.primary_color_tint40);
                    changeColorImageView(ivRepeat, R.color.primary_color_tint40);
                    changeColorImageView(ivPriority, R.color.primary_color_tint40);

                    changeColorTextView(tvReminder, R.color.grey700);
                    changeColorTextView(tvReminderType, R.color.grey700);
                    changeColorTextView(tvRepeat, R.color.grey700);
                    changeColorTextView(tvPriority, R.color.grey700);
                }
                if(taskDetail.isHasDueDate()){
                    tvShowDueDate.setText(taskDetail.getDueDate());
                    if(taskDetail.getReminder() != null){
                        tvShowReminder.setText(taskDetail.getReminder().getTimeReminder());
                        if(taskDetail.getReminder().getTypeNotify() != null) {
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
                if(taskDetail.getRepeat() != null){
                    if(taskDetail.getRepeat().getType() != TypeRepeat.NO_REPEAT) {
                        tvShowRepeat.setText(getResources().getString(R.string.yes));
                    }
                    else {
                        tvShowRepeat.setText(getResources().getString(R.string.no));
                    }
                }

                switch (taskDetail.getPriority()){
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
                if(taskDetail.getDescriptions() != null){
                    noteInput.setText(taskDetail.getDescriptions());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                mDatabase.child(taskID).child("title").setValue(newTaskName);
                dialog.dismiss();
            } else {
                Toast.makeText(this, R.string.update_fail, Toast.LENGTH_SHORT).show();
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
}