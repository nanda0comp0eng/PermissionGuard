package com.permission.guard.data.classifier

import android.content.pm.PermissionInfo
import javax.inject.Inject
import javax.inject.Singleton

enum class PermissionLevel {
    NORMAL,
    DANGEROUS,
    SPECIAL
}

@Singleton
class PermissionClassifier @Inject constructor() {

    data class Explanation(
        val level: PermissionLevel,
        val explanation: String
    )

    private val explanations = mapOf(
        // Dangerous Permissions
        "android.permission.CAMERA" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi mengakses kamera untuk foto atau video diam-diam"
        ),
        "android.permission.RECORD_AUDIO" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi merekam suara dari mikrofon"
        ),
        "android.permission.READ_CONTACTS" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi membaca seluruh daftar kontak"
        ),
        "android.permission.ACCESS_FINE_LOCATION" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi mengetahui lokasi GPS secara presisi"
        ),
        "android.permission.ACCESS_COARSE_LOCATION" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi mengetahui lokasi kasar Anda melalui BTS seluler dan Wi-Fi"
        ),
        "android.permission.READ_CALL_LOG" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi membaca riwayat panggilan telepon"
        ),
        "android.permission.READ_SMS" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi membaca seluruh pesan SMS"
        ),
        "android.permission.SEND_SMS" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi mengirim pesan SMS tanpa sepengetahuan Anda yang bisa memicu biaya"
        ),
        "android.permission.RECEIVE_SMS" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi menerima dan memantau pesan SMS masuk"
        ),
        "android.permission.PROCESS_OUTGOING_CALLS" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi memantau dan memodifikasi panggilan keluar"
        ),
        "android.permission.GET_ACCOUNTS" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi mengakses daftar akun Google dan akun lain di perangkat"
        ),
        "android.permission.READ_EXTERNAL_STORAGE" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi membaca semua file di penyimpanan"
        ),
        "android.permission.WRITE_EXTERNAL_STORAGE" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi menulis atau menghapus file di penyimpanan"
        ),
        "android.permission.READ_PHONE_STATE" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi mengakses fungsi telepon, termasuk nomor telepon, status jaringan seluler, dan panggilan aktif"
        ),
        "android.permission.CALL_PHONE" to Explanation(
            PermissionLevel.DANGEROUS,
            "Memungkinkan aplikasi melakukan panggilan telepon langsung tanpa interaksi Anda"
        ),
        
        // Special Permissions
        "android.permission.SYSTEM_ALERT_WINDOW" to Explanation(
            PermissionLevel.SPECIAL,
            "Memungkinkan aplikasi menampilkan jendela melayang di atas aplikasi lain, berisiko merekam layar atau menampilkan iklan mengganggu"
        ),
        "android.permission.WRITE_SETTINGS" to Explanation(
            PermissionLevel.SPECIAL,
            "Memungkinkan aplikasi mengubah pengaturan sistem Android"
        ),
        "android.permission.BIND_ACCESSIBILITY_SERVICE" to Explanation(
            PermissionLevel.SPECIAL,
            "Memungkinkan aplikasi memantau semua aktivitas layar dan input tombol Anda (bisa bertindak sebagai keylogger)"
        ),
        "android.permission.PACKAGE_USAGE_STATS" to Explanation(
            PermissionLevel.SPECIAL,
            "Memungkinkan aplikasi melacak aplikasi lain yang Anda buka dan seberapa sering Anda menggunakannya"
        ),
        "android.permission.REQUEST_INSTALL_PACKAGES" to Explanation(
            PermissionLevel.SPECIAL,
            "Memungkinkan aplikasi memasang aplikasi lain secara mandiri dari luar Google Play Store"
        ),
        "android.permission.MANAGE_EXTERNAL_STORAGE" to Explanation(
            PermissionLevel.SPECIAL,
            "Memungkinkan akses penuh ke semua file di penyimpanan internal dan eksternal"
        )
    )

    fun getLevel(permissionName: String, protectionLevel: Int): PermissionLevel {
        val mapped = explanations[permissionName]?.level
        if (mapped != null) return mapped

        // Check special permissions list
        val isSpecial = permissionName in listOf(
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.WRITE_SETTINGS",
            "android.permission.BIND_ACCESSIBILITY_SERVICE",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.REQUEST_INSTALL_PACKAGES",
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.ACCESS_NOTIFICATION_POLICY"
        )
        if (isSpecial) return PermissionLevel.SPECIAL

        // Check if protection level is dangerous using base protection level mask
        val baseLevel = protectionLevel and PermissionInfo.PROTECTION_MASK_BASE
        if (baseLevel == PermissionInfo.PROTECTION_DANGEROUS) {
            return PermissionLevel.DANGEROUS
        }

        return PermissionLevel.NORMAL
    }

    fun getExplanation(permissionName: String): String {
        return explanations[permissionName]?.explanation ?: "Izin standar untuk menjalankan fungsi dasar aplikasi."
    }
}
