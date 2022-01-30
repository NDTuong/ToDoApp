package com.example.todooo.CustomDialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooo.Adapter.ItemSubTaskAdapter;
import com.example.todooo.Model.Reminder;
import com.example.todooo.Model.Repeat;
import com.example.todooo.Model.Task;
import com.example.todooo.Model.TypeRepeat;
import com.example.todooo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class BottomSheetAddTaskDialog extends BottomSheetDialogFragment {
    EditText etTaskName, etSubTask;
    ImageView ivSchedule, ivAddSubTask, ivAddTask, ivAdd, ivAddNote;
    RecyclerView rcvSubTask;
    LinearLayout llAddSubTask;
    ScrollView scrollViewSubTask;
    ItemSubTaskAdapter itemSubTaskAdapter;
    Spinner addTag;


    final static String TAG = "BottomSheet";

    // Lưu danh sách các sub-task
    List<String> subtask = new ArrayList<>();

    // checkSize: dùng để kiểm tra số lượng sub-task giúp giới hạn height của layout
    int checkSize = 0;

    // stateNoDate: lưu trạng thái có/không có ngày đến hạn (due date/deadline)
    boolean stateNoDate = false;

    // chưa biết
    int hour, minute, year, month, day;
    int numOfDayRepeat, numOfWeekRepeat, numOfYearRepeat;

    // repeatState: lưu type repeat hiện tại, mặc định sẽ set = 0
    // 0 (No repeat) | 1 (Daily) | 2 (Weekly) | 3 (Monthly) | 4 (Yearly)
    int repeatState = 0;

    // mảng lưu các ngày trong tuần được chọn để lặp lại nếu type repeat = weekly
    boolean[] dayOfWeekSelected = new boolean[7];

    // biến kiểu Repeat để lưu thông tin người dùng chọn, sau đó cập nhập vào newTask
    Repeat repeat = new Repeat();

    // biến kiểu Reminder để lưu thông tin người dùng chọn, sau đó cập nhập vào newTask
    Reminder reminder = new Reminder();

    // newTask: lưu thông tin của task được tạo
    Task newTask = new Task();
    Task tempTask = new Task();

    //
    boolean isAddNewTask = false;

    // Firebase
    private DatabaseReference mDatabase;
    String UID = "hsfdgsdfhfhh988jk";

    @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables"})
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_add_task, container, false);

        //Kết nối database
        mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);

        // KHỞI TẠO GIAO DIỆN
        ivAddSubTask = v.findViewById(R.id.ivAddSubTask);
        etSubTask = v.findViewById(R.id.etSubTask);
        llAddSubTask = v.findViewById(R.id.llAddSubTask);
        ivAdd = v.findViewById(R.id.ivAdd);
        scrollViewSubTask = v.findViewById(R.id.scrollViewSubTask);
        ivSchedule = v.findViewById(R.id.ivSchedule);
        ivAddTask = v.findViewById(R.id.ivAddTask);
        rcvSubTask = v.findViewById(R.id.listSubTask);
        ivAddNote = v.findViewById(R.id.ivAddNote);
        etTaskName = v.findViewById(R.id.etTaskName);

        // KHỞI TẠO 1 SỐ GIÁ TRỊ MẶC ĐỊNH CHO BIẾN newTask
        newTask.setPriority(3); // mặc định mức ưu tiên là 3
        newTask.setHasDueDate(false); // mặc định là không có ngày đến hạn
        repeat.setType(TypeRepeat.NO_REPEAT); //mặc định là không lặp lại
        newTask.setRepeat(repeat);
//        newTask.setReminder(reminder);
//        newTask.setDescriptions(null);
//        newTask.setSubTask(null);

        // SPINNER ADD TAG
        addTag = v.findViewById(R.id.addTagSpinner);
        List<String> listTag = new ArrayList<>();
        listTag.add(getResources().getString(R.string.no_tag));
        addTag.setAdapter(new ArrayAdapter<>(getContext(), R.layout.my_spinner, listTag));
        addTag.setSelection(0);
        mDatabase.child("tag").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listTag.clear();
                listTag.add(getResources().getString(R.string.no_tag));
                for (DataSnapshot snap :