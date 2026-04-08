package com.projetmobile.mobile.data.repository.admin

import com.projetmobile.mobile.data.entity.admin.AdminUserCreateInput
import com.projetmobile.mobile.data.entity.admin.AdminUserUpdateInput
import com.projetmobile.mobile.data.mapper.auth.toAuthUser
import com.projetmobile.mobile.data.remote.admin.AdminApiService
import com.projetmobile.mobile.data.remote.admin.AdminCreateUserRequestDto
import com.projetmobile.mobile.data.remote.admin.AdminUpdateUserRequestDto
import com.projetmobile.mobile.data.repository.runRepositoryCall

/**
 * Rôle : Implémente l'interface administrative [AdminRepository] pour opérer concrètement 
 * sur les profils utilisateurs via les endpoints Retrofit dédiés.
 * 
 * Précondition : [AdminApiService] doit être injecté et prêt.
 * Postcondition : Toute réponse brute issue de l'API est mappée localement (ex: toAuthUser) 
 * et protégée par `runRepositoryCall`.
 */
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

/**
 * Rôle : Fonction de conversion de la couche "Entité Métier" vers "Dto Réseau" pour la Création.
 * 
 * Précondition : Objet métier renseigné.
 * Postcondition : Construit le DTO de requête JSON conforme aux spécifications du serveur.
 */
private fun AdminUserCreateInput.toDto() = AdminCreateUserRequestDto(
    login = login,
    password = password,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    role = role,
)

/**
 * Rôle : Fonction de conversion de l'Entité d'altération vers un format de Requête JSON (Update).
 * 
 * Précondition : Propriétés métier existantes.
 * Postcondition : Prépare la charge utile qui sera consommée en PUT/PATCH par le Backend.
 */
private fun AdminUserUpdateInput.toDto() = AdminUpdateUserRequestDto(
    login = login,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    role = role,
    emailVerified = emailVerified,
)
