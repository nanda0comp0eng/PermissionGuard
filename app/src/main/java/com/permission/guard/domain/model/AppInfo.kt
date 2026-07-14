package com.permission.guard.domain.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val lastScanned: Long,
    val riskLevel: String,
    val permissions: List<PermissionInfo> = emptyList()
)
