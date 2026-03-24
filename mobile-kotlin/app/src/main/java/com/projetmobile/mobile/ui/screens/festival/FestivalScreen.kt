package com.projetmobile.mobile.ui.screens.festival

import com.projetmobile.mobile.ui.screens.festival.FestivalViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projetmobile.mobile.ui.components.festival.FestivalList

/**
 * Écran liste des festivals.
 *
 * Équivalent du FestivalListComponent Angular côté orchestration :
 *  - Lit le ViewModel (comme inject(FestivalService) + inject(FestivalState))
 *  - Calcule currentFestivalId (comme computed() dans Angular)
 *  - Branche les callbacks vers le ViewModel
 *  - Délègue l'affichage à FestivalList (comme le template Angular délègue à FestivalCard)
 *
 * ⚠️ FestivalList et FestivalCard ne connaissent pas le ViewModel.
 *    Seul ce fichier fait le pont entre UI et logique métier.
 *
 * @param viewModel       Source de vérité partagée (festivals + festival courant).
 * @param canDelete       Équivalent isSuperOrganizer Angular — fourni par la nav/auth.
 * @param onFestivalClick Navigation vers le détail après sélection.
 */
@Composable
fun FestivalScreen(
    viewModel: FestivalViewModel,
    modifier: Modifier = Modifier,
    canDelete: Boolean = false,
    onFestivalClick: (id: Int) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Équivalent : computed(() => festivalStore.currentFestival()?.id)
    val currentFestivalId by viewModel.currentFestivalId.collectAsStateWithLifecycle()

    FestivalList(
        festivals = uiState.festivals,
        currentFestivalId = currentFestivalId,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        canDelete = canDelete,
        modifier = modifier,

        // Équivalent onFestivalClick() Angular → setCurrentFestival + navigate
        onSelect = { id ->
            if (id != null) {
                viewModel.selectFestival(id)
                onFestivalClick(id)
            } else {
                viewModel.clearSelection()
            }
        },

        // Équivalent requestDeleteFestival() Angular
        onDeleteRequest = { id ->
            viewModel.requestDeleteFestival(id)
        },

        // Équivalent rechargement liste
        onRetry = { viewModel.loadFestivals() },
    )
}