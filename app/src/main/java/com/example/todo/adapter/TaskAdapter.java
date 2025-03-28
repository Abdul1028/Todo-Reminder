package com.example.todo.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.model.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private final Context context;
    private final TaskAdapterListener listener;

    // Interface for callback to activity
    public interface TaskAdapterListener {
        void onTaskClick(Task task, int position);
        void onTaskCheckChanged(Task task, boolean isChecked);
        void onDeleteClick(Task task, int position);
    }

    public TaskAdapter(Context context, List<Task> taskList, TaskAdapterListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        
        // Set task title
        holder.titleTextView.setText(task.getTitle());
        
        // Set task description (if available)
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.descriptionTextView.setText(task.getDescription());
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }
        
        // Set priority indicator color
        int priorityColor;
        switch (task.getPriority()) {
            case 2: // High
                priorityColor = R.color.priority_high;
                break;
            case 1: // Medium
                priorityColor = R.color.priority_medium;
                break;
            default: // Low
                priorityColor = R.color.priority_low;
                break;
        }
        holder.priorityView.setBackgroundColor(ContextCompat.getColor(context, priorityColor));
        
        // Set completion status
        holder.checkBox.setChecked(task.isCompleted());
        
        // Apply strikethrough for completed tasks
        if (task.isCompleted()) {
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task, holder.getAdapterPosition());
            }
        });
        
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && buttonView.isPressed()) { // Only trigger if user changed it
                listener.onTaskCheckChanged(task, isChecked);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(task, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public void updateTasks(List<Task> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        View priorityView;
        TextView titleTextView;
        TextView descriptionTextView;
        CheckBox checkBox;
        ImageButton deleteButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            priorityView = itemView.findViewById(R.id.view_priority);
            titleTextView = itemView.findViewById(R.id.text_task_title);
            descriptionTextView = itemView.findViewById(R.id.text_task_description);
            checkBox = itemView.findViewById(R.id.checkbox_task);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
} 