# Personality Selection Feature Fix

## Problem Statement
When users clicked on the Personality row in Settings, the dialog showed all personality options but did not indicate which personality was currently selected. This made it difficult for users to know their current selection.

## Solution Implemented

### Changes Made

#### 1. FragmentSettingsActivity.kt
Modified `showPersonalitySelectionDialog()` method to:
- Query both all available personalities AND the user's current personality
- Use `setSingleChoiceItems()` instead of `setItems()` to display radio buttons
- Calculate the checked item index based on the current personality
- Track the selected personality ID in a mutable variable
- Add an "OK" button to confirm the selection (instead of immediate selection)
- Keep the "Cancel" button to allow dismissing without changes

#### 2. MainActivity.kt
Applied the same changes to maintain consistency across the app.

### Key Changes

**Before:**
```kotlin
.setItems(personalityNames) { _, which ->
    val selectedPersonality = personalities[which]
    updateUserPersonality(selectedPersonality.Personality_ID)
}
```

**After:**
```kotlin
// Get current personality
val (personalities, currentPersonality) = withContext(Dispatchers.IO) {
    val allPersonalities = db.personalityDao().getAll()
    val userPersonality = db.personalityDao().getPersonalityByUserIdFromUser(userId)
    Pair(allPersonalities, userPersonality)
}

// Find current selection index
val checkedItem = if (currentPersonality != null) {
    personalities.indexOfFirst { it.Personality_ID == currentPersonality.Personality_ID }
} else {
    -1  // No selection
}

// Use single choice items with radio buttons
.setSingleChoiceItems(personalityNames, checkedItem) { _, which ->
    selectedPersonalityId = personalities[which].Personality_ID
}
.setPositiveButton("OK") { _, _ ->
    selectedPersonalityId?.let { personalityId ->
        updateUserPersonality(personalityId)
    }
}
```

### Features

1. **Visual Indication**: Users can now see which personality is currently selected with a radio button
2. **All 5 Personalities Displayed**: Shows all available personalities (Kalog, Gen Z, Softy, Grey, Flirty)
3. **Confirmation Required**: Users must click "OK" to confirm their selection (prevents accidental changes)
4. **Cancel Option**: Users can dismiss the dialog without making changes
5. **System-Wide Update**: When personality is updated, it reflects across the entire application through the existing `updateUserPersonality()` and `loadSettingsUserData()` methods

### Database Query Used

The implementation uses the `getPersonalityByUserIdFromUser()` method from PersonalityDao:
```kotlin
@Query("""
    SELECT p.* FROM Personality p 
    INNER JOIN User u ON p.Personality_ID = u.Personality_ID 
    WHERE u.User_ID = :userId
""")
suspend fun getPersonalityByUserIdFromUser(userId: Int): Personality?
```

This query joins the Personality and User tables to fetch the current personality based on the user's Personality_ID field.

### Files Modified

1. `app/src/main/java/com/example/cheermateapp/FragmentSettingsActivity.kt`
2. `app/src/main/java/com/example/cheermateapp/MainActivity.kt`

### Testing Recommendations

1. Open the Settings screen
2. Click on the "Personality" row
3. Verify that:
   - All 5 personality options are displayed
   - The current personality has a radio button checked
   - Selecting a different personality updates the radio button
   - Clicking "OK" updates the personality throughout the app
   - Clicking "Cancel" dismisses the dialog without changes
   - The updated personality is reflected in:
     - `tvCurrentPersona` (Settings screen)
     - `chipPersona` (Settings card)
     - `personalityTitle` and `personalityDesc` (Home screen)

### Impact

This change provides a better user experience by:
- Making the current selection visible at a glance
- Preventing accidental changes with the confirmation button
- Following standard Android dialog patterns for selection
- Maintaining consistency between MainActivity and FragmentSettingsActivity
