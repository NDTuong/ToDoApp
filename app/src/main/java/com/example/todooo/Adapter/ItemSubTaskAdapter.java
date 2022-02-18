package com.example.todooo.Adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooo.CustomDialog.BottomSheetAddTaskDialog;
import com.example.todo.R;
import com.example.todooo.TaskDetailActivity;

import java.util.List;

public class ItemSubTaskAdapter extends RecyclerView.Adapter<ItemSubTaskAdapter.ViewHolder> {

    BottomSheetAddTaskDialog context;
    TaskDetailActivity context2;
    List<String> subtask;

    public ItemSubTaskAdapter(BottomSheetAddTaskDialog context){
        this.context = context;
    }
    public ItemSubTaskAdapter(TaskDetailActivity context){
        this.context2 = context;
    }
    public void setData(List<String> subtask) {
        this.subtask = subtask;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String subTaskName = subtask.get(position);
        holder.tvSubTask.setText(subTaskName);
        holder.ivDelete.setOnClickListener(v -> {
            subtask.remove(position);
            if(subtask.size() <= 5){
                if(context != null) {
                    context.setWidthScrollView();
                }
                if(context2 != null) {
                    context2.setWidthScrollView();
                }
            }
            notifyDataSetChanged();
        });

    }

    @Override
    public int getItemCount() {
        if(subtask != null){
            return subtask.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSubTask;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubTask = itemView.findViewById(R.id.tvSubTask);
            ivDelete = itemView.findViewById(R.id.ivDelete);

        }
    }
}
