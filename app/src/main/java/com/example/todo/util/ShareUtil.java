package com.example.todo.util;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import com.example.todo.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShareUtil {
    
    public static void shareTask(Context context, Task task) {
        StringBuilder shareText = new StringBuilder();
        
        // Build share text
        shareText.append("Task: ").append(task.getTitle()).append("\n\n");
        
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            shareText.append("Description: ").append(task.getDescription()).append("\n\n");
        }
        
        shareText.append("Priority: ");
        switch (task.getPriority()) {
            case 2:
                shareText.append("High");
                break;
            case 1:
                shareText.append("Medium");
                break;
            default:
                shareText.append("Low");
                break;
        }
        shareText.append("\n");
        
        shareText.append("Category: ").append(task.getCategory()).append("\n");
        
        if (task.getDueDate() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dueDate = dateFormat.format(new Date(task.getDueDate()));
            shareText.append("Due Date: ").append(dueDate).append("\n");
        }
        
        shareText.append("Status: ").append(task.isCompleted() ? "Completed" : "Pending").append("\n\n");
        
        shareText.append("Shared from My TODO App");
        
        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Task: " + task.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        
        // Start activity
        context.startActivity(Intent.createChooser(shareIntent, "Share Task"));
    }
} 