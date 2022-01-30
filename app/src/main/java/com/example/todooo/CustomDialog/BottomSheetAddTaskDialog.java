package com.example.todooo.CustomDialog;

import android.app.Dialog;
import android.content.Intent;
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
import com.example.todooo.LoginActivity;
import com.example.todooo.Model.Reminder;
import com.example.todooo.Model.Repeat;
import com.example.todooo.Model.Task;
import com.example.todooo.Model.TypeRepeat;
import com.example.todo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


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

    List<String> tagList;
    List<String> tagListKey;
    ArrayAdapter arrayAdapter;

    String ADD_NEW_TASK;
    String NO_TAG;
//    AdapterView.OnItemSelectedListener onItemSelectedListener;

    //
    boolean isAddNewTask;
    String newTag;

    // Firebase
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    String UID;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_add_task, container, false);

        // Get current user ID
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            UID = currentUser.getUid();
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
        //Kết nối database
        mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);
        NO_TAG = BottomSheetAddTaskDialog.this.getResources().getString(R.string.no_tag);
        ADD_NEW_TASK = BottomSheetAddTaskDialog.this.getResources().getString(R.string.add_new_tag);

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

        // KHỞI TẠO GIAO DIỆN KHI VỪA MỞ LÊN
        changeColorImageView(ivAddSubTask, R.color.primary_color_tint40);
        changeColorImageView(ivSchedule, R.color.primary_color_tint40);
        changeColorImageView(ivAddNote, R.color.primary_color_tint40);


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
        getData();
        arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.my_spinner, tagList);
        addTag.setAdapter(arrayAdapter);
//        addTag.setAdapter(new ArrayAdapter<>(getContext(), R.layout.my_spinner, listTag));
        addTag.setSelection(0);




//        mDatabase.child("tag").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                listTag.clear();
//                listTag.add(getActivity().getString(R.string.no_tag));
//                for (DataSnapshot snap : snapshot.getChildren()) {
//                    listTag.add(snap.getValue(String.class));
//                }
//                listTag.add(getActivity().getString(R.string.add_new_tag));
//                addTag.setAdapter(new ArrayAdapter<>(getContext(), R.layout.my_spinner, listTag));
//                if(isAddNewTask == false){
//                    addTag.setSelection(0);
//                } else {
//                    addTag.setSelection(listTag.size() - 2);
//                    isAddNewTask = false;
//                }
        addTag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(addTag.getSelectedItem().toString().equals(ADD_NEW_TASK)){
                            openAddNewTag(mDatabase, addTag);
                            return;
//                            Log.d(TAG, "Selected TAG: Vào" + isAddNewTask);
//                            if(isAddNewTask){
//                                isAddNewTask = false;
//                                addTag.setSelection(tagList.size()-2);
//                                newTask.setTag(tagList.get(tagList.size()-2));
//                                return;
//                            }
                        }
//                        if(isAddNewTask){
//                            newTask.setTag(newTag);
//                            isAddNewTask = false;
//                        } else {
//                            newTask.setTag(tagList.get(position));
//                        }
                        newTask.setTag(addTag.getSelectedItem().toString());
                        Log.d(TAG, "Selected TAG: " + addTag.getSelectedItem().toString());
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
//                addTag.setOnItemSelectedListener(onItemSelectedListener);
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        });

        // ADD NOTE
        ivAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddNoteDialog(ivAddNote);
            }
        });

        // RECYCLE VIEW ĐỂ THÊM SUB - TASK
        itemSubTaskAdapter = new ItemSubTaskAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rcvSubTask.setLayoutManager(linearLayoutManager);

        // CLICK ADD SUB TASK ICON SẼ SHOW/HIDE RECYCLE VIEW VÀ EDIT TEXT ĐỂ ADD SUB TASK
        ivAddSubTask.setOnClickListener(v12 -> {
            if (subtask.size() == 0) {
                changeColorImageView(ivAddSubTask, R.color.primary_color_tint40);
            } else {
                changeColorImageView(ivAddSubTask, R.color.primary_color);
            }
            if (llAddSubTask.getVisibility() != View.VISIBLE) {
                llAddSubTask.setVisibility(View.VISIBLE);
            } else {
                llAddSubTask.setVisibility(View.GONE);
            }
        });

        // XỬ LÝ SỰ KIỆN CLICK NÚT ADD SUB-TASK
        ivAdd.setOnClickListener(v13 -> {
            // kiểm tra nếu edit text rỗng thì thông báo rỗng
            if (etSubTask.getText().toString().trim().equals("")) {
                Toast.makeText(getContext(), "Sub-task field is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            // nếu không thì thêm sub-task vào list "subtask" và hiển thị
            subtask.add(etSubTask.getText().toString().trim());
            changeColorImageView(ivAddSubTask, R.color.primary_color);
            itemSubTaskAdapter.setData(subtask);
            rcvSubTask.setAdapter(itemSubTaskAdapter);
            // sau khi hiển thị reset edit text
            etSubTask.setText("");
            // kiểm tra số sub-task, nếu lớn hơn 5 thì set height cố Minh là 540
            // checksize: dùng để kiểm tra đã set height cố định hay chưa. (1: rồi | 2: chưa)
            if (subtask.size() > 5 && checkSize == 0) {
                ViewGroup.LayoutParams layoutParams = scrollViewSubTask.getLayoutParams();
                layoutParams.height = 540;
                scrollViewSubTask.setLayoutParams(layoutParams);
                checkSize += 1;
            }
            // focus vào item ở cuối list và thông báo dataset change để hiển thị
            rcvSubTask.smoothScrollToPosition(subtask.size() - 1);
            itemSubTaskAdapter.notifyDataSetChanged();
        });

        // XỬ LÝ SỰ KIỆN KHI CLICK VÀO ICON LÊN LỊCH CHO TASK
        ivSchedule.setOnClickListener(v1 -> {
            tempTask = newTask;
            final ScheduleDialog scheduleDialog = new ScheduleDialog(getContext());
            scheduleDialog.setTask(newTask);
            scheduleDialog.setType(0);
            scheduleDialog.setPositiveButton(v22 -> {
                Log.d(TAG, "TASK INFO: \n" + scheduleDialog.getTask().toString());
                Task t = scheduleDialog.getTask();
                newTask.setDueDate(t.getDueDate());
                newTask.setReminder(t.getReminder());
                newTask.setRepeat(t.getRepeat());
                newTask.setPriority(t.getPriority());
                newTask.setHasDueDate(t.isHasDueDate());
                if(newTask.isHasDueDate()){
                    changeColorImageView(ivSchedule, R.color.primary_color);
                } else {
                    changeColorImageView(ivSchedule, R.color.primary_color_tint40);
                }
                scheduleDialog.dismiss();
            });
            scheduleDialog.setNegativeButton(v2 -> {
                newTask = tempTask;
                if(newTask.isHasDueDate()){
                    changeColorImageView(ivSchedule, R.color.primary_color);
                } else {
                    changeColorImageView(ivSchedule, R.color.primary_color_tint40);
                }
                Log.d(TAG, "TASK INFO ON CANCEL: \n" + scheduleDialog.getTask().toString());
                scheduleDialog.dismiss();
            });
            scheduleDialog.show();
        });

        // XỬ LÝ SỰ KIỆN KHI CLICK NÚT ADD TASK
        ivAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTask.setSubTask(subtask);
                String taskName = etTaskName.getText().toString().trim();
                if(!TextUtils.isEmpty(taskName)){
                    newTask.setTitle(taskName);
                } else {
                    Toast.makeText(getContext(), R.string.add_new_task_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "TASK INFO: \n" + newTask.toString());
                String key = mDatabase.push().getKey();
                newTask.setID(key);
                mDatabase.child("task").child(key).setValue(newTask).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), R.string.add_to_database_error,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return v;
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
                    newTask.setTag(tagName);
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
        if(tagList.size() == 1){{
            tagList.add(ADD_NEW_TASK);
        }}
    }




    private void openAddNoteDialog(ImageView iv) {
        final Dialog dialog = createDialog(R.layout.dialog_add_note);

        EditText noteInput = dialog.findViewById(R.id.noteInput);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);

        if(newTask.getDescriptions() != null){
            noteInput.setText(newTask.getDescriptions());
        }
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String note = noteInput.getText().toString().trim();
            if(!TextUtils.isEmpty(note)) {
                newTask.setDescriptions(note);
                changeColorImageView(iv, R.color.primary_color);
            } else {
                changeColorImageView(iv, R.color.primary_color_tint40);
            }
            dialog.dismiss();
        });
        dialog.show();

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
                     Toast.makeText(getContext(),R.string.add_new_tag_error,Toast.LENGTH_SHORT).show();
                     return;
                 }
                 if(tagList.contains(newTagName)){
                     spinner.setSelection(spinner.getAdapter().getCount() - 2);
                     Toast.makeText(getContext(),R.string.add_new_tag_error_2,Toast.LENGTH_SHORT).show();
                 } else {
                     String key = mDatabase.push().getKey();
                     mDatabase.child("tag").child(key).setValue(newTagName);
                     spinner.setSelection(spinner.getAdapter().getCount() - 1);
                 }
                 dialog.dismiss();

             } else {
                 Toast.makeText(getContext(),R.string.add_new_tag_error_empty,Toast.LENGTH_SHORT).show();
             }

        });
        dialog.show();
    }

    private Dialog createDialog(int menu){
        final Dialog dialog = new Dialog(getActivity());
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


    // OVERRIDE GET THEME ĐỂ ADD THEME CUSTOM CHO DIALOG
    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    // HÀM THAY ĐỔI MÀU CÁC IMAGE VIEW
    public void changeColorImageView(ImageView iv, int color) {
        Drawable drawable = iv.getBackground();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, getResources().getColor(color));
        iv.setBackground(drawable);
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

}