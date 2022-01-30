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

    // repeatState: lưu type repeat