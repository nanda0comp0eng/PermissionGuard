package com.permission.guard.presentation.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.permission.guard.domain.usecase.GetHistoryUseCase
import com.permission.guard.domain.usecase.ScanAppsUseCase
import com.permission.guard.worker.PeriodicScanWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanAppsUseCase: ScanAppsUseCase,
    private val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _notificationsEnabled = MutableStateFlow(sharedPrefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _scanIntervalHours = MutableStateFlow(sharedPrefs.getInt("scan_interval_hours", 12))
    val scanIntervalHours: StateFlow<Int> = _scanIntervalHours

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun setScanInterval(hours: Int) {
        _scanIntervalHours.value = hours
        sharedPrefs.edit().putInt("scan_interval_hours", hours).apply()
        schedulePeriodicScan(hours)
    }

    fun triggerManualScan() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                scanAppsUseCase.executeScan()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            getHistoryUseCase.clearHistory()
        }
    }

    fun schedulePeriodicScan(hours: Int) {
        val workManager = WorkManager.getInstance(context)
        val workRequest = PeriodicWorkRequestBuilder<PeriodicScanWorker>(
            hours.toLong(), TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "PeriodicScanWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
