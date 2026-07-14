package com.permission.guard.domain.usecase

import com.permission.guard.domain.model.HistoryEntry
import com.permission.guard.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: AppRepository
) {
    fun getHistory(): Flow<List<HistoryEntry>> {
        return repository.getHistory()
    }
    
    suspend fun addHistory(entry: HistoryEntry) {
        repository.addHistory(entry)
    }
    
    suspend fun clearHistory() {
        repository.clearHistory()
    }
}
