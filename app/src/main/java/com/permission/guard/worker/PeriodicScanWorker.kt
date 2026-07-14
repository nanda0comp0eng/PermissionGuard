package com.permission.guard.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.permission.guard.data.classifier.PermissionClassifier
import com.permission.guard.data.classifier.PermissionLevel
import com.permission.guard.data.scanner.PermissionScanner
import com.permission.guard.domain.model.HistoryEntry
import com.permission.guard.domain.repository.AppRepository
import com.permission.guard.domain.usecase.DiffPermissionUseCase
import com.permission.guard.domain.usecase.GetHistoryUseCase
import com.permission.guard.domain.usecase.WhitelistUseCase
import com.permission.guard.service.NotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PeriodicScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val scanner: PermissionScanner,
    private val repository: AppRepository,
    private val diffPermissionUseCase: DiffPermissionUseCase,
    private val whitelistUseCase: WhitelistUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val notificationService: NotificationService,
    private val classifier: PermissionClassifier
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val scannedApps = scanner.scanAllApps()
            
            for (scannedApp in scannedApps) {
                val packageName = scannedApp.packageName
                
                // 1. Get difference with database snapshot
                val addedPermissions = diffPermissionUseCase.execute(packageName, scannedApp.permissions)
                
                // 2. Save new snapshot
                repository.scanAndSavePackage(packageName)
                
                // 3. Process new permissions
                for (perm in addedPermissions) {
                    val protectionLevel = perm.protectionLevel
                    val level = classifier.getLevel(perm.permissionName, protectionLevel)
                    
                    if (level == PermissionLevel.DANGEROUS || level == PermissionLevel.SPECIAL) {
                        val isWhitelisted = whitelistUseCase.isWhitelisted(packageName, perm.permissionName)
                        if (!isWhitelisted) {
                            notificationService.showPermissionAlert(
                                packageName = packageName,
                                appName = scannedApp.appName,
                                permissionName = perm.permissionName,
                                riskLevel = if (level == PermissionLevel.SPECIAL) "Berbahaya (Khusus)" else "Waspada (Sensitif)"
                            )
                        }
                        
                        repository.addHistory(
                            HistoryEntry(
                                packageName = packageName,
                                appName = scannedApp.appName,
                                permissionName = perm.permissionName,
                                action = "Dideteksi",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
