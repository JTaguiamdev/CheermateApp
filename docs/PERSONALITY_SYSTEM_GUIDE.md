# Personality System Documentation

## Overview
The Cheermate app features a personality-based messaging system that provides personalized motivational messages based on the user's chosen personality type. Each personality has a unique voice and style of communication.

## Personality Types

The system supports 5 distinct personality types, each with a unique ID:

### 1. Kalog Vibes (ID: 1)
- **UI Card**: `@+id/cardKalog`
- **Character**: The funny friend who makes everything entertaining
- **Message Style**: Funny, casual, uses Filipino slang
- **Example Messages**:
  - Task Created: "Hala sige! Let's get this party started! 🎉"
  - Task Completed: "Yown! Tapos na yan! Bili tayo ice cream to celebrate! 🍦"

### 2. GenZ Conyo (ID: 2)
- **UI Card**: `@+id/cardGenZ`
- **Character**: Rich tita energy with a modern twist
- **Message Style**: Modern slang, trendy, mix of English and Tagalog
- **Example Messages**:
  - Task Created: "OMG! Ganern?! Let's do this, babe! ✨"
  - Task Completed: "Yayyy! So proud of you, babe! You're lit AF! 💅"

### 3. Softy Bebe (ID: 3)
- **UI Card**: `@+id/cardSofty`
- **Character**: Gentle, caring companion who believes in you
- **Message Style**: Soft, encouraging, warm
- **Example Messages**:
  - Task Created: "I believe in you! You can do this! 🌸"
  - Task Completed: "I'm so proud of you! You did an amazing job! 💕"

### 4. Mr. Grey (ID: 4)
- **UI Card**: `@+id/cardGrey`
- **Character**: Professional, direct, mysteriously motivating
- **Message Style**: Brief, professional, assertive
- **Example Messages**:
  - Task Created: "Task acknowledged. Let's proceed efficiently."
  - Task Completed: "Excellent work. Your discipline is commendable."

### 5. Flirty Vibes (ID: 5)
- **UI Card**: `@+id/cardFlirty`
- **Character**: Playful, charming, smoothly motivating
- **Message Style**: Flirtatious, playful, charming
- **Example Messages**:
  - Task Created: "Ooh, a new challenge? I love watching you work! 😉"
  - Task Completed: "Look at you go! You're absolutely crushing it! 😍"

## Database Schema

### Personality Table
```sql
CREATE TABLE Personality (
    Personality_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    User_ID INTEGER NOT NULL,
    PersonalityType INTEGER NOT NULL,  -- References Type_ID (1-5)
    Name TEXT NOT NULL,
    Description TEXT,
    FOREIGN KEY (User_ID) REFERENCES User(User_ID) ON DELETE CASCADE
)
```

### PersonalityType Table
```sql
CREATE TABLE PersonalityType (
    Type_ID INTEGER PRIMARY KEY,      -- 1, 2, 3, 4, or 5
    Name TEXT NOT NULL,
    Description TEXT NOT NULL,
    IsActive INTEGER NOT NULL DEFAULT 1,
    CreatedAt INTEGER NOT NULL,
    UpdatedAt INTEGER NOT NULL
)
```

### User Table (relevant fields)
```sql
CREATE TABLE User (
    User_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    Username TEXT NOT NULL UNIQUE,
    Email TEXT NOT NULL UNIQUE,
    Personality_ID INTEGER,            -- Links to Personality.Personality_ID
    ...
)
```

### MessageTemplate Table
```sql
CREATE TABLE MessageTemplate (
    Template_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    Personality_ID INTEGER,            -- References PersonalityType.Type_ID (1-5)
    Category TEXT,                     -- e.g., "task_created", "task_completed", "motivation", "reminder"
    TextTemplate TEXT,
    FOREIGN KEY (Personality_ID) REFERENCES Personality(Personality_ID) ON DELETE SET NULL
)
```

## How It Works

### 1. User Selects Personality
When a user signs up and reaches `PersonalityActivity`:

```kotlin
// User clicks on a personality card
cardKalog.setOnClickListener {
    // Saves personality with Type_ID = 1
    savePersonality(personalityType)
}
```

### 2. Personality Saved to Database
```kotlin
// Creates Personality record
val personality = Personality(
    User_ID = userId,
    PersonalityType = 1,  // For Kalog Vibes
    Name = "Kalog",
    Description = "The funny friend..."
)
db.personalityDao().upsert(personality)

// Updates User record with Personality_ID
db.userDao().updatePersonality(userId, personalityId)
```

### 3. Retrieving Personality-Based Messages
```kotlin
// Using MessageTemplateRepository
val messageRepo = MessageTemplateRepository(context)

// Get a task completion message for user's personality
val message = messageRepo.getTaskCompletedMessage(personalityId)
// Returns: "Yown! Tapos na yan! Bili tayo ice cream to celebrate! 🍦" for Kalog
```

## Message Categories

The system supports the following message categories:

1. **task_created** - When a new task is created
2. **task_completed** - When a task is marked as complete
3. **motivation** - General motivational messages
4. **reminder** - Task reminder notifications

## Usage Examples

### Getting Messages in Activities

```kotlin
class TaskActivity : AppCompatActivity() {
    private lateinit var messageRepo: MessageTemplateRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messageRepo = MessageTemplateRepository(this)
        
        // Get user's personality
        lifecycleScope.launch {
            val user = db.userDao().getById(userId)
            val personalityId = user?.Personality_ID
            
            if (personalityId != null) {
                // Show personalized message when task is created
                val message = messageRepo.getTaskCreatedMessage(personalityId)
                Toast.makeText(this@TaskActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

### Getting Messages for Notifications

```kotlin
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val userId = intent.getIntExtra("USER_ID", -1)
        
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDb.get(context)
            val user = db.userDao().getById(userId)
            val personalityId = user?.Personality_ID
            
            if (personalityId != null) {
                val messageRepo = MessageTemplateRepository(context)
                val message = messageRepo.getReminderMessage(personalityId)
                
                // Show notification with personalized message
                showNotification(context, message)
            }
        }
    }
}
```

## Code Architecture

### Key Classes

1. **PersonalityActivity.kt**
   - Displays personality selection UI
   - Maps card clicks to Type_IDs (1-5)
   - Saves user's personality choice

2. **MessageTemplateDao.kt**
   - Database access for message templates
   - Queries by personality ID and category

3. **MessageTemplateRepository.kt**
   - Business logic layer for message retrieval
   - Provides convenience methods for each message type
   - Handles fallback to default messages

4. **DatabaseSeeder.kt**
   - Seeds initial personality types
   - Seeds message templates for all personalities
   - Runs automatically on app first launch

### Data Flow

```
User Selection → PersonalityActivity
                        ↓
                 Personality saved to DB
                 (PersonalityType = 1-5)
                        ↓
                 User.Personality_ID updated
                        ↓
         MessageTemplateRepository retrieves messages
                        ↓
              Display personalized messages
```

## Testing

### Manual Testing Steps

1. **Test Personality Selection**
   - Sign up as a new user
   - Select each personality type
   - Verify the correct Type_ID (1-5) is saved

2. **Test Message Retrieval**
   - For each personality:
     - Create a task → verify personalized "task_created" message
     - Complete a task → verify personalized "task_completed" message
     - Check notifications → verify personalized "reminder" message

3. **Test Database Seeding**
   - Clear app data
   - Launch app
   - Check database for:
     - 5 PersonalityType records (Type_ID 1-5)
     - 20 MessageTemplate records (4 per personality)

### Expected Results

| Personality | Type_ID | Card ID | Message Style |
|-------------|---------|---------|---------------|
| Kalog Vibes | 1 | cardKalog | Filipino, funny |
| GenZ Conyo | 2 | cardGenZ | Trendy, conyo |
| Softy Bebe | 3 | cardSofty | Gentle, caring |
| Mr. Grey | 4 | cardGrey | Professional |
| Flirty Vibes | 5 | cardFlirty | Playful, charming |

## Adding New Message Templates

To add new message templates for existing personalities:

```kotlin
val newTemplate = MessageTemplate(
    Personality_ID = 1,  // Kalog Vibes
    Category = "task_overdue",
    TextTemplate = "Hoy! Nalate ka na! Bilisan mo! ⏰"
)
db.messageTemplateDao().insert(newTemplate)
```

## Future Enhancements

Potential improvements to the personality system:

1. **Custom Personalities** - Allow users to create custom personalities
2. **Personality Evolution** - Adapt messages based on user behavior
3. **More Categories** - Add categories like "encouragement", "celebration", "sympathy"
4. **Multilingual Support** - Support multiple languages per personality
5. **Voice Messages** - Add text-to-speech with personality-specific voices
6. **Dynamic Templates** - Use variables in templates (e.g., "{username}, you're doing great!")

## Troubleshooting

### Common Issues

**Issue**: Messages not showing personality-specific text
- **Solution**: Check if User.Personality_ID is set correctly
- **Solution**: Verify MessageTemplate records exist in database

**Issue**: Personality selection not saving
- **Solution**: Check PersonalityActivity logs for errors
- **Solution**: Verify User_ID is passed correctly to PersonalityActivity

**Issue**: Wrong personality messages displaying
- **Solution**: Verify the PersonalityType mapping (1-5) is correct
- **Solution**: Check MessageTemplate.Personality_ID values

## Summary

The personality system provides a engaging, personalized experience by:
- Allowing users to choose from 5 distinct personality types
- Storing personality choice as Type_ID (1-5) in the database
- Linking users to their chosen personality via User.Personality_ID
- Providing category-based message templates for each personality
- Making it easy to retrieve and display personalized messages throughout the app

This creates a more engaging and personalized user experience that adapts to each user's preferred communication style.
