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

internal enum class ReservantsSortOption(val label: String) {
    NameAsc("Nom A -> Z"),
    NameDesc("Nom Z -> A"),
}

internal fun defaultReservantTypes(): List<ReservantTypeChoice> = ReservantTypeChoice.entries

internal fun reservantTypeLabel(value: String?): String {
    return defaultReservantTypes()
        .firstOrNull { it.value == value?.trim()?.lowercase() }
        ?.label
        ?: value.orEmpty().ifBlank { "-" }
}

internal typealias ReservantsLoader = suspend () -> Result<List<ReservantListItem>>
internal typealias ReservantObserver = (Int) -> Flow<ReservantDetail?>
internal typealias ReservantLoader = suspend (Int) -> Result<ReservantDetail>
internal typealias ReservantSave = suspend (ReservantDraft) -> Result<ReservantDetail>
internal typealias ReservantUpdate = suspend (Int, ReservantDraft) -> Result<ReservantDetail>
internal typealias ReservantDelete = suspend (Int) -> Result<String>
internal typealias ReservantDeleteSummaryLoader = suspend (Int) -> Result<ReservantDeleteSummary>
internal typealias ReservantContactsLoader = suspend (Int) -> Result<List<ReservantContact>>
internal typealias ReservantContactCreator = suspend (Int, ReservantContactDraft) -> Result<ReservantContact>
internal typealias ReservantEditorsLoader = suspend () -> Result<List<ReservantEditorOption>>
internal typealias ReservantGamesLoader = suspend (Int) -> Result<List<GameListItem>>

internal data class ReservantDeleteSummaryDialogModel(
    val contactsCount: Int = 0,
    val workflowsCount: Int = 0,
    val reservationsCount: Int = 0,
    val highlights: List<String> = emptyList(),
)

internal fun ReservantDeleteSummary.toDialogModel(): ReservantDeleteSummaryDialogModel {
    val highlights = buildList {
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

internal fun GameListItem.playersLabel(): String? {
    val minPlayersValue = minPlayers ?: return maxPlayers?.toString()
    val maxPlayersValue = maxPlayers ?: return minPlayersValue.toString()
    return if (minPlayersValue == maxPlayersValue) {
        minPlayersValue.toString()
    } else {
        "$minPlayersValue-$maxPlayersValue"
    }
}
