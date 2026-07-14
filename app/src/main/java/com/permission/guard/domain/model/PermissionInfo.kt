package com.permission.guard.domain.model

data class PermissionInfo(
    val packageName: String,
    val permissionName: String,
    val protectionLevel: Int,
    val isDangerous: Boolean
)
