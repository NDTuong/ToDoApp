package com.example.todooo.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooo.Model.Task;
import com.example.todo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecycleViewTagManagementAdapter extends RecyclerView.Adapter<RecycleViewTagManagementAdapter.ViewHolder> {

    Context context;
    List<String> tagList;
    List<String> tagListKey;
    DatabaseReference mDatabase;
    List<String> taskKey = new ArrayList<>();

    String UID;

    public RecycleViewTagManagementAdapter(Context context){
        this.context = context;
    }

    public void setData(List<String> tagList, List<String> tagListKey, String UID){
        this.tagList = tagList;
        this.tagListKey = tagListKey;
        this.UID = UID;
        this.mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String tagName = tagList.get(position);
        holder.tvTagName.setText(tagName);
        holder.ivMenuDeleteEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, position);
            }
        });
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view, Gravity.CENTER);
        popupMenu.getMenuInflater().inflate(R.menu.menu_edit_delete_tag_management, popupMenu.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.menuDelete:
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.Base_Theme_AppCompat_Light_Dialog_Alert);
                    alertDialogBuilder.setTitle(R.string.confirmation_delete).setMessage(R.string.sure_delete).
                            setPositiveButton(R.string.yes, (dialog, which) -> {
                                String taskRemove = tagList.get(position);
                                mDatabase.child("task").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot snap : snapshot.getChildren()){
                                            Task task = snap.getValue(Task.class);
                                            if(task != null && task.getTag() != null){
                                                if(task.getTag().equals(taskRemove)){
                                                    taskKey.add(snap.getKey());
                                                }
                                            }
                                        }
                                        if(taskKey != null) {
                                            for (int i = 0; i < taskKey.size(); i++){
                                                mDatabase.child("task").child(taskKey.get(i)).
                                                        child("tag").setValue(context.getResources().getString(R.string.no_tag));
                                            }
                                        }
                                        taskKey.clear();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                tagList.remove(tagList.get(position));
                                mDatabase.child("tag").child(tagListKey.get(position)).removeValue();
                                tagListKey.remove(tagListKey.get(position));
                                notifyDataSetChanged();
                            })
                            .setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    Button btnYes = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    Button btnNo = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (btnYes != null && btnNo != null) {
                        btnYes.setTextColor(context.getResources().getColor(R.color.primary_color));
                        btnNo.setTextColor(context.getResources().getColor(R.color.primary_color));
                    }
                    break;
                default:
                    showEditDialog(position);
            }
            return false;
        });
        popupMenu.show();
    }

    private void showEditDialog(int position) {
        final Dialog dialog = createDialog(R.layout.dialog_input_tag);

        EditText tagInput = dialog.findViewById(R.id.tagInput);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);

        String oldTagName = tagList.get(position);
        tagInput.setText(oldTagName);

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnDone.setOnClickListener(v -> {
            String newTagName = tagInput.getText().toString().trim();
            if(!TextUtils.isEmpty(newTagName)) {
                if(newTagName.length() > 50){
                    Toast.makeText(context ,R.string.add_new_tag_error,Toast.LENGTH_SHORT).show();
                }
                tagList.set(position, newTagName);
                mDatabase.child("tag").child(tagListKey.get(position)).setValue(newTagName);
                dialog.dismiss();
            } else {
                Toast.makeText(context,R.string.add_new_tag_error_empty,Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();
    }

    private Dialog createDialog(int menu){
        final Dialog dialog = new Dialog(context);
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

    @Override
    public int getItemCount() {
        if(tagList != null){
            return tagList.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView ivMenuDeleteEdit;
        TextView tvTagName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMenuDeleteEdit = (ImageView) itemView.findViewById(R.id.menuEditDeleteTag);
            tvTagName = (TextView) itemView.findViewById(R.id.tvTagName);
        }
    }
}
