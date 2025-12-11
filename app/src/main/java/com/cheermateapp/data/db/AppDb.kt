package com.cheermateapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cheermateapp.data.dao.*
import com.cheermateapp.data.model.*
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
        MessageTemplate::class
    ],
    version = 38,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDb : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun subTaskDao(): SubTaskDao
    abstract fun taskReminderDao(): TaskReminderDao
    abstract fun securityDao(): SecurityDao
    abstract fun personalityDao(): PersonalityDao
    abstract fun messageTemplateDao(): MessageTemplateDao

    companion object {
        private const val TAG = "AppDb"
        private const val DATABASE_NAME = "cheermate_database"

        @Volatile
        private var INSTANCE: AppDb? = null

        fun get(context: Context): AppDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        // Migration definitions
        private val MIGRATION_19_20 = createMigration(19, 20) { db ->
            // Create new table with NOT NULL constraints
            db.execSQL("""
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
            """.trimIndent())

            // Copy data from old table to new table
            db.execSQL("""
                INSERT INTO User_new (User_ID, Username, Email, PasswordHash, FirstName, LastName, Birthdate, Personality_ID, CreatedAt, UpdatedAt, DeletedAt)
                SELECT User_ID, Username, Email, PasswordHash, COALESCE(FirstName, ''), COALESCE(LastName, ''), Birthdate, Personality_ID, CreatedAt, UpdatedAt, DeletedAt FROM User
            """.trimIndent())

            // Replace old table
            replaceTable(db, "User", "User_new")

            // Re-create indexes
            createIndexesForUser(db)
        }

        private val MIGRATION_20_21 = createMigration(20, 21) { db ->
            // Drop unused tables
            db.execSQL("DROP TABLE IF EXISTS RecurringTask")
            db.execSQL("DROP TABLE IF EXISTS TaskTemplate")
        }



        private val MIGRATION_22_23 = createMigration(22, 23) { db ->
            db.execSQL("ALTER TABLE MessageTemplate RENAME COLUMN TextTemplate TO MessageText")
        }

        private val MIGRATION_23_24 = createMigration(23, 24) { db ->
            recreateTableWithSchema(db, "Personality",
                createSql = """
                    CREATE TABLE Personality_new (
                        Personality_ID INTEGER NOT NULL PRIMARY KEY,
                        Name TEXT NOT NULL,
                        Description TEXT NOT NULL
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO Personality_new (Personality_ID, Name, Description)
                    SELECT Personality_ID, Name, Description FROM Personality
                """.trimIndent()
            )
        }

        private val MIGRATION_24_25 = createMigration(24, 25) { db ->
            recreateTableWithSchema(db, "User",
                createSql = """
                    CREATE TABLE User_new (
                        User_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        Username TEXT NOT NULL,
                        Email TEXT NOT NULL,
                        PasswordHash TEXT NOT NULL,
                        FirstName TEXT NOT NULL DEFAULT '',
                        LastName TEXT NOT NULL DEFAULT '',
                        Personality_ID INTEGER,
                        CreatedAt INTEGER NOT NULL,
                        UpdatedAt INTEGER NOT NULL,
                        DeletedAt INTEGER
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO User_new (User_ID, Username, Email, PasswordHash, FirstName, LastName, Personality_ID, CreatedAt, UpdatedAt, DeletedAt)
                    SELECT User_ID, Username, Email, PasswordHash, FirstName, LastName, Personality_ID, CreatedAt, UpdatedAt, DeletedAt FROM User
                """.trimIndent()
            ) { createIndexesForUser(it) }
        }



        private val MIGRATION_26_27 = createMigration(26, 27) { db ->
            recreateTableWithSchema(db, "SubTask",
                createSql = """
                    CREATE TABLE SubTask_new (
                        SubTask_ID INTEGER NOT NULL,
                        Task_ID INTEGER NOT NULL,
                        User_ID INTEGER NOT NULL,
                        Name TEXT NOT NULL,
                        IsCompleted INTEGER NOT NULL,
                        CreatedAt INTEGER NOT NULL,
                        PRIMARY KEY(Task_ID, User_ID, SubTask_ID),
                        FOREIGN KEY(Task_ID, User_ID) REFERENCES Task(Task_ID, User_ID) ON DELETE CASCADE
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO SubTask_new (SubTask_ID, Task_ID, User_ID, Name, IsCompleted, CreatedAt)
                    SELECT SubTask_ID, Task_ID, User_ID, Name, IsCompleted, CreatedAt FROM SubTask
                """.trimIndent()
            ) {
                it.execSQL("CREATE INDEX IF NOT EXISTS index_SubTask_Task_ID ON SubTask(Task_ID)")
                it.execSQL("CREATE INDEX IF NOT EXISTS index_SubTask_User_ID ON SubTask(User_ID)")
            }
        }

        private val MIGRATION_27_28 = createMigration(27, 28) { db ->
            // Recreate SecurityQuestion table
            recreateTableWithSchema(db, "SecurityQuestion",
                createSql = """
                    CREATE TABLE SecurityQuestion_new (
                        Question_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        Prompt TEXT NOT NULL
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO SecurityQuestion_new (Question_ID, Prompt)
                    SELECT SecurityQuestion_ID, Prompt FROM SecurityQuestion
                """.trimIndent()
            )

            // Recreate UserSecurityAnswer table
            recreateTableWithSchema(db, "UserSecurityAnswer",
                createSql = """
                    CREATE TABLE UserSecurityAnswer_new (
                        Answer_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        User_ID INTEGER NOT NULL,
                        Question_ID INTEGER NOT NULL,
                        AnswerHash TEXT NOT NULL,
                        FOREIGN KEY(Question_ID) REFERENCES SecurityQuestion(Question_ID) ON DELETE CASCADE
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO UserSecurityAnswer_new (Answer_ID, User_ID, Question_ID, AnswerHash)
                    SELECT Answer_ID, User_ID, Question_ID, AnswerHash FROM UserSecurityAnswer
                """.trimIndent()
            ) {
                it.execSQL("CREATE INDEX IF NOT EXISTS index_UserSecurityAnswer_User_ID ON UserSecurityAnswer(User_ID)")
                it.execSQL("CREATE INDEX IF NOT EXISTS index_UserSecurityAnswer_Question_ID ON UserSecurityAnswer(Question_ID)")
            }
        }

        private val MIGRATION_29_30 = createMigration(29, 30) { db ->
            // Same as migration 27_28 for version jump
            MIGRATION_27_28.migrate(db)
        }

        private val MIGRATION_30_31 = createMigration(30, 31) { db ->
            // No schema changes, just version bump for TypeConverter update
        }





        private val MIGRATION_33_34 = createMigration(33, 34) { db ->
            recreateTableWithSchema(db, "User",
                createSql = """
                    CREATE TABLE User_new (
                        User_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        Username TEXT NOT NULL,
                        Email TEXT NOT NULL,
                        PasswordHash TEXT NOT NULL,
                        FirstName TEXT NOT NULL DEFAULT '',
                        LastName TEXT NOT NULL DEFAULT '',
                        Personality_ID INTEGER,
                        CreatedAt INTEGER NOT NULL,
                        UpdatedAt INTEGER NOT NULL
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO User_new (User_ID, Username, Email, PasswordHash, FirstName, LastName, Personality_ID, CreatedAt, UpdatedAt)
                    SELECT User_ID, Username, Email, PasswordHash, FirstName, LastName, Personality_ID, CreatedAt, UpdatedAt FROM User
                """.trimIndent()
            ) { createIndexesForUser(it) }
        }

        private val MIGRATION_34_35 = createMigration(34, 35) { db ->
            db.execSQL("DROP TABLE IF EXISTS TaskDependency")
        }
        
        private val MIGRATION_35_36 = createMigration(35, 36) { db ->
            recreateTableWithSchema(db, "User",
                createSql = """
                    CREATE TABLE User_new (
                        User_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        Username TEXT NOT NULL,
                        Email TEXT NOT NULL,
                        PasswordHash TEXT NOT NULL,
                        FirstName TEXT NOT NULL DEFAULT '',
                        LastName TEXT NOT NULL DEFAULT '',
                        Personality_ID INTEGER,
                        CreatedAt TEXT NOT NULL,
                        UpdatedAt TEXT NOT NULL
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO User_new (User_ID, Username, Email, PasswordHash, FirstName, LastName, Personality_ID, CreatedAt, UpdatedAt)
                    SELECT User_ID, Username, Email, PasswordHash, FirstName, LastName, Personality_ID, datetime(CreatedAt/1000, 'unixepoch'), datetime(UpdatedAt/1000, 'unixepoch') FROM User
                """.trimIndent()
            ) { createIndexesForUser(it) }

            recreateTableWithSchema(db, "Task",
                createSql = """
                    CREATE TABLE Task_new (
                        Task_ID INTEGER NOT NULL,
                        User_ID INTEGER NOT NULL,
                        Title TEXT NOT NULL,
                        Description TEXT,
                        Category TEXT NOT NULL,
                        Priority TEXT NOT NULL,
                        DueAt TEXT,
                        DueTime TEXT,
                        Status TEXT NOT NULL,
                        TaskProgress INTEGER NOT NULL,
                        CreatedAt TEXT NOT NULL,
                        UpdatedAt TEXT NOT NULL,
                        DeletedAt INTEGER,
                        PRIMARY KEY(Task_ID, User_ID),
                        FOREIGN KEY(User_ID) REFERENCES User(User_ID) ON DELETE CASCADE
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO Task_new (Task_ID, User_ID, Title, Description, Category, Priority, DueAt, DueTime, Status, TaskProgress, CreatedAt, UpdatedAt, DeletedAt)
                    SELECT Task_ID, User_ID, Title, Description, Category, Priority, DueAt, DueTime, Status, TaskProgress, datetime(CreatedAt/1000, 'unixepoch'), datetime(UpdatedAt/1000, 'unixepoch'), DeletedAt FROM Task
                """.trimIndent()
            ) {
                it.execSQL("CREATE INDEX IF NOT EXISTS index_Task_User_ID ON Task(User_ID)")
            }

            recreateTableWithSchema(db, "TaskReminder",
                createSql = """
                    CREATE TABLE TaskReminder_new (
                        TaskReminder_ID INTEGER NOT NULL,
                        Task_ID INTEGER NOT NULL,
                        User_ID INTEGER NOT NULL,
                        RemindAt INTEGER NOT NULL,
                        ReminderType TEXT,
                        IsActive INTEGER NOT NULL,
                        CreatedAt TEXT NOT NULL,
                        UpdatedAt TEXT NOT NULL,
                        PRIMARY KEY(TaskReminder_ID, Task_ID, User_ID),
                        FOREIGN KEY(Task_ID, User_ID) REFERENCES Task(Task_ID, User_ID) ON DELETE CASCADE
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO TaskReminder_new (TaskReminder_ID, Task_ID, User_ID, RemindAt, ReminderType, IsActive, CreatedAt, UpdatedAt)
                    SELECT TaskReminder_ID, Task_ID, User_ID, RemindAt, ReminderType, IsActive, datetime(CreatedAt/1000, 'unixepoch'), datetime(UpdatedAt/1000, 'unixepoch') FROM TaskReminder
                """.trimIndent()
            ) {
                it.execSQL("CREATE INDEX IF NOT EXISTS index_TaskReminder_Task_ID ON TaskReminder(Task_ID)")
                it.execSQL("CREATE INDEX IF NOT EXISTS index_TaskReminder_User_ID ON TaskReminder(User_ID)")
                it.execSQL("CREATE INDEX IF NOT EXISTS `index_TaskReminder_Task_ID_User_ID` ON `TaskReminder` (`Task_ID`, `User_ID`)")
            }

            recreateTableWithSchema(db, "SubTask",
                createSql = """
                    CREATE TABLE SubTask_new (
                        SubTask_ID INTEGER NOT NULL,
                        Task_ID INTEGER NOT NULL,
                        User_ID INTEGER NOT NULL,
                        Name TEXT NOT NULL,
                        IsCompleted INTEGER NOT NULL,
                        CreatedAt TEXT NOT NULL,
                        UpdatedAt TEXT NOT NULL,
                        PRIMARY KEY(Task_ID, User_ID, SubTask_ID),
                        FOREIGN KEY(Task_ID, User_ID) REFERENCES Task(Task_ID, User_ID) ON DELETE CASCADE
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO SubTask_new (SubTask_ID, Task_ID, User_ID, Name, IsCompleted, CreatedAt, UpdatedAt)
                    SELECT SubTask_ID, Task_ID, User_ID, Name, IsCompleted, datetime(CreatedAt/1000, 'unixepoch'), datetime(CreatedAt/1000, 'unixepoch') FROM SubTask
                """.trimIndent()
            ) {
                it.execSQL("CREATE INDEX IF NOT EXISTS index_SubTask_Task_ID ON SubTask(Task_ID)")
                it.execSQL("CREATE INDEX IF NOT EXISTS index_SubTask_User_ID ON SubTask(User_ID)")
            }
        }

        private val MIGRATION_36_37 = createMigration(36, 37) { db ->
            recreateTableWithSchema(db, "Task",
                createSql = """
                    CREATE TABLE Task_new (
                        Task_ID INTEGER NOT NULL,
                        User_ID INTEGER NOT NULL,
                        Title TEXT NOT NULL,
                        Description TEXT,
                        Category TEXT NOT NULL,
                        Priority TEXT NOT NULL,
                        DueAt TEXT,
                        DueTime TEXT,
                        Status TEXT NOT NULL,
                        TaskProgress INTEGER NOT NULL,
                        CreatedAt TEXT NOT NULL,
                        UpdatedAt TEXT NOT NULL,
                        PRIMARY KEY(Task_ID, User_ID),
                        FOREIGN KEY(User_ID) REFERENCES User(User_ID) ON DELETE CASCADE
                    )
                """.trimIndent(),
                copySql = """
                    INSERT INTO Task_new (Task_ID, User_ID, Title, Description, Category, Priority, DueAt, DueTime, Status, TaskProgress, CreatedAt, UpdatedAt)
                    SELECT Task_ID, User_ID, Title, Description, Category, Priority, DueAt, DueTime, Status, TaskProgress, CreatedAt, UpdatedAt FROM Task
                """.trimIndent()
            ) {
                it.execSQL("CREATE INDEX IF NOT EXISTS index_Task_User_ID ON Task(User_ID)")
            }
        }

        private val MIGRATION_37_38 = createMigration(37, 38) { db ->
            db.execSQL("DROP TABLE IF EXISTS UserSettings")
        }

        private fun buildDatabase(appContext: Context): AppDb {
            return Room.databaseBuilder(
                appContext,
                AppDb::class.java,
                DATABASE_NAME
            )
                .addMigrations(
                    MIGRATION_19_20,
                    MIGRATION_20_21,
                    MIGRATION_22_23,
                    MIGRATION_23_24,
                    MIGRATION_24_25,
                    MIGRATION_26_27,
                    MIGRATION_33_34,
                    MIGRATION_34_35,
                    MIGRATION_35_36,
                    MIGRATION_36_37,
                    MIGRATION_37_38
                )
                .addTypeConverter(AppTypeConverters(Gson()))
                .build()
        }

        // Helper functions for common migration patterns
        private fun createMigration(
            startVersion: Int,
            endVersion: Int,
            migrate: (SupportSQLiteDatabase) -> Unit
        ): Migration {
            return object : Migration(startVersion, endVersion) {
                override fun migrate(db: SupportSQLiteDatabase) = migrate(db)
            }
        }

        private fun recreateTableWithSchema(
            db: SupportSQLiteDatabase,
            tableName: String,
            createSql: String,
            copySql: String,
            postMigration: (SupportSQLiteDatabase) -> Unit = {}
        ) {
            // Create new table
            db.execSQL(createSql)

            // Copy data
            db.execSQL(copySql)

            // Replace table
            replaceTable(db, tableName, "${tableName}_new")

            // Run post-migration tasks
            postMigration(db)
        }

        private fun replaceTable(
            db: SupportSQLiteDatabase,
            oldTableName: String,
            newTableName: String
        ) {
            db.execSQL("DROP TABLE $oldTableName")
            db.execSQL("ALTER TABLE $newTableName RENAME TO $oldTableName")
        }

        private fun createIndexesForUser(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_User_Username ON User(Username)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_User_Email ON User(Email)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_User_Personality_ID ON User(Personality_ID)")
        }
    }
}