package com.example.todo.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.todo.MainActivity;
import com.example.todo.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "todo_reminder_channel";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get task details from intent
        String taskId = intent.getStringExtra("task_id");
        String taskTitle = intent.getStringExtra("task_title");
        
        // Create intent for notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_calendar_month_24)
                .setContentTitle("Task Reminder")
                .setContentText(taskTitle + " is due soon!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Show notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(taskId.hashCode(), builder.build());
    }
} 