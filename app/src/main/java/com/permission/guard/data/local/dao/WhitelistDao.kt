package com.permission.guard.data.local.dao

import android.content.ContentValues
import android.database.Cursor
import com.permission.guard.data.local.db.PermissionGuardDatabase
import com.permission.guard.data.local.entity.WhitelistEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhitelistDao @Inject constructor(
    private val dbHelper: PermissionGuardDatabase
) {

    fun addToWhitelist(packageName: String, permissionName: String, markedSafeAt: Long) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("package_name", packageName)
            put("permission_name", permissionName)
            put("marked_safe_at", markedSafeAt)
        }
        db.replace("whitelist", null, values)
    }

    fun removeFromWhitelist(packageName: String, permissionName: String) {
        val db = dbHelper.writableDatabase
        db.delete(
            "whitelist",
            "package_name = ? AND permission_name = ?",
            arrayOf(packageName, permissionName)
        )
    }

    fun isWhitelisted(packageName: String, permissionName: String): Boolean {
        val db = dbHelper.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "whitelist",
                arrayOf("marked_safe_at"),
                "package_name = ? AND permission_name = ?",
                arrayOf(packageName, permissionName),
                null,
                null,
                null
            )
            return cursor != null && cursor.count > 0
        } finally {
            cursor?.close()
        }
    }

    fun getWhitelistForApp(packageName: String): List<WhitelistEntity> {
        val db = dbHelper.readableDatabase
        val whitelist = mutableListOf<WhitelistEntity>()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "whitelist",
                null,
                "package_name = ?",
                arrayOf(packageName),
                null,
                null,
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    whitelist.add(mapCursorToEntity(cursor))
                }
            }
        } finally {
            cursor?.close()
        }
        return whitelist
    }

    fun getAllWhitelist(): List<WhitelistEntity> {
        val db = dbHelper.readableDatabase
        val whitelist = mutableListOf<WhitelistEntity>()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "whitelist",
                null,
                null,
                null,
                null,
                null,
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    whitelist.add(mapCursorToEntity(cursor))
                }
            }
        } finally {
            cursor?.close()
        }
        return whitelist
    }

    private fun mapCursorToEntity(cursor: Cursor): WhitelistEntity {
        return WhitelistEntity(
            packageName = cursor.getString(cursor.getColumnIndexOrThrow("package_name")),
            permissionName = cursor.getString(cursor.getColumnIndexOrThrow("permission_name")),
            markedSafeAt = cursor.getLong(cursor.getColumnIndexOrThrow("marked_safe_at"))
        )
    }
}
