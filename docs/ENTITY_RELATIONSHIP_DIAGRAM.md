# Entity-Relationship Diagram (ERD) - CheermateApp

This document describes the normalized database schema for the CheermateApp task management system.

## 📊 Entity-Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                              CheermateApp Database Schema (Normalized)                               │
└─────────────────────────────────────────────────────────────────────────────────────────────────────┘

                                    ┌─────────────────────┐
                                    │    Personality      │
                                    ├─────────────────────┤
                                    │ PK Personality_ID   │
                                    │    Name             │
                                    │    Description      │
                                    │    MotivationMessage│
                                    │    IsActive         │
                                    │    CreatedAt        │
                                    │    UpdatedAt        │
                                    └──────────┬──────────┘
                                               │
                                               │ 1
                                               │
                                               ▼ N
                    ┌──────────────────────────┴──────────────────────────┐
                    │                                                     │
                    ▼                                                     ▼
    ┌─────────────────────────┐                           ┌─────────────────────────┐
    │         User            │                           │    MessageTemplate      │
    ├─────────────────────────┤                           ├─────────────────────────┤
    │ PK User_ID              │                           │ PK Template_ID          │
    │    Username (UNIQUE)    │                           │ FK Personality_ID       │
    │    Email (UNIQUE)       │                           │    Category             │
    │    PasswordHash         │                           │    TextTemplate         │
    │    FirstName            │                           └─────────────────────────┘
    │    LastName             │
    │    Birthdate            │
    │ FK Personality_ID       │
    │    CreatedAt            │
    │    UpdatedAt            │
    │    DeletedAt            │
    └────────────┬────────────┘
                 │
    ┌────────────┼────────────┬─────────────────┐
    │            │            │                 │
    │ 1          │ 1          │ 1               │
    ▼ N          ▼ N          ▼ N               │
┌───────────┐ ┌─────────┐ ┌───────────────────┐ │
│   Task    │ │Settings │ │UserSecurityAnswer │ │
├───────────┤ ├─────────┤ ├───────────────────┤ │
│PK Task_ID │ │PK Set_ID│ │PK Answer_ID       │ │
│PK User_ID │ │PK User_ID│ │FK User_ID         │ │
│   Title   │ │FK Per_ID│ │FK Question_ID     │ │
│Description│ │Appearance│ │   AnswerHash      │ │
│  Category │ │Notif.   │ └─────────┬─────────┘ │
│  Priority │ │DataMgmt │           │           │
│   DueAt   │ │Statistics│           │ N         │
│  DueTime  │ └─────────┘           │           │
│   Status  │                       ▼ 1         │
│TaskProgress│           ┌───────────────────┐  │
│ CreatedAt │            │ SecurityQuestion  │  │
│ UpdatedAt │            ├───────────────────┤  │
│ DeletedAt │            │PK SecQuestion_ID  │  │
└─────┬─────┘            │   Prompt          │  │
      │                  │   IsActive        │  │
      │                  │   CreatedAt       │  │
      │                  │   UpdatedAt       │  │
      │                  └───────────────────┘  │
      │                                         │
      ├─────────────────────────────────────────┘
      │
      │ 1                        1                         N
      ├──────────────────────────┼─────────────────────────┐
      ▼ N                        ▼ N                       ▼ 1
┌─────────────────┐    ┌─────────────────────┐    ┌───────────────────────┐
│    SubTask      │    │   TaskReminder      │    │   TaskDependency      │
├─────────────────┤    ├─────────────────────┤    ├───────────────────────┤
│PK Subtask_ID    │    │PK TaskReminder_ID   │    │PK Task_ID             │
│PK Task_ID       │    │PK Task_ID           │    │PK User_ID             │
│PK User_ID       │    │PK User_ID           │    │PK DependsOn_Task_ID   │
│   Name          │    │   RemindAt          │    │FK DependsOn_User_ID   │
│   IsCompleted   │    │   ReminderType      │    │   CreatedAt           │
│   SortOrder     │    │   IsActive          │    └───────────────────────┘
│   CreatedAt     │    │   CreatedAt         │
│   UpdatedAt     │    │   UpdatedAt         │
│   DeletedAt     │    └─────────────────────┘
└─────────────────┘
```

---

## 📋 Entity Descriptions

### 1. **User** (Core Entity)
The central entity representing users of the application.

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| User_ID | INTEGER | PK, AUTO_INCREMENT | Unique user identifier |
| Username | TEXT | NOT NULL, UNIQUE | User's login name |
| Email | TEXT | NOT NULL, UNIQUE | User's email address |
| PasswordHash | TEXT | NOT NULL | Hashed password for authentication |
| FirstName | TEXT | NOT NULL (default '') | User's first name |
| LastName | TEXT | NOT NULL (default '') | User's last name |
| Birthdate | TEXT | NULLABLE | User's date of birth |
| Personality_ID | INTEGER | FK → Personality | Selected personality type |
| CreatedAt | INTEGER | NOT NULL | Timestamp of account creation |
| UpdatedAt | INTEGER | NOT NULL | Timestamp of last update |
| DeletedAt | INTEGER | NULLABLE | Soft delete timestamp |

---

### 2. **Task** (Core Entity)
Represents tasks created by users.

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| Task_ID | INTEGER | PK (composite) | Task identifier |
| User_ID | INTEGER | PK (composite), FK → User | Owner of the task |
| Title | TEXT | NOT NULL | Task title |
| Description | TEXT | NULLABLE | Task description |
| Category | ENUM | NOT NULL | Work, Personal, Shopping, Others |
| Priority | ENUM | NOT NULL | Low, Medium, High |
| DueAt | TEXT | NULLABLE | Due date (yyyy-MM-dd) |
| DueTime | TEXT | NULLABLE | Due time (HH:mm) |
| Status | ENUM | NOT NULL | Pending, InProgress, Completed, Cancelled, OverDue |
| TaskProgress | INTEGER | NOT NULL (default 0) | Progress percentage (0-100) |
| CreatedAt | INTEGER | NOT NULL | Task creation timestamp |
| UpdatedAt | INTEGER | NOT NULL | Last update timestamp |
| DeletedAt | INTEGER | NULLABLE | Soft delete timestamp |

---

### 3. **SubTask**
Represents sub-tasks within a main task.

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| Subtask_ID | INTEGER | PK (composite) | SubTask identifier |
| Task_ID | INTEGER | PK (composite), FK → Task | Parent task |
| User_ID | INTEGER | PK (composite), FK → Task | Task owner |
| Name | TEXT | NOT NULL | SubTask name |
| IsCompleted | BOOLEAN | NOT NULL (default false) | Completion status |
| SortOrder | INTEGER | NOT NULL (default 0) | Display order |
| CreatedAt | INTEGER | NOT NULL | Creation timestamp |
| UpdatedAt | INTEGER | NOT NULL | Last update timestamp |
| DeletedAt | INTEGER | NULLABLE | Soft delete timestamp |

---

### 4. **TaskReminder**
Reminders associated with tasks.

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| TaskReminder_ID | INTEGER | PK (composite) | Reminder identifier |
| Task_ID | INTEGER | PK (composite), FK → Task | Associated task |
| User_ID | INTEGER | PK (composite), FK → Task | Task owner |
| RemindAt | INTEGER | NOT NULL | Reminder timestamp |
| ReminderType | ENUM | NULLABLE | TEN_MINUTES_BEFORE, THIRTY_MINUTES_BEFORE, AT_SPECIFIC_TIME |
| IsActive | BOOLEAN | NOT NULL (default true) | Whether reminder is active |
| CreatedAt | INTEGER | NOT NULL | Creation timestamp |
| UpdatedAt | INTEGER | NOT NULL | Last update timestamp |

---

### 5. **TaskDependency**
Represents dependencies between tasks (prerequisite relationships).

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| Task_ID | INTEGER | PK (composite), FK → Task | Dependent task |
| User_ID | INTEGER | PK (composite), FK → Task | Dependent task owner |
| DependsOn_Task_ID | INTEGER | PK (composite), FK → Task | Prerequisite task |
| DependsOn_User_ID | INTEGER | FK → Task | Prerequisite task owner |
| CreatedAt | INTEGER | NOT NULL | Relationship creation timestamp |

---

### 6. **Personality**
Predefined personality types that influence app messaging.

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| Personality_ID | INTEGER | PK | Personality identifier (1-5) |
| Name | TEXT | NOT NULL | Personality name |
| Description | TEXT | NOT NULL | Personality description |
| MotivationMessage | TEXT | NULLABLE | Default motivation message |
| IsActive | BOOLEAN | NOT NULL (default true) | Whether personality is available |
| CreatedAt | INTEGER | NOT NULL | Creation timestamp |
| UpdatedAt | INTEGER | NOT NULL | Last update timestamp |

**Personality Types:**
1. **Kalog** - Traditional/Classic style
2. **Gen Z** - Modern/Trendy style
3. **Softy** - Gentle/Supportive style
4. **Grey** - Neutral/Professional style
5. **Flirty** - Playful/Fun style

---

### 7. **MessageTemplate**
Templates for personality-based messages.

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| Template_ID | INTEGER | PK, AUTO_INCREMENT | Message template identifier |
| Personality_ID | INTEGER | FK → Personality | Associated personality |
| Category | TEXT | NOT NULL | Message category (motivation, task_work, etc.) |
| TextTemplate | TEXT | NOT NULL | Message template text |

---

### 8. **Settings**
User-specific application settings.

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| Settings_ID | INTEGER | PK (composite) | Settings identifier |
| User_ID | INTEGER | PK (composite), FK → User | Settings owner |
| Personality_ID | INTEGER | FK → Personality, NULLABLE | User's selected personality |
| Appearance | JSON | NULLABLE | Theme, font size, color scheme |
| Notification | JSON | NULLABLE | Notification preferences |
| DataManagement | JSON | NULLABLE | Backup and sync settings |
| Statistics | JSON | NULLABLE | Analytics preferences |

---

### 9. **SecurityQuestion**
Predefined security questions for account recovery.

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| SecurityQuestion_ID | INTEGER | PK, AUTO_INCREMENT | Question identifier |
| Prompt | TEXT | NOT NULL | Security question text |
| IsActive | BOOLEAN | NOT NULL (default true) | Whether question is available |
| CreatedAt | INTEGER | NOT NULL | Creation timestamp |
| UpdatedAt | INTEGER | NOT NULL | Last update timestamp |

---

### 10. **UserSecurityAnswer**
User answers to security questions.

| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| Answer_ID | INTEGER | PK, AUTO_INCREMENT | Answer identifier |
| User_ID | INTEGER | FK → User | User who answered |
| Question_ID | INTEGER | FK → SecurityQuestion | Related question |
| AnswerHash | TEXT | NOT NULL | Hashed answer for security |

---

## 🔗 Relationship Summary

| Relationship | Cardinality | Description |
|--------------|-------------|-------------|
| User → Task | 1:N | A user can have many tasks |
| User → Settings | 1:N | A user can have multiple settings configurations |
| User → UserSecurityAnswer | 1:N | A user can have multiple security answers |
| Task → SubTask | 1:N | A task can have multiple sub-tasks |
| Task → TaskReminder | 1:N | A task can have multiple reminders |
| Task → TaskDependency | N:N | Tasks can depend on other tasks |
| Personality → User | 1:N | A personality can be used by many users |
| Personality → MessageTemplate | 1:N | A personality has multiple message templates |
| Personality → Settings | 1:N | A personality can be referenced in settings |
| SecurityQuestion → UserSecurityAnswer | 1:N | A question can have many user answers |

---

## ✅ Normalization Analysis

### First Normal Form (1NF) ✓
- All tables have a primary key
- All columns contain atomic (indivisible) values
- No repeating groups or arrays in columns

### Second Normal Form (2NF) ✓
- All tables are in 1NF
- All non-key attributes are fully functionally dependent on the entire primary key
- Composite keys (Task_ID, User_ID) ensure proper dependencies

### Third Normal Form (3NF) ✓
- All tables are in 2NF
- No transitive dependencies exist
- Non-key attributes depend only on the primary key
- Settings uses embedded JSON for preferences (denormalized for performance but logically separate)

### Boyce-Codd Normal Form (BCNF) ✓
- All tables satisfy 3NF requirements
- Every determinant is a candidate key
- No anomalies in data insertion, deletion, or update

---

## 🔐 Data Integrity Features

### Foreign Key Constraints
- **CASCADE DELETE**: User deletion cascades to Tasks, Settings
- **CASCADE DELETE**: Task deletion cascades to SubTask, TaskReminder, TaskDependency
- **CASCADE DELETE**: Personality deletion cascades to MessageTemplate

### Unique Constraints
- User.Username (unique)
- User.Email (unique)

### Soft Delete Pattern
- User, Task, SubTask support soft delete via `DeletedAt` column
- Allows data recovery and audit trails

### Audit Timestamps
- All entities track `CreatedAt` and `UpdatedAt` timestamps
- Enables change tracking and data synchronization

---

## 📁 Schema File Location

The actual Room database schema is exported to:
```
app/schemas/com.example.cheermateapp.data.AppDb/
```

> Note: The schema folder uses the legacy package name for backward compatibility.

Entity model files are located at:
```
app/src/main/java/com/cheermateapp/data/model/
```
