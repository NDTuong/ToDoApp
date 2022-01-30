package com.example.todooo.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecycleViewTagAdapter extends RecyclerView.Adapter<RecycleViewTagAdapter.ViewHolder> {

    Context context;
    List<String> tagList;
    List<Boolean> isSelected;
    private OnTagClickListener onTagClickListener;
    String UID;

    final static String TAG = "RcvCalendarAdapter";


    public RecycleViewTagAdapter(Context context, OnTagClickListener onTagClickListener){
        this.context = context;
        this.onTagClickListener = onTagClickListener;
    }

    public void setData(List<String> tagList, String UID){
        this.tagList = tagList;
        this.UID = UID;
        this.isSelected = new ArrayList<Boolean>(Arrays.asList(new Boolean[10000]));
        Collections.fill(isSelected, Boolean.FALSE);
        isSelected.set(0, true);
        notifyDataSetChanged();
    }

    public interface OnTagClickListener {
        void onTagClick(String tagTitle);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String tag = tagList.get(position);
        boolean isSelect = isSelected.get(position);
        holder.tvTagItem.setText(tag);

        if(isSelect) {
            holder.tvTagItem.setBackgroundTintList(ContextCompat.getColorStateList(context,
                    R.color.primary_color_tint40));
        } else {
            holder.tvTagItem.setBackgroundTintList(ContextCompat.getColorStateList(context,
                    R.color.primary_color_tint80));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTagClickListener.onTagClick(tag);
                if(!isSelect) {
                    holder.tvTagItem.setBackgroundTintList(ContextCompat.getColorStateList(context,
                            R.color.primary_color_tint40));
                }
//                else {
//                    holder.tvTagItem.setBackgroundTintList(ContextCompat.getColorStateList(context,
//                            R.color.primary_color_tint40));
//                }
                Collections.fill(isSelected, Boolean.FALSE);
                isSelected.set(position, true);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(tagList != null){
            return tagList.size();
        }

        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTagItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTagItem = itemView.findViewById(R.id.tvTagItem);
        }
    }
}
