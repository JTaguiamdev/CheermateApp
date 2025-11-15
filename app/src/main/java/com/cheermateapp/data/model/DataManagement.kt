package com.cheermateapp.data.model

data class DataManagement(
    val autoBackup: Boolean = true,
    val cloudSync: Boolean = false,
    val dataRetentionDays: Int = 365
)
