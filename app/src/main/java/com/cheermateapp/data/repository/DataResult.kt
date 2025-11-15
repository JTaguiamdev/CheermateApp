package com.cheermateapp.data.repository

/**
 * A sealed class representing the result of a data operation
 * Provides type-safe error handling for CRUD operations
 */
sealed class DataResult<out T> {
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : DataResult<T>()
    
    /**
     * Error state with exception
     */
    data class Error(val exception: Exception, val message: String? = null) : DataResult<Nothing>()
    
    /**
     * Loading state
     */
    data object Loading : DataResult<Nothing>()
}

/**
 * Extension function to check if result is successful
 */
fun <T> DataResult<T>.isSuccess(): Boolean = this is DataResult.Success

/**
 * Extension function to get data or null
 */
fun <T> DataResult<T>.getDataOrNull(): T? {
    return when (this) {
        is DataResult.Success -> data
        else -> null
    }
}

/**
 * Extension function to get error or null
 */
fun <T> DataResult<T>.getErrorOrNull(): Exception? {
    return when (this) {
        is DataResult.Error -> exception
        else -> null
    }
}

/**
 * Extension function to handle result with callbacks
 */
inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(data)
    return this
}

/**
 * Extension function to handle error with callback
 */
inline fun <T> DataResult<T>.onError(action: (Exception) -> Unit): DataResult<T> {
    if (this is DataResult.Error) action(exception)
    return this
}
