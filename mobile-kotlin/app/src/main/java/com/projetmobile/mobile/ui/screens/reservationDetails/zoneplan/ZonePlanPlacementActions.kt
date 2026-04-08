/**
 * Rôle : Gère les événements d'actions liées au placement sur le plan (éditer prix, chaises).
 *
 * Précondition : Différencie l'action d'édition de celle de soumission ou d'annulation.
 *
 * Postcondition : Informe le réducteur ou viewModel de la volonté du joueur ou de l'admin.
 */
package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.remote.zoneplan.GameAllocationUpdateDto
import com.projetmobile.mobile.data.remote.zoneplan.SimpleAllocationPayloadDto
import com.projetmobile.mobile.data.repository.toRepositoryException
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.placement.PlacementFormState
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * Rôle : Ouvre le formulaire de placement pour une zone de plan donnée.
 *
 * Précondition : Le ViewModel doit être dans un état succès et `zonePlanId` doit identifier une zone existante.
 *
 * Postcondition : Le formulaire de placement devient visible avec un état initial cohérent.
 */
fun ZonePlanViewModel.openPlacementForm(zonePlanId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        showPlacementForm = true,
        placementForm = PlacementFormState(zonePlanId = zonePlanId),
    )
}

/**
 * Rôle : Ferme le formulaire de placement sans modifier les allocations existantes.
 *
 * Précondition : Le formulaire de placement doit être potentiellement ouvert dans un état succès.
 *
 * Postcondition : L'UI masque le formulaire de placement.
 */
fun ZonePlanViewModel.closePlacementForm() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(showPlacementForm = false)
}

/**
 * Rôle : Bascule entre un placement simple et un placement lié à un jeu.
 *
 * Précondition : Le formulaire de placement doit être actif dans un état succès.
 *
 * Postcondition : Le mode sélectionné est mis à jour et la sélection de jeu est réinitialisée si nécessaire.
 */
fun ZonePlanViewModel.onWithGameChanged(value: Boolean) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(
            withGame = value,
            selectedGameAllocationId = null,
        ),
    )
}

/**
 * Rôle : Enregistre l'allocation de jeu sélectionnée pour le formulaire de placement.
 *
 * Précondition : Le formulaire doit être en mode lié à un jeu.
 *
 * Postcondition : L'identifiant sélectionné est stocké dans l'état du formulaire.
 */
fun ZonePlanViewModel.onGameSelected(allocationId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(selectedGameAllocationId = allocationId),
    )
}

/**
 * Rôle : Met à jour le nombre de places par exemplaire affiché dans le formulaire.
 *
 * Précondition : Le formulaire doit être actif et la saisie peut contenir des séparateurs décimaux.
 *
 * Postcondition : La valeur est nettoyée puis stockée dans l'état de formulaire.
 */
fun ZonePlanViewModel.onPlacePerCopyChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(placePerCopy = sanitizeDecimal(value)),
    )
}

/**
 * Rôle : Met à jour le nombre d'exemplaires saisis pour un jeu à placer.
 *
 * Précondition : Le formulaire doit être actif et la valeur saisie doit être interprétée comme un nombre.
 *
 * Postcondition : La valeur est nettoyée puis stockée dans l'état de formulaire.
 */
fun ZonePlanViewModel.onNbCopiesChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(nbCopies = sanitizeDecimal(value)),
    )
}

/**
 * Rôle : Met à jour le type de table utilisé par le placement.
 *
 * Précondition : Le formulaire de placement doit être visible.
 *
 * Postcondition : Le type de table courant est remplacé par la nouvelle valeur saisie.
 */
fun ZonePlanViewModel.onTableTypeChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(tableType = value),
    )
}

/**
 * Rôle : Met à jour le nombre de chaises associé au placement.
 *
 * Précondition : Le formulaire doit être actif et la saisie peut contenir des caractères à nettoyer.
 *
 * Postcondition : La valeur nettoyée est stockée dans le formulaire.
 */
fun ZonePlanViewModel.onChairsChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(nbChaises = sanitizeInt(value)),
    )
}

/**
 * Rôle : Met à jour le nombre de tables saisi pour un placement simple.
 *
 * Précondition : Le formulaire de placement doit être actif.
 *
 * Postcondition : La valeur numérique nettoyée est conservée dans le formulaire.
 */
fun ZonePlanViewModel.onNbTablesChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(nbTables = sanitizeInt(value)),
    )
}

/**
 * Rôle : Active ou désactive la saisie de surface en mètres carrés pour le placement.
 *
 * Précondition : Le formulaire doit être ouvert et en état succès.
 *
 * Postcondition : Le mode de saisie est basculé et les champs dépendants sont réinitialisés.
 */
fun ZonePlanViewModel.onUseM2Changed(value: Boolean) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(useM2 = value, m2Value = "", nbTables = "1"),
    )
}

/**
 * Rôle : Met à jour la surface en mètres carrés et déduit le nombre de tables correspondant.
 *
 * Précondition : Le formulaire doit être en mode saisie de surface.
 *
 * Postcondition : Le champ `m2Value` est nettoyé et `nbTables` est recalculé à partir de la surface saisie.
 */
fun ZonePlanViewModel.onM2ValueChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val sanitized = sanitizeDecimal(value)
    val m2 = sanitized.replace(',', '.').toDoubleOrNull() ?: 0.0
    val tables = if (m2 > 0) ceil(m2 / ZonePlanViewModel.M2_PER_TABLE).toInt() else 0
    uiState = current.copy(
        placementForm = current.placementForm.copy(m2Value = sanitized, nbTables = tables.toString()),
    )
}

/**
 * Rôle : Valide puis enregistre le placement courant, simple ou lié à un jeu.
 *
 * Précondition : Le formulaire doit être rempli dans un état succès et la zone ciblée doit être éligible à un placement.
 *
 * Postcondition : Le repository est mis à jour, l'état est rafraîchi et le formulaire se referme en cas de succès.
 */
fun ZonePlanViewModel.savePlacement() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val form = current.placementForm
    val zone = current.zones.find { it.id == form.zonePlanId } ?: return

    // Validate: reservant must have tables in linked zone tarifaire
    if (!zone.hasReservationInLinkedZone) {
        uiState = current.copy(userMessage = "Pas de tables réservées dans la zone tarifaire liée")
        return
    }

    viewModelScope.launch {
        uiState = current.copy(isSaving = true, userMessage = null)
        try {
            if (form.withGame) {
                saveGamePlacement(current)
            } else {
                saveSimplePlacement(current)
            }
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(
                showPlacementForm = false,
                userMessage = "Placement enregistré",
            )
        } catch (throwable: Throwable) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(
                    isSaving = false,
                    userMessage = throwable.zonePlanErrorMessage("Impossible d'enregistrer le placement."),
                )
            }
        }
    }
}

/**
 * Rôle : Supprime un placement simple existant par son identifiant.
 *
 * Précondition : `allocationId` doit identifier un placement simple réellement présent dans le repository.
 *
 * Postcondition : Le placement est supprimé, puis l'état est rafraîchi pour refléter la disparition de l'élément.
 */
fun ZonePlanViewModel.deleteSimpleAllocation(allocationId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    viewModelScope.launch {
        uiState = current.copy(isSaving = true)
        try {
            zonePlanRepository.deleteSimpleAllocationById(allocationId)
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(userMessage = "Placement supprimé")
        } catch (throwable: Throwable) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(
                    isSaving = false,
                    userMessage = throwable.zonePlanErrorMessage("Impossible de supprimer le placement."),
                )
            }
        }
    }
}

/**
 * Rôle : Retire un jeu déjà affecté à une zone de plan.
 *
 * Précondition : `allocationId` doit identifier une allocation de jeu existante.
 *
 * Postcondition : L'allocation est détachée de la zone et l'état est rechargé.
 */
fun ZonePlanViewModel.removeGameFromZone(allocationId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    viewModelScope.launch {
        uiState = current.copy(isSaving = true)
        try {
            zonePlanRepository.updateGameAllocation(
                allocationId,
                GameAllocationUpdateDto(zonePlanId = null),
            )
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(userMessage = "Jeu retiré de la zone")
        } catch (throwable: Throwable) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(
                    isSaving = false,
                    userMessage = throwable.zonePlanErrorMessage("Impossible de retirer le jeu de la zone."),
                )
            }
        }
    }
}

/**
 * Rôle : Convertit une exception de zone plan en message lisible pour l'utilisateur final.
 *
 * Précondition : L'exception doit provenir d'une opération repository, réseau ou métier du module zone plan.
 *
 * Postcondition : Retourne un message prêt à l'affichage, avec repli sur la valeur par défaut si nécessaire.
 */
private fun Throwable.zonePlanErrorMessage(defaultMessage: String): String {
    return toRepositoryException(defaultMessage).localizedMessage ?: defaultMessage
}

/**
 * Rôle : Enregistre un placement simple sur la zone ciblée.
 *
 * Précondition : L'état succès doit contenir une zone valide et au moins une table ou des chaises à allouer.
 *
 * Postcondition : Le repository reçoit une allocation simple conforme au formulaire.
 */
internal suspend fun ZonePlanViewModel.saveSimplePlacement(state: ZonePlanUiState.Success) {
    val form = state.placementForm
    val nbTables = form.nbTables.toIntOrNull() ?: 0
    val nbChaises = form.nbChaises.toIntOrNull() ?: 0
    if (nbTables <= 0 && nbChaises <= 0) throw IllegalArgumentException("Tables ou chaises requis")

    zonePlanRepository.createSimpleAllocation(
        reservationId = state.reservationId,
        zonePlanId = form.zonePlanId,
        payload = SimpleAllocationPayloadDto(
            nbTables = nbTables,
            nbChaises = nbChaises,
            tailleTable = form.tableType,
        ),
    )
}

/**
 * Rôle : Enregistre un placement de jeu sur la zone ciblée.
 *
 * Précondition : L'état succès doit contenir une allocation de jeu sélectionnée et des valeurs numériques exploitables.
 *
 * Postcondition : Le repository reçoit une mise à jour de l'allocation du jeu vers la zone choisie.
 */
internal suspend fun ZonePlanViewModel.saveGamePlacement(state: ZonePlanUiState.Success) {
    val form = state.placementForm
    val allocationId = form.selectedGameAllocationId
        ?: throw IllegalArgumentException("Sélectionnez un jeu")

    val placePerCopy = form.placePerCopy.replace(',', '.').toDoubleOrNull() ?: 1.0
    val nbCopies = form.nbCopies.replace(',', '.').toDoubleOrNull() ?: 1.0
    val nbChaises = form.nbChaises.toIntOrNull() ?: 0

    zonePlanRepository.updateGameAllocation(
        allocationId = allocationId,
        payload = GameAllocationUpdateDto(
            zonePlanId = form.zonePlanId,
            nbTablesOccupees = placePerCopy,
            nbExemplaires = nbCopies,
            nbChaises = nbChaises,
            tailleTableRequise = form.tableType,
        ),
    )
}
