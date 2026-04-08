package com.projetmobile.mobile.data.remote.reservation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rôle : Permet de modéliser les blocs de progression administrative ou suivi logistique. 
 * Formulaire de validation lors d'un envoi PUT.
 * 
 * Précondition : Le workflow possède les propriétés réelles en corrélation.
 * Postcondition : Format envoyé dans Request Body.
 */
@Serializable
data class WorkflowUpdatePayload(
    val state: String,
    val liste_jeux_demandee: Boolean,
    val liste_jeux_obtenue: Boolean,
    val jeux_recus: Boolean,
    val presentera_jeux: Boolean

)

/**
 * Rôle : Cartographie intégrale des étapes logistiques à l'instant T lues depuis le Backend.
 * 
 * Précondition : Réservation existante assortie d'un module logistique lié.
 * Postcondition : Contient aussi un dictionnaire des moments de prise de contact (relance).
 */
@Serializable
data class WorkflowDto(
    val id: Int,
    val state: String = "Pas_de_contact",
    val liste_jeux_demandee: Boolean = false,
    val liste_jeux_obtenue: Boolean = false,
    val jeux_recus: Boolean = false,
    val presentera_jeux: Boolean = false,
    val contact_dates: List<String> = emptyList()
)