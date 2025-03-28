package com.example.todo.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.model.Task;

public class NotificationHelper {
    private static final String CHANNEL_ID = "todo_reminder_channel";
    private static final String CHANNEL_NAME = "Task Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for task reminders";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void scheduleNotification(Context context, Task task) {
        // Only schedule if task has a due date
        if (task.getDueDate() <= 0) return;
        
        // Check if we have permission to schedule exact alarms on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // We don't have permission, so show a notification now instead
                showTaskReminderNotification(context, task);
                return;
            }
        }
        
        // Create intent for notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                task.getId().hashCode(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle("Task Reminder: " + task.getTitle())
                .setContentText("This task is due soon!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Schedule notification for 1 hour before due date
        long notificationTime = task.getDueDate() - (60 * 60 * 1000);
        
        // If due date is less than 1 hour away, schedule for now
        if (notificationTime < System.currentTimeMillis()) {
            notificationTime = System.currentTimeMillis() + (60 * 1000); // 1 minute from now
        }
        
        // Get alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Create intent for alarm receiver
        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        alarmIntent.putExtra("task_id", task.getId());
        alarmIntent.putExtra("task_title", task.getTitle());
        
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId().hashCode(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Set alarm
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        alarmPendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        alarmPendingIntent
                );
            }
        }
    }

    public static void cancelNotification(Context context, Task task) {
        // Get alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Create intent for alarm receiver
        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId().hashCode(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Cancel alarm
        if (alarmManager != null) {
            alarmManager.cancel(alarmPendingIntent);
        }
    }

    private static void showTaskReminderNotification(Context context, Task task) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                task.getId().hashCode(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle("Task Due Date Set")
                .setContentText(task.getTitle() + " has been scheduled with a due date")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(task.getId().hashCode(), builder.build());
    }
} 