package com.permission.guard.domain.usecase

import com.permission.guard.domain.model.PermissionInfo
import com.permission.guard.domain.repository.AppRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DiffPermissionUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend fun execute(packageName: String, newPermissions: List<PermissionInfo>): List<PermissionInfo> {
        val oldApp = repository.getApp(packageName).first()
        val oldPermissions = oldApp?.permissions?.map { it.permissionName }?.toSet() ?: emptySet()
        
        // Find permissions that are in newPermissions but not in oldPermissions
        return newPermissions.filter { it.permissionName !in oldPermissions }
    }
}
