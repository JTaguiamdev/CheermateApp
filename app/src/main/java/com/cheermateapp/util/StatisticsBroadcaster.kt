package com.cheermateapp.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Global statistics broadcaster that maintains the latest statistics
 * and notifies all observers when data changes, regardless of current screen.
 */
object StatisticsBroadcaster {
    
    data class Statistics(
        val total: Int,
        val completed: Int,
        val successRate: Int,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    // Global state holder for statistics
    private val _statistics = MutableStateFlow<Statistics?>(null)
    val statistics: StateFlow<Statistics?> = _statistics
    
    /**
     * Update statistics globally - this will notify ALL observers
     */
    fun updateStatistics(total: Int, completed: Int) {
        val successRate = if (total > 0) {
            (completed * 100) / total
        } else 0
        
        val newStats = Statistics(total, completed, successRate)
        val oldStats = _statistics.value
        
        // Only emit if data actually changed
        if (oldStats == null || oldStats.total != total || oldStats.completed != completed) {
            _statistics.value = newStats
            Log.d("StatisticsBroadcaster", "📡 Broadcasting new statistics: Total=$total, Completed=$completed, Success=$successRate%")
        } else {
            Log.d("StatisticsBroadcaster", "⚡ Statistics unchanged - no broadcast needed")
        }
    }
    
    /**
     * Get current statistics or null if not available
     */
    fun getCurrentStatistics(): Statistics? {
        return _statistics.value
    }
    
    /**
     * Force refresh statistics from database for a specific user
     */
    suspend fun refreshFromDatabase(db: com.cheermateapp.data.db.AppDb, userId: Int) {
        try {
            val total = db.taskDao().getAllTasksCount(userId)
            val completed = db.taskDao().getCompletedTasksCount(userId)
            updateStatistics(total, completed)
            Log.d("StatisticsBroadcaster", "🔄 Refreshed statistics from database")
        } catch (e: Exception) {
            Log.e("StatisticsBroadcaster", "Error refreshing statistics from database", e)
        }
    }
}