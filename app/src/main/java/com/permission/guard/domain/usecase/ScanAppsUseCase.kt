package com.permission.guard.domain.usecase

import com.permission.guard.domain.model.AppInfo
import com.permission.guard.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanAppsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend fun executeScan(): List<AppInfo> {
        return repository.scanAndSaveAll()
    }
    
    fun getApps(): Flow<List<AppInfo>> {
        return repository.getApps()
    }

    fun getApp(packageName: String): Flow<AppInfo?> {
        return repository.getApp(packageName)
    }
}
