package com.cheermateapp.examples

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cheermateapp.R
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.Priority
import com.cheermateapp.data.model.Status
import com.cheermateapp.data.repository.UiState
import com.cheermateapp.ui.viewmodel.TaskViewModel
import com.cheermateapp.util.observeOperationState
import com.cheermateapp.util.observeUiState
import kotlinx.coroutines.launch

/**
 * Example Activity demonstrating proper ViewModel usage with realtime updates
 * 
 * This example shows:
 * - Proper ViewModel initialization
 * - StateFlow observation with lifecycle awareness
 * - Automatic UI updates via Flow
 * - Progress indicator integration
 * - Error handling with Toast/Snackbar
 * - Clean architecture patterns
 * 
 * USE THIS AS A REFERENCE FOR MIGRATING OTHER ACTIVITIES!
 */
class ProperViewModelUsageExampleActivity : AppCompatActivity() {

    // ==================== VIEWMODEL INITIALIZATION ====================
    // Using 'by viewModels()' for automatic ViewModel creation
    private val taskViewModel: TaskViewModel by viewModels()
    
    // ==================== UI COMPONENTS ====================
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: TextView
    private lateinit var btnAddTask: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    
    // Sample adapter (replace with your actual adapter)
    private lateinit var taskAdapter: SimpleTaskAdapter
    
    private var userId: Int = 1 // Would come from intent in real app

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Note: You would need to create this layout file
        // setContentView(R.layout.activity_example_proper_viewmodel)
        
        // Initialize views
        initializeViews()
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup observers BEFORE loading data
        setupObservers()
        
        // Setup click listeners
        setupClickListeners()
        
        // Start loading data - this triggers Flow collection
        loadInitialData()
    }

    // ==================== INITIALIZATION ====================
    
    private fun initializeViews() {
        // Initialize all views here
        // progressBar = findViewById(R.id.progressBar)
        // recyclerView = findViewById(R.id.recyclerView)
        // emptyStateView = findViewById(R.id.emptyStateView)
        // btnAddTask = findViewById(R.id.btnAddTask)
        // swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
    }
    
    private fun setupRecyclerView() {
        taskAdapter = SimpleTaskAdapter(
            onTaskClick = { task ->
                // Handle task click
                navigateToTaskDetail(task)
            },
            onTaskLongClick = { task ->
                // Handle task long click (e.g., show options)
                showTaskOptions(task)
            },
            onCompleteClick = { task ->
                // Mark task as completed
                taskViewModel.markTaskCompleted(userId, task.Task_ID)
            }
        )
        
        // recyclerView.apply {
        //     layoutManager = LinearLayoutManager(this@ProperViewModelUsageExampleActivity)
        //     adapter = taskAdapter
        // }
    }

    // ==================== OBSERVERS (CRITICAL!) ====================
    
    private fun setupObservers() {
        // Option 1: Using helper extension (RECOMMENDED)
        observeUiState(
            stateFlow = taskViewModel.allTasksState,
            progressBar = progressBar,
            onLoading = {
                // Optional: Additional loading logic
                emptyStateView.visibility = View.GONE
            },
            onSuccess = { tasks ->
                // Update UI with tasks
                displayTasks(tasks)
            },
            onError = { message ->
                // Error already shown by extension, but you can add custom logic
                emptyStateView.visibility = View.VISIBLE
                emptyStateView.text = "Error: $message"
            }
        )
        
        // Option 2: Manual observation (for more control)
        observeTaskOperations()
        
        // Option 3: Direct Flow collection (most flexible)
        observeTasksManually()
    }
    
    /**
     * Observe operation results (insert, update, delete)
     */
    private fun observeTaskOperations() {
        observeOperationState(
            stateFlow = taskViewModel.taskOperationState,
            onSuccess = { message ->
                // Success message already shown via Toast
                // Reset state after showing message
                taskViewModel.resetOperationState()
            }
        )
    }
    
    /**
     * Manual Flow observation (most control over behavior)
     */
    private fun observeTasksManually() {
        lifecycleScope.launch {
            taskViewModel.allTasksState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        // Initial state, do nothing
                    }
                    is UiState.Loading -> {
                        // Show loading indicator
                        progressBar.visibility = View.VISIBLE
                        swipeRefreshLayout.isRefreshing = true
                    }
                    is UiState.Success -> {
                        // Hide loading, show data
                        progressBar.visibility = View.GONE
                        swipeRefreshLayout.isRefreshing = false
                        
                        // Update UI - THIS HAPPENS AUTOMATICALLY ON ANY DB CHANGE!
                        displayTasks(state.data)
                    }
                    is UiState.Error -> {
                        // Hide loading, show error
                        progressBar.visibility = View.GONE
                        swipeRefreshLayout.isRefreshing = false
                        
                        // Show error message
                        Toast.makeText(
                            this@ProperViewModelUsageExampleActivity,
                            state.message,
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Show empty state
                        emptyStateView.visibility = View.VISIBLE
                        emptyStateView.text = "Failed to load tasks"
                    }
                }
            }
        }
    }

    // ==================== CLICK LISTENERS ====================
    
    private fun setupClickListeners() {
        btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }
        
        swipeRefreshLayout.setOnRefreshListener {
            // Refresh is automatic with Flow!
            // But we can manually trigger reload if needed
            taskViewModel.loadAllTasks(userId)
        }
    }

    // ==================== DATA LOADING ====================
    
    private fun loadInitialData() {
        // Load all tasks - Flow will automatically update UI on changes
        taskViewModel.loadAllTasks(userId)
    }

    // ==================== UI UPDATES ====================
    
    private fun displayTasks(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
            emptyStateView.text = "No tasks yet. Tap + to add one!"
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
            
            // Update adapter
            taskAdapter.submitList(tasks)
        }
    }

    // ==================== TASK OPERATIONS ====================
    
    private fun showAddTaskDialog() {
        // Create sample task
        val newTask = Task(
            Task_ID = 0, // Auto-generated
            User_ID = userId,
            Title = "Sample Task",
            Description = "Task created via ViewModel",
            Priority = Priority.Medium,
            Status = Status.Pending,
            CreatedAt = System.currentTimeMillis()
        )
        
        // Insert via ViewModel - UI will auto-update via Flow!
        taskViewModel.insertTask(newTask)
        
        // That's it! No manual refresh needed!
        // The Flow observer will automatically receive the new task
        // and update the UI
    }
    
    private fun navigateToTaskDetail(task: Task) {
        // Navigate to task detail screen
        // Pass task ID, not the entire task object
        // The detail screen will also observe via Flow for realtime updates
    }
    
    private fun showTaskOptions(task: Task) {
        // Show bottom sheet or dialog with options:
        // - Edit
        // - Delete
        // - Mark as complete
        // - Set reminder
        
        // Example: Delete task
        // taskViewModel.deleteTask(userId, task.Task_ID)
        // UI will automatically update via Flow!
    }

    // ==================== ADAPTER EXAMPLE ====================
    
    /**
     * Simple adapter example
     * Replace with your actual adapter implementation
     */
    private class SimpleTaskAdapter(
        private val onTaskClick: (Task) -> Unit,
        private val onTaskLongClick: (Task) -> Unit,
        private val onCompleteClick: (Task) -> Unit
    ) : RecyclerView.Adapter<SimpleTaskAdapter.ViewHolder>() {
        
        private var tasks: List<Task> = emptyList()
        
        fun submitList(newTasks: List<Task>) {
            tasks = newTasks
            notifyDataSetChanged() // In production, use DiffUtil for better performance
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            // Create view holder
            // val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
            // return ViewHolder(view)
            TODO("Implement view holder creation")
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val task = tasks[position]
            holder.bind(task)
        }
        
        override fun getItemCount(): Int = tasks.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(task: Task) {
                // Bind task data to views
                itemView.setOnClickListener { onTaskClick(task) }
                itemView.setOnLongClickListener { 
                    onTaskLongClick(task)
                    true 
                }
                // Bind complete button
                // completeButton.setOnClickListener { onCompleteClick(task) }
            }
        }
    }
}

// ==================== KEY TAKEAWAYS ====================
/*
1. NEVER access DAOs directly from Activities
2. ALWAYS use ViewModels for data operations
3. Observe StateFlow in lifecycleScope
4. Flow automatically updates UI on database changes
5. Use UiState for loading/success/error states
6. Progress indicators controlled by UiState.Loading
7. Error handling via UiState.Error with Toast/Snackbar
8. No manual refresh needed - Flow handles it!
9. Single source of truth: ViewModel StateFlow
10. Lifecycle-aware data observation

ANTI-PATTERNS TO AVOID:
❌ Direct DAO access from Activities
❌ Manual UI refresh after CRUD operations
❌ Not handling loading/error states
❌ Blocking main thread with database operations
❌ Not using Dispatchers.IO for database operations
❌ Missing try/catch around database operations
❌ Hardcoded strings for error messages
❌ Not resetting operation state after showing messages

FOLLOW THIS PATTERN FOR ALL ACTIVITIES!
*/
