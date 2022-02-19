package com.example.todooo.Fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooo.Adapter.RecycleViewCalendarAdapter;
import com.example.todooo.Adapter.RecycleViewTagAdapter;
import com.example.todooo.LoginActivity;
import com.example.todooo.Model.Task;
import com.example.todo.R;
import com.example.todooo.TagManagementActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskFragment extends Fragment implements RecycleViewTagAdapter.OnTagClickListener {

    View view;
    RecyclerView rcvTagList, rcvTask;
    RecycleViewTagAdapter rcvTagAdapter;
    RecycleViewCalendarAdapter rcvViewTaskAdapter;
    ImageView ivTagManagement;
    Spinner filterSpinner;
    String tagTitle;
    List<String> tagList;
    List<String> tagListKey;
    List<Task> taskList;
    boolean isBackFromB;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    final static String TAG = "RcvCalendarAdapter";
    String UID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_task, container, false);
        isBackFromB = false;
        // Get current user ID

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            UID = currentUser.getUid();
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);


        rcvTask = view.findViewById(R.id.rcvTask);
        rcvTagList = view.findViewById(R.id.rcvListTag);
        ivTagManagement = view.findViewById(R.id.ivTagManagement);
        tagTitle = getResources().getString(R.string.all);

        rcvTagAdapter = new RecycleViewTagAdapter(view.getContext(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext(), RecyclerView.HORIZONTAL, false);
        rcvTagList.setLayoutManager(linearLayoutManager);
        getData();
        rcvTagAdapter.setData(tagList, UID);
        rcvTagList.setAdapter(rcvTagAdapter);

        filterSpinner = view.findViewById(R.id.filterSpinner);
        List<String> listFilter = new ArrayList<>();
        listFilter.add(getResources().getString(R.string.filter_spinner_incomplete));
        listFilter.add(getResources().getString(R.string.filter_spinner_completed));
        filterSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.my_spinner, listFilter));
        filterSpinner.setSelection(0);

        rcvViewTaskAdapter = new RecycleViewCalendarAdapter(view.getContext());
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(view.getContext(), RecyclerView.VERTICAL, false);
        rcvTask.setLayoutManager(linearLayoutManager2);
        getTaskData(tagTitle, filterSpinner.getSelectedItemPosition());
        rcvViewTaskAdapter.setData(taskList, UID);
        rcvTask.setAdapter(rcvViewTaskAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getTaskData(tagTitle, position);
                Log.d(TAG, "Tag title spinner choice: " + tagTitle);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ivTagManagement.setOnClickListener(v -> showPopUpMenu(ivTagManagement));

        return view;
    }

//    private void getData() {
//        tagList = new ArrayList<>();
//        tagList.add(getResources().getString(R.string.all));
//        mDatabase.child("tag").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
////                tagList = new ArrayList<>();
////                tagList.add(getResources().getString(R.string.all));
//                for(DataSnapshot snap : snapshot.getChildren()){
//                    String tagName = snap.getValue(String.class);
//                    if(tagName != null){
//                        tagList.add(tagName);
//                    }
//                }
//                rcvTagAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void getData() {
        tagList = new ArrayList<>();
        tagListKey = new ArrayList<>();
        tagList.add(getResources().getString(R.string.all));
        mDatabase.child("tag").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String tagName = snapshot.getValue(String.class);
                if (tagName != null) {
                    tagList.add(tagName);
                    tagListKey.add(snapshot.getKey());
                }
                rcvTagAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String tagName = snapshot.getValue(String.class);
                String key = snapshot.getKey();
                int i = tagListKey.indexOf(key);
                if (tagName != null) {
                    tagList.set(i, tagName);
                }
                rcvTagAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String tagName = snapshot.getValue(String.class);
                String key = snapshot.getKey();
                if (tagName == null || tagList == null || tagName.isEmpty()) {
                    return;
                }
                tagList.remove(tagName);
                tagListKey.remove(key);
                rcvTagAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getTaskData(String tag_title, int filter){
        boolean isCompleted;
        boolean isAll;

        if(filter == 0 ){
            isCompleted = false;
        } else {
            isCompleted = true;
        }

        if(tag_title.equals(getContext().getResources().getString(R.string.all))){
            isAll = true;
        } else {
            isAll = false;
        }

        taskList = new ArrayList<>();
        mDatabase.child("task").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Task task = snapshot.getValue(Task.class);
                if (task != null) {
                    if (taskList != null && taskList.contains(task)) {
                        return;
                    }
                    if (isAll) {
                        if (isCompleted) {
                            if (task.isComplete()) {
                                taskList.add(task);
                            }
                        } else {
                            if (!task.isComplete()) {
                                taskList.add(task);
                            }
                        }
                    } else {
                        if (isCompleted) {
                            if (task.isComplete() && task.getTag() != null && task.getTag().equals(tag_title)) {
                                taskList.add(task);
                            }
                        } else {
                            if(!task.isComplete() && task.getTag() != null && task.getTag().equals(tag_title)) {
                                taskList.add(task);
                            }
                        }
                    }
                    rcvViewTaskAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "TAG title childchange: " + tag_title);
                Task task = snapshot.getValue(Task.class);
                if (task == null || taskList == null || taskList.isEmpty()) {
                    return;
                }
                if(isCompleted != task.isComplete()){
                    taskList.remove(task);
                    rcvViewTaskAdapter.notifyDataSetChanged();
                    return;
                }
                if (isAll) {
                    if (isCompleted) {
                        boolean isInList = false;
                        if (task.isComplete()) {
                            for(int i = 0; i < taskList.size(); i++){
                                if(task.getID().equals(taskList.get(i).getID())){
                                    taskList.set(i, task);
                                    isInList = true;
                                    break;
                                }
                            }
                            if(!isInList){
                                taskList.add(task);
                            }
                        }
                    }
                    else {
                        boolean isInList2 = false;
                        if (!task.isComplete()) {
                            for(int i = 0; i < taskList.size(); i++){
                                if(task.getID().equals(taskList.get(i).getID())){
                                    taskList.set(i, task);
                                    isInList2 = true;
                                    break;
                                }
                            }
                            if(!isInList2){
                                taskList.add(task);
                            }
                        }
                    }
                }
                else {
                    if (isCompleted) {
                        boolean isInList3 = false;
                        if (task.isComplete() && task.getTag() != null && task.getTag().equals(tag_title)) {
                            for(int i = 0; i < taskList.size(); i++){
                                if(task.getID().equals(taskList.get(i).getID())){
                                    taskList.set(i, task);
                                    isInList3 = true;
                                    break;
                                }
                            }
                            if(!isInList3){
                                taskList.add(task);
                            }
                        }
                    } else {
                        if(!task.isComplete() && task.getTag() != null && task.getTag().equals(tag_title)) {
                            boolean isInList4 = false;
                            for(int i = 0; i < taskList.size(); i++){
                                if(task.getID().equals(taskList.get(i).getID())){
                                    taskList.set(i, task);
                                    isInList4 = true;
                                    break;
                                }
                            }
                            if(!isInList4){
                                taskList.add(task);
                            }
                        }
                    }
                }
                rcvViewTaskAdapter.notifyDataSetChanged();
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
                rcvViewTaskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rcvViewTaskAdapter.setData(taskList, UID);
        rcvTask.setAdapter(rcvViewTaskAdapter);
    }

    @Override
    public void onTagClick(String tagTitle) {
        this.tagTitle = tagTitle;
        getTaskData(tagTitle,filterSpinner.getSelectedItemPosition());
    }

    private void showPopUpMenu(View v){
        PopupMenu popupMenu = new PopupMenu(getContext(), v, Gravity.CENTER);
        popupMenu.getMenuInflater().inflate(R.menu.menu_tag_management, popupMenu.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(getContext(), TagManagementActivity.class);
            startActivity(intent);
            return false;
        });
        popupMenu.show();
    }

    public void refresh(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        ft.detach(TaskFragment.this).attach(TaskFragment.this).commit();
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        isBackFromB = true;
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
////        updateUI();
//        if (isBackFromB){
//            refresh();
//        }
//    }
}