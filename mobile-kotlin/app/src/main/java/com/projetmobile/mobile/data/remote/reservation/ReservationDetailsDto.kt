package com.projetmobile.mobile.data.remote.reservation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkflowUpdatePayload(
    val state: String,
    val liste_jeux_demandee: Boolean,
    val liste_jeux_obtenue: Boolean,
    val jeux_recus: Boolean,
    val presentera_jeux: Boolean

)


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