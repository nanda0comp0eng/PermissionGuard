package com.permission.guard.data.local.dao

import android.content.ContentValues
import android.database.Cursor
import com.permission.guard.data.local.db.PermissionGuardDatabase
import com.permission.guard.data.local.entity.PermissionEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionDao @Inject constructor(
    private val dbHelper: PermissionGuardDatabase
) {

    fun insert(permission: PermissionEntity) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("package_name", permission.packageName)
            put("permission_name", permission.permissionName)
            put("protection_level", permission.protectionLevel)
            put("is_dangerous", if (permission.isDangerous) 1 else 0)
        }
        db.insert("permissions", null, values)
    }

    fun deleteForApp(packageName: String) {
        val db = dbHelper.writableDatabase
        db.delete("permissions", "package_name = ?", arrayOf(packageName))
    }

    fun getPermissionsForApp(packageName: String): List<PermissionEntity> {
        val db = dbHelper.readableDatabase
        val permissions = mutableListOf<PermissionEntity>()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "permissions",
                null,
                "package_name = ?",
                arrayOf(packageName),
                null,
                null,
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    permissions.add(mapCursorToEntity(cursor))
                }
            }
        } finally {
            cursor?.close()
        }
        return permissions
    }

    fun getAllPermissions(): List<PermissionEntity> {
        val db = dbHelper.readableDatabase
        val permissions = mutableListOf<PermissionEntity>()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "permissions",
                null,
                null,
                null,
                null,
                null,
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    permissions.add(mapCursorToEntity(cursor))
                }
            }
        } finally {
            cursor?.close()
        }
        return permissions
    }

    fun deleteAll() {
        val db = dbHelper.writableDatabase
        db.delete("permissions", null, null)
    }

    private fun mapCursorToEntity(cursor: Cursor): PermissionEntity {
        return PermissionEntity(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            packageName = cursor.getString(cursor.getColumnIndexOrThrow("package_name")),
            permissionName = cursor.getString(cursor.getColumnIndexOrThrow("permission_name")),
            protectionLevel = cursor.getInt(cursor.getColumnIndexOrThrow("protection_level")),
            isDangerous = cursor.getInt(cursor.getColumnIndexOrThrow("is_dangerous")) == 1
        )
    }
}
