import * as SQLite from 'expo-sqlite';
import { Task, User, SubTask, TaskTemplate, RecurringTask, Priority, Status, Category } from '../models/types';

const DB_NAME = 'cheermate.db';

class DatabaseService {
  private db: SQLite.SQLiteDatabase | null = null;

  async init(): Promise<void> {
    try {
      this.db = await SQLite.openDatabaseAsync(DB_NAME);
      await this.createTables();
      console.log('Database initialized successfully');
    } catch (error) {
      console.error('Error initializing database:', error);
      throw error;
    }
  }

  private async createTables(): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    // Create User table
    await this.db.execAsync(`
      CREATE TABLE IF NOT EXISTS User (
        User_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        Username TEXT UNIQUE NOT NULL,
        Email TEXT UNIQUE NOT NULL,
        PasswordHash TEXT NOT NULL,
        FirstName TEXT,
        LastName TEXT,
        Birthdate TEXT,
        Personality_ID INTEGER,
        CreatedAt INTEGER NOT NULL,
        UpdatedAt INTEGER NOT NULL,
        DeletedAt INTEGER
      );
    `);

    // Create Task table
    await this.db.execAsync(`
      CREATE TABLE IF NOT EXISTS Task (
        Task_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        User_ID INTEGER NOT NULL,
        Title TEXT NOT NULL,
        Description TEXT,
        Category TEXT NOT NULL,
        Priority TEXT NOT NULL,
        DueAt TEXT,
        DueTime TEXT,
        Status TEXT NOT NULL,
        TaskProgress INTEGER DEFAULT 0,
        CreatedAt INTEGER NOT NULL,
        UpdatedAt INTEGER NOT NULL,
        DeletedAt INTEGER,
        FOREIGN KEY (User_ID) REFERENCES User(User_ID) ON DELETE CASCADE
      );
    `);

    // Create index on User_ID for faster queries
    await this.db.execAsync(`
      CREATE INDEX IF NOT EXISTS idx_task_user ON Task(User_ID);
    `);

    // Create SubTask table
    await this.db.execAsync(`
      CREATE TABLE IF NOT EXISTS SubTask (
        SubTask_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        Task_ID INTEGER NOT NULL,
        User_ID INTEGER NOT NULL,
        Title TEXT NOT NULL,
        IsCompleted INTEGER DEFAULT 0,
        CreatedAt INTEGER NOT NULL,
        FOREIGN KEY (Task_ID) REFERENCES Task(Task_ID) ON DELETE CASCADE,
        FOREIGN KEY (User_ID) REFERENCES User(User_ID) ON DELETE CASCADE
      );
    `);

    // Create TaskTemplate table
    await this.db.execAsync(`
      CREATE TABLE IF NOT EXISTS TaskTemplate (
        Template_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        User_ID INTEGER NOT NULL,
        Title TEXT NOT NULL,
        Description TEXT,
        Category TEXT NOT NULL,
        Priority TEXT NOT NULL,
        CreatedAt INTEGER NOT NULL,
        FOREIGN KEY (User_ID) REFERENCES User(User_ID) ON DELETE CASCADE
      );
    `);

    // Create RecurringTask table
    await this.db.execAsync(`
      CREATE TABLE IF NOT EXISTS RecurringTask (
        Recurring_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        User_ID INTEGER NOT NULL,
        Title TEXT NOT NULL,
        Description TEXT,
        Category TEXT NOT NULL,
        Priority TEXT NOT NULL,
        Frequency TEXT NOT NULL,
        StartDate TEXT NOT NULL,
        EndDate TEXT,
        LastGenerated INTEGER,
        IsActive INTEGER DEFAULT 1,
        CreatedAt INTEGER NOT NULL,
        FOREIGN KEY (User_ID) REFERENCES User(User_ID) ON DELETE CASCADE
      );
    `);

    // Create TaskDependency table
    await this.db.execAsync(`
      CREATE TABLE IF NOT EXISTS TaskDependency (
        Dependency_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        Task_ID INTEGER NOT NULL,
        DependsOn_Task_ID INTEGER NOT NULL,
        User_ID INTEGER NOT NULL,
        CreatedAt INTEGER NOT NULL,
        FOREIGN KEY (Task_ID) REFERENCES Task(Task_ID) ON DELETE CASCADE,
        FOREIGN KEY (DependsOn_Task_ID) REFERENCES Task(Task_ID) ON DELETE CASCADE,
        FOREIGN KEY (User_ID) REFERENCES User(User_ID) ON DELETE CASCADE
      );
    `);

    // Create Settings table
    await this.db.execAsync(`
      CREATE TABLE IF NOT EXISTS Settings (
        Settings_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        User_ID INTEGER UNIQUE NOT NULL,
        Theme TEXT DEFAULT 'System',
        NotificationsEnabled INTEGER DEFAULT 1,
        SoundEnabled INTEGER DEFAULT 1,
        Language TEXT DEFAULT 'en',
        UpdatedAt INTEGER NOT NULL,
        FOREIGN KEY (User_ID) REFERENCES User(User_ID) ON DELETE CASCADE
      );
    `);

    // Create Personality table
    await this.db.execAsync(`
      CREATE TABLE IF NOT EXISTS Personality (
        Personality_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        Name TEXT NOT NULL,
        Description TEXT NOT NULL,
        Icon TEXT
      );
    `);

    // Insert default personalities if not exists
    await this.insertDefaultPersonalities();
  }

  private async insertDefaultPersonalities(): Promise<void> {
    if (!this.db) return;

    const result = await this.db.getFirstAsync<{ count: number }>(
      'SELECT COUNT(*) as count FROM Personality'
    );

    if (result && result.count === 0) {
      await this.db.runAsync(`
        INSERT INTO Personality (Name, Description, Icon) VALUES
        ('Motivator', 'Energetic and encouraging, pushes you to do your best', '🔥'),
        ('Calm', 'Peaceful and understanding, helps you stay relaxed', '🧘'),
        ('Analytical', 'Logical and organized, helps you plan effectively', '📊'),
        ('Creative', 'Imaginative and inspiring, encourages creative thinking', '🎨'),
        ('Friendly', 'Warm and supportive, like a helpful friend', '🤗');
      `);
    }
  }

  async getDatabase(): Promise<SQLite.SQLiteDatabase> {
    if (!this.db) {
      await this.init();
    }
    return this.db!;
  }

  // User operations
  async createUser(user: Omit<User, 'User_ID'>): Promise<number> {
    const db = await this.getDatabase();
    const result = await db.runAsync(
      `INSERT INTO User (Username, Email, PasswordHash, FirstName, LastName, Birthdate, Personality_ID, CreatedAt, UpdatedAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        user.Username,
        user.Email,
        user.PasswordHash,
        user.FirstName || null,
        user.LastName || null,
        user.Birthdate || null,
        user.Personality_ID || null,
        user.CreatedAt,
        user.UpdatedAt,
      ]
    );
    return result.lastInsertRowId;
  }

  async getUserByUsername(username: string): Promise<User | null> {
    const db = await this.getDatabase();
    const user = await db.getFirstAsync<User>(
      'SELECT * FROM User WHERE Username = ? AND DeletedAt IS NULL',
      [username]
    );
    return user || null;
  }

  async getUserByEmail(email: string): Promise<User | null> {
    const db = await this.getDatabase();
    const user = await db.getFirstAsync<User>(
      'SELECT * FROM User WHERE Email = ? AND DeletedAt IS NULL',
      [email]
    );
    return user || null;
  }

  async getUserById(userId: number): Promise<User | null> {
    const db = await this.getDatabase();
    const user = await db.getFirstAsync<User>(
      'SELECT * FROM User WHERE User_ID = ? AND DeletedAt IS NULL',
      [userId]
    );
    return user || null;
  }

  // Task operations
  async createTask(task: Omit<Task, 'Task_ID'>): Promise<number> {
    const db = await this.getDatabase();
    const result = await db.runAsync(
      `INSERT INTO Task (User_ID, Title, Description, Category, Priority, DueAt, DueTime, Status, TaskProgress, CreatedAt, UpdatedAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        task.User_ID,
        task.Title,
        task.Description || null,
        task.Category,
        task.Priority,
        task.DueAt || null,
        task.DueTime || null,
        task.Status,
        task.TaskProgress,
        task.CreatedAt,
        task.UpdatedAt,
      ]
    );
    return result.lastInsertRowId;
  }

  async getTasksByUser(userId: number): Promise<Task[]> {
    const db = await this.getDatabase();
    const tasks = await db.getAllAsync<Task>(
      'SELECT * FROM Task WHERE User_ID = ? AND DeletedAt IS NULL ORDER BY CreatedAt DESC',
      [userId]
    );
    return tasks;
  }

  async getTaskById(taskId: number, userId: number): Promise<Task | null> {
    const db = await this.getDatabase();
    const task = await db.getFirstAsync<Task>(
      'SELECT * FROM Task WHERE Task_ID = ? AND User_ID = ? AND DeletedAt IS NULL',
      [taskId, userId]
    );
    return task || null;
  }

  async updateTask(taskId: number, userId: number, updates: Partial<Task>): Promise<void> {
    const db = await this.getDatabase();
    const fields = Object.keys(updates).filter(k => k !== 'Task_ID' && k !== 'User_ID');
    const values = fields.map(f => (updates as any)[f]);
    
    const setClause = fields.map(f => `${f} = ?`).join(', ');
    await db.runAsync(
      `UPDATE Task SET ${setClause}, UpdatedAt = ? WHERE Task_ID = ? AND User_ID = ?`,
      [...values, Date.now(), taskId, userId]
    );
  }

  async deleteTask(taskId: number, userId: number): Promise<void> {
    const db = await this.getDatabase();
    await db.runAsync(
      'UPDATE Task SET DeletedAt = ? WHERE Task_ID = ? AND User_ID = ?',
      [Date.now(), taskId, userId]
    );
  }

  async hardDeleteTask(taskId: number, userId: number): Promise<void> {
    const db = await this.getDatabase();
    await db.runAsync(
      'DELETE FROM Task WHERE Task_ID = ? AND User_ID = ?',
      [taskId, userId]
    );
  }

  // SubTask operations
  async createSubTask(subTask: Omit<SubTask, 'SubTask_ID'>): Promise<number> {
    const db = await this.getDatabase();
    const result = await db.runAsync(
      `INSERT INTO SubTask (Task_ID, User_ID, Title, IsCompleted, CreatedAt)
       VALUES (?, ?, ?, ?, ?)`,
      [
        subTask.Task_ID,
        subTask.User_ID,
        subTask.Title,
        subTask.IsCompleted ? 1 : 0,
        subTask.CreatedAt,
      ]
    );
    return result.lastInsertRowId;
  }

  async getSubTasksByTask(taskId: number): Promise<SubTask[]> {
    const db = await this.getDatabase();
    const subTasks = await db.getAllAsync<SubTask>(
      'SELECT * FROM SubTask WHERE Task_ID = ? ORDER BY CreatedAt ASC',
      [taskId]
    );
    return subTasks.map(st => ({ ...st, IsCompleted: !!st.IsCompleted }));
  }

  async updateSubTask(subTaskId: number, updates: Partial<SubTask>): Promise<void> {
    const db = await this.getDatabase();
    const fields = Object.keys(updates).filter(k => k !== 'SubTask_ID');
    const values = fields.map(f => {
      const val = (updates as any)[f];
      return f === 'IsCompleted' ? (val ? 1 : 0) : val;
    });
    
    const setClause = fields.map(f => `${f} = ?`).join(', ');
    await db.runAsync(
      `UPDATE SubTask SET ${setClause} WHERE SubTask_ID = ?`,
      [...values, subTaskId]
    );
  }

  async deleteSubTask(subTaskId: number): Promise<void> {
    const db = await this.getDatabase();
    await db.runAsync('DELETE FROM SubTask WHERE SubTask_ID = ?', [subTaskId]);
  }
}

export const databaseService = new DatabaseService();
