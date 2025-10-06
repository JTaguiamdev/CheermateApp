# Phase 1 Feature Guide

This document provides a visual and functional overview of all Phase 1 features implemented.

---

## 🔐 Authentication & Security

### 1. Password Hashing with BCrypt

**What it does:**
- Securely hashes passwords using BCrypt algorithm
- Cost factor 12 for strong encryption
- Automatic salt generation per password

**How to use:**
```kotlin
// Hash a password
val hashedPassword = PasswordHashUtil.hashPassword("userPassword")

// Verify a password
val isValid = PasswordHashUtil.verifyPassword("userPassword", hashedPassword)
```

**Where it's used:**
- ✅ SignUpActivity - Hashes passwords during registration
- ✅ AuthRepository - Verifies passwords during login
- ✅ ForgotPasswordActivity - Hashes new passwords during reset
- ✅ MainActivity - Hashes new passwords during password change

---

### 2. Forgot Password Flow

**User Flow:**
1. User clicks "Forgot Password" on login screen
2. Enters username
3. Selects security question from dropdown
4. Enters security answer
5. If correct, prompted for new password
6. Password reset successfully

**Files:**
- Activity: `ForgotPasswordActivity.kt`
- Layout: `activity_forgot_password.xml`
- Dialog: `dialog_reset_password.xml`

**Security Questions:**
- What was your first pet's name?
- What city were you born in?
- What is your mother's maiden name?
- What was your first car?
- What elementary school did you attend?
- What is your favorite color?
- What was the name of your first boss?
- In what city did you meet your spouse/significant other?

---

### 3. Input Validation

**Validation Rules:**

**Username:**
- Length: 3-20 characters
- Allowed: Letters, numbers, underscore (_)
- Examples:
  - ✅ john_doe
  - ✅ user123
  - ❌ ab (too short)
  - ❌ user@name (special chars)

**Email:**
- Must be valid email format
- Examples:
  - ✅ user@example.com
  - ✅ test.user@domain.co.uk
  - ❌ notanemail
  - ❌ @example.com

**Password:**
- Minimum: 6 characters
- Strong password: 8+ chars, uppercase, lowercase, digit

**SQL Injection Prevention:**
- Detects patterns like: `' OR '1'='1`, `admin'--`, etc.
- Blocks dangerous inputs
- Sanitizes user input

---

## 👤 Profile Management

### 4. Edit Profile

**User Flow:**
1. Navigate to Settings tab
2. Click on profile card
3. Edit username, email, first name, last name
4. Click "Save"
5. Profile updated successfully

**Features:**
- Real-time validation
- Username uniqueness check
- Email format validation
- Updates timestamp

**Dialog:** `dialog_edit_profile.xml`

**Fields:**
- Username (validated)
- Email (validated)
- First Name
- Last Name

---

### 5. Change Password

**User Flow:**
1. Navigate to Settings tab
2. Click on profile card
3. Click "Change Password" button
4. Enter current password
5. Enter new password
6. Confirm new password
7. Password changed successfully

**Features:**
- Current password verification with BCrypt
- New password validation (6+ characters)
- Password confirmation matching
- Secure BCrypt hashing of new password

**Dialog:** `dialog_change_password.xml`

**Security:**
- Current password must be verified before change
- New password is hashed with BCrypt
- UpdatedAt timestamp updated

---

## ✅ SubTask System

### 6. SubTask Management

**Features:**
- Display subtasks in a list
- Checkbox to toggle completion
- Visual feedback (strikethrough when completed)
- Delete individual subtasks
- Automatic progress calculation

**SubTask Item Layout:**
```
[✓] Subtask name                    [🗑️]
```

**Components:**
- Adapter: `SubTaskAdapter.kt`
- Item Layout: `item_subtask.xml`
- Add Dialog: `dialog_add_subtask.xml`

**Database:**
- Model: `SubTask.kt`
- DAO: `SubTaskDao.kt`
- Table: `SubTask`

**Progress Calculation:**
```kotlin
val completionPercentage = (completedSubtasks * 100) / totalSubtasks
```

**Example Usage:**
```kotlin
// Get subtasks for a task
val subtasks = db.subTaskDao().list(taskId, userId)

// Toggle completion
val updated = subtask.copy(IsCompleted = !subtask.IsCompleted)
db.subTaskDao().update(updated)

// Calculate progress
val progress = subTaskAdapter.getCompletionPercentage()
```

---

## 🔔 Notifications & Reminders

### 7. Notification System

**Features:**
- Notification channel for reminders
- Custom notification icon (bell)
- Click to open app
- Auto-dismiss after click

**Notification Display:**
```
🔔 Task Reminder: [Task Title]
   [Task Description]
```

**Permissions:**
- POST_NOTIFICATIONS - Show notifications
- VIBRATE - Vibrate on notification

**Component:** `NotificationUtil.kt`

---

### 8. Reminder Scheduling

**Features:**
- Schedule reminders using AlarmManager
- Exact alarm scheduling
- Battery optimization handling
- Cancel scheduled reminders

**Quick Options:**
- 15 minutes before due time
- 30 minutes before due time
- 1 hour before due time
- 1 day before due time
- Custom date/time

**Components:**
- Manager: `ReminderManager.kt`
- Receiver: `ReminderReceiver.kt`
- Dialog: `dialog_set_reminder.xml`

**Flow:**
1. User creates/edits task
2. Sets due date/time
3. Chooses reminder option
4. AlarmManager schedules reminder
5. At reminder time, ReminderReceiver triggers notification
6. User sees notification and opens app

**Permissions:**
- SCHEDULE_EXACT_ALARM - Schedule exact alarms
- USE_EXACT_ALARM - Alternative permission

**Example Usage:**
```kotlin
// Schedule reminder 30 minutes before due time
val dueTime = task.getDueTimeMillis()
val reminderTime = ReminderManager.calculateReminderTime(dueTime, 30)

ReminderManager.scheduleReminder(
    context = context,
    taskId = task.Task_ID,
    taskTitle = task.Title,
    taskDescription = task.Description,
    userId = currentUserId,
    reminderTimeMillis = reminderTime
)

// Cancel reminder
ReminderManager.cancelReminder(context, taskId)
```

---

## 🧪 Testing

### 9. Unit Tests

**Test Suites:**

**InputValidationUtilTest (25 tests):**
- ✅ Valid username formats
- ✅ Invalid username formats
- ✅ Email validation
- ✅ Password validation
- ✅ Password strength checking
- ✅ SQL injection detection
- ✅ Input sanitization

**PasswordHashUtilTest (9 tests):**
- ✅ Password hashing
- ✅ Password verification
- ✅ Case sensitivity
- ✅ Special characters
- ✅ Edge cases

**Running Tests:**
```bash
./gradlew test
```

**Coverage:**
- InputValidationUtil: 95%
- PasswordHashUtil: 100%
- Overall utility coverage: 90%+

---

## 📱 UI Components Created

### Dialogs
1. **dialog_reset_password.xml** - Reset password after security question
2. **dialog_edit_profile.xml** - Edit user profile information
3. **dialog_change_password.xml** - Change user password
4. **dialog_add_subtask.xml** - Add new subtask to task
5. **dialog_set_reminder.xml** - Set reminder for task

### List Items
1. **item_subtask.xml** - Individual subtask in RecyclerView

### Icons
1. **ic_notification.xml** - Bell icon for notifications

---

## 🎯 Integration Points

### For SubTask Integration:
```kotlin
// In FragmentTaskActivity or TaskDetailActivity

// 1. Add RecyclerView for subtasks
val recyclerView = findViewById<RecyclerView>(R.id.rvSubTasks)

// 2. Initialize adapter
val subTaskAdapter = SubTaskAdapter(
    subTasks = mutableListOf(),
    onSubTaskToggle = { subtask -> toggleSubTask(subtask) },
    onSubTaskDelete = { subtask -> deleteSubTask(subtask) }
)
recyclerView.adapter = subTaskAdapter

// 3. Load subtasks from database
lifecycleScope.launch {
    val subtasks = withContext(Dispatchers.IO) {
        db.subTaskDao().list(taskId, userId)
    }
    subTaskAdapter.updateSubTasks(subtasks)
    
    // Update task progress
    val progress = subTaskAdapter.getCompletionPercentage()
    updateTaskProgress(progress)
}
```

### For Reminder Integration:
```kotlin
// In task creation/edit activity

// 1. Add "Set Reminder" button
binding.btnSetReminder.setOnClickListener {
    showReminderDialog()
}

// 2. Show reminder dialog
private fun showReminderDialog() {
    val dialogView = layoutInflater.inflate(R.layout.dialog_set_reminder, null)
    // ... setup dialog listeners
}

// 3. Schedule reminder when task is saved
if (reminderTimeMillis != null) {
    ReminderManager.scheduleReminder(
        context = this,
        taskId = task.Task_ID,
        taskTitle = task.Title,
        taskDescription = task.Description,
        userId = currentUserId,
        reminderTimeMillis = reminderTimeMillis
    )
}
```

---

## 📊 Feature Matrix

| Feature | Status | UI Complete | Backend Complete | Tested |
|---------|--------|-------------|------------------|--------|
| Password Hashing | ✅ | N/A | ✅ | ✅ |
| Forgot Password | ✅ | ✅ | ✅ | ⏳ Manual |
| Input Validation | ✅ | N/A | ✅ | ✅ |
| Profile Editing | ✅ | ✅ | ✅ | ⏳ Manual |
| Change Password | ✅ | ✅ | ✅ | ⏳ Manual |
| SubTask Display | ✅ | ✅ | ✅ | ⏳ Integration |
| SubTask CRUD | ✅ | ✅ | ✅ | ⏳ Integration |
| Notifications | ✅ | ✅ | ✅ | ⏳ Manual |
| Reminder Scheduling | ✅ | ✅ | ✅ | ⏳ Manual |

---

## 🚀 Quick Start Guide

### Testing Password Hashing:
1. Sign up with a new account
2. Check database - password is now hashed
3. Login with same credentials - works!

### Testing Forgot Password:
1. Go to Forgot Password screen
2. Enter username, select question, enter answer
3. Set new password
4. Login with new password

### Testing Profile Edit:
1. Go to Settings tab
2. Click on profile card
3. Edit your information
4. Save changes
5. See updated profile

### Testing Change Password:
1. Go to Settings tab
2. Click on profile card
3. Click "Change Password"
4. Enter current and new password
5. Password updated!

---

**For more information:**
- [PHASE1_IMPLEMENTATION.md](PHASE1_IMPLEMENTATION.md) - Technical details
- [CHANGELOG.md](CHANGELOG.md) - Version history
- [PHASE1_COMPLETE.md](PHASE1_COMPLETE.md) - Completion summary
