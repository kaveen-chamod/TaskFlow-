package com.example.mymobileapp;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<TodoItem> todoList;
    private OnTodoClickListener listener;

    public interface OnTodoClickListener {
        void onEditClick(TodoItem item, int position);
        void onDeleteClick(int position);
        void onToggleCompletion(TodoItem item, int position);
    }

    public TodoAdapter(List<TodoItem> todoList, OnTodoClickListener listener) {
        this.todoList = todoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem item = todoList.get(position);
        holder.tvTaskName.setText(item.getTaskName());
        holder.cbDone.setChecked(item.getIsCompleted());

        updateItemState(holder.tvTaskName, item.getIsCompleted());

        holder.cbDone.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleCompletion(item, holder.getAdapterPosition());
            }
        });

        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(item, holder.getAdapterPosition());
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    private void updateItemState(TextView textView, boolean isCompleted) {
        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(Color.GRAY);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbDone;
        TextView tvTaskName;
        ImageView ivEdit, ivDelete;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cbDone);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
