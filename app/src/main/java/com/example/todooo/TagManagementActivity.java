package com.example.todooo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todooo.Adapter.RecycleViewTagManagementAdapter;
import com.example.todooo.Model.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class  TagManagementActivity extends AppCompatActivity {

    ImageView ivBack;
    RecyclerView rcvTagManagement;
    RecycleViewTagManagementAdapter rcvAdapter;
    LinearLayout llAddNewTag;

    DatabaseReference mDatabase;

    List<String> tagList;
    List<String> tagListKey;

    final static String TAG = "TagManagement";
    String UID;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_management);

        // Get current user ID
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            UID = currentUser.getUid();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);

        ivBack = findViewById(R.id.ivBack);
        rcvTagManagement = findViewById(R.id.rcvTagManagement);
        llAddNewTag = findViewById(R.id.llAddNewTag);

        ivBack.setOnClickListener(v -> finish());

        rcvAdapter = new RecycleViewTagManagementAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rcvTagManagement.setLayoutManager(linearLayoutManager);
        getData();
        rcvAdapter.setData(tagList, tagListKey, UID);
        rcvTagManagement.setAdapter(rcvAdapter);

        llAddNewTag.setOnClickListener(v -> shopAddTagDialog());


    }

    private void shopAddTagDialog() {
        final Dialog dialog = createDialog(R.layout.dialog_input_tag);

        EditText tagInput = dialog.findViewById(R.id.tagInput);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String newTagName = tagInput.getText().toString().trim();
            if (!TextUtils.isEmpty(newTagName)) {
                if (newTagName.length() > 50) {
                    Toast.makeText(this, R.string.add_new_tag_error, Toast.LENGTH_SHORT).show();
                }
                if(tagList.contains(newTagName)){
                    Toast.makeText(this,R.string.add_new_tag_error_2,Toast.LENGTH_SHORT).show();
                } else {
                    String key = mDatabase.push().getKey();
                    mDatabase.child("tag").child(key).setValue(newTagName);
                }
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

    private void getData() {
        tagList = new ArrayList<>();
        tagListKey = new ArrayList<>();
        mDatabase.child("tag").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String tagName = snapshot.getValue(String.class);
                if (tagName != null) {
                    tagList.add(tagName);
                    tagListKey.add(snapshot.getKey());
                }
                rcvTagManagement.smoothScrollToPosition(tagList.size() - 1);
                rcvAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String tagName = snapshot.getValue(String.class);
                String key = snapshot.getKey();
                int i = tagListKey.indexOf(key);
                if (tagName != null) {
                    tagList.set(i, tagName);
                }
                rcvAdapter.notifyDataSetChanged();
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
                rcvAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}