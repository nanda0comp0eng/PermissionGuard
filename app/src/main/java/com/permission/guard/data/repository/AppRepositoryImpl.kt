package com.permission.guard.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.permission.guard.data.local.dao.AppDao
import com.permission.guard.data.local.dao.HistoryDao
import com.permission.guard.data.local.dao.PermissionDao
import com.permission.guard.data.local.dao.WhitelistDao
import com.permission.guard.data.local.entity.AppEntity
import com.permission.guard.data.local.entity.HistoryEntity
import com.permission.guard.data.local.entity.PermissionEntity
import com.permission.guard.data.scanner.PermissionScanner
import com.permission.guard.domain.model.AppInfo
import com.permission.guard.domain.model.HistoryEntry
import com.permission.guard.domain.model.PermissionInfo
import com.permission.guard.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDao: AppDao,
    private val permissionDao: PermissionDao,
    private val whitelistDao: WhitelistDao,
    private val historyDao: HistoryDao,
    private val scanner: PermissionScanner
) : AppRepository {

    // Signal used to reload reactive flows
    private val updateSignal = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    private fun notifyChanged() {
        updateSignal.tryEmit(Unit)
    }

    override suspend fun scanAndSaveAll(): List<AppInfo> = withContext(Dispatchers.IO) {
        val scannedApps = scanner.scanAllApps()
        for (app in scannedApps) {
            saveAppDetails(app)
        }
        notifyChanged()
        scannedApps
    }

    override suspend fun scanAndSavePackage(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        val app = scanner.scanPackageName(packageName)
        if (app != null) {
            saveAppDetails(app)
            notifyChanged()
        }
        app
    }

    private fun saveAppDetails(app: AppInfo) {
        appDao.insertOrUpdate(
            AppEntity(
                packageName = app.packageName,
                appName = app.appName,
                lastScanned = app.lastScanned,
                riskLevel = app.riskLevel
            )
        )
        permissionDao.deleteForApp(app.packageName)
        for (perm in app.permissions) {
            permissionDao.insert(
                PermissionEntity(
                    packageName = perm.packageName,
                    permissionName = perm.permissionName,
                    protectionLevel = perm.protectionLevel,
                    isDangerous = perm.isDangerous
                )
            )
        }
    }

    override fun getApps(): Flow<List<AppInfo>> = flow {
        updateSignal.collect {
            val apps = withContext(Dispatchers.IO) {
                appDao.getAllApps().map { entity ->
                    val perms = permissionDao.getPermissionsForApp(entity.packageName).map {
                        PermissionInfo(it.packageName, it.permissionName, it.protectionLevel, it.isDangerous)
                    }
                    AppInfo(entity.packageName, entity.appName, entity.lastScanned, entity.riskLevel, perms)
                }
            }
            emit(apps)
        }
    }.flowOn(Dispatchers.IO)

    override fun getApp(packageName: String): Flow<AppInfo?> = flow {
        updateSignal.collect {
            val app = withContext(Dispatchers.IO) {
                appDao.getApp(packageName)?.let { entity ->
                    val perms = permissionDao.getPermissionsForApp(entity.packageName).map {
                        PermissionInfo(it.packageName, it.permissionName, it.protectionLevel, it.isDangerous)
                    }
                    AppInfo(entity.packageName, entity.appName, entity.lastScanned, entity.riskLevel, perms)
                }
            }
            emit(app)
        }
    }.flowOn(Dispatchers.IO)

    override fun getHistory(): Flow<List<HistoryEntry>> = flow {
        updateSignal.collect {
            val history = withContext(Dispatchers.IO) {
                historyDao.getHistory().map { entity ->
                    val appName = try {
                        val info = context.packageManager.getApplicationInfo(entity.packageName, 0)
                        context.packageManager.getApplicationLabel(info).toString()
                    } catch (e: Exception) {
                        appDao.getApp(entity.packageName)?.appName ?: entity.packageName
                    }
                    HistoryEntry(
                        id = entity.id,
                        packageName = entity.packageName,
                        appName = appName,
                        permissionName = entity.permissionName,
                        action = entity.action,
                        timestamp = entity.timestamp
                    )
                }
            }
            emit(history)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun addHistory(entry: HistoryEntry) = withContext(Dispatchers.IO) {
        historyDao.insert(
            HistoryEntity(
                packageName = entry.packageName,
                permissionName = entry.permissionName,
                action = entry.action,
                timestamp = entry.timestamp
            )
        )
        notifyChanged()
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
        notifyChanged()
    }

    override suspend fun addToWhitelist(packageName: String, permissionName: String) = withContext(Dispatchers.IO) {
        whitelistDao.addToWhitelist(packageName, permissionName, System.currentTimeMillis())
        notifyChanged()
    }

    override suspend fun removeFromWhitelist(packageName: String, permissionName: String) = withContext(Dispatchers.IO) {
        whitelistDao.removeFromWhitelist(packageName, permissionName)
        notifyChanged()
    }

    override suspend fun isWhitelisted(packageName: String, permissionName: String): Boolean = withContext(Dispatchers.IO) {
        whitelistDao.isWhitelisted(packageName, permissionName)
    }

    override fun getWhitelist(packageName: String): Flow<List<String>> = flow {
        updateSignal.collect {
            val list = withContext(Dispatchers.IO) {
                whitelistDao.getWhitelistForApp(packageName).map { it.permissionName }
            }
            emit(list)
        }
    }.flowOn(Dispatchers.IO)
}
