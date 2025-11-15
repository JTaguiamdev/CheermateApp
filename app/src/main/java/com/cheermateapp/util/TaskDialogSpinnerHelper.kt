package com.cheermateapp.util

import android.content.Context
import android.widget.Spinner
import com.cheermateapp.IconSpinnerAdapter

/**
 * Helper object to set up spinners with icons for the Add Task dialog
 * 
 * Usage example:
 * ```kotlin
 * val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
 * val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
 * val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
 * val spinnerReminder = dialogView.findViewById<Spinner>(R.id.spinnerReminder)
 * 
 * TaskDialogSpinnerHelper.setupCategorySpinner(context, spinnerCategory)
 * TaskDialogSpinnerHelper.setupPrioritySpinner(context, spinnerPriority)
 * TaskDialogSpinnerHelper.setupReminderSpinner(context, spinnerReminder)
 * ```
 */
object TaskDialogSpinnerHelper {
    
    /**
     * Set up the Category spinner with icons
     * Categories: Work, Personal, Shopping, Others
     */
    fun setupCategorySpinner(context: Context, spinner: Spinner) {
        val categoryItems = listOf(
            IconSpinnerAdapter.SpinnerItem("💼", "Work"),
            IconSpinnerAdapter.SpinnerItem("👤", "Personal"),
            IconSpinnerAdapter.SpinnerItem("🛒", "Shopping"),
            IconSpinnerAdapter.SpinnerItem("📋", "Others")
        )
        
        val adapter = IconSpinnerAdapter(context, categoryItems)
        spinner.adapter = adapter
        spinner.setSelection(0) // Default to Work
    }
    
    /**
     * Set up the Priority spinner with icons
     * Priorities: Low, Medium, High
     */
    fun setupPrioritySpinner(context: Context, spinner: Spinner) {
        val priorityItems = listOf(
            IconSpinnerAdapter.SpinnerItem("🟢", "Low"),
            IconSpinnerAdapter.SpinnerItem("🟡", "Medium"),
            IconSpinnerAdapter.SpinnerItem("🔴", "High")
        )
        
        val adapter = IconSpinnerAdapter(context, priorityItems)
        spinner.adapter = adapter
        spinner.setSelection(1) // Default to Medium
    }
    
    /**
     * Set up the Reminder spinner with icons
     * Options: None, 10 minutes before, 30 minutes before, At specific time
     */
    fun setupReminderSpinner(context: Context, spinner: Spinner) {
        val reminderItems = listOf(
            IconSpinnerAdapter.SpinnerItem("🔕", "None"),
            IconSpinnerAdapter.SpinnerItem("⏰", "10 minutes before"),
            IconSpinnerAdapter.SpinnerItem("⏰", "30 minutes before"),
            IconSpinnerAdapter.SpinnerItem("🕐", "At specific time")
        )
        
        val adapter = IconSpinnerAdapter(context, reminderItems)
        spinner.adapter = adapter
        spinner.setSelection(0) // Default to None
    }
    
    /**
     * Get the selected category as a string
     */
    fun getSelectedCategory(spinner: Spinner): String {
        val item = spinner.selectedItem as? IconSpinnerAdapter.SpinnerItem
        return item?.text ?: "Work"
    }
    
    /**
     * Get the selected priority as a string
     */
    fun getSelectedPriority(spinner: Spinner): String {
        val item = spinner.selectedItem as? IconSpinnerAdapter.SpinnerItem
        return item?.text ?: "Medium"
    }
    
    /**
     * Get the selected reminder option as a string
     */
    fun getSelectedReminder(spinner: Spinner): String {
        val item = spinner.selectedItem as? IconSpinnerAdapter.SpinnerItem
        return item?.text ?: "None"
    }
}
