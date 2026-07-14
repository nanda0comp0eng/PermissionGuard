package com.permission.guard.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.permission.guard.domain.model.HistoryEntry
import com.permission.guard.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    data class HistoryState(
        val history: List<HistoryEntry> = emptyList(),
        val isLoading: Boolean = false
    )

    val state: StateFlow<HistoryState> = combine(
        getHistoryUseCase.getHistory(),
        _searchQuery
    ) { history, query ->
        val filtered = if (query.isBlank()) {
            history
        } else {
            history.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.permissionName.contains(query, ignoreCase = true) ||
                        it.action.contains(query, ignoreCase = true)
            }
        }
        HistoryState(history = filtered, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryState(isLoading = true)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearHistory() {
        viewModelScope.launch {
            getHistoryUseCase.clearHistory()
        }
    }
}
