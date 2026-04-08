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

/**
 * Rôle : Modéliser un objet Reservant du format d'échange API (DTO) vers son composant graphique léger en liste.
 * 
 * Précondition : DTO contenant les données JSON obligatoires minimums mappées.
 * Postcondition : Un résultat propre et confiné pour les Data Classes du domaine UI (liste).
 */
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

/**
 * Rôle : Transformer un DTO Reservant vers l'objet descriptif global du Réservant (Domaine/Métier).
 * 
 * Précondition : DTO contenant toutes les données nécessaires issues du profil détaillé.
 * Postcondition : Composant `ReservantDetail` complet prêt pour l'écran de consultation.
 */
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

/**
 * Rôle : Assure le mapping un-à-un d'un contact d'exposant avec l'entité interne de domaine de ce contact.
 * 
 * Précondition : Objet JSON contact dé-sérialisé de façon valide en `ReservantContactDto`.
 * Postcondition : `ReservantContact` encapsulé indépendamment de la couche réseau.
 */
fun ReservantContactDto.toReservantContact(): ReservantContact = ReservantContact(
    id = id,
    name = name,
    email = email,
    phoneNumber = phoneNumber,
    jobTitle = jobTitle,
    priority = priority,
)

/**
 * Rôle : Traduire la pré-requête complexe envoyée lors de la suppression d'un réservant serveur (liste des choses supprimées en cascade)
 * vers l'outil d'avertissement métier Android.
 * 
 * Précondition : Le DTO `ReservantDeleteSummaryDto` regroupe des listes complètes des contacts, résas et modérations corollaires à la suppression du compte.
 * Postcondition : Map la réponse globale et cascade le mapping sur chacune de ses sous-listes (workflows, reservations, contacts).
 */
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

/**
 * Rôle : Transformer un éditeur "parent" (du coté `Reservant` - API Exposant) pour qu'il soit sélectionnable.
 * 
 * Précondition : Le DTO inclut les flags isExhibitor/Distributor entre autres informations d'entreprise.
 * Postcondition : `ReservantEditorOption` prêt pour remplir le Spinner/Dropdown.
 */
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

/**
 * Rôle : Extraire les composantes simples d'un contact censé disparaître (cascade SQL).
 * 
 * Précondition : Réception d'un objet réseau statuant le contact visé par la suppression.
 * Postcondition : Map et retourne le `ReservantDeleteContactSummary`.
 */
fun ReservantDeleteContactSummaryDto.toReservantDeleteContactSummary(): ReservantDeleteContactSummary {
    return ReservantDeleteContactSummary(
        id = id,
        name = name,
        email = email,
    )
}

/**
 * Rôle : Traduire et formater un flux de modération (workflow de réservation) voué à s'annuler
 * dans le contexte de suppression d'un utilisateur.
 * 
 * Précondition : Le DTO contient l'association workflow-référence festival.
 * Postcondition : `ReservantDeleteWorkflowSummary` pour alerter l'utilisateur de l'ampleur de la suppression.
 */
fun ReservantDeleteWorkflowSummaryDto.toReservantDeleteWorkflowSummary(): ReservantDeleteWorkflowSummary {
    return ReservantDeleteWorkflowSummary(
        id = id,
        festivalId = festivalId,
        state = state,
        festivalName = festivalName,
    )
}

/**
 * Rôle : Expliciter et rendre lisible les données relatives à une réservation d'exposant
 * qui sautera inévitablement lors du delete cascade.
 * 
 * Précondition : `ReservantDeleteReservationSummaryDto` avec les données festival + statut de facturation.
 * Postcondition : Un record métier `ReservantDeleteReservationSummary` avertissant des conséquences métier.
 */
fun ReservantDeleteReservationSummaryDto.toReservantDeleteReservationSummary(): ReservantDeleteReservationSummary {
    return ReservantDeleteReservationSummary(
        id = id,
        festivalId = festivalId,
        paymentStatus = paymentStatus,
        festivalName = festivalName,
        relation = relation,
    )
}
