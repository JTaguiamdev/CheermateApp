# ViewPager2 Crash Fix

## Problem
The app was crashing with the following error:
```
FATAL EXCEPTION: main
java.lang.IllegalStateException: Pages must fill the whole ViewPager2 (use match_parent)
at androidx.viewpager2.widget.ViewPager2$4.onChildViewAttachedToWindow(ViewPager2.java:270)
```

## Root Cause
The `item_task_swipeable.xml` layout file had `android:layout_height="wrap_content"` which violated ViewPager2's requirement that all pages must have both width and height set to `match_parent`.

## Solution
Changed the root LinearLayout in `item_task_swipeable.xml`:

### Before
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@drawable/gradient_background"
    android:elevation="4dp">
```

### After
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@drawable/gradient_background"
    android:elevation="4dp"
    android:padding="16dp">
```

### Changes Made
1. **Height**: Changed from `wrap_content` to `match_parent`
2. **Padding**: Added `android:padding="16dp"` to ensure proper spacing of content within the full-height layout

## Why This Works
ViewPager2 uses RecyclerView internally and requires all child views to fill the entire viewport. This is a fundamental design requirement for ViewPager2 because:
1. It needs consistent page sizes for proper horizontal scrolling
2. It uses the full dimensions for page snapping behavior
3. It manages page visibility based on the full viewport size

By setting both dimensions to `match_parent`, each task card now fills the entire ViewPager2 area, which satisfies the framework's requirements.

## Impact
- ✅ Fixes the crash when displaying tasks in the swipeable ViewPager2
- ✅ Maintains the same visual appearance with proper padding
- ✅ Minimal change - only 2 lines modified in 1 file
- ✅ No code changes required in MainActivity or TaskPagerAdapter

## Testing
To verify this fix:
1. Launch the app
2. Navigate to the home screen where recent tasks are displayed
3. The app should now load without crashing when ViewPager2 tries to display task cards
4. Swipe left/right to navigate between tasks
5. Verify that task content is properly visible and spaced within each page
