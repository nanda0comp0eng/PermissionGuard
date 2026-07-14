package com.permission.guard.presentation.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.permission.guard.domain.model.AppInfo
import com.permission.guard.domain.usecase.ScanAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val scanAppsUseCase: ScanAppsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedRiskFilter = MutableStateFlow("Semua") // "Semua", "Aman", "Waspada", "Berbahaya"
    val selectedRiskFilter: StateFlow<String> = _selectedRiskFilter

    data class AppListState(
        val apps: List<AppInfo> = emptyList(),
        val isLoading: Boolean = false
    )

    val state: StateFlow<AppListState> = combine(
        scanAppsUseCase.getApps(),
        _searchQuery,
        _selectedRiskFilter
    ) { apps, query, filter ->
        val filtered = apps.filter { app ->
            val matchesQuery = app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
            val matchesFilter = filter == "Semua" || app.riskLevel.equals(filter, ignoreCase = true)
            matchesQuery && matchesFilter
        }
        AppListState(apps = filtered, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppListState(isLoading = true)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRiskFilter(filter: String) {
        _selectedRiskFilter.value = filter
    }
}
