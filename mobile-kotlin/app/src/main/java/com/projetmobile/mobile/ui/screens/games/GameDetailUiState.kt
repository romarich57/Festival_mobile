/**
 * Rôle : Décrit l'état UI immuable du module les jeux.
 */

package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.GameDetail

/**
 * Rôle : Décrit l'affichage d'un jeu particulier.
 *
 * Précondition : Appelé par GameDetailViewModel.
 *
 * Postcondition : Stocke le jeu, son identifiant, l'état de chargement et l'erreur possible dans un record propre.
 */
data class GameDetailUiState(
    val gameId: Int,
    val game: GameDetail? = null,
    val canManageGames: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
