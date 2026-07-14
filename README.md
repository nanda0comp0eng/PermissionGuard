# Permission Guard

## FILE .APK
# https://github.com/nanda0comp0eng/PermissionGuard/blob/main/PermissionGuard.apk

Permission Guard adalah aplikasi Android modern yang membantu pengguna memantau, menganalisis, dan memahami izin (permission) yang dideklarasikan oleh aplikasi-aplikasi yang terpasang di perangkat mereka. Aplikasi ini mendeteksi instalasi dan pembaruan aplikasi secara real-time, membandingkan permission baru dengan snapshot sebelumnya, lalu mengirimkan notifikasi sistem jika terdeteksi adanya izin yang berisiko tinggi.

Seluruh fitur dibangun hanya menggunakan Android Public API resmi tanpa memerlukan akses root.

---

## 🚀 Fitur Utama

1.  **Dashboard Ringkasan Privasi**:
    *   Skor Privasi Perangkat (0-100%) yang dihitung secara dinamis berdasarkan proporsi aplikasi aman vs berisiko.
    *   Statistik jumlah total aplikasi terpasang dan aplikasi berkategori berisiko.
2.  **Daftar Aplikasi & Filter**:
    *   LazyColumn teroptimasi untuk memuat seluruh aplikasi terpasang lengkap dengan nama, package name, dan logo dinamis.
    *   Pencarian teks real-time dan filter cepat berdasarkan tingkat risiko (*Aman*, *Waspada*, *Berbahaya*).
3.  **Detail Izin & Edukasi Privasi**:
    *   Visualisasi seluruh izin yang dideklarasikan di dalam Manifest.
    *   Klasifikasi izin (*Normal*, *Dangerous*, *Special Access*).
    *   Penjelasan edukatif dalam bahasa Indonesia untuk mempermudah pengguna awam memahami risiko dari masing-masing izin berbahaya/khusus.
    *   Aksi cepat menuju Pengaturan Izin Sistem Android.
4.  **Fitur Whitelist (Tandai Aman)**:
    *   Switch internal untuk memutihkan izin tertentu pada aplikasi tepercaya, guna mencegah munculnya peringatan notifikasi berulang.
5.  **Pemantauan Real-time & Scan Diff**:
    *   BroadcastReceiver yang memantau penambahan (`ACTION_PACKAGE_ADDED`) dan pembaruan (`ACTION_PACKAGE_REPLACED`) aplikasi.
    *   Komparator (diff scan) yang menyaring penambahan izin baru setelah pembaruan versi aplikasi.
6.  **Peringatan Notifikasi Interaktif**:
    *   Kanal notifikasi sistem `permission_alert` dengan aksi cepat **Tinjau** (buka setting) dan **Tandai Aman** (Whitelist secara instan dari status bar).
7.  **Scan Background Berkala**:
    *   Integrasi `WorkManager` untuk menjalankan pemindaian menyeluruh secara terjadwal (opsi interval 6, 12, atau 24 jam) secara efisien tanpa menguras daya baterai.
8.  **Riwayat Deteksi**:
    *   Pencatatan log aktivitas peringatan deteksi izin dan tindakan whitelist yang diambil oleh pengguna secara kronologis.

---

## 🛠️ Tech Stack & Arsitektur

*   **Bahasa Pemrograman**: Kotlin
*   **UI Framework**: Jetpack Compose dengan Material 3 (Dark Theme style)
*   **Arsitektur**: MVVM (Model-View-ViewModel) + Clean Architecture (Presentation, Domain, dan Data layer)
*   **Dependency Injection**: Hilt (Dagger-Hilt) dengan support `HiltWorkerFactory`
*   **Database Lokal**: SQLite Native menggunakan `SQLiteOpenHelper` dan `SQLiteDatabase` dengan penulisan query SQL manual (non-Room)
*   **Asynchronous**: Kotlin Coroutines & Flow (Reactive DB updates diimplementasikan menggunakan `MutableSharedFlow` sebagai sinyal pembaruan data)
*   **Navigasi**: Jetpack Navigation Compose
*   **Background Task**: WorkManager (CoroutineWorker)
*   **Kompilator**: Kotlin Symbol Processing (KSP) kompatibel dengan built-in Kotlin AGP 9+
*   **Android SDK**: Minimum SDK 26 (Android 8.0) | Target SDK 37 (Android 15)

---

## 🗄️ Skema Database SQLite

Database disimpan secara lokal di perangkat (`permission_guard.db`) dengan struktur tabel berikut:

```sql
CREATE TABLE apps (
    package_name TEXT PRIMARY KEY,
    app_name TEXT NOT NULL,
    last_scanned INTEGER NOT NULL,
    risk_level TEXT NOT NULL
);

CREATE TABLE permissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    package_name TEXT NOT NULL,
    permission_name TEXT NOT NULL,
    protection_level INTEGER NOT NULL,
    is_dangerous INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE whitelist (
    package_name TEXT NOT NULL,
    permission_name TEXT NOT NULL,
    marked_safe_at INTEGER NOT NULL,
    PRIMARY KEY (package_name, permission_name)
);

CREATE TABLE history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    package_name TEXT NOT NULL,
    permission_name TEXT NOT NULL,
    action TEXT NOT NULL,
    timestamp INTEGER NOT NULL
);
```

---

## 📦 Cara Membangun & Menjalankan Proyek

### Prasyarat
*   Android Studio Ladybug (atau versi lebih baru yang mendukung AGP 9+)
*   Android SDK Platform 37 (Android 15)
*   Java Development Kit (JDK) 17 atau 21

### Langkah-langkah
1.  **Clone / Download Repository** ini ke mesin lokal Anda.
2.  Buka Android Studio, lalu pilih **Open** dan arahkan ke direktori proyek.
3.  Biarkan Gradle melakukan sinkronisasi otomatis. Pastikan file `gradle.properties` memuat parameter berikut untuk mendukung KSP pada built-in Kotlin:
    ```properties
    android.disallowKotlinSourceSets=false
    ```
4.  Jalankan pembersihan dan kompilasi melalui terminal:
    ```bash
    ./gradlew clean compileDebugKotlin
    ```
5.  Untuk membuat file APK Debug:
    ```bash
    ./gradlew assembleDebug
    ```
    Output APK akan tersimpan di: `app/build/outputs/apk/debug/app-debug.apk`.
6.  Hubungkan perangkat Android fisik (aktifkan USB Debugging) atau gunakan Emulator, lalu klik tombol **Run** di Android Studio untuk menginstal aplikasi.
