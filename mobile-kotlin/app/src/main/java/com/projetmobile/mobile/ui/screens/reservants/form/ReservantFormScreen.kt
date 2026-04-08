/**
 * Rôle : Compose l'écran les réservants formulaire et orchestre l'affichage de l'état et des actions utilisateur.
 */

package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

/**
 * Rôle : UI permettant l'insertion d'un nouveau contact / éditeur ou la modification des attributs de ces derniers.
 *
 * Précondition : Appliqué au mode de fonctionnement `mode` de [uiState] qui définit s'il s'agit d'une création ou édition.
 *
 * Postcondition : Assure la validation de premier plan et intercepte la soumission via les callbacks de [actions].
 */
@Composable
internal fun ReservantFormScreen(
    uiState: ReservantFormUiState,
    actions: ReservantFormActions,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("reservant-form-root"),
    ) {
        val showHorizontalActions = maxWidth >= 520.dp

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (uiState.isLoading) {
                // Un chargement initial remplace le formulaire pour éviter d'exposer un état partiellement prêt.
                ReservantsLoadingCard(text = "Chargement du réservant…")
                return@Box
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        // Les erreurs bloquantes prennent la priorité sur les messages de lookup secondaires.
                        AuthFeedbackBanner(
                            message = uiState.errorMessage,
                            tone = AuthFeedbackTone.Error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                if (uiState.errorMessage == null && uiState.lookupErrorMessage != null) {
                    item {
                        // Un échec de chargement des lookups reste visible mais ne doit pas masquer un formulaire valide.
                        AuthFeedbackBanner(
                            message = uiState.lookupErrorMessage,
                            tone = AuthFeedbackTone.Error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item {
                    AuthCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            FestivalTextField(
                                value = uiState.fields.name,
                                onValueChange = actions.onNameChanged,
                                label = "Nom",
                                isError = uiState.fields.nameError != null,
                                supportingText = uiState.fields.nameError,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            FestivalTextField(
                                value = uiState.fields.email,
                                onValueChange = actions.onEmailChanged,
                                label = "Email",
                                isError = uiState.fields.emailError != null,
                                supportingText = uiState.fields.emailError,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            // Le sélecteur de type ouvre aussi le choix de l'éditeur lié quand le modèle métier l'exige.
                            ReservantsDropdownSelector(
                                label = "Type",
                                selectedLabel = defaultReservantTypes()
                                    .firstOrNull { it.value == uiState.fields.type }
                                    ?.label ?: "Choisir",
                                options = buildList {
                                    add("Choisir" to null)
                                    addAll(defaultReservantTypes().map { it.label to it.value })
                                },
                                onValueSelected = actions.onTypeSelected,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            uiState.fields.typeError?.let { typeError ->
                                Text(
                                    text = typeError,
                                    color = Color(0xFFC62828),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            if (uiState.shouldShowEditorSelector) {
                                // Le champ éditeur n'apparaît que pour les types qui en ont besoin.
                                ReservantsDropdownSelector(
                                    label = "Éditeur lié",
                                    selectedLabel = uiState.availableEditors
                                        .firstOrNull { it.id == uiState.fields.linkedEditorId }
                                        ?.name ?: "Choisir",
                                    options = uiState.availableEditors.map { it.name to it.id },
                                    onValueSelected = actions.onLinkedEditorSelected,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                uiState.fields.linkedEditorError?.let { editorError ->
                                    Text(
                                        text = editorError,
                                        color = Color(0xFFC62828),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            FestivalTextField(
                                value = uiState.fields.phoneNumber,
                                onValueChange = actions.onPhoneNumberChanged,
                                label = "Téléphone",
                                isError = uiState.fields.phoneNumberError != null,
                                supportingText = uiState.fields.phoneNumberError,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            FestivalTextField(
                                value = uiState.fields.address,
                                onValueChange = actions.onAddressChanged,
                                label = "Adresse",
                                modifier = Modifier.fillMaxWidth(),
                            )
                            FestivalTextField(
                                value = uiState.fields.siret,
                                onValueChange = actions.onSiretChanged,
                                label = "Siret",
                                modifier = Modifier.fillMaxWidth(),
                            )
                            FestivalTextField(
                                value = uiState.fields.notes,
                                onValueChange = actions.onNotesChanged,
                                label = "Notes",
                                singleLine = false,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            // La barre d'actions s'adapte à la largeur pour conserver des boutons lisibles sur mobile.
                            FormActionRow(
                                showHorizontalActions = showHorizontalActions,
                                isSaving = uiState.isSaving,
                                onBackToList = actions.onBackToList,
                                onSaveReservant = actions.onSaveReservant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
/**
 * Rôle : Exécute l'action formulaire action row du module les réservants formulaire.
 *
 * Précondition : L'état UI et les callbacks ou dépendances nécessaires doivent être disponibles.
 *
 * Postcondition : L'interface reflète l'état courant et propage les événements utilisateur.
 */
private fun FormActionRow(
    showHorizontalActions: Boolean,
    isSaving: Boolean,
    onBackToList: () -> Unit,
    onSaveReservant: () -> Unit,
    showSave: Boolean = true,
) {
    if (showHorizontalActions) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onBackToList,
                modifier = Modifier.weight(1f),
            ) {
                Text("Annuler")
            }
            if (showSave) {
                PrimaryAuthButton(
                    text = if (isSaving) "Enregistrement…" else "Enregistrer",
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    onClick = onSaveReservant,
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onBackToList,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Annuler")
            }
            if (showSave) {
                PrimaryAuthButton(
                    text = if (isSaving) "Enregistrement…" else "Enregistrer",
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSaveReservant,
                )
            }
        }
    }
}
