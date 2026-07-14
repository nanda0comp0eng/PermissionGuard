package com.permission.guard.domain.repository

import com.permission.guard.domain.model.AppInfo
import com.permission.guard.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun scanAndSaveAll(): List<AppInfo>
    suspend fun scanAndSavePackage(packageName: String): AppInfo?
    
    fun getApps(): Flow<List<AppInfo>>
    fun getApp(packageName: String): Flow<AppInfo?>
    
    fun getHistory(): Flow<List<HistoryEntry>>
    suspend fun addHistory(entry: HistoryEntry)
    suspend fun clearHistory()
    
    suspend fun addToWhitelist(packageName: String, permissionName: String)
    suspend fun removeFromWhitelist(packageName: String, permissionName: String)
    suspend fun isWhitelisted(packageName: String, permissionName: String): Boolean
    fun getWhitelist(packageName: String): Flow<List<String>>
}
