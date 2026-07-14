package com.permission.guard.data.local.entity

data class AppEntity(
    val packageName: String,
    val appName: String,
    val lastScanned: Long,
    val riskLevel: String
)
