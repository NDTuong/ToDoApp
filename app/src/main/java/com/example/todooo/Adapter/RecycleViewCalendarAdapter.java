package com.example.todooo.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooo.CustomDialog.ScheduleDialog;
import com.example.todooo.Model.Task;
import com.example.todooo.Model.TypeRepeat;
import com.example.todo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RecycleViewCalendarAdapter extends RecyclerView.Adapter<RecycleViewCalendarAdapter.ViewHolder> {
    Context context;
    List<Task> listTask;
    DatabaseReference mDatabase;
    String UID;

    final static String TAG = "RcvCalendarAdapter";

    public RecycleViewCalendarAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Task> listTask, String UID) {
        this.listTask = listTask;
        this.mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID + "/task");
        this.UID = UID;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Task task = listTask.get(position);

        holder.tvTitle.setText(task.getTitle());

        if(task.getDescriptions() != null) {
            holder.tvDescriptions.setText(task.getDescriptions());
        } else {
            holder.tvDescriptions.setText(context.getResources().getString(R.string.descriptions));
        }
        if(!task.getTag().equals(context.getResources().getString(R.string.no_tag))){
            holder.tvTag.setVisibility(View.VISIBLE);
            holder.tvTag.setText(task.getTag());
        } else {
            holder.tvTag.setVisibility(View.GONE);
        }
        if(task.getReminder() == null){
            holder.ivNotify.setVisibility(View.GONE);
        } else {
            if (task.getReminder().getTimeReminder() == null){
                holder.ivNotify.setVisibility(View.GONE);
            } else {
                holder.ivNotify.setVisibility(View.VISIBLE);
            }
        }

        if(task.getRepeat() == null){
            holder.ivRepeat.setVisibility(View.GONE);
        } else {
            if(task.getRepeat().getType() == TypeRepeat.NO_REPEAT){
                holder.ivRepeat.setVisibility(View.GONE);
            } else {
                holder.ivRepeat.setVisibility(View.VISIBLE);
            }
        }

        if(task.getSubTask() == null){
            holder.ivSubTask.setVisibility(View.GONE);
        }

        int priority = task.getPriority();
        switch (priority){
            case 1:
                holder.ivPriority.setImageResource(R.drawable.ic_circle_1);
                break;
            case 2:
                holder.ivPriority.setImageResource(R.drawable.ic_circle_2);
                break;
            default:
                holder.ivPriority.setImageResource(R.drawable.ic_circle_3);
                break;
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public boolean onLongClick(View v) {
                showPopUpMenu(v, position);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        if(listTask != null){
            return listTask.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle, tvDescriptions, tvTag;
        ImageView ivPriority, ivNotify, ivRepeat,ivSubTask;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDescriptions = (TextView) itemView.findViewById(R.id.tvDescriptions);
            tvTag = (TextView) itemView.findViewById(R.id.tvTag);
            ivNotify = (ImageView) itemView.findViewById(R.id.ivNotify);
            ivRepeat = (ImageView) itemView.findViewById(R.id.ivRepeat);
            ivPriority = (ImageView) itemView.findViewById(R.id.ivPriority);
            ivSubTask = (ImageView) itemView.findViewById(R.id.ivSubTask);
        }
    }

    private void showPopUpMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        Task task = listTask.get(position);
        if(!task.isComplete()){
            popupMenu.getMenuInflater().inflate(R.menu.menu_click_item, popupMenu.getMenu());
        } else {
            popupMenu.getMenuInflater().inflate(R.menu.menu_click_item_2, popupMenu.getMenu());
        }


        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menuDelete:
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.Base_Theme_AppCompat_Light_Dialog_Alert);
                    alertDialogBuilder.setTitle(R.string.confirmation_delete).setMessage(R.string.sure_delete).
                            setPositiveButton(R.string.yes, (dialog, which) -> {
//                                listTask.remove(task);
                                mDatabase.child(task.getID()).removeValue();
//                                notifyDataSetChanged();
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
                case R.id.menuCompleted:
                    AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(context, R.style.Base_Theme_AppCompat_Light_Dialog_Alert);
                    alertDialogBuilder2.setTitle(R.string.confirmation_completed).setMessage(R.string.sure_to_mark_as_complete).
                            setPositiveButton(R.string.yes, (dialog, which) -> {
                                listTask.remove(task);
                                mDatabase.child(task.getID()).child("complete").setValue(!task.isComplete());
                                notifyDataSetChanged();
                            })
                            .setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());


                    AlertDialog alertDialog2 = alertDialogBuilder2.create();
                    alertDialog2.show();
                    Button btnYes2 = alertDialog2.getButton(DialogInterface.BUTTON_POSITIVE);
                    Button btnNo2 = alertDialog2.getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (btnYes2 != null && btnNo2 != null) {
                        btnYes2.setTextColor(context.getResources().getColor(R.color.primary_color));
                        btnNo2.setTextColor(context.getResources().getColor(R.color.primary_color));
                    }
                    break;
                case R.id.menuReschedule:
                    final ScheduleDialog scheduleDialog = new ScheduleDialog(context);
                    scheduleDialog.setTask(task);
                    scheduleDialog.setType(0);
                    scheduleDialog.setPositiveButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listTask.set(position, scheduleDialog.getTask());
                            Log.d(TAG, "TASK INFO: \n" + listTask.get(position).toString());
                            mDatabase.child(task.getID()).setValue(scheduleDialog.getTask());
                            notifyDataSetChanged();
                            scheduleDialog.dismiss();
                        }
                    });
                    scheduleDialog.setNegativeButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDatabase.child(task.getID()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Task tempTask = snapshot.getValue(Task.class);
                                    listTask.set(position, tempTask);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            notifyDataSetChanged();
                            scheduleDialog.dismiss();
                        }
                    });
                    scheduleDialog.show();
                    break;
            }
            return false;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();
    }

}
