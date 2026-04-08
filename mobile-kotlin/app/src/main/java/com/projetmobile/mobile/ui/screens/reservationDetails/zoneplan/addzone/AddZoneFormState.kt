/**
 * Rôle : Dictionnaire contenant les différentes valeurs du formulaire pour la création d'une nouvelle zone.
 *
 * Précondition : Doit englober tous les champs textes, numériques et erreurs de validations associés.
 *
 * Postcondition : Garantit la source de vérité pour le fonctionnement du formulaire de `AddZoneFormCard`.
 */
package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.addzone

/**
 * Rôle : Décrit l'état immuable du module la zone plan des réservations.
 */
data class ZoneTarifaireOptionState(
    val id: Int,
    val name: String,
    val nbTables: Int,
)

/**
 * Rôle : Décrit l'état immuable du module la zone plan des réservations.
 */
data class AddZoneFormState(
    val name: String = "",
    val selectedZoneTarifaireId: Int? = null,
    val nbTables: String = "",
    // Max tables autorisé selon les tables restantes dans la zone tarifaire
    val maxTables: Int = Int.MAX_VALUE,
)
