package com.permission.guard.data.local.entity

data class HistoryEntity(
    val id: Long? = null,
    val packageName: String,
    val permissionName: String,
    val action: String,
    val timestamp: Long
)
