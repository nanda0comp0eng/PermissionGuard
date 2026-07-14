package com.permission.guard.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.permission.guard.MainActivity
import com.permission.guard.receiver.PackageEventReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "permission_alert"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Peringatan Izin"
            val descriptionText = "Notifikasi saat aplikasi baru meminta izin berbahaya"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showPermissionAlert(
        packageName: String,
        appName: String,
        permissionName: String,
        riskLevel: String
    ) {
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!sharedPrefs.getBoolean("notifications_enabled", true)) {
            return
        }

        val notificationId = (packageName + permissionName).hashCode()

        // 1. Intent for main click: Open MainActivity
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Action "Tinjau": Open System App Settings
        val reviewIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        val reviewPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 1,
            reviewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Action "Tandai Aman": Send Broadcast to save in whitelist
        val markSafeIntent = Intent(context, PackageEventReceiver::class.java).apply {
            action = PackageEventReceiver.ACTION_MARK_SAFE
            putExtra(PackageEventReceiver.EXTRA_PACKAGE_NAME, packageName)
            putExtra(PackageEventReceiver.EXTRA_PERMISSION_NAME, permissionName)
            putExtra(PackageEventReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markSafePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            markSafeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Standardize permission name presentation (e.g. android.permission.CAMERA -> CAMERA)
        val shortPermName = permissionName.substringAfterLast(".")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Standard Android system warning icon
            .setContentTitle("Izin Baru: $appName")
            .setContentText("Aplikasi ini meminta izin: $shortPermName ($riskLevel)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Aplikasi $appName meminta izin baru yang berisiko: $shortPermName.\nLevel: $riskLevel"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Tinjau", reviewPendingIntent)
            .addAction(android.R.drawable.ic_menu_save, "Tandai Aman", markSafePendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }
}
