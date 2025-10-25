import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Crypto from 'expo-crypto';
import { databaseService } from './database';
import { User } from '../models/types';

const CURRENT_USER_KEY = '@current_user';
const SESSION_TOKEN_KEY = '@session_token';

export class AuthService {
  private currentUser: User | null = null;

  async hashPassword(password: string): Promise<string> {
    // Using SHA256 for password hashing (in production, use a proper library like bcrypt)
    const hash = await Crypto.digestStringAsync(
      Crypto.CryptoDigestAlgorithm.SHA256,
      password
    );
    return hash;
  }

  async register(
    username: string,
    email: string,
    password: string,
    firstName?: string,
    lastName?: string,
    personalityId?: number
  ): Promise<{ success: boolean; error?: string; userId?: number }> {
    try {
      // Check if username already exists
      const existingUser = await databaseService.getUserByUsername(username);
      if (existingUser) {
        return { success: false, error: 'Username already exists' };
      }

      // Check if email already exists
      const existingEmail = await databaseService.getUserByEmail(email);
      if (existingEmail) {
        return { success: false, error: 'Email already exists' };
      }

      // Hash password
      const passwordHash = await this.hashPassword(password);

      // Create user
      const now = Date.now();
      const userId = await databaseService.createUser({
        Username: username,
        Email: email,
        PasswordHash: passwordHash,
        FirstName: firstName,
        LastName: lastName,
        Personality_ID: personalityId,
        CreatedAt: now,
        UpdatedAt: now,
      });

      return { success: true, userId };
    } catch (error) {
      console.error('Registration error:', error);
      return { success: false, error: 'Failed to register user' };
    }
  }

  async login(
    usernameOrEmail: string,
    password: string
  ): Promise<{ success: boolean; error?: string; user?: User }> {
    try {
      // Try to find user by username or email
      let user = await databaseService.getUserByUsername(usernameOrEmail);
      if (!user) {
        user = await databaseService.getUserByEmail(usernameOrEmail);
      }

      if (!user) {
        return { success: false, error: 'User not found' };
      }

      // Verify password
      const passwordHash = await this.hashPassword(password);
      if (passwordHash !== user.PasswordHash) {
        return { success: false, error: 'Invalid password' };
      }

      // Store session
      await this.setCurrentUser(user);

      return { success: true, user };
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, error: 'Failed to login' };
    }
  }

  async logout(): Promise<void> {
    try {
      await AsyncStorage.multiRemove([CURRENT_USER_KEY, SESSION_TOKEN_KEY]);
      this.currentUser = null;
    } catch (error) {
      console.error('Logout error:', error);
    }
  }

  async getCurrentUser(): Promise<User | null> {
    if (this.currentUser) {
      return this.currentUser;
    }

    try {
      const userJson = await AsyncStorage.getItem(CURRENT_USER_KEY);
      if (userJson) {
        this.currentUser = JSON.parse(userJson);
        return this.currentUser;
      }
    } catch (error) {
      console.error('Error getting current user:', error);
    }

    return null;
  }

  async setCurrentUser(user: User): Promise<void> {
    try {
      await AsyncStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user));
      this.currentUser = user;
    } catch (error) {
      console.error('Error setting current user:', error);
    }
  }

  async isAuthenticated(): Promise<boolean> {
    const user = await this.getCurrentUser();
    return user !== null;
  }

  async updatePassword(
    userId: number,
    oldPassword: string,
    newPassword: string
  ): Promise<{ success: boolean; error?: string }> {
    try {
      const user = await databaseService.getUserById(userId);
      if (!user) {
        return { success: false, error: 'User not found' };
      }

      // Verify old password
      const oldPasswordHash = await this.hashPassword(oldPassword);
      if (oldPasswordHash !== user.PasswordHash) {
        return { success: false, error: 'Invalid current password' };
      }

      // Hash new password
      const newPasswordHash = await this.hashPassword(newPassword);

      // Update password in database
      const db = await databaseService.getDatabase();
      await db.runAsync(
        'UPDATE User SET PasswordHash = ?, UpdatedAt = ? WHERE User_ID = ?',
        [newPasswordHash, Date.now(), userId]
      );

      return { success: true };
    } catch (error) {
      console.error('Password update error:', error);
      return { success: false, error: 'Failed to update password' };
    }
  }

  async resetPassword(
    email: string,
    securityAnswer: string,
    newPassword: string
  ): Promise<{ success: boolean; error?: string }> {
    try {
      // In a real app, you'd verify the security answer here
      const user = await databaseService.getUserByEmail(email);
      if (!user) {
        return { success: false, error: 'User not found' };
      }

      // Hash new password
      const newPasswordHash = await this.hashPassword(newPassword);

      // Update password in database
      const db = await databaseService.getDatabase();
      await db.runAsync(
        'UPDATE User SET PasswordHash = ?, UpdatedAt = ? WHERE User_ID = ?',
        [newPasswordHash, Date.now(), user.User_ID]
      );

      return { success: true };
    } catch (error) {
      console.error('Password reset error:', error);
      return { success: false, error: 'Failed to reset password' };
    }
  }
}

export const authService = new AuthService();
