package com.example.todo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import android.view.Menu;
import android.view.MenuItem;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import android.app.DatePickerDialog;
import android.text.format.DateUtils;

import com.example.todo.notification.NotificationHelper;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Intent;
import android.app.AlarmManager;

public class MainActivity extends AppCompatActivity implements TaskAdapter.TaskAdapterListener {

    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private TextView greetingTextView;
    private TextView dateTextView;
    private TextView totalTasksTextView;
    private TextView completedTasksTextView;
    private TextView pendingTasksTextView;
    private ProgressBar tasksProgressBar;
    private TextView progressPercentageTextView;
    private static final String[] CATEGORIES = {"General", "Work", "Personal", "Shopping", "Health", "Education"};
    private KonfettiView konfettiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_tasks);
        emptyView = findViewById(R.id.text_empty_view);
        progressBar = findViewById(R.id.progress_bar);
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        konfettiView = findViewById(R.id.konfetti_view);


        // Set up RecyclerView with animation
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        
        // Apply layout animation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation);
        recyclerView.setLayoutAnimation(animation);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, 
            new ViewModelProvider.AndroidViewModelFactory(getApplication()))
            .get(TaskViewModel.class);

        // Initialize dashboard header views
        View headerView = findViewById(R.id.header_dashboard);
        greetingTextView = headerView.findViewById(R.id.text_greeting);
        dateTextView = headerView.findViewById(R.id.text_date);
        totalTasksTextView = headerView.findViewById(R.id.text_total_tasks);
        completedTasksTextView = headerView.findViewById(R.id.text_completed_tasks);
        pendingTasksTextView = headerView.findViewById(R.id.text_pending_tasks);
        tasksProgressBar = headerView.findViewById(R.id.progress_tasks);
        progressPercentageTextView = headerView.findViewById(R.id.text_progress_percentage);

        // Set greeting based on time of day
        updateGreeting();

        // Set current date
        updateDate();

        // Observe tasks
        viewModel.getTasks().observe(this, tasks -> {
            adapter.updateTasks(tasks);
            updateEmptyView(tasks);
            updateDashboardStats(tasks);
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

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);

        // Check and request alarm permission
        checkAndRequestAlarmPermission();
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

        // Show confetti when task is completed
        if (isChecked) {
            showConfetti();
        }

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
        Spinner categorySpinner = view.findViewById(R.id.spinner_category);
        TextView dueDateTextView = view.findViewById(R.id.text_due_date);
        Button clearDateButton = view.findViewById(R.id.button_clear_date);

        // Set up category adapter
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Make sure a default selection is set
        categorySpinner.setSelection(0); // Select "General" by default

        // Variable to store selected due date
        final long[] selectedDueDate = {0};

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
            
            // Set selected category
            String category = task.getCategory();
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equals(category)) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }

            // If editing, set the due date if available
            if (task.getDueDate() > 0) {
                selectedDueDate[0] = task.getDueDate();
                updateDueDateText(dueDateTextView, selectedDueDate[0]);
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
            
            // Get selected category
            String category;
            int categoryPosition = categorySpinner.getSelectedItemPosition();
            // Check if position is valid before accessing the array
            if (categoryPosition >= 0 && categoryPosition < CATEGORIES.length) {
                category = CATEGORIES[categoryPosition];
            } else {
                // Default to "General" if position is invalid
                category = "General";
            }
            
            // Create or update task
            if (isEdit) {
                task.setTitle(title);
                task.setDescription(description);
                task.setPriority(priority);
                task.setCategory(category);
                task.setDueDate(selectedDueDate[0]);
                viewModel.updateTask(task);
                
                // Schedule notification if due date is set
                if (selectedDueDate[0] > 0) {
                    NotificationHelper.scheduleNotification(MainActivity.this, task);
                } else {
                    NotificationHelper.cancelNotification(MainActivity.this, task);
                }
                
                Snackbar.make(recyclerView, "Task updated successfully", Snackbar.LENGTH_SHORT).show();
            } else {
                Task newTask = new Task(null, title, description, priority, false, category, selectedDueDate[0]);
                viewModel.addTask(newTask);
                
                // Schedule notification if due date is set
                if (selectedDueDate[0] > 0) {
                    // We need to wait for the task to be added to get its ID
                    // This will be handled in the ViewModel when the task is added
                }
                
                Snackbar.make(recyclerView, "New task added", Snackbar.LENGTH_SHORT).show();
            }
            
            dialog.dismiss();
        });

        // Set up date picker dialog
        dueDateTextView.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            
            // If a date is already selected, use it as the default
            if (selectedDueDate[0] > 0) {
                c.setTimeInMillis(selectedDueDate[0]);
            }
            
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        // Set time to end of day (23:59:59)
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        
                        selectedDueDate[0] = calendar.getTimeInMillis();
                        updateDueDateText(dueDateTextView, selectedDueDate[0]);
                    },
                    year, month, day);
            
            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            
            datePickerDialog.show();
        });

        // Set up clear button
        clearDateButton.setOnClickListener(v -> {
            selectedDueDate[0] = 0;
            dueDateTextView.setText("No due date");
        });
    }

    private void updateGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Good morning!";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            greeting = "Good afternoon!";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            greeting = "Good evening!";
        } else {
            greeting = "Good night!";
        }
        greetingTextView.setText(greeting);
    }

    private void showConfetti() {
        konfettiView.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(1000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 1000L);
    }

    private void updateDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        String date = dateFormat.format(new Date());
        dateTextView.setText(date);
    }

    private void updateDashboardStats(List<Task> tasks) {
        if (tasks == null) return;
        
        int total = tasks.size();
        int completed = 0;
        
        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        
        int pending = total - completed;
        int progressPercentage = total > 0 ? (completed * 100) / total : 0;
        
        totalTasksTextView.setText(String.valueOf(total));
        completedTasksTextView.setText(String.valueOf(completed));
        pendingTasksTextView.setText(String.valueOf(pending));
        tasksProgressBar.setProgress(progressPercentage);
        progressPercentageTextView.setText(progressPercentage + "% completed");
    }

    private void updateDueDateText(TextView textView, long timestamp) {
        if (timestamp <= 0) {
            textView.setText("No due date");
            return;
        }
        
        // Format the date
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        
        // Add relative time (today, tomorrow, etc.)
        String relativeTime = getRelativeDateString(timestamp);
        if (!relativeTime.isEmpty()) {
            formattedDate += " (" + relativeTime + ")";
        }
        
        textView.setText(formattedDate);
    }

    private String getRelativeDateString(long timestamp) {
        long now = System.currentTimeMillis();
        
        // Check if it's today
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        
        Calendar tomorrowCal = (Calendar) todayCal.clone();
        tomorrowCal.add(Calendar.DAY_OF_MONTH, 1);
        
        Calendar timestampCal = Calendar.getInstance();
        timestampCal.setTimeInMillis(timestamp);
        timestampCal.set(Calendar.HOUR_OF_DAY, 0);
        timestampCal.set(Calendar.MINUTE, 0);
        timestampCal.set(Calendar.SECOND, 0);
        timestampCal.set(Calendar.MILLISECOND, 0);
        
        if (timestampCal.getTimeInMillis() == todayCal.getTimeInMillis()) {
            return "Today";
        } else if (timestampCal.getTimeInMillis() == tomorrowCal.getTimeInMillis()) {
            return "Tomorrow";
        } else {
            // Check if it's within a week
            long diff = timestampCal.getTimeInMillis() - todayCal.getTimeInMillis();
            long days = diff / (24 * 60 * 60 * 1000);
            
            if (days > 1 && days < 7) {
                return "in " + days + " days";
            }
        }
        
        return "";
    }

    private void checkAndRequestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!getSystemService(AlarmManager.class).canScheduleExactAlarms()) {
                // Show a dialog explaining why we need this permission
                new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("To set reminders for tasks with due dates, this app needs permission to schedule exact alarms.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intent);
                    })
                    .setNegativeButton("Not Now", null)
                    .show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        
        // Set up search functionality
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTasks(newText);
                return true;
            }
        });
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        // Handle sort options
        if (id == R.id.sort_priority) {
            sortTasksByPriority();
            return true;
        } else if (id == R.id.sort_due_date) {
            sortTasksByDueDate();
            return true;
        } else if (id == R.id.sort_category) {
            sortTasksByCategory();
            return true;
        } else if (id == R.id.sort_creation_date) {
            sortTasksByCreationDate();
            return true;
        }
        
        // Handle filter options
        else if (id == R.id.filter_all) {
            filterAllTasks();
            item.setChecked(true);
            return true;
        } else if (id == R.id.filter_active) {
            filterActiveTasks();
            item.setChecked(true);
            return true;
        } else if (id == R.id.filter_completed) {
            filterCompletedTasks();
            item.setChecked(true);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void sortTasksByPriority() {
        List<Task> tasks = new ArrayList<>(viewModel.getTasks().getValue());
        if (tasks != null) {
            Collections.sort(tasks, (t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
            adapter.updateTasks(tasks);
            Snackbar.make(recyclerView, "Sorted by priority", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void sortTasksByDueDate() {
        List<Task> tasks = new ArrayList<>(viewModel.getTasks().getValue());
        if (tasks != null) {
            Collections.sort(tasks, (t1, t2) -> {
                // Put tasks without due dates at the end
                if (t1.getDueDate() == 0 && t2.getDueDate() == 0) {
                    return 0;
                } else if (t1.getDueDate() == 0) {
                    return 1;
                } else if (t2.getDueDate() == 0) {
                    return -1;
                }
                return Long.compare(t1.getDueDate(), t2.getDueDate());
            });
            adapter.updateTasks(tasks);
            Snackbar.make(recyclerView, "Sorted by due date", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void sortTasksByCategory() {
        List<Task> tasks = new ArrayList<>(viewModel.getTasks().getValue());
        if (tasks != null) {
            Collections.sort(tasks, (t1, t2) -> t1.getCategory().compareTo(t2.getCategory()));
            adapter.updateTasks(tasks);
            Snackbar.make(recyclerView, "Sorted by category", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void sortTasksByCreationDate() {
        List<Task> tasks = new ArrayList<>(viewModel.getTasks().getValue());
        if (tasks != null) {
            Collections.sort(tasks, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
            adapter.updateTasks(tasks);
            Snackbar.make(recyclerView, "Sorted by creation date", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void filterAllTasks() {
        viewModel.loadTasks(); // Reset to default sorting/filtering
        Snackbar.make(recyclerView, "Showing all tasks", Snackbar.LENGTH_SHORT).show();
    }

    private void filterActiveTasks() {
        List<Task> allTasks = viewModel.getTasks().getValue();
        if (allTasks != null) {
            List<Task> activeTasks = new ArrayList<>();
            for (Task task : allTasks) {
                if (!task.isCompleted()) {
                    activeTasks.add(task);
                }
            }
            adapter.updateTasks(activeTasks);
            Snackbar.make(recyclerView, "Showing active tasks", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void filterCompletedTasks() {
        List<Task> allTasks = viewModel.getTasks().getValue();
        if (allTasks != null) {
            List<Task> completedTasks = new ArrayList<>();
            for (Task task : allTasks) {
                if (task.isCompleted()) {
                    completedTasks.add(task);
                }
            }
            adapter.updateTasks(completedTasks);
            Snackbar.make(recyclerView, "Showing completed tasks", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void searchTasks(String query) {
        List<Task> allTasks = viewModel.getTasks().getValue();
        if (allTasks != null) {
            if (query.isEmpty()) {
                adapter.updateTasks(allTasks);
            } else {
                List<Task> filteredTasks = new ArrayList<>();
                String lowerCaseQuery = query.toLowerCase();
                
                for (Task task : allTasks) {
                    if (task.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        (task.getDescription() != null && 
                         task.getDescription().toLowerCase().contains(lowerCaseQuery)) ||
                        task.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                        filteredTasks.add(task);
                    }
                }
                
                adapter.updateTasks(filteredTasks);
            }
        }
    }
}