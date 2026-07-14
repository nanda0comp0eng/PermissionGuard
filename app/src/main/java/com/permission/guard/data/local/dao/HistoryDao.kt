package com.permission.guard.data.local.dao

import android.content.ContentValues
import android.database.Cursor
import com.permission.guard.data.local.db.PermissionGuardDatabase
import com.permission.guard.data.local.entity.HistoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryDao @Inject constructor(
    private val dbHelper: PermissionGuardDatabase
) {

    fun insert(history: HistoryEntity) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("package_name", history.packageName)
            put("permission_name", history.permissionName)
            put("action", history.action)
            put("timestamp", history.timestamp)
        }
        db.insert("history", null, values)
    }

    fun getHistory(): List<HistoryEntity> {
        val db = dbHelper.readableDatabase
        val historyList = mutableListOf<HistoryEntity>()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "history",
                null,
                null,
                null,
                null,
                null,
                "timestamp DESC"
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    historyList.add(mapCursorToEntity(cursor))
                }
            }
        } finally {
            cursor?.close()
        }
        return historyList
    }

    fun delete(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete("history", "id = ?", arrayOf(id.toString()))
    }

    fun clearHistory() {
        val db = dbHelper.writableDatabase
        db.delete("history", null, null)
    }

    private fun mapCursorToEntity(cursor: Cursor): HistoryEntity {
        return HistoryEntity(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            packageName = cursor.getString(cursor.getColumnIndexOrThrow("package_name")),
            permissionName = cursor.getString(cursor.getColumnIndexOrThrow("permission_name")),
            action = cursor.getString(cursor.getColumnIndexOrThrow("action")),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
        )
    }
}
