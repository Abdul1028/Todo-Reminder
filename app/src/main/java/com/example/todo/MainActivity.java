package com.example.todo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.adapter.TaskAdapter;
import com.example.todo.model.Task;
import com.example.todo.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.TaskAdapterListener {

    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_tasks);
        emptyView = findViewById(R.id.text_empty_view);
        progressBar = findViewById(R.id.progress_bar);
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);

        // Set up RecyclerView with animation
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        
        // Apply layout animation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation);
        recyclerView.setLayoutAnimation(animation);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Observe tasks
        viewModel.getTasks().observe(this, tasks -> {
            adapter.updateTasks(tasks);
            updateEmptyView(tasks);
            // Run the layout animation again
            recyclerView.scheduleLayoutAnimation();
        });

        // Observe loading state
        viewModel.getLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(recyclerView, error, Snackbar.LENGTH_LONG).show();
            }
        });

        // Set up FAB click listener with animation
        fabAddTask.setOnClickListener(v -> {
            fabAddTask.animate()
                    .rotationBy(360f)
                    .setDuration(300)
                    .withEndAction(() -> showTaskDialog(null))
                    .start();
        });

        // Add swipe to delete functionality
        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task deletedTask = viewModel.getTasks().getValue().get(position);
                
                // Delete the task
                viewModel.deleteTask(deletedTask.getId());
                
                // Show undo option
                Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", view -> {
                            // Restore the task
                            viewModel.addTask(deletedTask);
                        }).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void updateEmptyView(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskClick(Task task, int position) {
        showTaskDialog(task);
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        viewModel.updateTask(task);
        
        // Show feedback
        String message = isChecked ? "Task completed!" : "Task marked as incomplete";
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Task task, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> viewModel.deleteTask(task.getId()))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showTaskDialog(Task task) {
        boolean isEdit = task != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? R.string.edit_task : R.string.add_task);

        // Inflate the dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_task, null);
        builder.setView(view);

        // Get references to views
        EditText titleEditText = view.findViewById(R.id.edit_task_title);
        EditText descriptionEditText = view.findViewById(R.id.edit_task_description);
        RadioGroup priorityRadioGroup = view.findViewById(R.id.radio_group_priority);

        // If editing, populate the fields
        if (isEdit) {
            titleEditText.setText(task.getTitle());
            descriptionEditText.setText(task.getDescription());
            
            // Set the correct radio button based on priority
            switch (task.getPriority()) {
                case 0:
                    priorityRadioGroup.check(R.id.radio_priority_low);
                    break;
                case 1:
                    priorityRadioGroup.check(R.id.radio_priority_medium);
                    break;
                case 2:
                    priorityRadioGroup.check(R.id.radio_priority_high);
                    break;
            }
        }

        // Set up buttons
        builder.setPositiveButton(R.string.save, null); // We'll override this below
        builder.setNegativeButton(R.string.cancel, null);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button to prevent automatic dismissal when input is invalid
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            
            // Validate input
            if (title.isEmpty()) {
                titleEditText.setError(getString(R.string.error_empty_title));
                return;
            }
            
            // Get selected priority
            int priority;
            int selectedId = priorityRadioGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.radio_priority_high) {
                priority = 2;
            } else if (selectedId == R.id.radio_priority_medium) {
                priority = 1;
            } else {
                priority = 0;
            }
            
            // Create or update task
            if (isEdit) {
                task.setTitle(title);
                task.setDescription(description);
                task.setPriority(priority);
                viewModel.updateTask(task);
                Snackbar.make(recyclerView, "Task updated successfully", Snackbar.LENGTH_SHORT).show();
            } else {
                Task newTask = new Task(null, title, description, priority, false);
                viewModel.addTask(newTask);
                Snackbar.make(recyclerView, "New task added", Snackbar.LENGTH_SHORT).show();
            }
            
            dialog.dismiss();
        });
    }
}