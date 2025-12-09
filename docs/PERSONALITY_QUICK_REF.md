# Quick Reference: Personality System

## Getting Started

### Get Personality-Based Message in Activity

```kotlin
val messageRepo = MessageTemplateRepository(context)

lifecycleScope.launch {
    val user = db.userDao().getById(userId)
    val personalityId = user?.Personality_ID
    
    if (personalityId != null) {
        val message = messageRepo.getTaskCreatedMessage(personalityId)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
```

## Message Methods

| Method | When to Use | Example |
|--------|------------|---------|
| `getTaskCreatedMessage(id)` | Task created | "Hala sige! Let's go!" |
| `getTaskCompletedMessage(id)` | Task done | "Yown! Tapos na!" |
| `getMotivationalMessage(id)` | Encouragement | "Kaya mo yan!" |
| `getReminderMessage(id)` | Reminder | "Hoy! May task ka pa!" |

## Personality Mapping

| Card | ID | Style | Sample |
|------|----|----|-------|
| cardKalog | 1 | Filipino, funny | "Hala sige! 🎉" |
| cardGenZ | 2 | Trendy, conyo | "OMG! Ganern?! ✨" |
| cardSofty | 3 | Gentle, caring | "I believe in you! 🌸" |
| cardGrey | 4 | Professional | "Task acknowledged." |
| cardFlirty | 5 | Playful | "Ooh! Love it! 😉" |

## Complete Example

```kotlin
class TaskActivity : AppCompatActivity() {
    private lateinit var messageRepo: MessageTemplateRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messageRepo = MessageTemplateRepository(this)
        
        btnCreateTask.setOnClickListener {
            createTask()
            showMessage("task_created")
        }
    }
    
    private fun showMessage(category: String) {
        lifecycleScope.launch {
            val personalityId = getUserPersonalityId()
            val message = when(category) {
                "task_created" -> messageRepo.getTaskCreatedMessage(personalityId)
                "task_completed" -> messageRepo.getTaskCompletedMessage(personalityId)
                "motivation" -> messageRepo.getMotivationalMessage(personalityId)
                else -> messageRepo.getReminderMessage(personalityId)
            }
            Toast.makeText(this@TaskActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
```

## More Information

- **Complete Guide**: PERSONALITY_SYSTEM_GUIDE.md
- **Implementation**: IMPLEMENTATION_SUMMARY.md
- **Examples**: PersonalitySystemExample.kt
