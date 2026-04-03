package com.projetmobile.mobile.data.remote.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReservantDto(
    val id: Int,
    val name: String,
    val email: String,
    val type: String,
    @SerialName("editor_id") val editorId: Int? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val address: String? = null,
    val siret: String? = null,
    val notes: String? = null,
)

@Serializable
data class ReservantUpsertRequestDto(
    val name: String,
    val email: String,
    val type: String,
    @SerialName("editor_id") val editorId: Int? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val address: String? = null,
    val siret: String? = null,
    val notes: String? = null,
)

@Serializable
data class DeleteReservantResponseDto(
    val message: String,
)

@Serializable
data class ReservantContactDto(
    val id: Int,
    val name: String,
    val email: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("job_title") val jobTitle: String,
    val priority: Int,
)

@Serializable
data class ReservantContactUpsertRequestDto(
    val name: String,
    val email: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("job_title") val jobTitle: String,
    val priority: Int,
)

@Serializable
data class ReservantDeleteSummaryDto(
    @SerialName("reservant_id") val reservantId: Int,
    val contacts: List<ReservantDeleteContactSummaryDto> = emptyList(),
    val workflows: List<ReservantDeleteWorkflowSummaryDto> = emptyList(),
    val reservations: List<ReservantDeleteReservationSummaryDto> = emptyList(),
)

@Serializable
data class ReservantDeleteContactSummaryDto(
    val id: Int,
    val name: String,
    val email: String? = null,
)

@Serializable
data class ReservantDeleteWorkflowSummaryDto(
    val id: Int,
    @SerialName("festival_id") val festivalId: Int? = null,
    val state: String? = null,
    @SerialName("festival_name") val festivalName: String? = null,
)

@Serializable
data class ReservantDeleteReservationSummaryDto(
    val id: Int,
    @SerialName("festival_id") val festivalId: Int? = null,
    @SerialName("statut_paiement") val paymentStatus: String? = null,
    @SerialName("festival_name") val festivalName: String? = null,
    val relation: String? = null,
)

@Serializable
data class ReservantEditorDto(
    val id: Int,
    val name: String,
    val email: String? = null,
    val website: String? = null,
    val description: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("is_exhibitor") val isExhibitor: Boolean = false,
    @SerialName("is_distributor") val isDistributor: Boolean = false,
)

internal fun ReservantDraft.toRequestDto(): ReservantUpsertRequestDto {
    val normalizedType = type.trim().lowercase()
    return ReservantUpsertRequestDto(
        name = name.trim(),
        email = email.trim(),
        type = normalizedType,
        editorId = editorId.takeIf { normalizedType == "editeur" },
        phoneNumber = phoneNumber.trimmedOrNull(),
        address = address.trimmedOrNull(),
        siret = siret.trimmedOrNull(),
        notes = notes.trimmedOrNull(),
    )
}

internal fun ReservantContactDraft.toRequestDto(): ReservantContactUpsertRequestDto {
    return ReservantContactUpsertRequestDto(
        name = name.trim(),
        email = email.trim(),
        phoneNumber = phoneNumber.trim(),
        jobTitle = jobTitle.trim(),
        priority = priority,
    )
}

private fun String?.trimmedOrNull(): String? {
    return this?.trim()?.takeIf { value -> value.isNotEmpty() }
}
