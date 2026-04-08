package com.projetmobile.mobile.ui.screens.festival

import com.projetmobile.mobile.data.entity.festival.FestivalSummary

/**
 * Rôle : Représente l'état de l'interface utilisateur pour la liste des festivals.
 *
 * Précondition : Utilisé et mis à jour par le FestivalViewModel.
 *
 * Postcondition : Détient les variables de succès, de chargement, et d'affichage de messages d'erreur.
 */
data class FestivalUiState(
    val isLoading: Boolean = true,
    val festivals: List<FestivalSummary> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
