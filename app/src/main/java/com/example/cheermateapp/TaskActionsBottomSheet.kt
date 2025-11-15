package com.example.cheermateapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import com.example.cheermateapp.util.ThemeManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Bottom sheet dialog for task actions including dark mode toggle
 */
class TaskActionsBottomSheet : BottomSheetDialogFragment() {

    private var onMarkCompletedListener: (() -> Unit)? = null
    private var onSnoozeListener: (() -> Unit)? = null
    private var onWontDoListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_task_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actionMarkCompleted = view.findViewById<LinearLayout>(R.id.action_mark_completed)
        val actionSnooze = view.findViewById<LinearLayout>(R.id.action_snooze)
        val actionWontDo = view.findViewById<LinearLayout>(R.id.action_wont_do)
        val actionDarkModeToggle = view.findViewById<LinearLayout>(R.id.action_dark_mode_toggle)
        val switchDarkMode = view.findViewById<SwitchCompat>(R.id.switch_dark_mode)

        // Set up dark mode switch
        val isDarkMode = ThemeManager.isDarkModeActive(requireContext())
        switchDarkMode.isChecked = isDarkMode

        // Mark as Completed click listener
        actionMarkCompleted.setOnClickListener {
            onMarkCompletedListener?.invoke()
            dismiss()
        }

        // Snooze click listener
        actionSnooze.setOnClickListener {
            onSnoozeListener?.invoke()
            dismiss()
        }

        // Won't Do click listener
        actionWontDo.setOnClickListener {
            onWontDoListener?.invoke()
            dismiss()
        }

        // Dark mode toggle click listener - toggle when clicking the entire row
        actionDarkModeToggle.setOnClickListener {
            switchDarkMode.isChecked = !switchDarkMode.isChecked
        }

        // Dark mode switch change listener
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.toggleDarkMode(requireContext())
            // Don't dismiss the bottom sheet - let the activity recreate
        }
    }

    // Setter methods for action listeners
    fun setOnMarkCompletedListener(listener: () -> Unit) {
        onMarkCompletedListener = listener
    }

    fun setOnSnoozeListener(listener: () -> Unit) {
        onSnoozeListener = listener
    }

    fun setOnWontDoListener(listener: () -> Unit) {
        onWontDoListener = listener
    }

    companion object {
        const val TAG = "TaskActionsBottomSheet"

        fun newInstance(): TaskActionsBottomSheet {
            return TaskActionsBottomSheet()
        }
    }
}
