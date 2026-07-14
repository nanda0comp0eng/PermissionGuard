package com.permission.guard.domain.usecase

import com.permission.guard.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WhitelistUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend fun addToWhitelist(packageName: String, permissionName: String) {
        repository.addToWhitelist(packageName, permissionName)
    }
    
    suspend fun removeFromWhitelist(packageName: String, permissionName: String) {
        repository.removeFromWhitelist(packageName, permissionName)
    }
    
    suspend fun isWhitelisted(packageName: String, permissionName: String): Boolean {
        return repository.isWhitelisted(packageName, permissionName)
    }
    
    fun getWhitelist(packageName: String): Flow<List<String>> {
        return repository.getWhitelist(packageName)
    }
}
