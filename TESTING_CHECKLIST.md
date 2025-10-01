# Testing Checklist After DAO Cleanup

## Overview
This checklist helps verify that all functionality still works correctly after the DAO cleanup and method consolidation.

---

## 🔧 Pre-Testing Setup

- [ ] Build the project successfully
- [ ] Ensure no compilation errors
- [ ] Clear app data to start with a fresh database

---

## 👤 User Management Tests

### Registration & Login
- [ ] Create a new user account
- [ ] Verify username uniqueness check works
- [ ] Verify email uniqueness check works
- [ ] Login with correct credentials
- [ ] Verify login fails with incorrect credentials
- [ ] Test "Forgot Password" flow

**Methods Used:**
- `UserDao.insert()`
- `UserDao.findByUsername()`
- `UserDao.findByEmail()`
- `UserDao.login()`

---

## 📝 Task Management Tests

### Create Tasks
- [ ] Create a task with all fields filled
- [ ] Create a task with minimum required fields
- [ ] Verify task appears in task list
- [ ] Verify task ID is generated correctly

**Methods Used:**
- `TaskDao.insert()`
- `TaskDao.getNextTaskIdForUser()`

### Read Tasks
- [ ] View all tasks
- [ ] View tasks for today
- [ ] View pending tasks
- [ ] View completed tasks
- [ ] Test search functionality
- [ ] Test filter by priority
- [ ] Test filter by status

**Methods Used:**
- `TaskDao.getAllTasksForUser()`
- `TaskDao.getTodayTasks()`
- `TaskDao.getPendingTasks()`
- `TaskDao.getCompletedTasks()`
- `TaskDao.searchTasks()`
- `TaskDao.getTasksByPriority()`
- `TaskDao.getTasksByStatus()`

### Update Tasks
- [ ] Edit task details
- [ ] Update task status (Pending → In Progress → Completed)
- [ ] Update task progress percentage
- [ ] Mark task as completed
- [ ] Verify updates persist after reopening app

**Methods Used:**
- `TaskDao.update()`
- `TaskDao.updateTaskStatus()`  ← **Updated method**
- `TaskDao.updateTaskProgress()`
- `TaskDao.markTaskCompleted()`

### Delete Tasks
- [ ] Soft delete a task
- [ ] Verify deleted task doesn't appear in lists
- [ ] Restore a deleted task
- [ ] Verify restored task appears again

**Methods Used:**
- `TaskDao.softDelete()`  ← **Updated method**
- `TaskDao.restoreTask()`

---

## 📊 Statistics & Counts

### Task Counts
- [ ] Verify "All Tasks" count is correct
- [ ] Verify "Today Tasks" count is correct
- [ ] Verify "Pending Tasks" count is correct
- [ ] Verify "Completed Tasks" count is correct
- [ ] Verify "Overdue Tasks" count is correct

**Methods Used:**
- `TaskDao.getAllTasksCount()`
- `TaskDao.getTodayTasksCount()`
- `TaskDao.getPendingTasksCount()`
- `TaskDao.getCompletedTasksCount()`
- `TaskDao.getOverdueTasksCount()`  ← **Updated method**

### Dashboard Statistics
- [ ] View total tasks for user
- [ ] View completed today count
- [ ] View completed days in range

**Methods Used:**
- `TaskDao.getTotalTasksForUser()`
- `TaskDao.getCompletedTodayTasksCount()`
- `TaskDao.getCompletedDaysInRange()`

---

## 🎨 Personality Tests

### Personality Selection
- [ ] View all available personalities
- [ ] Select a personality during signup
- [ ] Change personality in settings
- [ ] Verify personality displays correctly in dashboard

**Methods Used:**
- `PersonalityDao.getAll()`  ← **Updated method**
- `PersonalityDao.getById()`
- `PersonalityDao.getByUserId()`  ← **Updated method (type fixed)**

---

## ⚙️ Settings Tests

### Profile Settings
- [ ] View user profile
- [ ] Update user information
- [ ] Change personality
- [ ] View settings

**Methods Used:**
- `UserDao.getById()`  ← **Updated method**
- `UserDao.update()`
- `UserDao.updatePersonality()`

### Data Management
- [ ] Export data (if applicable)
- [ ] Clear all tasks
- [ ] Delete user account

**Methods Used:**
- `TaskDao.deleteAllTasksForUser()`
- `UserDao.deleteById()`  ← **Renamed method**

---

## 🔄 LiveData & Real-time Updates

### UI Updates
- [ ] Create a task and verify it appears immediately
- [ ] Mark task as done and verify UI updates
- [ ] Delete task and verify it disappears
- [ ] Filter tasks and verify counts update

**Methods Used:**
- `TaskDao.getAllTasksLive()`
- `TaskDao.getTodayTasksLive()`
- `TaskDao.getPendingTasksLive()`
- `TaskDao.getCompletedTasksLive()`

---

## 🔍 Edge Cases & Error Handling

### Boundary Conditions
- [ ] Test with zero tasks
- [ ] Test with 1 task
- [ ] Test with many tasks (100+)
- [ ] Test with very long task titles/descriptions
- [ ] Test with special characters in text fields

### Data Integrity
- [ ] Verify foreign key constraints work
- [ ] Verify unique constraints work (username, email)
- [ ] Test with null values where allowed
- [ ] Test timestamp handling (CreatedAt, UpdatedAt, DeletedAt)

---

## 🐛 Known Changes to Verify

### Parameter Order Changes
The following methods now use consistent parameter order `(userId, taskId)`:

- [ ] Verify `softDelete(userId, taskId)` works correctly
- [ ] Verify `updateTaskStatus(userId, taskId, status)` works correctly
- [ ] Verify `markTaskCompleted(userId, taskId)` works correctly
- [ ] Verify `updateTaskProgress(userId, taskId, progress)` works correctly

### Method Name Changes
The following methods were renamed:

- [ ] Verify `getById()` works (was `findById()`)
- [ ] Verify `login()` works (was `findByCredentials()`)
- [ ] Verify `deleteById()` works (was `delete(userId)`)
- [ ] Verify `getAll()` works (was `getAllPersonalities()`)
- [ ] Verify `getByUserId()` works (was `getPersonalityByUserId()`)

### Type Changes
- [ ] Verify `PersonalityDao.getByUserId(userId: Int)` works with integer parameter

---

## ✅ Sign-off

After completing all tests:

- [ ] All tests passed
- [ ] No crashes observed
- [ ] No data corruption
- [ ] UI behaves as expected
- [ ] Performance is acceptable

**Tested by:** _________________  
**Date:** _________________  
**Notes:**

---

## 📝 Test Results Template

```
Test: [Test Name]
Status: [PASS/FAIL]
Notes: [Any observations]

Example:
Test: Create new task
Status: PASS
Notes: Task created successfully and appears in list
```

---

## 🚨 If Issues Found

If any test fails:

1. Note the specific test that failed
2. Check which DAO method is involved
3. Verify the method call uses correct parameters
4. Check the method implementation in the DAO
5. Review MIGRATION_SUMMARY.md for correct usage

Report issues with:
- Test that failed
- Steps to reproduce
- Expected vs actual behavior
- Relevant log messages
