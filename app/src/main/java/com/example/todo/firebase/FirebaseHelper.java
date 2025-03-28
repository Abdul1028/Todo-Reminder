package com.example.todo.firebase;

import androidx.annotation.NonNull;

import com.example.todo.model.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manage Firebase Realtime Database operations
 * This class follows the Singleton pattern to ensure only one instance exists
 */
public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final DatabaseReference tasksRef;

    // Interface for callbacks
    public interface TasksCallback {
        void onTasksLoaded(List<Task> tasks);
        void onError(String error);
    }

    public interface TaskCallback {
        void onSuccess();
        void onError(String error);
    }

    private FirebaseHelper() {
        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        tasksRef = database.getReference("tasks");
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    /**
     * Add a new task to Firebase
     */
    public void addTask(Task task, final TaskCallback callback) {
        // Generate a unique ID if not provided
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(tasksRef.push().getKey());
        }

        tasksRef.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Update an existing task
     */
    public void updateTask(Task task, final TaskCallback callback) {
        tasksRef.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Delete a task
     */
    public void deleteTask(String taskId, final TaskCallback callback) {
        tasksRef.child(taskId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Get all tasks
     */
    public void getAllTasks(final TasksCallback callback) {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Task> taskList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);
                    }
                }
                if (callback != null) {
                    callback.onTasksLoaded(taskList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null) {
                    callback.onError(databaseError.getMessage());
                }
            }
        });
    }
} 