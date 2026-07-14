package com.permission.guard.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.permission.guard.domain.usecase.ScanAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val scanAppsUseCase: ScanAppsUseCase
) : ViewModel() {

    data class DashboardState(
        val totalApps: Int = 0,
        val riskApps: Int = 0,
        val privacyScore: Int = 100,
        val isLoading: Boolean = false
    )

    val state: StateFlow<DashboardState> = scanAppsUseCase.getApps().map { apps ->
        if (apps.isEmpty()) {
            DashboardState(isLoading = false)
        } else {
            val total = apps.size
            val riskCount = apps.count { it.riskLevel == "Berbahaya" || it.riskLevel == "Waspada" }
            val safeCount = apps.count { it.riskLevel == "Aman" }
            val score = if (total > 0) (safeCount * 100) / total else 100
            DashboardState(
                totalApps = total,
                riskApps = riskCount,
                privacyScore = score,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState(isLoading = true)
    )

    fun scanApps() {
        viewModelScope.launch {
            scanAppsUseCase.executeScan()
        }
    }
}
