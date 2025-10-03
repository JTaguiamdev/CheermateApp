# UI Visual Guide - ListView with FAB

## Screen Layout

```
┌─────────────────────────────────────┐
│         Tasks                       │  ← Header (no add button)
│  0 total tasks                      │  ← Subtitle
├─────────────────────────────────────┤
│  🔍 Search tasks            [Sort]  │  ← Search bar with sort button
│  0 found                            │  ← Found chip
├─────────────────────────────────────┤
│ [All (5)] [Today (2)] [Pending (3)] │  ← Filter tabs
│ [Done (2)]                          │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │█ Complete Android App         │ │  ← Task Card 1
│  │  Finish the CheermateApp...   │ │     █ = Priority indicator (red)
│  │  🎯 High    ⏳ Pending        │ │
│  │              📅 Sep 29        │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │█ Study for Exam               │ │  ← Task Card 2
│  │  Computer Science midterm...  │ │     █ = Priority indicator (orange)
│  │  🎯 Medium  🔄 InProgress     │ │
│  │              📅 Sep 30        │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │█ Buy Groceries                │ │  ← Task Card 3
│  │  🎯 Low     ⏳ Pending        │ │     █ = Priority indicator (green)
│  │              📅 Oct 01        │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │█ Review Pull Requests         │ │  ← Task Card 4
│  │  Check team code reviews      │ │
│  │  🎯 High    ⏳ Pending        │ │
│  │              📅 Sep 29        │ │
│  └───────────────────────────────┘ │
│                                     │
│                                 ┌─┐ │
│                                 │+│ │  ← FAB (Floating Action Button)
│                                 └─┘ │     Purple circle with + icon
│                                     │
└─────────────────────────────────────┘
```

## Task Card Detail View

When user taps on a task card:

```
┌─────────────────────────────────────┐
│  ████████████████████████████████   │  ← Priority indicator bar (full width)
│                                     │
│  Complete Android App               │  ← Title (large, bold)
│                                     │
│  Finish the CheermateApp project   │  ← Description (regular)
│  with all CRUD operations           │
│                                     │
│  Priority: High                     │  ← Priority
│                                     │
│  Status: ⏳ Pending                 │  ← Status with emoji
│                                     │
│  Progress: ▬▬▬▬▬▬▬▬▬░ 75%          │  ← Progress bar (if > 0%)
│                                     │
│  Due: Sep 29, 2024 at 2:30 PM      │  ← Due date and time
│                                     │
├─────────────────────────────────────┤
│                                     │
│  [✅ Complete] [✏️ Edit] [🗑️ Delete]│  ← Action buttons
│                                     │
└─────────────────────────────────────┘
```

## Empty State Views

### All Tasks Empty
```
┌─────────────────────────────────────┐
│         Tasks                       │
│  0 total tasks                      │
├─────────────────────────────────────┤
│  🔍 Search tasks            [Sort]  │
│  0 found                            │
├─────────────────────────────────────┤
│ [All (0)] [Today (0)] [Pending (0)] │
│ [Done (0)]                          │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │                               │ │
│  │    📋 No tasks available      │ │
│  │                               │ │
│  │   Tap the + button to create  │ │
│  │      your first task!         │ │
│  │                               │ │
│  └───────────────────────────────┘ │
│                                     │
│                                 ┌─┐ │
│                                 │+│ │
│                                 └─┘ │
└─────────────────────────────────────┘
```

### Done Tasks Empty
```
┌─────────────────────────────────────┐
│  ┌───────────────────────────────┐ │
│  │                               │ │
│  │  ✅ No completed tasks yet    │ │
│  │                               │ │
│  │   Start completing tasks to   │ │
│  │       see them here!          │ │
│  │                               │ │
│  └───────────────────────────────┘ │
└─────────────────────────────────────┘
```

## Color Scheme

### Priority Colors
- **High**: Red (#FFE53E3E)
- **Medium**: Orange (#FFA500)
- **Low**: Green (#00FF00)

### Status Emojis
- **Pending**: ⏳
- **InProgress**: 🔄
- **Completed**: ✅
- **Cancelled**: ❌
- **Overdue**: 🔴

### Glass Theme
- **Background**: Semi-transparent with blur effect
- **Cards**: `@drawable/bg_card_glass_hover` with elevation
- **Chips**: `@drawable/bg_chip_glass`
- **FAB**: Purple (#6B48FF) with white + icon

## Interaction Flow

### Adding a Task
```
1. User taps FAB (+)
2. Add Task dialog opens
3. User fills: Title, Description, Priority, Status, Due Date
4. User taps Save
5. Dialog closes
6. RecyclerView updates with new task
7. Task appears in appropriate filter
```

### Viewing Task Details
```
1. User taps a task card in the list
2. Detail dialog opens with full information
3. User can see all task details
4. User can tap Complete/Edit/Delete
5. Dialog closes after action
6. RecyclerView updates if needed
```

### Completing a Task
```
1. User taps task to open details
2. User taps Complete button
3. Task status updates to Completed
4. Dialog closes
5. Task moves to Done filter
6. Pending filter count decreases
7. Done filter count increases
```

### Filtering Tasks
```
1. User taps a filter tab (e.g., "Today")
2. Tab highlights
3. RecyclerView updates to show only matching tasks
4. If no tasks, empty state shows
5. Counter shows task count for that filter
```

### Searching Tasks
```
1. User taps search box
2. Keyboard appears
3. User types query
4. As user types, list filters in real-time
5. "found" chip updates with count
6. Matching tasks highlight search term (optional)
7. User clears search to see all tasks again
```

## Comparison: Old vs New

### Old UI (Single Task View)
```
Pros:
- Focus on one task at a time
- Clear task details visible immediately

Cons:
- Can only see one task
- Need to navigate with Previous/Next
- Counter needed to show position
- More taps to reach a specific task
```

### New UI (List View)
```
Pros:
- See multiple tasks at once
- Quick overview of all tasks
- Direct access to any task
- Modern FAB pattern
- Better use of screen space

Cons:
- Less detail visible per task (by design)
- Need to tap to see full details
- Requires dialog for details
```

## Accessibility Notes

- All buttons have proper content descriptions
- RecyclerView supports screen reader navigation
- FAB has clear label for screen readers
- Color is not the only indicator (uses emojis + text)
- Touch targets are minimum 48dp
- Contrast ratios meet WCAG guidelines

## Performance Considerations

- RecyclerView only renders visible items
- Efficient view recycling
- Smooth scrolling even with 100+ tasks
- Lazy loading ready if needed
- No performance degradation with large lists
