package com.permission.guard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.permission.guard.data.classifier.PermissionClassifier
import com.permission.guard.data.classifier.PermissionLevel
import com.permission.guard.data.scanner.PermissionScanner
import com.permission.guard.domain.model.HistoryEntry
import com.permission.guard.domain.repository.AppRepository
import com.permission.guard.domain.usecase.DiffPermissionUseCase
import com.permission.guard.domain.usecase.GetHistoryUseCase
import com.permission.guard.domain.usecase.WhitelistUseCase
import com.permission.guard.service.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageEventReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scanner: PermissionScanner

    @Inject
    lateinit var repository: AppRepository

    @Inject
    lateinit var diffPermissionUseCase: DiffPermissionUseCase

    @Inject
    lateinit var whitelistUseCase: WhitelistUseCase

    @Inject
    lateinit var getHistoryUseCase: GetHistoryUseCase

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var classifier: PermissionClassifier

    companion object {
        const val ACTION_MARK_SAFE = "com.permission.guard.ACTION_MARK_SAFE"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_PERMISSION_NAME = "permission_name"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                when (action) {
                    Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REPLACED -> {
                        val packageName = intent.data?.schemeSpecificPart ?: return@launch
                        
                        // 1. Scan the package (do not save to DB yet, we need to diff first)
                        val scannedApp = scanner.scanPackageName(packageName) ?: return@launch
                        
                        // 2. Perform diff check with old snapshot
                        val addedPermissions = diffPermissionUseCase.execute(packageName, scannedApp.permissions)
                        
                        // 3. Save new snapshot to DB
                        repository.scanAndSavePackage(packageName)
                        
                        // 4. For each added permission, check if dangerous/special and not whitelisted
                        for (perm in addedPermissions) {
                            val protectionLevel = perm.protectionLevel
                            val level = classifier.getLevel(perm.permissionName, protectionLevel)
                            
                            if (level == PermissionLevel.DANGEROUS || level == PermissionLevel.SPECIAL) {
                                val isWhitelisted = whitelistUseCase.isWhitelisted(packageName, perm.permissionName)
                                if (!isWhitelisted) {
                                    // Trigger notification
                                    notificationService.showPermissionAlert(
                                        packageName = packageName,
                                        appName = scannedApp.appName,
                                        permissionName = perm.permissionName,
                                        riskLevel = if (level == PermissionLevel.SPECIAL) "Berbahaya (Khusus)" else "Waspada (Sensitif)"
                                    )
                                }
                                
                                // Record in history
                                getHistoryUseCase.addHistory(
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
                    
                    ACTION_MARK_SAFE -> {
                        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return@launch
                        val permissionName = intent.getStringExtra(EXTRA_PERMISSION_NAME) ?: return@launch
                        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

                        // Add to whitelist
                        whitelistUseCase.addToWhitelist(packageName, permissionName)

                        // Resolve app name
                        val appName = try {
                            val info = context.packageManager.getApplicationInfo(packageName, 0)
                            context.packageManager.getApplicationLabel(info).toString()
                        } catch (e: Exception) {
                            packageName
                        }

                        // Add history entry
                        getHistoryUseCase.addHistory(
                            HistoryEntry(
                                packageName = packageName,
                                appName = appName,
                                permissionName = permissionName,
                                action = "Ditandai Aman",
                                timestamp = System.currentTimeMillis()
                            )
                        )

                        // Dismiss notification
                        if (notificationId != -1) {
                            notificationService.cancelNotification(notificationId)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
