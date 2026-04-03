package com.projetmobile.mobile.data.mapper.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantContact
import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteContactSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteReservationSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteWorkflowSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.data.entity.reservants.ReservantEditorOption
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import com.projetmobile.mobile.data.remote.reservants.ReservantContactDto
import com.projetmobile.mobile.data.remote.reservants.ReservantDeleteContactSummaryDto
import com.projetmobile.mobile.data.remote.reservants.ReservantDeleteReservationSummaryDto
import com.projetmobile.mobile.data.remote.reservants.ReservantDeleteSummaryDto
import com.projetmobile.mobile.data.remote.reservants.ReservantDeleteWorkflowSummaryDto
import com.projetmobile.mobile.data.remote.reservants.ReservantDto
import com.projetmobile.mobile.data.remote.reservants.ReservantEditorDto

fun ReservantDto.toReservantListItem(): ReservantListItem = ReservantListItem(
    id = id,
    name = name,
    email = email,
    type = type,
    editorId = editorId,
    phoneNumber = phoneNumber,
    address = address,
    siret = siret,
    notes = notes,
)

fun ReservantDto.toReservantDetail(): ReservantDetail = ReservantDetail(
    id = id,
    name = name,
    email = email,
    type = type,
    editorId = editorId,
    phoneNumber = phoneNumber,
    address = address,
    siret = siret,
    notes = notes,
)

fun ReservantContactDto.toReservantContact(): ReservantContact = ReservantContact(
    id = id,
    name = name,
    email = email,
    phoneNumber = phoneNumber,
    jobTitle = jobTitle,
    priority = priority,
)

fun ReservantDeleteSummaryDto.toReservantDeleteSummary(): ReservantDeleteSummary {
    return ReservantDeleteSummary(
        reservantId = reservantId,
        contacts = contacts.map(ReservantDeleteContactSummaryDto::toReservantDeleteContactSummary),
        workflows = workflows.map(ReservantDeleteWorkflowSummaryDto::toReservantDeleteWorkflowSummary),
        reservations = reservations.map(
            ReservantDeleteReservationSummaryDto::toReservantDeleteReservationSummary,
        ),
    )
}

fun ReservantEditorDto.toReservantEditorOption(): ReservantEditorOption = ReservantEditorOption(
    id = id,
    name = name,
    email = email,
    website = website,
    description = description,
    logoUrl = logoUrl,
    isExhibitor = isExhibitor,
    isDistributor = isDistributor,
)

fun ReservantDeleteContactSummaryDto.toReservantDeleteContactSummary(): ReservantDeleteContactSummary {
    return ReservantDeleteContactSummary(
        id = id,
        name = name,
        email = email,
    )
}

fun ReservantDeleteWorkflowSummaryDto.toReservantDeleteWorkflowSummary(): ReservantDeleteWorkflowSummary {
    return ReservantDeleteWorkflowSummary(
        id = id,
        festivalId = festivalId,
        state = state,
        festivalName = festivalName,
    )
}

fun ReservantDeleteReservationSummaryDto.toReservantDeleteReservationSummary(): ReservantDeleteReservationSummary {
    return ReservantDeleteReservationSummary(
        id = id,
        festivalId = festivalId,
        paymentStatus = paymentStatus,
        festivalName = festivalName,
        relation = relation,
    )
}
