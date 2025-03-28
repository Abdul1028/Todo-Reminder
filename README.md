# Todo Reminder App

A feature-rich task management application for Android that helps you organize your daily activities, set reminders, and boost your productivity.

## Features

### Task Management
- Create, edit, and delete tasks with ease
- Add detailed descriptions to your tasks
- Set priority levels (Low, Medium, High) with color coding
- Categorize tasks (Work, Personal, Shopping, Health, Education, etc.)
- Mark tasks as complete with visual indicators

### Time Management
- Set due dates for your tasks
- Smart date display showing "Today", "Tomorrow", or specific dates
- Color-coded due date indicators (red for overdue, orange for today)
- Automatic reminders for upcoming tasks

### Organization
- Sort tasks by priority, due date, category, or creation date
- Filter tasks by completion status (All, Active, Completed)
- Search functionality to quickly find specific tasks
- Swipe to delete with undo option

### User Experience
- Clean, modern Material Design interface
- Animated task transitions and interactions
- Dashboard with task completion statistics
- Celebration animations when completing tasks
- Dark mode support

## Getting Started

### Prerequisites
- Android Studio
- Android device or emulator running Android 5.0 (API level 21) or higher
- Firebase account (for cloud synchronization)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Abdul1028/Todo-Reminder.git
   ```

2. Open the project in Android Studio

3. Connect the app with Firebase:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download the `google-services.json` file and place it in the app directory
   - Enable Realtime Database in your Firebase project

4. Build and run the application on your device or emulator

## Permissions

The app requires the following permissions:
- `SCHEDULE_EXACT_ALARM`: For setting task reminders
- `USE_EXACT_ALARM`: For precise notification timing
- `INTERNET`: For Firebase connectivity

## How to Use

### Adding a Task
1. Tap the "+" floating action button
2. Enter task title and optional description
3. Select priority level (Low, Medium, High)
4. Choose a category from the dropdown
5. Set a due date by tapping on "No due date" (optional)
6. Tap "Add" to create the task

### Managing Tasks
- Complete a task by tapping the checkbox
- Edit a task by tapping on it
- Delete a task by swiping left
- Sort tasks by tapping the sort icon and selecting a sorting method
- Filter tasks by tapping the filter icon
- Search for tasks by tapping the search icon

### Task Reminders
- Receive notifications before tasks are due
- Grant the app permission to schedule exact alarms when prompted
- Notifications will appear even when the app is closed

## Technical Details

### Architecture
- MVVM (Model-View-ViewModel) architecture
- LiveData for reactive UI updates
- Firebase Realtime Database for data persistence

### Libraries Used
- Firebase Realtime Database
- Material Components for Android
- AndroidX libraries
- Konfetti for celebration animations

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

Developed by [Abdul1028](https://github.com/Abdul1028)
    
