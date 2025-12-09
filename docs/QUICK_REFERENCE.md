# Quick Reference Guide: Proper DAO/Repository/ViewModel Usage

## ✅ DO's and ❌ DON'Ts

### Architecture

#### ❌ DON'T: Access DAOs directly from Activities
```kotlin
class MyActivity : AppCompatActivity() {
    private fun loadTasks() {
        lifecycleScope.launch {
            val db = AppDb.get(this)
            val tasks = db.taskDao().getAllTasks() // ❌ WRONG!
        }
    }
}
```

#### ✅ DO: Use ViewModels and Repositories
```kotlin
class MyActivity : AppCompatActivity() {
    private val viewModel: TaskViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadAllTasks(userId)
        observeUiState()
    }
}
```

---

### Data Loading

#### ❌ DON'T: Manual refresh after operations
```kotlin
lifecycleScope.launch {
    taskDao.insert(task) // ❌
    loadTasks() // Manual refresh - WRONG!
}
```

#### ✅ DO: Let Flow handle updates automatically
```kotlin
// In ViewModel
taskRepository.insertTask(task) // Automatically triggers Flow

// In Activity - Flow observer auto-updates UI
viewModel.tasksFlow.collect { tasks ->
    adapter.submitList(tasks) // Updates automatically!
}
```

---

### Error Handling

#### ❌ DON'T: Bare try/catch without proper handling
```kotlin
try {
    taskDao.insert(task) // ❌ No error handling
} catch (e: Exception) {
    // Silent failure or generic message
}
```

#### ✅ DO: Use DataResult with proper error messages
```kotlin
suspend fun insertTask(task: Task): DataResult<Long> {
    return try {
        val id = taskDao.insert(task)
        DataResult.Success(id)
    } catch (e: Exception) {
        Log.e(TAG, "Error inserting task", e)
        DataResult.Error(e, "Failed to create task: ${e.message}")
    }
}
```

---

### Loading States

#### ❌ DON'T: Manual loading indicator management
```kotlin
progressBar.visibility = View.VISIBLE // ❌
loadData()
progressBar.visibility = View.GONE // ❌ Might not execute if error
```

#### ✅ DO: Use UiState for loading indicators
```kotlin
observeUiState(
    stateFlow = viewModel.tasksState,
    progressBar = progressBar,
    onSuccess = { tasks -> displayTasks(tasks) }
)
// Progress bar automatically shown/hidden by UiState!
```

---

### Threading

#### ❌ DON'T: Block main thread with database operations
```kotlin
val tasks = db.taskDao().getAllTasks() // ❌ Blocks UI!
```

#### ✅ DO: Use proper coroutine dispatchers
```kotlin
// In Repository
suspend fun getAllTasks(): DataResult<List<Task>> = withContext(Dispatchers.IO) {
    taskDao.getAllTasks() // ✅ Runs on IO thread
}

// In ViewModel
viewModelScope.launch { // ✅ Runs on Main thread
    val result = repository.getAllTasks() // Repository handles IO
}
```

---

### Batch Operations

#### ❌ DON'T: Loop without transaction
```kotlin
tasks.forEach { task ->
    taskDao.insert(task) // ❌ Multiple transactions, slow!
}
```

#### ✅ DO: Use batch operations with @Transaction
```kotlin
@Transaction
@Insert
suspend fun insertAll(tasks: List<Task>)

// Usage
taskDao.insertAll(tasks) // ✅ Single transaction, fast!
```

---

## Common Patterns

### 1. Load and Display Data

```kotlin
class MyActivity : AppCompatActivity() {
    private val viewModel: TaskViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_layout)
        
        // Setup observer
        observeUiState(
            stateFlow = viewModel.allTasksState,
            progressBar = findViewById(R.id.progressBar),
            onSuccess = { tasks ->
                adapter.submitList(tasks)
            }
        )
        
        // Load data
        viewModel.loadAllTasks(userId)
    }
}
```

### 2. Insert Data

```kotlin
// In Activity
btnAddTask.setOnClickListener {
    val task = createNewTask()
    viewModel.insertTask(task)
}

// Observe operation result
observeOperationState(
    stateFlow = viewModel.taskOperationState,
    onSuccess = { message ->
        // Success toast already shown
        viewModel.resetOperationState()
    }
)
```

### 3. Update Data

```kotlin
// In Activity
btnSave.setOnClickListener {
    val updatedTask = task.copy(
        Title = etTitle.text.toString(),
        UpdatedAt = System.currentTimeMillis()
    )
    viewModel.updateTask(updatedTask)
}
// UI auto-updates via Flow!
```

### 4. Delete Data

```kotlin
// In Activity
btnDelete.setOnClickListener {
    AlertDialog.Builder(this)
        .setTitle("Delete Task")
        .setMessage("Are you sure?")
        .setPositiveButton("Delete") { _, _ ->
            viewModel.deleteTask(userId, taskId)
        }
        .show()
}
// UI auto-updates via Flow!
```

### 5. Search/Filter

```kotlin
// In Activity
searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
    override fun onQueryTextChange(query: String): Boolean {
        viewModel.searchTasks(userId, query) { state ->
            when (state) {
                is UiState.Success -> adapter.submitList(state.data)
                is UiState.Error -> showError(state.message)
                else -> {}
            }
        }
        return true
    }
    
    override fun onQueryTextSubmit(query: String) = true
})
```

---

## State Flow Collection Patterns

### Pattern 1: Using Helper Extension (Recommended for beginners)
```kotlin
observeUiState(
    stateFlow = viewModel.tasksState,
    progressBar = progressBar,
    onSuccess = { tasks -> displayTasks(tasks) }
)
```

### Pattern 2: Manual Collection (More control)
```kotlin
lifecycleScope.launch {
    viewModel.tasksState.collect { state ->
        when (state) {
            is UiState.Loading -> showLoading()
            is UiState.Success -> showData(state.data)
            is UiState.Error -> showError(state.message)
            is UiState.Idle -> {}
        }
    }
}
```

### Pattern 3: With Lifecycle Awareness (Best practice)
```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.tasksState.collect { state ->
            handleState(state)
        }
    }
}
```

---

## Testing Quick Reference

### Repository Test
```kotlin
@Test
fun `insertTask returns Success when DAO succeeds`() = runTest {
    `when`(taskDao.insert(task)).thenReturn(1L)
    
    val result = repository.insertTask(task)
    
    assertTrue(result is DataResult.Success)
}
```

### ViewModel Test
```kotlin
@Test
fun `loadTasks updates state to Success with data`() = runTest {
    val tasks = listOf(sampleTask)
    `when`(repository.getAllTasksFlow(1)).thenReturn(flowOf(tasks))
    
    viewModel.loadAllTasks(1)
    advanceUntilIdle()
    
    assertEquals(UiState.Success(tasks), viewModel.allTasksState.value)
}
```

---

## Checklist for New Screens

When creating a new screen that displays data:

- [ ] Create or use existing ViewModel
- [ ] Setup StateFlow observers in onCreate()
- [ ] Use helper extensions or manual Flow collection
- [ ] Load data via ViewModel method
- [ ] Handle Loading state (show progress bar)
- [ ] Handle Success state (display data)
- [ ] Handle Error state (show error message)
- [ ] For operations, observe operation state
- [ ] Reset operation state after showing messages
- [ ] NEVER access DAOs directly
- [ ] NEVER manually refresh after CRUD operations
- [ ] Test with unit tests

---

## Migration Checklist for Existing Screens

When updating an existing Activity:

- [ ] Identify all direct DAO access points
- [ ] Create ViewModel if doesn't exist
- [ ] Move DAO calls to Repository
- [ ] Move Repository calls to ViewModel
- [ ] Replace manual refresh with Flow observation
- [ ] Add loading state handling
- [ ] Add error state handling
- [ ] Remove manual progress bar management
- [ ] Test realtime updates work
- [ ] Test error handling works
- [ ] Test loading indicators work
- [ ] Remove unused DAO references

---

## Performance Tips

1. **Use Flow for lists**: Automatic updates, no manual refresh
2. **Use @Transaction**: Batch operations are faster
3. **Proper Dispatchers**: IO for database, Main for UI
4. **Avoid N+1 queries**: Use JOINs or batch queries
5. **Cache when appropriate**: Use Flow caching for static data
6. **Lifecycle awareness**: Use repeatOnLifecycle to avoid leaks
7. **Cancel unnecessary work**: Use viewModelScope for auto-cancellation

---

## Common Mistakes to Avoid

1. ❌ Accessing DAOs from Activities
2. ❌ Not handling loading states
3. ❌ Not handling error states
4. ❌ Manually refreshing after CRUD operations
5. ❌ Blocking main thread with database operations
6. ❌ Not using @Transaction for batch operations
7. ❌ Not logging errors properly
8. ❌ Silent error swallowing
9. ❌ Memory leaks from improper Flow collection
10. ❌ Not testing Repository and ViewModel

---

## Resources

- **Full Documentation**: `DAO_VALIDATION_GUIDE.md`
- **Example Activity**: `ProperViewModelUsageExampleActivity.kt`
- **Helper Extensions**: `UiStateExtensions.kt`
- **Test Examples**: `TaskRepositoryTest.kt`
- **Data Classes**: `DataResult.kt`, `UiState.kt`

---

## Questions?

When in doubt:
1. Check `ProperViewModelUsageExampleActivity.kt` for patterns
2. Look at `TaskViewModel.kt` for ViewModel structure
3. Review `TaskRepository.kt` for Repository patterns
4. Read `DAO_VALIDATION_GUIDE.md` for detailed explanations

**Remember**: Activity → ViewModel → Repository → DAO
**Never skip layers!**
