package com.permission.guard.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionGuardDatabase @Inject constructor(
    @ApplicationContext context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "permission_guard.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE apps (
                package_name TEXT PRIMARY KEY,
                app_name TEXT NOT NULL,
                last_scanned INTEGER NOT NULL,
                risk_level TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE permissions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                package_name TEXT NOT NULL,
                permission_name TEXT NOT NULL,
                protection_level INTEGER NOT NULL,
                is_dangerous INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE whitelist (
                package_name TEXT NOT NULL,
                permission_name TEXT NOT NULL,
                marked_safe_at INTEGER NOT NULL,
                PRIMARY KEY (package_name, permission_name)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                package_name TEXT NOT NULL,
                permission_name TEXT NOT NULL,
                action TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS apps")
        db.execSQL("DROP TABLE IF EXISTS permissions")
        db.execSQL("DROP TABLE IF EXISTS whitelist")
        db.execSQL("DROP TABLE IF EXISTS history")
        onCreate(db)
    }
}
