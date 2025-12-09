# How to Use Icon Spinners in Other Dialogs

This guide shows how to use the icon spinner infrastructure in other parts of the app.

## Quick Start

### 1. Add Spinners to Your XML Layout

```xml
<!-- In your_dialog.xml -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">
    
    <!-- Category Spinner -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Category"
        android:textStyle="bold" />
    
    <Spinner
        android:id="@+id/spinnerCategory"
        android:layout_width="match_parent"
        android:layout_height="48dp" />
    
    <!-- Priority Spinner -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Priority"
        android:textStyle="bold" />
    
    <Spinner
        android:id="@+id/spinnerPriority"
        android:layout_width="match_parent"
        android:layout_height="48dp" />
        
    <!-- Add more spinners as needed -->
</LinearLayout>
```

### 2. Set Up Spinners in Your Activity/Fragment

```kotlin
import android.widget.Spinner
import com.example.cheermateapp.util.TaskDialogSpinnerHelper

class YourActivity : AppCompatActivity() {
    
    private fun showYourDialog() {
        val dialogView = layoutInflater.inflate(R.layout.your_dialog, null)
        
        // Get spinner references
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val spinnerReminder = dialogView.findViewById<Spinner>(R.id.spinnerReminder)
        
        // Set up spinners with icons (one line each!)
        TaskDialogSpinnerHelper.setupCategorySpinner(this, spinnerCategory)
        TaskDialogSpinnerHelper.setupPrioritySpinner(this, spinnerPriority)
        TaskDialogSpinnerHelper.setupReminderSpinner(this, spinnerReminder)
        
        // Create and show dialog
        AlertDialog.Builder(this)
            .setTitle("Your Dialog")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                // Get selected values
                val category = TaskDialogSpinnerHelper.getSelectedCategory(spinnerCategory)
                val priority = TaskDialogSpinnerHelper.getSelectedPriority(spinnerPriority)
                val reminder = TaskDialogSpinnerHelper.getSelectedReminder(spinnerReminder)
                
                // Use the values
                saveData(category, priority, reminder)
            }
            .show()
    }
}
```

### 3. Get Selected Values

```kotlin
// When user clicks Save/OK button
val selectedCategory = TaskDialogSpinnerHelper.getSelectedCategory(spinnerCategory)
// Returns: "Work", "Personal", "Shopping", or "Others"

val selectedPriority = TaskDialogSpinnerHelper.getSelectedPriority(spinnerPriority)
// Returns: "Low", "Medium", or "High"

val selectedReminder = TaskDialogSpinnerHelper.getSelectedReminder(spinnerReminder)
// Returns: "None", "10 minutes before", "30 minutes before", or "At specific time"
```

## Creating Custom Icon Spinners

If you need a spinner with different options, use `IconSpinnerAdapter` directly:

```kotlin
import com.example.cheermateapp.IconSpinnerAdapter

// Define your custom items
val statusItems = listOf(
    IconSpinnerAdapter.SpinnerItem("✅", "Complete"),
    IconSpinnerAdapter.SpinnerItem("⏳", "In Progress"),
    IconSpinnerAdapter.SpinnerItem("🔴", "Blocked"),
    IconSpinnerAdapter.SpinnerItem("📋", "Todo")
)

// Create adapter and set it
val adapter = IconSpinnerAdapter(context, statusItems)
spinnerStatus.adapter = adapter
spinnerStatus.setSelection(0) // Default to first item

// Get selected value
val selectedItem = spinnerStatus.selectedItem as IconSpinnerAdapter.SpinnerItem
val selectedText = selectedItem.text  // e.g., "Complete"
val selectedIcon = selectedItem.icon  // e.g., "✅"
```

## Available Helper Methods

### Setup Methods
```kotlin
TaskDialogSpinnerHelper.setupCategorySpinner(context: Context, spinner: Spinner)
TaskDialogSpinnerHelper.setupPrioritySpinner(context: Context, spinner: Spinner)
TaskDialogSpinnerHelper.setupReminderSpinner(context: Context, spinner: Spinner)
```

### Getter Methods
```kotlin
TaskDialogSpinnerHelper.getSelectedCategory(spinner: Spinner): String
TaskDialogSpinnerHelper.getSelectedPriority(spinner: Spinner): String
TaskDialogSpinnerHelper.getSelectedReminder(spinner: Spinner): String
```

## Pre-configured Options

### Category Spinner
| Icon | Label | Use Case |
|------|-------|----------|
| 💼 | Work | Professional tasks, meetings, deadlines |
| 👤 | Personal | Personal errands, appointments |
| 🛒 | Shopping | Shopping lists, purchases |
| 📋 | Others | Miscellaneous tasks |

**Default**: 💼 Work

### Priority Spinner
| Icon | Label | Urgency | Color |
|------|-------|---------|-------|
| 🟢 | Low | Can wait | Green |
| 🟡 | Medium | Important but not urgent | Yellow |
| 🔴 | High | Critical, urgent | Red |

**Default**: 🟡 Medium

### Reminder Spinner
| Icon | Label | Description |
|------|-------|-------------|
| 🔕 | None | No reminder |
| ⏰ | 10 minutes before | Alert 10 min before due time |
| ⏰ | 30 minutes before | Alert 30 min before due time |
| 🕐 | At specific time | Custom time reminder |

**Default**: 🔕 None

## Best Practices

### 1. Always Use Labels
```xml
<!-- Good: Label + Spinner -->
<TextView
    android:text="Category"
    android:textStyle="bold" />
<Spinner
    android:id="@+id/spinnerCategory" />
```

### 2. Provide Adequate Height
```xml
<!-- Spinners need at least 48dp height for touch targets -->
<Spinner
    android:layout_height="48dp" />
```

### 3. Handle Null Cases
```kotlin
// Safe way to get selected value
val category = TaskDialogSpinnerHelper.getSelectedCategory(spinnerCategory)
if (category.isNotEmpty()) {
    // Use category
}
```

### 4. Set Default Selections
```kotlin
// Set defaults after setting up the spinner
TaskDialogSpinnerHelper.setupPrioritySpinner(this, spinnerPriority)
// Default is already set to Medium (index 1)

// To change default:
spinnerPriority.setSelection(2) // Set to High
```

## Full Example: Edit Task Dialog

```kotlin
class EditTaskDialog(private val task: Task) {
    
    fun show(activity: AppCompatActivity) {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_edit_task, null)
        
        // Get views
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        
        // Set up spinners
        TaskDialogSpinnerHelper.setupCategorySpinner(activity, spinnerCategory)
        TaskDialogSpinnerHelper.setupPrioritySpinner(activity, spinnerPriority)
        
        // Pre-fill with existing task data
        etTitle.setText(task.title)
        
        // Set spinner to current task values
        when (task.category) {
            Category.Work -> spinnerCategory.setSelection(0)
            Category.Personal -> spinnerCategory.setSelection(1)
            Category.Shopping -> spinnerCategory.setSelection(2)
            Category.Others -> spinnerCategory.setSelection(3)
        }
        
        when (task.priority) {
            Priority.Low -> spinnerPriority.setSelection(0)
            Priority.Medium -> spinnerPriority.setSelection(1)
            Priority.High -> spinnerPriority.setSelection(2)
        }
        
        // Show dialog
        AlertDialog.Builder(activity)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                task.title = etTitle.text.toString()
                task.category = mapCategory(
                    TaskDialogSpinnerHelper.getSelectedCategory(spinnerCategory)
                )
                task.priority = mapPriority(
                    TaskDialogSpinnerHelper.getSelectedPriority(spinnerPriority)
                )
                // Save task to database
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun mapCategory(categoryStr: String): Category {
        return when (categoryStr) {
            "Work" -> Category.Work
            "Personal" -> Category.Personal
            "Shopping" -> Category.Shopping
            "Others" -> Category.Others
            else -> Category.Work
        }
    }
    
    private fun mapPriority(priorityStr: String): Priority {
        return when (priorityStr) {
            "High" -> Priority.High
            "Medium" -> Priority.Medium
            "Low" -> Priority.Low
            else -> Priority.Medium
        }
    }
}
```

## Troubleshooting

### Spinners Not Showing Icons
- ✅ Verify `item_spinner_with_icon.xml` exists in `res/layout/`
- ✅ Check that `IconSpinnerAdapter` is in the correct package
- ✅ Ensure you're calling the helper setup methods

### Wrong Default Selection
```kotlin
// After setup, explicitly set the selection
TaskDialogSpinnerHelper.setupPrioritySpinner(this, spinnerPriority)
spinnerPriority.setSelection(0) // Override to Low
```

### Custom Icons Not Working
- Use emoji characters that are widely supported
- Test on different Android versions
- Consider using drawable resources for complex icons

## Extending the Helper

To add more spinner types to `TaskDialogSpinnerHelper.kt`:

```kotlin
object TaskDialogSpinnerHelper {
    
    // Add new setup method
    fun setupStatusSpinner(context: Context, spinner: Spinner) {
        val statusItems = listOf(
            IconSpinnerAdapter.SpinnerItem("✅", "Complete"),
            IconSpinnerAdapter.SpinnerItem("⏳", "In Progress"),
            IconSpinnerAdapter.SpinnerItem("📋", "Todo")
        )
        
        val adapter = IconSpinnerAdapter(context, statusItems)
        spinner.adapter = adapter
        spinner.setSelection(2) // Default to Todo
    }
    
    // Add new getter method
    fun getSelectedStatus(spinner: Spinner): String {
        val item = spinner.selectedItem as? IconSpinnerAdapter.SpinnerItem
        return item?.text ?: "Todo"
    }
}
```

## Summary

The icon spinner infrastructure provides:
- ✅ One-line spinner setup
- ✅ Visual icons for better UX
- ✅ Consistent styling
- ✅ Easy to extend
- ✅ Reusable across the app

Just inflate your XML, call the helper method, and you're done!
