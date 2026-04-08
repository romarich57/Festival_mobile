/**
 * Rôle : Décrit les contrats, types partagés et helpers de base utilisés par les écrans de réservants.
 * Ce fichier centralise les choix de type, les options de tri, les chargeurs de données et les transformations de résumé avant affichage.
 * Précondition : Les écrans et ViewModels de réservants doivent dépendre de ces types au lieu de dupliquer leurs propres variantes.
 * Postcondition : Les couches UI disposent d'un vocabulaire commun et de fonctions de formatage cohérentes.
 */
package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.reservants.ReservantContact
import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantEditorOption
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import kotlinx.coroutines.flow.Flow

/**
 * Rôle : Représente les types de réservants proposés dans l'application.
 * Précondition : Les valeurs sont utilisées comme source de vérité pour les menus et filtres de type.
 * Postcondition : Chaque entrée fournit une valeur persistée et un libellé humain lisible.
 */
internal enum class ReservantTypeChoice(
    val value: String,
    val label: String,
) {
    Editor("editeur", "Éditeur"),
    Provider("prestataire", "Prestataire"),
    Shop("boutique", "Boutique"),
    Host("animateur", "Animateur"),
    Association("association", "Association"),
}

/**
 * Rôle : Définit les options de tri disponibles pour le catalogue des réservants.
 * Précondition : L'UI de catalogue doit utiliser ces options comme référence de tri.
 * Postcondition : Chaque option expose un libellé prêt à afficher dans un sélecteur.
 */
internal enum class ReservantsSortOption(val label: String) {
    NameAsc("Nom A -> Z"),
    NameDesc("Nom Z -> A"),
}

/**
 * Rôle : Fournit la liste canonique des types de réservants.
 * Précondition : Aucune donnée externe n'est requise.
 * Postcondition : Retourne toutes les entrées de `ReservantTypeChoice` dans leur ordre de déclaration.
 */
internal fun defaultReservantTypes(): List<ReservantTypeChoice> = ReservantTypeChoice.entries

/**
 * Rôle : Convertit une valeur technique de type réservant en libellé lisible pour l'interface.
 * Précondition : `value` peut être vide, nulle ou déjà normalisée.
 * Postcondition : Retourne le libellé correspondant ou un fallback lisible si la valeur n'est pas reconnue.
 */
internal fun reservantTypeLabel(value: String?): String {
    return defaultReservantTypes()
        .firstOrNull { it.value == value?.trim()?.lowercase() }
        ?.label
        ?: value.orEmpty().ifBlank { "-" }
}

/** Charge la liste de réservants pour les écrans de catalogue. */
internal typealias ReservantsLoader = suspend () -> Result<List<ReservantListItem>>

/** Observe en continu le détail d'un réservant identifié par son id. */
internal typealias ReservantObserver = (Int) -> Flow<ReservantDetail?>

/** Charge le détail complet d'un réservant pour les écrans de détail et de formulaire. */
internal typealias ReservantLoader = suspend (Int) -> Result<ReservantDetail>

/** Persiste un nouveau réservant depuis le formulaire de création. */
internal typealias ReservantSave = suspend (ReservantDraft) -> Result<ReservantDetail>

/** Met à jour un réservant existant à partir des valeurs du formulaire. */
internal typealias ReservantUpdate = suspend (Int, ReservantDraft) -> Result<ReservantDetail>

/** Supprime un réservant par son identifiant. */
internal typealias ReservantDelete = suspend (Int) -> Result<String>

/** Charge le résumé des dépendances avant confirmation de suppression. */
internal typealias ReservantDeleteSummaryLoader = suspend (Int) -> Result<ReservantDeleteSummary>

/** Charge les contacts associés à un réservant. */
internal typealias ReservantContactsLoader = suspend (Int) -> Result<List<ReservantContact>>

/** Crée un contact lié à un réservant. */
internal typealias ReservantContactCreator = suspend (Int, ReservantContactDraft) -> Result<ReservantContact>

/** Charge la liste des éditeurs disponibles pour le formulaire de réservant. */
internal typealias ReservantEditorsLoader = suspend () -> Result<List<ReservantEditorOption>>

/** Charge les jeux associés à un réservant. */
internal typealias ReservantGamesLoader = suspend (Int) -> Result<List<GameListItem>>

/**
 * Rôle : Résume les dépendances à afficher dans la boîte de dialogue de suppression d'un réservant.
 * Précondition : Les compteurs et la liste des points d'attention doivent déjà être calculés à partir du summary backend.
 * Postcondition : L'UI peut afficher une version compacte du risque de suppression.
 */
internal data class ReservantDeleteSummaryDialogModel(
    val contactsCount: Int = 0,
    val workflowsCount: Int = 0,
    val reservationsCount: Int = 0,
    val highlights: List<String> = emptyList(),
)

/**
 * Rôle : Convertit le résumé backend d'un réservant supprimable en modèle compact pour le dialogue de confirmation.
 * Précondition : Le summary doit provenir d'une requête de dépendances à jour.
 * Postcondition : Retourne des compteurs et quelques éléments d'aperçu lisibles par l'utilisateur.
 */
internal fun ReservantDeleteSummary.toDialogModel(): ReservantDeleteSummaryDialogModel {
    val highlights = buildList {
        // On limite l'aperçu à deux éléments par catégorie pour ne pas saturer la boîte de dialogue.
        contacts.take(2).forEach { contact ->
            add("Contact: ${contact.name}")
        }
        workflows.take(2).forEach { workflow ->
            val festivalLabel = workflow.festivalName?.takeIf { it.isNotBlank() }
                ?: workflow.festivalId?.let { "Festival #$it" }
                ?: "Festival inconnu"
            add("Workflow: $festivalLabel")
        }
        reservations.take(2).forEach { reservation ->
            val festivalLabel = reservation.festivalName?.takeIf { it.isNotBlank() }
                ?: reservation.festivalId?.let { "Festival #$it" }
                ?: "Festival inconnu"
            add("Réservation: $festivalLabel")
        }
    }

    return ReservantDeleteSummaryDialogModel(
        contactsCount = contacts.size,
        workflowsCount = workflows.size,
        reservationsCount = reservations.size,
        highlights = highlights,
    )
}

/**
 * Rôle : Formate la capacité de joueurs d'un jeu en libellé lisible pour l'UI réservants.
 * Précondition : L'item de jeu doit contenir au moins une borne de joueurs si une indication doit être affichée.
 * Postcondition : Retourne une plage, un nombre unique ou `null` selon les informations disponibles.
 */
internal fun GameListItem.playersLabel(): String? {
    val minPlayersValue = minPlayers ?: return maxPlayers?.toString()
    val maxPlayersValue = maxPlayers ?: return minPlayersValue.toString()
    return if (minPlayersValue == maxPlayersValue) {
        minPlayersValue.toString()
    } else {
        "$minPlayersValue-$maxPlayersValue"
    }
}
