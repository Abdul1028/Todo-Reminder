package com.example.todo.util;

import com.example.todo.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskTemplates {
    
    public static List<Task> getTemplates() {
        List<Task> templates = new ArrayList<>();
        
        // Shopping template
        templates.add(new Task(
                null,
                "Grocery Shopping",
                "Buy: \n- Milk\n- Bread\n- Eggs\n- Fruits\n- Vegetables",
                1,
                false,
                "Shopping",
                0
        ));
        
        // Work meeting template
        templates.add(new Task(
                null,
                "Team Meeting",
                "Prepare agenda and meeting notes",
                2,
                false,
                "Work",
                0
        ));
        
        // Exercise template
        templates.add(new Task(
                null,
                "Workout Session",
                "30 min cardio\n20 min strength training\n10 min stretching",
                1,
                false,
                "Health",
                0
        ));
        
        // Study template
        templates.add(new Task(
                null,
                "Study Session",
                "Review notes\nComplete practice problems\nPrepare questions",
                2,
                false,
                "Education",
                0
        ));
        
        // Personal task template
        templates.add(new Task(
                null,
                "Call Family",
                "Check in with parents and siblings",
                0,
                false,
                "Personal",
                0
        ));
        
        return templates;
    }
} 