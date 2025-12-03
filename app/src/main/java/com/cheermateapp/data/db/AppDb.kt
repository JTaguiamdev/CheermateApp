package com.cheermateapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cheermateapp.data.dao.MessageTemplateDao
import com.cheermateapp.data.dao.PersonalityDao
import com.cheermateapp.data.dao.SecurityDao
import com.cheermateapp.data.dao.SettingsDao
import com.cheermateapp.data.dao.SubTaskDao
import com.cheermateapp.data.dao.TaskDao
import com.cheermateapp.data.dao.TaskDependencyDao
import com.cheermateapp.data.dao.TaskReminderDao
import com.cheermateapp.data.dao.UserDao
import com.cheermateapp.data.model.MessageTemplate
import com.cheermateapp.data.model.Personality
import com.cheermateapp.data.model.SecurityQuestion
import com.cheermateapp.data.model.UserSecurityAnswer
import com.cheermateapp.data.model.Settings
import com.cheermateapp.data.model.SubTask
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.TaskDependency
import com.cheermateapp.data.model.TaskReminder
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
        TaskDependency::class
    ],
    version = 21,
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
    abstract fun taskDependencyDao(): TaskDependencyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDb? = null

        fun get(context: Context): AppDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table with NOT NULL constraints
                database.execSQL("""
                    CREATE TABLE User_new (
                        User_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        Username TEXT NOT NULL,
                        Email TEXT NOT NULL,
                        PasswordHash TEXT NOT NULL,
                        FirstName TEXT NOT NULL DEFAULT '',
                        LastName TEXT NOT NULL DEFAULT '',
                        Birthdate TEXT,
                        Personality_ID INTEGER,
                        CreatedAt INTEGER NOT NULL,
                        UpdatedAt INTEGER NOT NULL,
                        DeletedAt INTEGER
                    )
                """)

                // Copy data from old table to new table, coalescing null names to empty strings
                database.execSQL("""
                    INSERT INTO User_new (User_ID, Username, Email, PasswordHash, FirstName, LastName, Birthdate, Personality_ID, CreatedAt, UpdatedAt, DeletedAt)
                    SELECT User_ID, Username, Email, PasswordHash, COALESCE(FirstName, ''), COALESCE(LastName, ''), Birthdate, Personality_ID, CreatedAt, UpdatedAt, DeletedAt FROM User
                """)

                // Drop old table
                database.execSQL("DROP TABLE User")

                // Rename new table to old table name
                database.execSQL("ALTER TABLE User_new RENAME TO User")

                // Re-create indexes from original schema
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_User_Username ON User(Username)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_User_Email ON User(Email)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_User_Personality_ID ON User(Personality_ID)")
            }
        }

        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop RecurringTask table and its indexes
                database.execSQL("DROP TABLE IF EXISTS RecurringTask")
                
                // Drop TaskTemplate table and its indexes
                database.execSQL("DROP TABLE IF EXISTS TaskTemplate")
            }
        }


        private fun buildDatabase(appContext: Context): AppDb {
            val gson = Gson()
            return Room.databaseBuilder(
                appContext,
                AppDb::class.java,
                "cheermate_database"
            )
                .addTypeConverter(AppTypeConverters(gson))
                .addMigrations(MIGRATION_19_20, MIGRATION_20_21)
                .build()
        }
    }
}