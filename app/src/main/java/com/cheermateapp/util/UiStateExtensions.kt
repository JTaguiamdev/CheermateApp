package com.cheermateapp.util

import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cheermateapp.data.repository.UiState
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Extension functions for Activities to easily observe UiState and handle loading/error states
 */

/**
 * Observe a StateFlow with automatic loading indicator and error handling
 * 
 * @param stateFlow The StateFlow to observe
 * @param progressBar Optional ProgressBar to show/hide during loading
 * @param onLoading Optional callback for loading state
 * @param onSuccess Callback for success state with data
 * @param onError Optional callback for error state
 */
fun <T> AppCompatActivity.observeUiState(
    stateFlow: StateFlow<UiState<T>>,
    progressBar: ProgressBar? = null,
    onLoading: (() -> Unit)? = null,
    onSuccess: (T) -> Unit,
    onError: ((String) -> Unit)? = null
) {
    lifecycleScope.launch {
        stateFlow.collect { state ->
            when (state) {
                is UiState.Idle -> {
                    progressBar?.visibility = View.GONE
                }
                is UiState.Loading -> {
                    progressBar?.visibility = View.VISIBLE
                    onLoading?.invoke()
                }
                is UiState.Success -> {
                    progressBar?.visibility = View.GONE
                    onSuccess(state.data)
                }
                is UiState.Error -> {
                    progressBar?.visibility = View.GONE
                    
                    // Call custom error handler or show default error
                    if (onError != null) {
                        onError(state.message)
                    } else {
                        showError(state.message)
                    }
                }
            }
        }
    }
}

/**
 * Show error message using Snackbar
 */
fun AppCompatActivity.showError(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    val rootView = findViewById<View>(android.R.id.content)
    Snackbar.make(rootView, message, duration).show()
}

/**
 * Show success message using Toast
 */
fun AppCompatActivity.showSuccess(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Observe a StateFlow for operation results (like insert, update, delete)
 * Automatically shows Toast for success and Snackbar for errors
 * 
 * @param stateFlow The StateFlow to observe
 * @param progressBar Optional ProgressBar to show/hide during loading
 * @param onSuccess Optional callback after showing success message
 */
fun AppCompatActivity.observeOperationState(
    stateFlow: StateFlow<UiState<String>>,
    progressBar: ProgressBar? = null,
    onSuccess: ((String) -> Unit)? = null
) {
    observeUiState(
        stateFlow = stateFlow,
        progressBar = progressBar,
        onSuccess = { message ->
            showSuccess(message)
            onSuccess?.invoke(message)
        },
        onError = { message ->
            showError(message)
        }
    )
}

/**
 * Helper to handle multiple StateFlows with a single progress indicator
 */
class MultiStateObserver(private val activity: AppCompatActivity) {
    private var loadingCount = 0
    private var progressBar: ProgressBar? = null
    
    fun setProgressBar(progressBar: ProgressBar) {
        this.progressBar = progressBar
    }
    
    fun <T> observe(
        stateFlow: StateFlow<UiState<T>>,
        onSuccess: (T) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        activity.lifecycleScope.launch {
            stateFlow.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        // Do nothing
                    }
                    is UiState.Loading -> {
                        loadingCount++
                        updateProgressBar()
                    }
                    is UiState.Success -> {
                        loadingCount--
                        updateProgressBar()
                        onSuccess(state.data)
                    }
                    is UiState.Error -> {
                        loadingCount--
                        updateProgressBar()
                        
                        if (onError != null) {
                            onError(state.message)
                        } else {
                            activity.showError(state.message)
                        }
                    }
                }
            }
        }
    }
    
    private fun updateProgressBar() {
        progressBar?.visibility = if (loadingCount > 0) View.VISIBLE else View.GONE
    }
}

/**
 * Extension to check if StateFlow is currently loading
 */
fun <T> StateFlow<UiState<T>>.isLoading(): Boolean {
    return value is UiState.Loading
}

/**
 * Extension to check if StateFlow has data
 */
fun <T> StateFlow<UiState<T>>.hasData(): Boolean {
    return value is UiState.Success
}

/**
 * Extension to get data from StateFlow if available
 */
fun <T> StateFlow<UiState<T>>.getData(): T? {
    return (value as? UiState.Success)?.data
}
