package com.permission.guard.data.local.dao

import android.content.ContentValues
import android.database.Cursor
import com.permission.guard.data.local.db.PermissionGuardDatabase
import com.permission.guard.data.local.entity.AppEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDao @Inject constructor(
    private val dbHelper: PermissionGuardDatabase
) {

    fun insertOrUpdate(app: AppEntity) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("package_name", app.packageName)
            put("app_name", app.appName)
            put("last_scanned", app.lastScanned)
            put("risk_level", app.riskLevel)
        }
        db.replace("apps", null, values)
    }

    fun delete(packageName: String) {
        val db = dbHelper.writableDatabase
        db.delete("apps", "package_name = ?", arrayOf(packageName))
    }

    fun getApp(packageName: String): AppEntity? {
        val db = dbHelper.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "apps",
                null,
                "package_name = ?",
                arrayOf(packageName),
                null,
                null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToEntity(cursor)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getAllApps(): List<AppEntity> {
        val db = dbHelper.readableDatabase
        val apps = mutableListOf<AppEntity>()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "apps",
                null,
                null,
                null,
                null,
                null,
                "app_name ASC"
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    apps.add(mapCursorToEntity(cursor))
                }
            }
        } finally {
            cursor?.close()
        }
        return apps
    }

    fun deleteAll() {
        val db = dbHelper.writableDatabase
        db.delete("apps", null, null)
    }

    private fun mapCursorToEntity(cursor: Cursor): AppEntity {
        return AppEntity(
            packageName = cursor.getString(cursor.getColumnIndexOrThrow("package_name")),
            appName = cursor.getString(cursor.getColumnIndexOrThrow("app_name")),
            lastScanned = cursor.getLong(cursor.getColumnIndexOrThrow("last_scanned")),
            riskLevel = cursor.getString(cursor.getColumnIndexOrThrow("risk_level"))
        )
    }
}
