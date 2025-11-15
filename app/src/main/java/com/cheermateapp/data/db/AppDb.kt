package com.cheermateapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cheermateapp.data.dao.MessageTemplateDao
import com.cheermateapp.data.dao.PersonalityDao
import com.cheermateapp.data.dao.RecurringTaskDao
import com.cheermateapp.data.dao.SecurityDao
import com.cheermateapp.data.dao.SettingsDao
import com.cheermateapp.data.dao.SubTaskDao
import com.cheermateapp.data.dao.TaskDao
import com.cheermateapp.data.dao.TaskDependencyDao
import com.cheermateapp.data.dao.TaskReminderDao
import com.cheermateapp.data.dao.TaskTemplateDao
import com.cheermateapp.data.dao.UserDao
import com.cheermateapp.data.model.MessageTemplate
import com.cheermateapp.data.model.Personality
import com.cheermateapp.data.model.RecurringTask
import com.cheermateapp.data.model.SecurityQuestion
import com.cheermateapp.data.model.UserSecurityAnswer
import com.cheermateapp.data.model.Settings
import com.cheermateapp.data.model.SubTask
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.TaskDependency
import com.cheermateapp.data.model.TaskReminder
import com.cheermateapp.data.model.TaskTemplate
import com.cheermateapp.data.model.User
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
    version = 19,
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
    abstract fun messageTemplateDao(): MessageTemplateDao
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