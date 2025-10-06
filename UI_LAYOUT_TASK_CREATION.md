# Task Creation Dialog - UI Layout

## Before (Original)
```
┌─────────────────────────────────────┐
│        Add New Task                 │
├─────────────────────────────────────┤
│                                     │
│  Task Title (Required)              │
│  [_________________________]        │
│                                     │
│  Description (Optional)             │
│  [_________________________]        │
│  [_________________________]        │
│  [_________________________]        │
│                                     │
│  Priority:                          │
│  [Low ▼] [Medium] [High]            │
│                                     │
│  [Due Date (Req)] [Due Time (Opt)] │
│  [2024-01-15    ] [14:30         ] │
│                                     │
├─────────────────────────────────────┤
│          [Cancel] [Create Task]     │
└─────────────────────────────────────┘
```

## After (With Category and Reminder)
```
┌─────────────────────────────────────┐
│        Add New Task                 │
├─────────────────────────────────────┤
│                                     │
│  Task Title (Required)              │
│  [_________________________]        │
│                                     │
│  Description (Optional)             │
│  [_________________________]        │
│  [_________________________]        │
│  [_________________________]        │
│                                     │
│  Category:                     ← NEW
│  [Work ▼]                           │
│   • Work                            │
│   • Personal                        │
│   • Shopping                        │
│   • Others                          │
│                                     │
│  Priority:                          │
│  [Medium ▼]                         │
│   • Low                             │
│   • Medium                          │
│   • High                            │
│                                     │
│  [Due Date (Req)] [Due Time (Opt)] │
│  [2024-01-15    ] [14:30         ] │
│                                     │
│  Reminder:                     ← NEW
│  [None ▼]                           │
│   • None                            │
│   • 10 minutes before               │
│   • 30 minutes before               │
│   • At specific time                │
│                                     │
├─────────────────────────────────────┤
│          [Cancel] [Create Task]     │
└─────────────────────────────────────┘
```

## User Flow

### Step 1: Open FAB Dialog
User clicks the FAB (Floating Action Button) on either:
- Home dashboard (MainActivity)
- Tasks screen (FragmentTaskActivity)

### Step 2: Fill in Task Details
1. **Task Title** - Required field
2. **Description** - Optional, supports multiple lines
3. **Category** - Choose from dropdown (NEW):
   - Work (default)
   - Personal
   - Shopping
   - Others
4. **Priority** - Choose from dropdown:
   - Low
   - Medium (default)
   - High
5. **Due Date** - Click to open date picker (default: today)
6. **Due Time** - Click to open time picker (optional)
7. **Reminder** - Choose from dropdown (NEW):
   - None (default)
   - 10 minutes before
   - 30 minutes before
   - At specific time

### Step 3: Validation
When user clicks "Create Task":
- Title must not be empty
- Due date must be set
- If reminder is selected, due time must be set

### Step 4: Success
Task is created and confirmation message shows:
```
✅ Task 'Meeting with client' created for 2024-01-15 at 14:30 (Reminder: 10 minutes before)!
```

## Dialog Behavior

### Category Selection
- **Default**: Work
- **Options**: Work, Personal, Shopping, Others
- **Storage**: Saved as enum in Task.Category field
- **Display**: Standard Android Spinner dropdown

### Reminder Selection
- **Default**: None
- **Options**: None, 10 minutes before, 30 minutes before, At specific time
- **Validation**: Requires due time to be set (shows error if time is missing)
- **Storage**: Creates TaskReminder record with calculated RemindAt timestamp
- **Calculation**:
  - "10 minutes before": DueTime - 10 minutes
  - "30 minutes before": DueTime - 30 minutes
  - "At specific time": DueTime

### Error Messages

#### Missing Title
```
❌ Task title is required
```

#### Missing Due Date
```
❌ Due date is required
```

#### Reminder without Time
```
❌ Reminder requires a due time to be set
```

## Example Usage Scenarios

### Scenario 1: Work Task with Reminder
1. Title: "Prepare presentation"
2. Category: Work
3. Priority: High
4. Due Date: 2024-01-20
5. Due Time: 09:00
6. Reminder: 30 minutes before
7. Result: Task created, reminder will trigger at 08:30

### Scenario 2: Personal Task without Reminder
1. Title: "Buy groceries"
2. Category: Shopping
3. Priority: Medium
4. Due Date: 2024-01-18
5. Due Time: (not set)
6. Reminder: None
7. Result: Task created, no reminder

### Scenario 3: Shopping Task
1. Title: "Order birthday gift"
2. Category: Personal
3. Priority: High
4. Due Date: 2024-01-25
5. Due Time: 12:00
6. Reminder: At specific time
7. Result: Task created, reminder will trigger at 12:00

## Technical Implementation Notes

### UI Components
- **Spinner**: Android standard dropdown component
- **EditText**: Text input fields
- **Button**: Date/time picker triggers
- **LinearLayout**: Vertical layout container
- **ScrollView**: Allows scrolling if content exceeds screen

### Data Flow
```
User Input → Validation → Task Creation → Reminder Creation → Database → UI Update
```

### Database Operations
1. Get next task ID for user
2. Create Task object with category
3. Insert task into database
4. If reminder selected:
   - Calculate reminder timestamp
   - Get next reminder ID
   - Create TaskReminder object
   - Insert reminder into database
5. Reload UI data

### Thread Management
- UI operations on Main thread
- Database operations on IO thread using coroutines
- Proper context switching with withContext()

## Accessibility Considerations

- All spinners have clear labels
- Required fields are marked
- Error messages are descriptive
- Success messages include all relevant details
- Consistent behavior across both FAB dialogs

## Future Enhancements Preview

Potential additions (not in current scope):
- Custom reminder times (e.g., 1 hour, 1 day before)
- Multiple reminders per task
- Category color coding
- Category icons
- Recurring task support
- Task templates by category
