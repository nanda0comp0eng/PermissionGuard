package com.permission.guard.data.scanner

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import com.permission.guard.data.classifier.PermissionClassifier
import com.permission.guard.data.classifier.PermissionLevel
import com.permission.guard.domain.model.AppInfo
import com.permission.guard.domain.model.PermissionInfo as DomainPermissionInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifier: PermissionClassifier
) {

    private val packageManager: PackageManager = context.packageManager

    fun scanAllApps(): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val packages = getInstalledPackagesCompat()

        for (packageInfo in packages) {
            // Filter out system apps if needed, but the prompt says "seluruh aplikasi terpasang"
            // Let's include all apps that declare permissions
            val appInfo = scanPackage(packageInfo) ?: continue
            apps.add(appInfo)
        }
        return apps
    }

    fun scanPackageName(packageName: String): AppInfo? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            }
            scanPackage(packageInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun scanPackage(packageInfo: PackageInfo): AppInfo? {
        val appInfoDetails = packageInfo.applicationInfo ?: return null
        
        // Skip our own app from being flagged or monitored if desired, but we can keep it
        val appName = packageManager.getApplicationLabel(appInfoDetails).toString()
        val packageName = packageInfo.packageName

        val requestedPermissions = packageInfo.requestedPermissions ?: emptyArray()
        val permissionsList = mutableListOf<DomainPermissionInfo>()

        var hasSpecial = false
        var hasDangerous = false

        for (permName in requestedPermissions) {
            val protectionLevel = getPermissionProtectionLevel(permName)
            val level = classifier.getLevel(permName, protectionLevel)
            
            if (level == PermissionLevel.SPECIAL) {
                hasSpecial = true
            } else if (level == PermissionLevel.DANGEROUS) {
                hasDangerous = true
            }

            permissionsList.add(
                DomainPermissionInfo(
                    packageName = packageName,
                    permissionName = permName,
                    protectionLevel = protectionLevel,
                    isDangerous = level != PermissionLevel.NORMAL
                )
            )
        }

        val riskLevel = when {
            hasSpecial -> "Berbahaya"
            hasDangerous -> "Waspada"
            else -> "Aman"
        }

        return AppInfo(
            packageName = packageName,
            appName = appName,
            lastScanned = System.currentTimeMillis(),
            riskLevel = riskLevel,
            permissions = permissionsList
        )
    }

    private fun getPermissionProtectionLevel(permissionName: String): Int {
        return try {
            val info = packageManager.getPermissionInfo(permissionName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.protection
            } else {
                @Suppress("DEPRECATION")
                info.protectionLevel
            }
        } catch (e: Exception) {
            // Default to normal if we can't find the permission info
            PermissionInfo.PROTECTION_NORMAL
        }
    }

    private fun getInstalledPackagesCompat(): List<PackageInfo> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
