# Debugging Logs Reinstated

Debugging logs have been reinstated in `MainActivity.kt` to provide more precise information about the theme state and color application.

## Logs Added

The following logs have been added to the codebase to pinpoint the exact values being used at runtime:

1.  **`MainActivity.kt` `setupCalendarView`**:
    *   Logs the value of `isDarkMode` as determined by `ThemeManager.isDarkModeActive()`.
    *   Logs the hex color value (`textColor`) that is explicitly chosen (black for light mode, white for dark mode) for calendar elements.
    *   Logs the color being passed to `changeCalendarViewHeaderTextColor`.
2.  **`MainActivity.kt` `changeCalendarViewHeaderTextColor`**:
    *   Logs the hex color value (`desiredColor`) that the function receives.
3.  **`MainActivity.kt` `updateRecentTasksDisplay`**:
    *   Logs the value of `isDarkMode` as determined by `ThemeManager.isDarkModeActive()`.
    *   Logs the hex color value (`textColor`) that is explicitly chosen (black for light mode, white for dark mode) for dynamically created dashboard `TextView`s.
4.  **`MainActivity.kt` `onCreate`**:
    *   Logs the hex color value (`Color.WHITE`) explicitly set for the `fabAddTask` icon tint.

## Purpose

These logs will provide definitive evidence of:
-   What theme mode (`isDarkMode`) the application correctly identifies itself to be in.
-   What hardcoded color (`textColor`) is being selected by the application based on that theme mode.
-   What color is being passed to and received by the functions responsible for applying these colors.

If, after these logs, the visual issue persists, it indicates that the problem lies outside of the color selection logic and may involve a rendering issue, a conflicting style applied later in the Android view hierarchy, or a misunderstanding of the desired visual outcome.
