package com.example.todo.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;

import androidx.core.app.NotificationCompat;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.model.Task;

public class PomodoroTimer {
    private static final String CHANNEL_ID = "pomodoro_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private static final long WORK_DURATION = 25 * 60 * 1000; // 25 minutes
    private static final long BREAK_DURATION = 5 * 60 * 1000; // 5 minutes
    
    private CountDownTimer timer;
    private boolean isWorking = true;
    private Context context;
    private Task currentTask;
    private PomodoroListener listener;
    
    public interface PomodoroListener {
        void onTick(long millisUntilFinished);
        void onFinishWork();
        void onFinishBreak();
    }
    
    public PomodoroTimer(Context context, Task task, PomodoroListener listener) {
        this.context = context;
        this.currentTask = task;
        this.listener = listener;
    }
    
    public void startWorkSession() {
        isWorking = true;
        
        if (timer != null) {
            timer.cancel();
        }
        
        timer = new CountDownTimer(WORK_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (listener != null) {
                    listener.onTick(millisUntilFinished);
                }
            }
            
            @Override
            public void onFinish() {
                if (listener != null) {
                    listener.onFinishWork();
                }
                
                showNotification("Work session complete!", "Time for a break.");
            }
        };
        
        timer.start();
    }
    
    public void startBreakSession() {
        isWorking = false;
        
        if (timer != null) {
            timer.cancel();
        }
        
        timer = new CountDownTimer(BREAK_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (listener != null) {
                    listener.onTick(millisUntilFinished);
                }
            }
            
            @Override
            public void onFinish() {
                if (listener != null) {
                    listener.onFinishBreak();
                }
                
                showNotification("Break complete!", "Ready to get back to work?");
            }
        };
        
        timer.start();
    }
    
    public void cancel() {
        if (timer != null) {
            timer.cancel();
        }
    }
    
    private void showNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    
    public boolean isWorking() {
        return isWorking;
    }
} 