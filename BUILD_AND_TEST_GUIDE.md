# Build and Test Instructions

## Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK 24 or higher
- Internet connection (to download dependencies)

## Building the Project

### Method 1: Android Studio (Recommended)
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the project directory and select it
4. Wait for Gradle sync to complete
5. Click "Build" → "Make Project" (Ctrl+F9 / Cmd+F9)

### Method 2: Command Line
```bash
# Navigate to project directory
cd CheermateApp

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## Running the Application

### On Emulator:
1. Open AVD Manager in Android Studio
2. Create/start an Android emulator (API 24+)
3. Click "Run" → "Run 'app'" (Shift+F10 / Ctrl+R)

### On Physical Device:
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Click "Run" → "Run 'app'"

## Testing the Hybrid Task Stack

### Test Scenarios:

#### 1. Empty State
**Setup:** User with no tasks
**Expected:** 
- Message: "🎉 No pending tasks! Tap + to create your first task!"
- No ViewPager2 displayed
- No counter shown

#### 2. Single Task
**Setup:** User with exactly 1 active task
**Expected:**
- Task displayed in swipeable card
- Counter shows "1 / 1"
- No expand/collapse section
- Can't swipe left/right

#### 3. Multiple Tasks (Primary Test)
**Setup:** User with 5+ active tasks
**Expected:**
- First task shown prominently (highest priority/overdue)
- Counter shows "1 / 5" initially
- Swipe left → counter updates to "2 / 5"
- Swipe right → counter updates to "1 / 5"
- Expand button shows "▼ 4 more tasks pending"
- Tapping expand reveals compact list
- Button changes to "▲ Hide pending tasks"
- Tapping again collapses list

#### 4. Task Actions
**Setup:** Task in swipeable view
**Tests:**
a. Complete Action:
   - Tap "✅ Complete" button
   - Task should be marked as completed
   - ViewPager should update to show next task
   - Counter should decrement total

b. Edit Action:
   - Tap "✏️ Edit" button
   - Quick actions dialog should appear
   - Can modify task details

c. Delete Action:
   - Tap "🗑️ Delete" button
   - Confirmation dialog appears
   - Task removed from list
   - ViewPager updates

#### 5. Task Sorting
**Setup:** Multiple tasks with different priorities and due dates
**Expected Order:**
1. Overdue + High Priority
2. Overdue + Medium Priority
3. Overdue + Low Priority
4. Pending + High Priority
5. Pending + Medium Priority
6. Pending + Low Priority
7. In Progress tasks (same priority order)

#### 6. Compact Task List
**Setup:** Expanded task list
**Tests:**
- Each compact card shows: priority dot, title, status
- Tapping compact card opens task details dialog
- List properly displays all remaining tasks
- Scrollable if many tasks

#### 7. Page Change Callback
**Setup:** Multiple tasks in ViewPager2
**Tests:**
- Swipe between tasks
- Counter updates correctly on each swipe
- No lag or delays
- Smooth animation

### Manual Testing Checklist

- [ ] Empty state displays correctly
- [ ] Single task view (no swipe)
- [ ] Multiple tasks swipeable
- [ ] Counter updates on page change
- [ ] Expand/collapse toggle works
- [ ] Compact task list displays correctly
- [ ] Complete button works
- [ ] Edit button works
- [ ] Delete button works
- [ ] Tasks sorted correctly (overdue first, then priority)
- [ ] Progress bar displays when TaskProgress > 0
- [ ] Due date and time display correctly
- [ ] Priority indicator color correct
- [ ] Status emoji correct
- [ ] Swipe animation smooth
- [ ] No crashes or errors

### Automated Testing (Optional)

Create instrumented tests for:
```kotlin
// Example test structure
@Test
fun testTaskPagerAdapter_displaysCorrectTaskCount() {
    val tasks = listOf(/* test tasks */)
    val adapter = TaskPagerAdapter(tasks, {}, {}, {})
    assertEquals(5, adapter.itemCount)
}

@Test
fun testUpdateRecentTasksDisplay_withNoTasks_showsEmptyState() {
    // Test empty state
}

@Test
fun testUpdateRecentTasksDisplay_withMultipleTasks_showsSwipeableView() {
    // Test ViewPager2 creation
}
```

## Troubleshooting

### Build Issues

**Problem:** "Plugin with id 'com.android.application' not found"
**Solution:** 
- Check internet connection
- Sync Gradle files
- Invalidate caches: File → Invalidate Caches / Restart

**Problem:** "ViewPager2 not found"
**Solution:**
- Verify `implementation("androidx.viewpager2:viewpager2:1.0.0")` in build.gradle.kts
- Sync Gradle
- Clean and rebuild project

**Problem:** Kotlin version mismatch
**Solution:**
- Check gradle/libs.versions.toml has correct versions
- AGP: 8.3.0, Kotlin: 1.9.22

### Runtime Issues

**Problem:** ViewPager2 not displaying tasks
**Solution:**
- Check if tasks list is not empty
- Verify adapter is set correctly
- Check layout inflation

**Problem:** Counter not updating
**Solution:**
- Verify OnPageChangeCallback is registered
- Check TextView reference is not null

**Problem:** Expand/collapse not working
**Solution:**
- Verify click listener is set
- Check visibility changes are applied
- Ensure text updates correctly

## Performance Considerations

- ViewPager2 uses RecyclerView internally - efficient for large lists
- Only renders visible + adjacent pages
- Smooth animations even with many tasks
- Memory efficient compared to showing all tasks

## Accessibility

Ensure these are tested:
- [ ] Swipe gestures work with TalkBack
- [ ] Buttons have proper content descriptions
- [ ] Counter is announced by screen reader
- [ ] Task details readable by screen reader

## Code Review Checklist

Before merging, verify:
- [ ] No hardcoded strings (use strings.xml)
- [ ] No memory leaks (callbacks properly handled)
- [ ] Error handling in place
- [ ] Null safety checks
- [ ] Comments explain complex logic
- [ ] Code follows project style guide
- [ ] No TODOs or FIXMEs left
- [ ] Documentation updated
