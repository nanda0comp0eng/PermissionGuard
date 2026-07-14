package com.permission.guard.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.permission.guard.domain.model.AppInfo
import com.permission.guard.domain.model.HistoryEntry
import com.permission.guard.domain.usecase.ScanAppsUseCase
import com.permission.guard.domain.usecase.WhitelistUseCase
import com.permission.guard.domain.usecase.GetHistoryUseCase
import com.permission.guard.data.classifier.PermissionClassifier
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val scanAppsUseCase: ScanAppsUseCase,
    private val whitelistUseCase: WhitelistUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    val classifier: PermissionClassifier,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val packageName: String = checkNotNull(savedStateHandle["packageName"])

    data class AppDetailState(
        val appInfo: AppInfo? = null,
        val whitelist: List<String> = emptyList(),
        val isLoading: Boolean = false
    )

    val state: StateFlow<AppDetailState> = combine(
        scanAppsUseCase.getApp(packageName),
        whitelistUseCase.getWhitelist(packageName)
    ) { app, whitelist ->
        AppDetailState(appInfo = app, whitelist = whitelist, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppDetailState(isLoading = true)
    )

    fun toggleWhitelist(permissionName: String) {
        val currentState = state.value
        val isWhitelisted = currentState.whitelist.contains(permissionName)
        val appName = currentState.appInfo?.appName ?: packageName
        viewModelScope.launch {
            if (isWhitelisted) {
                whitelistUseCase.removeFromWhitelist(packageName, permissionName)
                getHistoryUseCase.addHistory(
                    HistoryEntry(
                        packageName = packageName,
                        appName = appName,
                        permissionName = permissionName,
                        action = "Dihapus dari Whitelist",
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                whitelistUseCase.addToWhitelist(packageName, permissionName)
                getHistoryUseCase.addHistory(
                    HistoryEntry(
                        packageName = packageName,
                        appName = appName,
                        permissionName = permissionName,
                        action = "Ditandai Aman",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
