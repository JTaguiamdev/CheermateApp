# Comprehensive Theme and Style Fix

This document outlines the diagnosis and resolution of a deep-rooted visual bug affecting multiple components, including the calendar and Floating Action Button, in both light and dark themes.

## 1. The Problem
A persistent visual bug was causing incorrect text and icon colors across the application.

-   **Calendar:** Text for dates and weekdays was hardcoded to white, making it unreadable in the light theme.
-   **Floating Action Button (`fabAddTask`):** The icon color had poor contrast with the button's background in dark mode.
-   **Root Cause:** The investigation revealed a conflict between the base `Theme.Material3.DayNight` theme and numerous style overrides that hardcoded colors, preventing the theme from working as intended.

## 2. Investigation
A deep-dive analysis of the codebase identified two primary sources for the theming conflict:

1.  **`app/src/main/res/values/styles.xml`**: This file contained multiple custom styles for the calendar (`CustomCalendarTheme`, `CustomDayDefault`, `CalendarViewWeekDayTextAppearance`, etc.) that forced text colors to `@android:color/white`, completely overriding the adaptive colors from the base theme.
2.  **`app/src/main/res/values-night/themes.xml`**: This file contained a global override, `<item name="android:colorForeground">@android:color/white</item>`, which forced all foreground elements (icons, text) in the dark theme to be white. This is an overly broad setting that caused contrast issues on any component with a light-colored background.

## 3. The Solution
A two-pronged approach was taken to resolve the issue by removing the conflicting overrides and allowing the base theme to manage colors properly.

### Step 1: Remove Hardcoded Calendar Styles
I systematically modified `app/src/main/res/values/styles.xml` to remove the hardcoded `@android:color/white` and `android:textColorPrimary` from the following styles:
-   `CustomCalendarTheme` (specifically the `colorOnSurface` item)
-   `CustomDayDefault`
-   `CalendarViewWeekDayTextAppearance`
-   `CustomCalendarButton`
-   `CustomDatePickerTheme`
-   `SelectedDateText`
-   `CalendarDayButton`

By removing these lines, the styles now correctly inherit their colors from theme attributes (e.g., `?attr/colorOnSurface`), allowing them to be black/dark in light mode and white in dark mode.

### Step 2: Remove Global Foreground Override
I removed the `<item name="android:colorForeground">@android:color/white</item>` line from `app/src/main/res/values-night/themes.xml`.

This change prevents the dark theme from indiscriminately applying white color to all foreground elements. It allows components like the `FloatingActionButton` to use their intended `app:tint` or derive their icon color from the appropriate theme attribute (e.g., `?attr/colorOnPrimary`), ensuring proper contrast.

## 4. Conclusion
These fixes address the root cause of the theming inconsistencies. The application now correctly leverages the `Material3.DayNight` theme, ensuring that UI components are displayed with appropriate, theme-aware colors. This resolves the reported bugs for the calendar and the Floating Action Button and improves the overall robustness of the app's theming.
