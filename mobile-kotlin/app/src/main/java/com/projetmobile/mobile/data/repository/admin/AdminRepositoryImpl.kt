package com.projetmobile.mobile.data.repository.admin

import com.projetmobile.mobile.data.entity.admin.AdminUserCreateInput
import com.projetmobile.mobile.data.entity.admin.AdminUserUpdateInput
import com.projetmobile.mobile.data.mapper.auth.toAuthUser
import com.projetmobile.mobile.data.remote.admin.AdminApiService
import com.projetmobile.mobile.data.remote.admin.AdminCreateUserRequestDto
import com.projetmobile.mobile.data.remote.admin.AdminUpdateUserRequestDto
import com.projetmobile.mobile.data.repository.runRepositoryCall

class AdminRepositoryImpl(
    private val adminApiService: AdminApiService,
) : AdminRepository {

    override suspend fun getUsers() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les utilisateurs.",
    ) {
        adminApiService.getUsers().map { it.toAuthUser() }
    }

    override suspend fun getUserById(id: Int) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer l'utilisateur.",
    ) {
        adminApiService.getUserById(id).toAuthUser()
    }

    override suspend fun createUser(input: AdminUserCreateInput) = runRepositoryCall(
        defaultMessage = "Impossible de créer l'utilisateur.",
    ) {
        adminApiService.createUser(input.toDto()).message
    }

    override suspend fun updateUser(id: Int, input: AdminUserUpdateInput) = runRepositoryCall(
        defaultMessage = "Impossible de mettre à jour l'utilisateur.",
    ) {
        adminApiService.updateUser(id, input.toDto()).user.toAuthUser()
    }

    override suspend fun deleteUser(id: Int) = runRepositoryCall(
        defaultMessage = "Impossible de supprimer l'utilisateur.",
    ) {
        adminApiService.deleteUser(id).message
    }
}

private fun AdminUserCreateInput.toDto() = AdminCreateUserRequestDto(
    login = login,
    password = password,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    role = role,
)

private fun AdminUserUpdateInput.toDto() = AdminUpdateUserRequestDto(
    login = login,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    role = role,
    emailVerified = emailVerified,
)
