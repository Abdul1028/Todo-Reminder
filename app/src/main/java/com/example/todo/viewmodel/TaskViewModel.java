package com.example.todo.viewmodel;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.example.todo.firebase.FirebaseHelper;
import com.example.todo.model.Task;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ViewModel for managing Task data and business logic
 */
public class TaskViewModel extends AndroidViewModel {
    private final FirebaseHelper firebaseHelper;
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final Application application;

    public TaskViewModel(Application application) {
        super(application);
        this.application = application;
        firebaseHelper = FirebaseHelper.getInstance();
        loadTasks();
    }

    // LiveData getters
    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Load tasks from Firebase
    public void loadTasks() {
        loadingLiveData.setValue(true);
        firebaseHelper.getAllTasks(new FirebaseHelper.TasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                // Sort tasks by priority (High to Low) and completion status
                Collections.sort(tasks, (t1, t2) -> {
                    // First sort by completion status (incomplete first)
                    if (t1.isCompleted() != t2.isCompleted()) {
                        return t1.isCompleted() ? 1 : -1;
                    }
                    // Then sort by priority (high to low)
                    return Integer.compare(t2.getPriority(), t1.getPriority());
                });
                
                tasksLiveData.setValue(tasks);
                loadingLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                loadingLiveData.setValue(false);
            }
        });
    }

    // Add a new task
    public void addTask(Task task) {
        isLoading.setValue(true);
        
        FirebaseHelper.getInstance().addTask(application, task, new FirebaseHelper.TaskCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    // Update an existing task
    public void updateTask(Task task) {
        loadingLiveData.setValue(true);
        firebaseHelper.updateTask(task, new FirebaseHelper.TaskCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                // Tasks will be automatically updated via the ValueEventListener
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                loadingLiveData.setValue(false);
            }
        });
    }

    // Delete a task
    public void deleteTask(String taskId) {
        loadingLiveData.setValue(true);
        firebaseHelper.deleteTask(taskId, new FirebaseHelper.TaskCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                // Tasks will be automatically updated via the ValueEventListener
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                loadingLiveData.setValue(false);
            }
        });
    }

    // Toggle task completion status
    public void toggleTaskCompletion(Task task) {
        task.setCompleted(!task.isCompleted());
        updateTask(task);
    }
} 