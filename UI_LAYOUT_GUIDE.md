# Task Detail Activity - UI Layout Guide

## Screen Layout Overview

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                                         ┃
┃  ┌───┐  Task Details                   ┃  <- Header with back button
┃  │ ← │                                  ┃
┃  └───┘                                  ┃
┃                                         ┃
┃  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ┃  <- Priority Indicator (colored bar)
┃                                         ┃
┃  Complete Quarterly Report              ┃  <- Task Title (bold, 24sp)
┃                                         ┃
┃  Finish the quarterly report and        ┃  <- Description (gray, 15sp)
┃  submit it to management by EOD         ┃
┃                                         ┃
┃  ╔═══════════════════════════════════╗ ┃
┃  ║ Category: 📋 Work                 ║ ┃  <- Category Card
┃  ╚═══════════════════════════════════╝ ┃
┃                                         ┃
┃  ╔═══════════════════════════════════╗ ┃
┃  ║ Priority: 🎯 High                 ║ ┃  <- Priority Card
┃  ╚═══════════════════════════════════╝ ┃
┃                                         ┃
┃  ╔═══════════════════════════════════╗ ┃
┃  ║ Status: ⏳ Pending                ║ ┃  <- Status Card
┃  ╚═══════════════════════════════════╝ ┃
┃                                         ┃
┃  ╔═══════════════════════════════════╗ ┃
┃  ║ Progress: ▓▓▓▓▓░░░░░  50%        ║ ┃  <- Progress Card (conditional)
┃  ╚═══════════════════════════════════╝ ┃
┃                                         ┃
┃  ╔═══════════════════════════════════╗ ┃
┃  ║ Due: Dec 25, 2024 at 3:30 PM     ║ ┃  <- Due Date Card
┃  ╚═══════════════════════════════════╝ ┃
┃                                         ┃
┃  ┌──────────┐ ┌──────────┐ ┌─────────┐┃
┃  │    ✅    │ │    ✏️    │ │   🗑️    │┃  <- Action Buttons
┃  │ Complete │ │   Edit   │ │ Delete  │┃
┃  └──────────┘ └──────────┘ └─────────┘┃
┃                                         ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

## Color Scheme

- **Background**: Gradient (matching app theme)
- **Priority Bar**: 
  - 🔴 Red (#E53E3E) - High Priority
  - 🟡 Yellow (#F59E0B) - Medium Priority
  - 🟢 Green (#48BB78) - Low Priority
- **Cards**: Glass morphism effect (semi-transparent white)
- **Text**: 
  - Title: White (bold)
  - Labels: White (70% opacity)
  - Values: White (bold)
- **Buttons**: Glass effect with hover states

## Responsive Behavior

1. **ScrollView**: Content scrolls if it exceeds screen height
2. **Cards**: Stack vertically with consistent spacing
3. **Buttons**: Equal width, horizontal layout
4. **Back Button**: Fixed at top, always visible

## Status-Based UI Changes

### Pending Task
```
┌──────────┐ ┌──────────┐ ┌─────────┐
│    ✅    │ │    ✏️    │ │   🗑️    │
│ Complete │ │   Edit   │ │ Delete  │  <- All buttons active
└──────────┘ └──────────┘ └─────────┘
```

### Completed Task
```
┌──────────┐ ┌──────────┐ ┌─────────┐
│    ✅    │ │    ✏️    │ │   🗑️    │
│Completed │ │   Edit   │ │ Delete  │  <- Complete button grayed out
└──────────┘ └──────────┘ └─────────┘
   (50% opacity, disabled)
```

## Layout Specifications

### Spacing
- Screen padding: 20dp
- Card margin bottom: 12dp
- Section margin bottom: 16dp
- Button margin: 6dp horizontal

### Font Sizes
- Back button: 18sp
- Screen title: 20sp
- Task title: 24sp
- Description: 15sp
- Card labels: 14sp
- Button text: 14sp

### Dimensions
- Priority bar height: 4dp
- Back button: 40dp x 40dp
- Action buttons: 48dp height
- Card padding: 14dp

## User Interactions

1. **Back Button**: Returns to task list
2. **Complete Button**: 
   - Shows confirmation dialog
   - Marks task as complete
   - Updates UI to show completed state
3. **Edit Button**: Opens edit functionality (placeholder)
4. **Delete Button**: 
   - Shows confirmation dialog
   - Deletes task from database
   - Returns to task list with result

## Accessibility

- Proper content descriptions for all interactive elements
- Sufficient touch targets (48dp minimum)
- High contrast text on background
- Clear visual hierarchy
- Support for screen readers

## Animation Opportunities (Future)

- Slide in from right on open
- Fade out on back
- Button ripple effects
- Progress bar animation
- Status change animation
