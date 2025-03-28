package com.example.todo.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.example.todo.model.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupUtil {
    
    public static void exportTasks(Context context, List<Task> tasks) {
        Gson gson = new Gson();
        String json = gson.toJson(tasks);
        
        try {
            // Create file name with current date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "todo_backup_" + dateFormat.format(new Date()) + ".json";
            
            // Get downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File backupFile = new File(downloadsDir, fileName);
            
            // Write to file
            FileOutputStream fos = new FileOutputStream(backupFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(json);
            writer.close();
            
            Toast.makeText(context, "Backup saved to Downloads/" + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving backup: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    public static List<Task> importTasks(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            
            // Parse JSON
            Gson gson = new Gson();
            Type taskListType = new TypeToken<List<Task>>(){}.getType();
            List<Task> tasks = gson.fromJson(stringBuilder.toString(), taskListType);
            
            return tasks;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error importing backup: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
} 