package com.permission.guard.data.local.entity

data class PermissionEntity(
    val id: Long? = null,
    val packageName: String,
    val permissionName: String,
    val protectionLevel: Int,
    val isDangerous: Boolean
)
