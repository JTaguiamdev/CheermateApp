package com.example.cheermateapp.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for managing user profile images
 */
object ProfileImageManager {
    
    private const val PREFS_NAME = "cheermate_profile_prefs"
    private const val KEY_PROFILE_IMAGE_PATH = "profile_image_path_"
    private const val PROFILE_IMAGE_DIR = "profile_images"
    private const val PROFILE_IMAGE_NAME = "profile_"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Get profile image path for user
     */
    fun getProfileImagePath(context: Context, userId: Int): String? {
        return getPreferences(context).getString("$KEY_PROFILE_IMAGE_PATH$userId", null)
    }
    
    /**
     * Save profile image for user
     */
    fun saveProfileImage(context: Context, userId: Int, imageUri: Uri): String? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val file = createImageFile(context, userId)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            val imagePath = file.absolutePath
            getPreferences(context).edit()
                .putString("$KEY_PROFILE_IMAGE_PATH$userId", imagePath)
                .apply()
            
            imagePath
        } catch (e: Exception) {
            android.util.Log.e("ProfileImageManager", "Error saving profile image", e)
            null
        }
    }
    
    /**
     * Save profile image from bitmap
     */
    fun saveProfileImageFromBitmap(context: Context, userId: Int, bitmap: Bitmap): String? {
        return try {
            val file = createImageFile(context, userId)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            val imagePath = file.absolutePath
            getPreferences(context).edit()
                .putString("$KEY_PROFILE_IMAGE_PATH$userId", imagePath)
                .apply()
            
            imagePath
        } catch (e: Exception) {
            android.util.Log.e("ProfileImageManager", "Error saving profile image", e)
            null
        }
    }
    
    /**
     * Load profile image bitmap
     */
    fun loadProfileImage(context: Context, userId: Int): Bitmap? {
        val imagePath = getProfileImagePath(context, userId)
        return if (imagePath != null) {
            try {
                BitmapFactory.decodeFile(imagePath)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Delete profile image
     */
    fun deleteProfileImage(context: Context, userId: Int): Boolean {
        val imagePath = getProfileImagePath(context, userId)
        return if (imagePath != null) {
            val file = File(imagePath)
            val deleted = file.delete()
            
            if (deleted) {
                getPreferences(context).edit()
                    .remove("$KEY_PROFILE_IMAGE_PATH$userId")
                    .apply()
            }
            
            deleted
        } else {
            false
        }
    }
    
    /**
     * Check if user has custom profile image
     */
    fun hasCustomProfileImage(context: Context, userId: Int): Boolean {
        val imagePath = getProfileImagePath(context, userId)
        return imagePath != null && File(imagePath).exists()
    }
    
    /**
     * Create image file for user
     */
    private fun createImageFile(context: Context, userId: Int): File {
        val storageDir = File(context.filesDir, PROFILE_IMAGE_DIR)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File(storageDir, "$PROFILE_IMAGE_NAME$userId.png")
    }
}
