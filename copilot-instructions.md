# GitHub Copilot Code Review Instructions for CheermateApp

## Project Overview
CheermateApp is an Android task management application built with Kotlin, featuring complete CRUD operations, Room database for local persistence, and Material Design UI components.

## Technology Stack
- **Language**: Kotlin 1.9.24
- **Framework**: Android SDK (minSdk 24, targetSdk 36)
- **Database**: SQLite with Room 2.8.1
- **Architecture**: MVVM pattern with LiveData
- **Async Operations**: Kotlin Coroutines with Dispatchers
- **UI**: Android Views with Material Design Components

## Code Review Focus Areas

### 1. Database & DAO Patterns
- **Room Database**: Verify proper use of Room annotations (@Entity, @Dao, @Database, @Query)
- **DAO Method Naming**: Follow standardized naming conventions (see below)
- **Type Safety**: Ensure User_ID and Task_ID are consistently typed as Int
- **Suspend Functions**: All database operations should be suspend functions
- **Transactions**: Check for proper @Transaction usage when needed

#### DAO Naming Conventions (Post-Cleanup)
The project recently underwent DAO cleanup. Ensure consistency with these standards:

**TaskDao Methods:**
- Use `getAllTasksForUser(userId)` (not `debugGetAllTasksForUser` or `listForUser`)
- Use `updateTaskStatus(userId, taskId, status, updatedAt)` (not `updateTaskStatusByCompositeKey`)
- Use `getTasksByUser(userId)` for filtered task retrieval
- Use `softDelete(userId, taskId, deletedAt, updatedAt)` with default timestamps
- Use `getPendingTasks(userId)` (not `getAllPendingTasks`)

**UserDao Methods:**
- Use `getById(userId)` (not `findById`)
- Use `login(username, password)` (not `findByCredentials`)
- Use `deleteById(userId)` to delete by ID (not overloaded `delete(userId: Int)`)
- Use `delete(user: User)` to delete by entity

**PersonalityDao Methods:**
- Use `getAll()` (not `getAllPersonalities`)
- Use `getByUserId(userId)` (not `getPersonalityByUserId` or `getByUser`)

### 2. Kotlin Code Style
- **Null Safety**: Prefer safe calls (?.) and Elvis operator (?:) over explicit null checks
- **Coroutines**: Use `lifecycleScope.launch` in Activities, `viewModelScope.launch` in ViewModels
- **Dispatchers**: Use `Dispatchers.IO` for database/network, `Dispatchers.Main` for UI updates
- **withContext**: Wrap blocking operations in `withContext(Dispatchers.IO)`
- **Data Classes**: Use for model objects with proper defaults
- **Extension Functions**: Review for appropriate use cases

### 3. Android Architecture
- **Activity Lifecycle**: Check for proper lifecycle-aware operations
- **Memory Leaks**: Verify proper cleanup in onDestroy/onPause
- **Context Usage**: Avoid holding activity context in long-lived objects
- **LiveData**: Use for observable data in MVVM pattern
- **ViewBinding**: Prefer ViewBinding over findViewById

### 4. CRUD Operations
- **Create**: Verify proper data validation before insert
- **Read**: Check for efficient queries and proper error handling
- **Update**: Ensure composite keys (userId + taskId) are handled correctly
- **Delete**: Verify soft delete implementation (setting DeletedAt timestamp)

### 5. UI & User Experience
- **Material Design**: Check for consistent Material Design component usage
- **User Feedback**: Ensure Toast/Snackbar messages for user actions
- **Loading States**: Verify proper loading indicators for async operations
- **Error Handling**: Check for user-friendly error messages
- **Input Validation**: Verify form validation before submission

### 6. Security & Data Privacy
- **User Isolation**: Ensure queries filter by userId to prevent data leakage
- **Password Handling**: Verify passwords are properly hashed (bcrypt)
- **SQL Injection**: Room handles this, but verify raw queries if any
- **Data Sanitization**: Check user input sanitization

### 7. Performance
- **Database Queries**: Check for N+1 query problems
- **List Operations**: Verify efficient list rendering with RecyclerView
- **Memory Usage**: Check for proper bitmap handling and resource cleanup
- **Background Operations**: Ensure long-running tasks use coroutines

### 8. Testing Considerations
- **Unit Tests**: Check for testable code structure
- **Edge Cases**: Verify handling of empty states, null values, boundary conditions
- **Error Cases**: Ensure proper error handling for database failures

## Common Issues to Flag

### High Priority
- ❌ Using deprecated method names (e.g., `debugGetAllTasksForUser`, `findById`)
- ❌ Incorrect parameter order in DAO methods (especially userId vs taskId)
- ❌ Type mismatches (String vs Int for User_ID/Task_ID)
- ❌ Database operations on main thread
- ❌ Uncaught exceptions in coroutines
- ❌ Memory leaks from context holding

### Medium Priority
- ⚠️ Missing null safety checks
- ⚠️ Inefficient database queries
- ⚠️ Missing user feedback for operations
- ⚠️ Inconsistent error handling
- ⚠️ Hard-coded strings (should use string resources)

### Low Priority
- 💡 Code duplication opportunities
- 💡 Potential for extension functions
- 💡 Missing documentation for complex logic
- 💡 Opportunities for sealed classes/enums

## Review Checklist
When reviewing pull requests, verify:

- [ ] All DAO method calls use updated naming conventions
- [ ] Database operations are properly wrapped in suspend functions
- [ ] Coroutines use appropriate dispatchers
- [ ] User_ID and Task_ID are consistently Int type
- [ ] Soft delete uses proper timestamp handling
- [ ] No duplicate or overloaded DAO methods
- [ ] Proper error handling with user-friendly messages
- [ ] Activity lifecycle is properly handled
- [ ] No hardcoded strings (use string resources)
- [ ] Material Design components used consistently
- [ ] User data is properly isolated by userId
- [ ] Code follows Kotlin coding conventions

## Project-Specific Context

### Recent Changes
The project recently underwent DAO cleanup (PR #1) where:
- 12 duplicate/redundant DAO methods were removed
- Method naming was standardized across all DAOs
- Type consistency was enforced (User_ID: Int)
- Parameter order was standardized (userId before taskId)

All future code should maintain these standards.

### Key Files
- **Database**: `app/src/main/java/com/example/cheermateapp/data/db/AppDb.kt`
- **DAOs**: `app/src/main/java/com/example/cheermateapp/data/dao/*.kt`
- **Models**: `app/src/main/java/com/example/cheermateapp/data/model/*.kt`
- **Activities**: `app/src/main/java/com/example/cheermateapp/*.kt`

### Documentation
- `CLEANUP_CHANGES.md`: Details of DAO cleanup
- `MIGRATION_SUMMARY.md`: Migration guide with before/after examples
- `TESTING_CHECKLIST.md`: Comprehensive testing guide
- `SUMMARY.md`: Executive summary of DAO cleanup

## Additional Notes
- This is an Android app with local-first architecture (not Spring Boot)
- Room Database is the appropriate choice for local data persistence
- Focus on offline-first functionality and battery efficiency
- User data privacy is paramount (data stays on device)
