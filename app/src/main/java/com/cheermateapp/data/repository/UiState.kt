package com.cheermateapp.data.repository

/**
 * Sealed class representing UI state with loading, success, and error states
 * Used by ViewModels to communicate state to UI components
 */
sealed class UiState<out T> {
    /**
     * Initial state before any operation
     */
    data object Idle : UiState<Nothing>()
    
    /**
     * Loading state - operation in progress
     */
    data object Loading : UiState<Nothing>()
    
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Error state with message
     */
    data class Error(val message: String, val exception: Exception? = null) : UiState<Nothing>()
}

/**
 * Extension to check if state is loading
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Extension to check if state is success
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * Extension to check if state is error
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * Extension to get data or null
 */
fun <T> UiState<T>.getDataOrNull(): T? {
    return when (this) {
        is UiState.Success -> data
        else -> null
    }
}
