// Type definitions migrated from Kotlin data models

export enum Priority {
  Low = 'Low',
  Medium = 'Medium',
  High = 'High',
}

export enum Status {
  Pending = 'Pending',
  InProgress = 'InProgress',
  Completed = 'Completed',
  Cancelled = 'Cancelled',
  OverDue = 'OverDue',
}

export enum Category {
  Work = 'Work',
  Personal = 'Personal',
  Shopping = 'Shopping',
  Others = 'Others',
}

export interface User {
  User_ID: number;
  Username: string;
  Email: string;
  PasswordHash: string;
  FirstName?: string;
  LastName?: string;
  Birthdate?: string;
  Personality_ID?: number;
  CreatedAt: number;
  UpdatedAt: number;
  DeletedAt?: number;
}

export interface Task {
  Task_ID: number;
  User_ID: number;
  Title: string;
  Description?: string;
  Category: Category;
  Priority: Priority;
  DueAt?: string;
  DueTime?: string;
  Status: Status;
  TaskProgress: number;
  CreatedAt: number;
  UpdatedAt: number;
  DeletedAt?: number;
}

export interface SubTask {
  SubTask_ID: number;
  Task_ID: number;
  User_ID: number;
  Title: string;
  IsCompleted: boolean;
  CreatedAt: number;
}

export interface TaskTemplate {
  Template_ID: number;
  User_ID: number;
  Title: string;
  Description?: string;
  Category: Category;
  Priority: Priority;
  CreatedAt: number;
}

export interface RecurringTask {
  Recurring_ID: number;
  User_ID: number;
  Title: string;
  Description?: string;
  Category: Category;
  Priority: Priority;
  Frequency: 'Daily' | 'Weekly' | 'Monthly' | 'Yearly';
  StartDate: string;
  EndDate?: string;
  LastGenerated?: number;
  IsActive: boolean;
  CreatedAt: number;
}

export interface TaskDependency {
  Dependency_ID: number;
  Task_ID: number;
  DependsOn_Task_ID: number;
  User_ID: number;
  CreatedAt: number;
}

export interface TaskReminder {
  Reminder_ID: number;
  Task_ID: number;
  User_ID: number;
  ReminderTime: number;
  IsTriggered: boolean;
  CreatedAt: number;
}

export interface Personality {
  Personality_ID: number;
  Name: string;
  Description: string;
  Icon?: string;
}

export interface Settings {
  Settings_ID: number;
  User_ID: number;
  Theme: 'Light' | 'Dark' | 'System';
  NotificationsEnabled: boolean;
  SoundEnabled: boolean;
  Language: string;
  UpdatedAt: number;
}

export interface SecurityQuestion {
  Question_ID: number;
  User_ID: number;
  Question: string;
  AnswerHash: string;
  CreatedAt: number;
}

export interface UserSecurityAnswer {
  Answer_ID: number;
  User_ID: number;
  Question_ID: number;
  AnswerHash: string;
  CreatedAt: number;
}

// Helper types for UI
export interface TaskWithSubTasks extends Task {
  subTasks?: SubTask[];
}

export interface TaskStats {
  total: number;
  completed: number;
  pending: number;
  inProgress: number;
  overdue: number;
}

export interface FilterOptions {
  status?: Status[];
  priority?: Priority[];
  category?: Category[];
  searchQuery?: string;
}

export interface SortOptions {
  field: 'DueAt' | 'Priority' | 'Title' | 'Status' | 'TaskProgress' | 'CreatedAt';
  order: 'asc' | 'desc';
}
