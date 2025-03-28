package com.example.todo.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private int lastPosition = -1;

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
        
        // Set priority indicator color and label
        int priorityColor;
        String priorityText;
        switch (task.getPriority()) {
            case 2: // High
                priorityColor = R.color.priority_high;
                priorityText = context.getString(R.string.priority_high);
                break;
            case 1: // Medium
                priorityColor = R.color.priority_medium;
                priorityText = context.getString(R.string.priority_medium);
                break;
            default: // Low
                priorityColor = R.color.priority_low;
                priorityText = context.getString(R.string.priority_low);
                break;
        }
        
        // Set priority color for both the side bar and the badge
        int color = ContextCompat.getColor(context, priorityColor);
        holder.priorityView.setBackgroundColor(color);
        holder.priorityLabel.setText(priorityText);
        holder.priorityLabel.setBackgroundTintList(ContextCompat.getColorStateList(context, priorityColor));
        
        // Set date added
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                task.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.dateAddedText.setText(context.getString(R.string.added_time_ago, timeAgo));
        
        // Set completion status
        holder.checkBox.setChecked(task.isCompleted());
        
        // Apply strikethrough and dim text for completed tasks
        if (task.isCompleted()) {
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.titleTextView.setAlpha(0.6f);
            holder.descriptionTextView.setAlpha(0.6f);
            holder.priorityLabel.setAlpha(0.6f);
            holder.dateAddedText.setAlpha(0.6f);
        } else {
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.titleTextView.setAlpha(1.0f);
            holder.descriptionTextView.setAlpha(1.0f);
            holder.priorityLabel.setAlpha(1.0f);
            holder.dateAddedText.setAlpha(1.0f);
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task, holder.getAdapterPosition());
            }
        });
        
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && buttonView.isPressed()) { // Only trigger if user changed it
                // Apply animation when task is completed
                if (isChecked) {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.task_complete_animation);
                    holder.itemView.startAnimation(animation);
                }
                listener.onTaskCheckChanged(task, isChecked);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.task_delete_animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        listener.onDeleteClick(task, holder.getAdapterPosition());
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                holder.itemView.startAnimation(animation);
            }
        });
        
        // Apply animation for new items
        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_from_right);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull TaskViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public void updateTasks(List<Task> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
        lastPosition = -1; // Reset for animations
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        View priorityView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView priorityLabel;
        TextView dateAddedText;
        CheckBox checkBox;
        ImageButton deleteButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            priorityView = itemView.findViewById(R.id.view_priority);
            titleTextView = itemView.findViewById(R.id.text_task_title);
            descriptionTextView = itemView.findViewById(R.id.text_task_description);
            priorityLabel = itemView.findViewById(R.id.text_priority_label);
            dateAddedText = itemView.findViewById(R.id.text_date_added);
            checkBox = itemView.findViewById(R.id.checkbox_task);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
} 