# Enhanced Task List Item - Visual Guide

## Layout Structure

### Collapsed View (Normal State)
```
┌─────────────────────────────────────────────────┐
│ █ │ Complete Android App                        │
│ █ │ Finish the CheermateApp project...         │
│ █ │ [📋 Work] [🎯 High] [⏳ Pending]  📅 Dec 25│
└─────────────────────────────────────────────────┘
```

Components:
- Left edge: Colored priority indicator (4dp vertical bar)
  - Red for High
  - Orange for Medium  
  - Green for Low
- Main content area:
  - Title: Bold, white text
  - Description: Secondary white text (70% opacity)
  - Info chips row:
    - Category chip with emoji
    - Priority chip with emoji
    - Status chip with emoji
    - Due date (right-aligned)

### Expanded View (Edit Mode)
```
┌─────────────────────────────────────────────────┐
│                                                  │
│  Category                                        │
│  [📋 Work] [👤 Personal] [🛒 Shop] [📌 Others]  │
│                                                  │
│  Priority                                        │
│  [🟢 Low]    [🟡 Medium]    [🔴 High]           │
│                                                  │
│  Due Date                                        │
│  [📅 Today] [📅 Tomorrow] [📅 Custom]           │
│                                                  │
│  [✅ Save Changes]                              │
│                                                  │
└─────────────────────────────────────────────────┘
```

Components:
- Section headers: Bold, white text
- Selection buttons: 
  - Unselected: Transparent glass background
  - Selected: Violet background (#FFA667C3)
  - All have white text and emojis
- Save button: Full width, glass card background

## Interaction Flow

### Step 1: Initial Display
User sees list of tasks in collapsed view with all information visible at a glance.

### Step 2: Tap to Expand
```
User taps on task
        ↓
Collapsed view slides out (visibility = GONE)
        ↓
Expanded view slides in (visibility = VISIBLE)
```

### Step 3: Select Category
```
Initial state: All buttons glass background
        ↓
User taps "Personal"
        ↓
Personal button → Violet background
Other buttons → Glass background
```

### Step 4: Select Priority
```
Default: Medium highlighted (violet)
        ↓
User taps "High"
        ↓
High button → Violet background
Medium button → Glass background
```

### Step 5: Select Due Date Option A (Quick Select)
```
User taps "Today"
        ↓
Today button → Violet background
Date set to current date
```

### Step 5: Select Due Date Option B (Custom Date)
```
User taps "Custom"
        ↓
Date picker dialog opens
        ↓
Calendar displays with theme:
  - Today's date: Blue circle (#FF00B4D8)
  - Selected date: Violet circle (#FFA667C3)
        ↓
User selects date
        ↓
Dialog closes
Custom button → Violet background
```

### Step 6: Save Changes
```
User taps "Save Changes"
        ↓
Database update triggered
        ↓
Success toast displayed
        ↓
Expanded view collapses
        ↓
Task list refreshes
        ↓
Updated values visible in collapsed view
```

## Calendar Date Picker Theme

### Visual Appearance
```
┌────────────────────────────────────┐
│  Select Date                    ✕  │
├────────────────────────────────────┤
│                                     │
│   December 2024                     │
│                                     │
│  M   T   W   T   F   S   S         │
│                          1          │
│  2   3   4   5   6   7   8         │
│  9  10  11  12  13  14  15         │
│ 16  17  18  19  20 ⊚21  22         │  ← Today (blue circle)
│ 23  24 ⊙25  26  27  28  29         │  ← Selected (violet circle)
│ 30  31                              │
│                                     │
│        [Cancel]      [OK]           │
└────────────────────────────────────┘
```

Key Visual Elements:
- Background: Gradient from violet to blue
- Header: White text
- Today's date (21): Blue circle (#FF00B4D8) with white background
- Selected date (25): Violet circle (#FFA667C3) with white background
- Regular dates: White text on transparent background
- Buttons: White text

## Color Scheme

### Theme Colors
- **Primary Violet**: #FFA667C3 (for selections, gradients)
- **Primary Blue**: #FF00B4D8 (for today, gradients)
- **Background**: Gradient from violet to blue
- **Text Primary**: #FFFFFFFF (white)
- **Text Secondary**: #B3FFFFFF (70% white)

### Priority Colors
- **High**: #FFE53E3E (Red)
- **Medium**: #FFED8936 (Orange)
- **Low**: #FF38A169 (Green)

### Button States
- **Normal**: Transparent glass with border
- **Selected**: Solid violet (#FFA667C3)
- **Pressed**: Slightly darker violet

## Typography

### Font Family
- SF Pro Rounded Bold: Titles, labels, button text
- SF Pro Rounded Regular: Body text, descriptions

### Font Sizes
- Task Title: 16sp
- Task Description: 13sp
- Section Headers: 12sp
- Chips/Buttons: 11sp
- Calendar Dates: 14sp

## Spacing

### Margins
- Item horizontal margin: 4dp
- Item vertical margin: 6dp
- Internal padding: 12dp
- Button spacing: 4-6dp

### Button Dimensions
- Height: 36dp (selection buttons)
- Height: 40dp (save button)
- Horizontal padding: 8dp
- Vertical padding: 4dp

## Accessibility

### Touch Targets
All interactive elements meet minimum 48dp touch target:
- Buttons expand touch area beyond visual bounds
- Adequate spacing between buttons

### Visual Feedback
- Ripple effects on button press
- Color change on selection
- Toast messages for actions

### Screen Reader Support
- Meaningful content descriptions
- Proper label associations
- Announcements for state changes

## Responsive Design

### Portrait Mode
- Full width cards
- Single column layout
- Buttons arranged horizontally (3-4 per row)

### Landscape Mode
- Same layout adapts to wider screen
- More horizontal space for chips
- Maintains readability

### Small Screens
- Text sizes remain readable
- Buttons stack if needed
- Scrollable content area

## Animation (Future Enhancement)

### Expand Animation
```
Duration: 300ms
Easing: DecelerateInterpolator

Collapsed view:
  - Alpha: 1.0 → 0.0
  - Height: match_content → 0

Expanded view:
  - Alpha: 0.0 → 1.0
  - Height: 0 → match_content
```

### Selection Animation
```
Duration: 150ms
Easing: FastOutSlowIn

Background:
  - Color: glass → violet
  - Alpha: 0.3 → 1.0
```

## Edge Cases

### No Description
- Description TextView visibility = GONE
- Title moves up to fill space

### No Due Date
- Due date TextView visibility = GONE
- Other chips spread to fill space

### Long Title
- Ellipsize at end
- Maximum 1 line in collapsed view

### Long Description
- Ellipsize at end
- Maximum 1 line in collapsed view

## Implementation Notes

### ViewHolder Pattern
- Reuses views efficiently
- Minimal inflation cost
- Smooth scrolling performance

### State Management
- Single source of truth: expandedPosition
- No conflicting states
- Clean expand/collapse logic

### Memory Efficiency
- Views recycled by RecyclerView
- No memory leaks
- Proper lifecycle management
