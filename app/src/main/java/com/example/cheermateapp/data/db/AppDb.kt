package com.example.cheermateapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cheermateapp.data.dao.PersonalityDao
import com.example.cheermateapp.data.dao.RecurringTaskDao
import com.example.cheermateapp.data.dao.SecurityDao
import com.example.cheermateapp.data.dao.SettingsDao
import com.example.cheermateapp.data.dao.SubTaskDao
import com.example.cheermateapp.data.dao.TaskDao
import com.example.cheermateapp.data.dao.TaskDependencyDao
import com.example.cheermateapp.data.dao.TaskReminderDao
import com.example.cheermateapp.data.dao.TaskTemplateDao
import com.example.cheermateapp.data.dao.UserDao
import com.example.cheermateapp.data.model.MessageTemplate
import com.example.cheermateapp.data.model.Personality
import com.example.cheermateapp.data.model.RecurringTask
import com.example.cheermateapp.data.model.SecurityQuestion
import com.example.cheermateapp.data.model.UserSecurityAnswer
import com.example.cheermateapp.data.model.Settings
import com.example.cheermateapp.data.model.SubTask
import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.TaskDependency
import com.example.cheermateapp.data.model.TaskReminder
import com.example.cheermateapp.data.model.TaskTemplate
import com.example.cheermateapp.data.model.User
import com.google.gson.Gson

@Database(
    entities = [
        Personality::class,
        User::class,
        Task::class,
        TaskReminder::class,
        SubTask::class,
        SecurityQuestion::class,
        UserSecurityAnswer::class,
        Settings::class,
        MessageTemplate::class,
        RecurringTask::class,
        TaskTemplate::class,
        TaskDependency::class
    ],
    version = 15,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun subTaskDao(): SubTaskDao
    abstract fun taskReminderDao(): TaskReminderDao
    abstract fun settingsDao(): SettingsDao
    abstract fun securityDao(): SecurityDao
    abstract fun personalityDao(): PersonalityDao
    abstract fun recurringTaskDao(): RecurringTaskDao
    abstract fun taskTemplateDao(): TaskTemplateDao
    abstract fun taskDependencyDao(): TaskDependencyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDb? = null

        fun get(context: Context): AppDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): AppDb {
            val gson = Gson()
            return Room.databaseBuilder(
                appContext,
                AppDb::class.java,
                "cheermate_database"
            )
                // Uses ProvidedTypeConverter to supply Gson at runtime
                .addTypeConverter(AppTypeConverters(gson))
                // Consider replacing with proper migrations in production
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}