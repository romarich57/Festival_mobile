/**
 * Rôle : Déclare les actions disponibles pour l'ajout d'une nouvelle zone.
 *
 * Précondition : Permet l'isolation des différents événements de texte et actions de validation.
 *
 * Postcondition : Utilisé par le ViewModel pour interpréter cette intention.
 */
package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.repository.toRepositoryException
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.addzone.AddZoneFormState
import kotlinx.coroutines.launch

/**
 * Rôle : Ouvre le formulaire d'ajout d'une zone dans l'écran zone plan.
 *
 * Précondition : Le ViewModel doit être dans un état succès pour pouvoir modifier l'UI.
 *
 * Postcondition : Le formulaire d'ajout devient visible et l'état de saisie est réinitialisé.
 */
fun ZonePlanViewModel.openAddZoneForm() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(showAddZoneForm = true, addZoneForm = AddZoneFormState())
}

/**
 * Rôle : Ferme le formulaire d'ajout de zone.
 *
 * Précondition : Le ViewModel doit être dans un état succès avec un formulaire potentiellement ouvert.
 *
 * Postcondition : Le formulaire n'est plus visible, sans altérer les données persistées.
 */
fun ZonePlanViewModel.closeAddZoneForm() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(showAddZoneForm = false)
}

/**
 * Rôle : Met à jour le nom saisi pour la nouvelle zone.
 *
 * Précondition : Le formulaire d'ajout doit être accessible dans un état succès.
 *
 * Postcondition : Le champ `name` du formulaire reflète la saisie courante.
 */
fun ZonePlanViewModel.onAddZoneNameChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(addZoneForm = current.addZoneForm.copy(name = value))
}

/**
 * Rôle : Sélectionne la zone tarifaire cible pour la nouvelle zone.
 *
 * Précondition : L'identifiant reçu doit correspondre à une zone tarifaire disponible.
 *
 * Postcondition : Le formulaire est recalé sur le nombre maximum de tables disponible pour cette zone tarifaire.
 */
fun ZonePlanViewModel.onAddZoneZoneTarifaireSelected(id: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val zt = current.zonesTarifaires.find { it.id == id } ?: return
    // Calculer les tables disponibles : total ZT - somme des zones plan existantes sur cette ZT
    val available = current.ztAvailableTables[id] ?: zt.nbTables
    uiState = current.copy(
        addZoneForm = current.addZoneForm.copy(
            selectedZoneTarifaireId = id,
            maxTables = available,
            // Reset nb tables si dépasse le nouveau max
            nbTables = current.addZoneForm.nbTables.toIntOrNull()
                ?.coerceAtMost(available)?.toString() ?: "",
        ),
    )
}

/**
 * Rôle : Met à jour le nombre de tables saisi pour la nouvelle zone.
 *
 * Précondition : Le formulaire d'ajout doit être actif.
 *
 * Postcondition : Le champ numérique est nettoyé et reflète la valeur saisie.
 */
fun ZonePlanViewModel.onAddZoneNbTablesChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val sanitized = sanitizeInt(value)
    uiState = current.copy(addZoneForm = current.addZoneForm.copy(nbTables = sanitized))
}

/**
 * Rôle : Valide et enregistre une nouvelle zone sur le plan.
 *
 * Précondition : Le formulaire doit être ouvert et contenir un nom, une zone tarifaire et un nombre de tables cohérents.
 *
 * Postcondition : La zone est créée, l'état est rafraîchi et le formulaire est refermé en cas de succès.
 */
fun ZonePlanViewModel.saveAddZone() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val form = current.addZoneForm

    val name = form.name.trim()
    val ztId = form.selectedZoneTarifaireId
    val nbTables = form.nbTables.toIntOrNull() ?: 0

    if (name.isBlank()) {
        uiState = current.copy(userMessage = "Le nom est requis")
        return
    }
    if (ztId == null) {
        uiState = current.copy(userMessage = "Sélectionnez une zone tarifaire")
        return
    }
    if (nbTables <= 0) {
        uiState = current.copy(userMessage = "Nombre de tables invalide")
        return
    }
    if (nbTables > form.maxTables) {
        uiState = current.copy(userMessage = "Maximum ${form.maxTables} tables disponibles pour cette zone tarifaire")
        return
    }

    viewModelScope.launch {
        uiState = current.copy(isSaving = true, userMessage = null)
        try {
            zonePlanRepository.createZonePlan(
                festivalId = current.festivalId,
                name = name,
                idZoneTarifaire = ztId,
                nbTables = nbTables,
            )
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(showAddZoneForm = false, userMessage = "Zone créée")
        } catch (throwable: Throwable) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(
                    isSaving = false,
                    userMessage = throwable.zonePlanAddZoneErrorMessage("Impossible de créer la zone."),
                )
            }
        }
    }
}

/**
 * Rôle : Supprime une zone de plan existante.
 *
 * Précondition : L'identifiant reçu doit correspondre à une zone réellement présente dans l'état succès.
 *
 * Postcondition : La zone est supprimée côté repository puis l'écran est rafraîchi.
 */
fun ZonePlanViewModel.deleteZonePlan(zonePlanId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    viewModelScope.launch {
        uiState = current.copy(isSaving = true)
        try {
            zonePlanRepository.deleteZonePlan(zonePlanId)
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(userMessage = "Zone de plan supprimée")
        } catch (throwable: Throwable) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(
                    isSaving = false,
                    userMessage = throwable.zonePlanAddZoneErrorMessage("Impossible de supprimer la zone."),
                )
            }
        }
    }
}

/**
 * Rôle : Convertit une exception liée à l'ajout de zone en message lisible pour l'utilisateur.
 *
 * Précondition : L'exception doit provenir d'une opération repository ou réseau.
 *
 * Postcondition : Retourne un message exploitable par l'UI, avec un repli sur le message par défaut si nécessaire.
 */
private fun Throwable.zonePlanAddZoneErrorMessage(defaultMessage: String): String {
    return toRepositoryException(defaultMessage).localizedMessage ?: defaultMessage
}
