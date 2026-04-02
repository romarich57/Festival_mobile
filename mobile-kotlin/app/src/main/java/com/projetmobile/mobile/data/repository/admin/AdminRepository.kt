package com.projetmobile.mobile.data.repository.admin

import com.projetmobile.mobile.data.entity.admin.AdminUserCreateInput
import com.projetmobile.mobile.data.entity.admin.AdminUserUpdateInput
import com.projetmobile.mobile.data.entity.auth.AuthUser

interface AdminRepository {
    suspend fun getUsers(): Result<List<AuthUser>>
    suspend fun getUserById(id: Int): Result<AuthUser>
    suspend fun createUser(input: AdminUserCreateInput): Result<String>
    suspend fun updateUser(id: Int, input: AdminUserUpdateInput): Result<AuthUser>
    suspend fun deleteUser(id: Int): Result<String>
}
