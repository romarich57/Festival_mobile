/**
 * Rôle : Décrit l'état UI immuable du module l'administration détail.
 */

package com.projetmobile.mobile.ui.screens.admin.detail

import com.projetmobile.mobile.data.entity.auth.AuthUser

/**
 * Rôle : Décrit l'état de l'administration du mode détaillé sur un AuthUser.
 *
 * Précondition : Met en attente tant que [isLoading] gère le chargement et offre l'état final.
 *
 * Postcondition : Affiche les informations formatées de l'utilisateur concerné ou l'erreur du fetch api.
 */
data class AdminUserDetailUiState(
    val user: AuthUser? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
