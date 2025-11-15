package com.example.cheermateapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Example activity demonstrating how to use TaskActionsBottomSheet
 * 
 * Usage in any activity:
 * 
 * val bottomSheet = TaskActionsBottomSheet.newInstance()
 * 
 * bottomSheet.setOnMarkCompletedListener {
 *     // Handle mark as completed action
 *     Toast.makeText(this, "Task marked as completed", Toast.LENGTH_SHORT).show()
 * }
 * 
 * bottomSheet.setOnSnoozeListener {
 *     // Handle snooze action
 *     Toast.makeText(this, "Task snoozed", Toast.LENGTH_SHORT).show()
 * }
 * 
 * bottomSheet.setOnWontDoListener {
 *     // Handle won't do action
 *     Toast.makeText(this, "Task marked as won't do", Toast.LENGTH_SHORT).show()
 * }
 * 
 * bottomSheet.show(supportFragmentManager, TaskActionsBottomSheet.TAG)
 */
class TaskActionsExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Example: Show the bottom sheet when a button is clicked
        // You can trigger this from anywhere in your activity
        showTaskActionsBottomSheet()
    }

    private fun showTaskActionsBottomSheet() {
        val bottomSheet = TaskActionsBottomSheet.newInstance()
        
        bottomSheet.setOnMarkCompletedListener {
            Toast.makeText(this, "Task marked as completed", Toast.LENGTH_SHORT).show()
            // Add your logic to mark the task as completed
        }
        
        bottomSheet.setOnSnoozeListener {
            Toast.makeText(this, "Task snoozed", Toast.LENGTH_SHORT).show()
            // Add your logic to snooze the task
        }
        
        bottomSheet.setOnWontDoListener {
            Toast.makeText(this, "Task marked as won't do", Toast.LENGTH_SHORT).show()
            // Add your logic to mark the task as won't do
        }
        
        bottomSheet.show(supportFragmentManager, TaskActionsBottomSheet.TAG)
    }
}
