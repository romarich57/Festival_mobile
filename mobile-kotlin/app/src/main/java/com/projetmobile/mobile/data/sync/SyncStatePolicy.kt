package com.projetmobile.mobile.data.sync

import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus

/**
 * Rôle : Déterminer la nature exacte de l'action de reprise (CREATE, UPDATE, DELETE) à effectuer sur une entité locale.
 * 
 * Précondition : Un statut de synchronisation original et potentiellement l'action de reprise actuelle.
 * Postcondition : Retourne la constante d'action de synchronisation pertinente ou un null explicite.
 */
fun resolveRetryAction(syncStatus: String, retryAction: String?): String? {
    return retryAction ?: when (syncStatus) {
        SyncStatus.PENDING_CREATE -> SyncRetryAction.CREATE
        SyncStatus.PENDING_UPDATE -> SyncRetryAction.UPDATE
        SyncStatus.PENDING_DELETE -> SyncRetryAction.DELETE
        else -> null
    }
}

/**
 * Rôle : Décider si une entité doit être masquée de l'affichage UI standard (ex: lors d'une suppression différée).
 * 
 * Précondition : Le tuple d'états d'erreur réseau d'une entité Room (état courant, action à rejouer).
 * Postcondition : Vrai si l'élément correspond à une suppression en attente ou erreur sur une suppression, rendant la visibilité UI inopportune.
 */
fun shouldHideFromCollections(syncStatus: String, retryAction: String?): Boolean {
    val resolvedAction = resolveRetryAction(syncStatus, retryAction)
    return syncStatus == SyncStatus.PENDING_DELETE ||
        (syncStatus == SyncStatus.ERROR && resolvedAction == SyncRetryAction.DELETE)
}

/**
 * Rôle : Assurer qu'une donnée modifiée hors-ligne mais non encore poussée vers le serveur n'est pas effacée par un rechargement descendant global.
 * 
 * Précondition : La configuration locale indique une éventuelle mise à jour/création avec une action de synchro prévue.
 * Postcondition : Renvoie vrai si l'entité locale est un "changement en attente" (pending).
 */
fun shouldPreserveLocalDuringRefresh(syncStatus: String, retryAction: String?): Boolean {
    return resolveRetryAction(syncStatus, retryAction) != null
}

/**
 * Rôle : Identifier les brouillons de création (dont l'ID asynchrone est temporaire / < 0) qui doivent survivre au rafraîchissement global.
 * 
 * Précondition : `id` d'entité, et états de synchronisation.
 * Postcondition : Retourne vrai pour tout élément local temporaire (id < 0) justifiant sa présence par une attente de création réseau (`retryAction` valide ou status d'`error`).
 */
fun shouldKeepLocalOnlyEntity(
    id: Int,
    syncStatus: String,
    retryAction: String?,
): Boolean {
    return id < 0 && (
        shouldPreserveLocalDuringRefresh(syncStatus, retryAction) ||
            syncStatus == SyncStatus.ERROR
        )
}
