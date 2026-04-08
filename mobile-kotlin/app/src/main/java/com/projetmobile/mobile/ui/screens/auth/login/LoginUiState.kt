/**
 * Rôle : Représente l'état immuable complet de l'interface utilisateur pour l'écran de connexion.
 * Contient les valeurs saisies, les messages d'erreurs, l'état de chargement et potentiellement l'utilisateur fraîchement connecté.
 * Précondition : Instancié et mis à jour continuellement par le `LoginViewModel`.
 * Postcondition : Fournit une photographie fiable des données à afficher à un instant T.
 */
package com.projetmobile.mobile.ui.screens.auth.login

import com.projetmobile.mobile.data.entity.auth.AuthUser

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val identifierError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val authenticatedUser: AuthUser? = null,
)
