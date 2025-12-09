# Calendar Theming and Style Fix

This document outlines the diagnosis and resolution of a visual bug related to the calendar view's text color in light and dark themes.

## 1. The Problem
A visual bug was identified in the calendar component where the text color of the dates did not adapt correctly to the theme or selection state, leading to poor readability.

-   **In Light Mode:** Unselected dates had white text on a light background, making them nearly invisible.
-   **In Both Modes:** Selected dates had white text on a bright violet background, which offered poor contrast.
-   **In Dark Mode:** The user reported the selected date's text turned black, which was inconsistent with the styling.

The root cause was traced to a single, hardcoded text color being used for all calendar date states.

## 2. Investigation
The investigation focused on the app's theme, style, and color resources.

1.  **`app/src/main/res/drawable/calendar_day_selector.xml`**: This file confirmed that the background of a selected date was set to `@color/calendar_selected` (a violet color). However, it did not control the text color.
2.  **`app/src/main/res/values/colors.xml` and `values-night/colors.xml`**: These files defined the color palette for the app. We confirmed that `text_primary` was black in light mode and white in dark mode. The `calendar_selected` color was violet in both modes.
3.  **`app/src/main/res/values/styles.xml`**: This was the key file. It contained a style named `CalendarViewDateTextAppearance` which explicitly set the text color for calendar dates to `@android:color/white` for all states and themes.

```xml
<!-- BEFORE THE FIX -->
<style name="CalendarViewDateTextAppearance" parent="android:TextAppearance.Material.Small">
    <item name="android:textColor">@android:color/white</item>
</style>
```

This hardcoded white color was the source of all the visual glitches.

## 3. The Solution
To fix the bug, the static text color was replaced with a dynamic `ColorStateList` that adapts to both the theme (light/dark) and the view's state (selected/default).

### Step 1: Create a Color State List
A new file, `app/src/main/res/color/calendar_date_text_color.xml`, was created. This selector defines the text color based on the date's state:
-   **Selected (`state_selected="true"`)**: Text color is white (`@android:color/white`) for high contrast against the violet selection background.
-   **Default**: Text color is `?attr/colorOnSurface`. This is a theme attribute that resolves to the primary text color for the current theme (black in light mode, white in dark mode).

```xml
<!-- app/src/main/res/color/calendar_date_text_color.xml -->
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Color for selected date -->
    <item android:color="@android:color/white" android:state_selected="true" />
    <!-- Default color, using a theme attribute for light/dark mode adaptability -->
    <item android:color="?attr/colorOnSurface" />
</selector>
```

### Step 2: Apply the Color State List
The `CalendarViewDateTextAppearance` style in `app/src/main/res/values/styles.xml` was updated to use this new color state list.

```xml
<!-- AFTER THE FIX -->
<style name="CalendarViewDateTextAppearance" parent="android:TextAppearance.Material.Small">
    <item name="android:textColor">@color/calendar_date_text_color</item>
</style>
```

## 4. Conclusion
This change ensures that the calendar date text is always legible:
-   It correctly displays as black-on-light or white-on-dark for unselected dates.
-   It switches to white-on-violet when a date is selected, maintaining readability.

This was a theme and style issue within the application code. **No changes are required in the Android Studio IDE settings.**
