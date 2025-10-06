# WindowInsets Quick Reference

## 🎯 What Changed

We replaced static `android:fitsSystemWindows="true"` with dynamic WindowInsets APIs for modern edge-to-edge display.

## 📋 Quick Setup Checklist

### For New Activities

```kotlin
// 1. Add imports
import androidx.core.view.WindowCompat
import com.example.cheermateapp.util.WindowInsetsUtil

// 2. In onCreate(), before setContentView()
WindowCompat.setDecorFitsSystemWindows(window, false)

// 3. After initializing views
private fun setupWindowInsets() {
    // Apply to appropriate views
    WindowInsetsUtil.applyStatusBarInsets(toolbar)
    WindowInsetsUtil.applyNavigationBarInsets(bottomNav)
}
```

### For Layouts

Remove this:
```xml
android:fitsSystemWindows="true"  ❌
```

Add this (if needed for view reference):
```xml
android:id="@+id/rootContainer"  ✅
```

## 🛠️ Common Use Cases

### Toolbar (Top of Screen)
```kotlin
val toolbar = findViewById<Toolbar>(R.id.toolbar)
WindowInsetsUtil.applyStatusBarInsets(toolbar)
```

### Bottom Navigation
```kotlin
val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
WindowInsetsUtil.applyNavigationBarInsets(bottomNav)
```

### Full Screen Container (Both Top and Bottom)
```kotlin
val container = findViewById<LinearLayout>(R.id.mainContainer)
WindowInsetsUtil.applySystemBarsInsets(container)
```

### Floating Action Button
```kotlin
val fab = findViewById<FloatingActionButton>(R.id.fab)
WindowInsetsUtil.applyNavigationBarInsets(fab)
// Or use margin version:
// WindowInsetsUtil.applySystemBarInsetsAsMargin(fab, bottom = true)
```

### Custom Control (Choose Edges)
```kotlin
val view = findViewById<View>(R.id.customView)
WindowInsetsUtil.applySystemBarInsets(
    view,
    left = true,    // Apply left inset
    top = true,     // Apply top inset (status bar)
    right = true,   // Apply right inset
    bottom = false  // Don't apply bottom inset
)
```

## 📝 WindowInsetsUtil Methods

| Method | Use Case | Applies |
|--------|----------|---------|
| `applyStatusBarInsets(view)` | Toolbar, top containers | Top padding |
| `applyNavigationBarInsets(view)` | Bottom nav, FAB | Bottom padding |
| `applySystemBarsInsets(view)` | Root containers | Top + Bottom padding |
| `applySystemBarInsets(view, ...)` | Custom control | Selected edges |
| `applySystemBarInsetsAsMargin(view, ...)` | Floating views | Margin instead of padding |

## 🎨 Theme Settings (Already Applied)

```xml
<!-- Transparent system bars -->
<item name="android:statusBarColor">@android:color/transparent</item>
<item name="android:navigationBarColor">@android:color/transparent</item>

<!-- Enable edge-to-edge -->
<item name="android:windowDrawsSystemBarBackgrounds">true</item>
<item name="android:enforceNavigationBarContrast">false</item>
```

## ✅ Files Modified

- ✅ `themes.xml` - Added edge-to-edge styles
- ✅ `themes.xml (night)` - Added edge-to-edge styles
- ✅ `MainActivity.kt` - Applied WindowInsets
- ✅ `FragmentTaskExtensionActivity.kt` - Applied WindowInsets
- ✅ `activity_main.xml` - Removed fitsSystemWindows
- ✅ `fragment_tasks_extension.xml` - Removed fitsSystemWindows
- ✅ `WindowInsetsUtil.kt` - Created utility class
- ✅ `gradle/libs.versions.toml` - Fixed Gradle version
- ✅ `gradle-wrapper.properties` - Updated to stable Gradle

## 🔍 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Content behind status bar | Call `setDecorFitsSystemWindows(false)` before `setContentView()` |
| Too much padding | Check existing padding - it's additive |
| Padding not applied | Verify view ID and that view exists |
| Works on one device only | Expected - insets are device-specific |

## 🧪 Quick Test

1. Run app on device with gesture navigation
2. Check status bar area - should see gradient through transparent bar
3. Check toolbar - should not be behind status bar
4. Check bottom nav - should not be behind navigation bar
5. Rotate device - padding should adjust automatically

## 📚 Full Documentation

See `WINDOWINSETS_IMPLEMENTATION_GUIDE.md` for:
- Detailed explanation
- Testing checklist
- Migration guide
- Best practices
- Troubleshooting

## 🎯 Remember

- Always enable edge-to-edge: `WindowCompat.setDecorFitsSystemWindows(window, false)`
- Apply insets after initializing views
- Test on multiple devices and orientations
- Use the right method for the right view
- WindowInsets preserves existing padding automatically

## 📱 Supported Configurations

✅ Button navigation (3-button)
✅ Gesture navigation (swipe)
✅ Portrait orientation
✅ Landscape orientation
✅ Tablets
✅ Foldable devices
✅ All screen sizes
✅ Android API 24+ (minSdk 24)

---

**Status**: ✅ Implemented and ready to use
**Priority**: High (Modern Android requirement)
**Impact**: Better UX, Modern design, Full device support
