package com.permission.guard.data.local.entity

data class WhitelistEntity(
    val packageName: String,
    val permissionName: String,
    val markedSafeAt: Long
)
