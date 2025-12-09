# Task Dialog Revamp - Implementation Guide

## Overview
The `dialog_add_task.xml` layout has been revamped to include icons beside Category, Priority, and Reminder options, providing a more intuitive and visually appealing user interface.

## Changes Made

### 1. XML Layout Updates (`dialog_add_task.xml`)

#### Added Category Selection
```xml
<!-- Category Selection -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    android:text="Category"
    android:textSize="16sp"
    android:textStyle="bold" />

<Spinner
    android:id="@+id/spinnerCategory"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:layout_marginBottom="16dp" />
```

**Categories with Icons:**
- 💼 Work
- 👤 Personal
- 🛒 Shopping
- 📋 Others

#### Priority Selection (Updated)
The Priority spinner now displays icons alongside priority levels:
- 🔴 High
- 🟡 Medium
- 🟢 Low

#### Added Reminder Selection
```xml
<!-- Reminder Selection -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="16dp"
    android:layout_marginBottom="8dp"
    android:text="Reminder"
    android:textSize="16sp"
    android:textStyle="bold" />

<Spinner
    android:id="@+id/spinnerReminder"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:layout_marginBottom="16dp" />
```

**Reminder Options with Icons:**
- 🔕 None
- ⏰ 10 minutes before
- ⏰ 30 minutes before
- 🕐 At specific time

### 2. New Files Created

#### `item_spinner_with_icon.xml`
A reusable spinner item layout that displays an icon (emoji) alongside text.

#### `IconSpinnerAdapter.kt`
Custom adapter class that handles spinner items with icons:
```kotlin
class IconSpinnerAdapter(
    context: Context,
    private val items: List<SpinnerItem>
) : ArrayAdapter<SpinnerItem>

data class SpinnerItem(
    val icon: String,  // Emoji or unicode character
    val text: String
)
```

#### `TaskDialogSpinnerHelper.kt`
Utility class that simplifies setting up the spinners with proper icons and options.

## Usage Example

### Option 1: Using the XML Layout Directly

```kotlin
// Inflate the dialog layout
val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)

// Get references to spinners
val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
val spinnerReminder = dialogView.findViewById<Spinner>(R.id.spinnerReminder)

// Set up spinners with icons using the helper
TaskDialogSpinnerHelper.setupCategorySpinner(this, spinnerCategory)
TaskDialogSpinnerHelper.setupPrioritySpinner(this, spinnerPriority)
TaskDialogSpinnerHelper.setupReminderSpinner(this, spinnerReminder)

// Get other input fields
val etTaskTitle = dialogView.findViewById<TextInputEditText>(R.id.etTaskTitle)
val etTaskDescription = dialogView.findViewById<TextInputEditText>(R.id.etTaskDescription)
val etDueDate = dialogView.findViewById<TextInputEditText>(R.id.etDueDate)
val etDueTime = dialogView.findViewById<TextInputEditText>(R.id.etDueTime)

// Create and show dialog
AlertDialog.Builder(this)
    .setTitle("Add New Task")
    .setView(dialogView)
    .setPositiveButton("Create") { _, _ ->
        val title = etTaskTitle.text.toString()
        val description = etTaskDescription.text.toString()
        val category = TaskDialogSpinnerHelper.getSelectedCategory(spinnerCategory)
        val priority = TaskDialogSpinnerHelper.getSelectedPriority(spinnerPriority)
        val reminder = TaskDialogSpinnerHelper.getSelectedReminder(spinnerReminder)
        val dueDate = etDueDate.text.toString()
        val dueTime = etDueTime.text.toString()
        
        // Create task with these values
        createTask(title, description, category, priority, reminder, dueDate, dueTime)
    }
    .setNegativeButton("Cancel", null)
    .show()
```

### Option 2: Using IconSpinnerAdapter Directly

```kotlin
// Create custom category items
val categoryItems = listOf(
    IconSpinnerAdapter.SpinnerItem("💼", "Work"),
    IconSpinnerAdapter.SpinnerItem("👤", "Personal"),
    IconSpinnerAdapter.SpinnerItem("🛒", "Shopping"),
    IconSpinnerAdapter.SpinnerItem("📋", "Others")
)

// Set up spinner
val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
val adapter = IconSpinnerAdapter(this, categoryItems)
spinnerCategory.adapter = adapter
spinnerCategory.setSelection(0) // Default to Work

// Get selected value
val selectedItem = spinnerCategory.selectedItem as IconSpinnerAdapter.SpinnerItem
val selectedText = selectedItem.text // "Work", "Personal", etc.
val selectedIcon = selectedItem.icon // "💼", "👤", etc.
```

## Integration with Existing Code

### MainActivity.kt
The existing `showQuickAddTaskDialog()` method creates the dialog programmatically. To use the new XML layout:

```kotlin
private fun showQuickAddTaskDialog() {
    // Instead of creating views programmatically, use the XML layout
    val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
    
    // Set up spinners
    val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
    val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
    val spinnerReminder = dialogView.findViewById<Spinner>(R.id.spinnerReminder)
    
    TaskDialogSpinnerHelper.setupCategorySpinner(this, spinnerCategory)
    TaskDialogSpinnerHelper.setupPrioritySpinner(this, spinnerPriority)
    TaskDialogSpinnerHelper.setupReminderSpinner(this, spinnerReminder)
    
    // Set up date/time pickers
    val etDueDate = dialogView.findViewById<TextInputEditText>(R.id.etDueDate)
    val etDueTime = dialogView.findViewById<TextInputEditText>(R.id.etDueTime)
    
    etDueDate.setOnClickListener { showDatePicker(etDueDate) }
    etDueTime.setOnClickListener { showTimePicker(etDueTime) }
    
    // Rest of the dialog setup...
}
```

### FragmentTaskActivity.kt
Similarly, update the `showAddTaskDialog()` method to use the XML layout instead of programmatic creation.

## Visual Preview

The updated dialog will look like this:

```
┌─────────────────────────────────────┐
│ Add New Task                     ×  │
├─────────────────────────────────────┤
│                                     │
│ Task Title *                        │
│ [___________________________]       │
│                                     │
│ Description (Optional)              │
│ [___________________________]       │
│ [___________________________]       │
│                                     │
│ Category                            │
│ [💼 Work                    ▼]     │
│                                     │
│ Priority                            │
│ [🟡 Medium                  ▼]     │
│                                     │
│ Due Date *        Due Time          │
│ [_______]         [_______]         │
│                                     │
│ Reminder                            │
│ [🔕 None                    ▼]     │
│                                     │
│ * Required fields                   │
│ Tap date/time fields to select      │
│                                     │
│           [Cancel]  [Create]        │
└─────────────────────────────────────┘
```

## Benefits

1. **Enhanced UX**: Visual icons help users quickly identify options
2. **Consistency**: All spinners follow the same icon + text pattern
3. **Maintainability**: Centralized helper class for spinner setup
4. **Reusability**: IconSpinnerAdapter can be used for other spinners
5. **Accessibility**: Text labels still provide full information

## Notes

- Icons use Unicode emojis for maximum compatibility
- The layout maintains Material Design guidelines
- All spinners default to sensible values (Work, Medium, None)
- The helper class provides easy methods to retrieve selected values
