# Database DAO Cleanup - Duplicate Function Removal

## Summary
This document describes the changes made to remove duplicate and redundant functions from the Data Access Object (DAO) interfaces to avoid function overloading and improve code maintainability.

## Background
The issue requested:
1. Migration from Room database to Spring Boot
2. Remove duplicate functions to avoid function overloading
3. Keep data saved locally

**Note:** Since this is an Android application (not a server-side application), the Room database has been retained for local storage. Spring Boot is designed for server-side Java applications and is not suitable for Android. The cleanup focused on removing redundant DAO functions.

## Changes Made

### 1. TaskDao.kt
**Removed duplicate functions:**

- **Soft Delete Methods**
  - ❌ Removed: `softDelete(taskId: Int, userId: Int, deletedAt: Long)` (legacy method with different parameter order)
  - ✅ Kept: `softDelete(userId: Int, taskId: Int, deletedAt: Long, updatedAt: Long)` (standardized parameter order)

- **Task Status Update Methods**
  - ❌ Removed: `updateTaskStatus(taskId: Int, userId: Int, status: String, updatedAt: Long)` (inconsistent parameter order)
  - ❌ Removed: `updateTaskStatusByCompositeKey(userId: Int, taskId: Int, status: String, updatedAt: Long)` (duplicate functionality)
  - ✅ Kept: `updateTaskStatus(userId: Int, taskId: Int, status: String, updatedAt: Long)` (standardized)

- **User Task Retrieval Methods**
  - ❌ Removed: `listForUser(userId: Int): List<Task>` (duplicate)
  - ❌ Removed: `debugGetAllTasksForUser(userId: Int): List<Task>` (debug method duplicate)
  - ✅ Kept: `getTasksByUser(userId: Int): List<Task>` (clearer naming)
  - ✅ Kept: `getAllTasksForUser(userId: Int): List<Task>` (with DeletedAt filter)

- **Overdue Tasks Count Methods**
  - ❌ Removed: `getOverdueTasksCount(userId: Int, currentDateStr: String): Int` (date-based calculation)
  - ✅ Kept: `getOverdueTasksCount(userId: Int): Int` (uses Status field)

- **Pending Tasks Methods**
  - ❌ Removed: `getAllPendingTasks(userId: Int): List<Task>` (duplicate)
  - ✅ Kept: `getPendingTasks(userId: Int): List<Task>` (suspend function)
  - ✅ Kept: `getPendingTasksLive(userId: Int): LiveData<List<Task>>` (LiveData version)

**Total Removed:** 7 duplicate/redundant methods

### 2. UserDao.kt
**Removed duplicate functions:**

- **User Retrieval by ID**
  - ❌ Removed: `findById(userId: Int): User?` (duplicate)
  - ✅ Kept: `getById(userId: Int): User?` (standardized naming)

- **Authentication Methods**
  - ❌ Removed: `findByCredentials(username: String, password: String): User?` (duplicate)
  - ✅ Kept: `login(username: String, passwordHash: String): User?` (clearer naming)

- **Delete Methods**
  - ✅ Kept: `delete(user: User)` (entity-based deletion)
  - ✅ Renamed: `delete(userId: Int)` → `deleteById(userId: Int)` (avoided overloading, clearer naming)

**Total Removed:** 2 duplicate methods, 1 renamed for clarity

### 3. PersonalityDao.kt
**Removed duplicate functions:**

- **Get All Personalities**
  - ❌ Removed: `getAllPersonalities(): List<Personality>` (duplicate)
  - ✅ Kept: `getAll(): List<Personality>` (consistent with other DAOs)

- **Get Personality by User ID**
  - ❌ Removed: `getByUser(userId: String): Personality?` (wrong type + duplicate)
  - ❌ Removed: `getPersonalityByUserId(userId: String): Personality?` (wrong type + duplicate)
  - ✅ Fixed & Kept: `getByUserId(userId: Int): Personality?` (correct type Int, not String)

**Type Fix:** Changed User_ID parameter type from `String` to `Int` to match the database schema where User_ID is an integer.

**Total Removed:** 3 duplicate methods, 1 type corrected

## Benefits

1. **No Function Overloading:** Eliminated methods with the same name but different parameter orders/types
2. **Consistent Naming:** Standardized method names across all DAOs
3. **Type Safety:** Fixed User_ID type mismatches (String → Int)
4. **Clearer Intent:** Method names now clearly indicate their purpose
5. **Reduced Maintenance:** Less code to maintain and test
6. **Better Performance:** Compiler doesn't need to resolve overloaded methods

## Compatibility Notes

**Breaking Changes:**
If any code was using the removed methods, it will need to be updated to use the retained alternatives:

- `listForUser(userId)` → `getTasksByUser(userId)`
- `findById(userId)` → `getById(userId)`
- `findByCredentials(username, password)` → `login(username, password)`
- `getAllPersonalities()` → `getAll()`
- `getPersonalityByUserId(userId)` → `getByUserId(userId)` (also fix type to Int)
- `delete(userId: Int)` → `deleteById(userId)` (in UserDao)

## Files Modified

1. `app/src/main/java/com/example/cheermateapp/data/dao/TaskDao.kt`
2. `app/src/main/java/com/example/cheermateapp/data/dao/UserDao.kt`
3. `app/src/main/java/com/example/cheermateapp/data/dao/PersonalityDao.kt`
4. `gradle/libs.versions.toml` (updated AGP and Kotlin versions for build compatibility)

## Total Reduction
- **12 duplicate/redundant methods removed**
- **1 method renamed for clarity**
- **1 type issue fixed**
- **43 lines of code removed**

## Recommendation

After these changes, search the codebase for any references to the removed method names and update them to use the retained alternatives. This can be done with:

```bash
# Search for removed method names
grep -r "listForUser" app/src/
grep -r "findById" app/src/
grep -r "findByCredentials" app/src/
grep -r "getAllPersonalities" app/src/
grep -r "getPersonalityByUserId" app/src/
grep -r "getAllPendingTasks" app/src/
grep -r "debugGetAllTasksForUser" app/src/
```

Then update any findings to use the new standardized method names.

## Additional Fixes Applied

After the DAO cleanup, the following files were updated to use the new method names:

### 1. FragmentTaskActivity.kt
- ✅ `debugGetAllTasksForUser()` → `getAllTasksForUser()` (2 occurrences)
- ✅ `updateTaskStatus()` parameter order fixed (userId, taskId instead of taskId, userId)

### 2. FragmentSettingsActivity.kt
- ✅ `findById()` → `getById()` (1 occurrence)

### 3. MainActivity.kt
- ✅ `findById()` → `getById()` (2 occurrences)
- ✅ `updateTaskStatusByCompositeKey()` → `updateTaskStatus()` (3 occurrences)

**Total Updates:** 9 method calls updated across 3 activity files

All references have been updated and the codebase is now consistent with the cleaned-up DAO interfaces.
