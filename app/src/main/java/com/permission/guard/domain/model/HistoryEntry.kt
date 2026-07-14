package com.permission.guard.domain.model

data class HistoryEntry(
    val id: Long? = null,
    val packageName: String,
    val appName: String,
    val permissionName: String,
    val action: String,
    val timestamp: Long
)
