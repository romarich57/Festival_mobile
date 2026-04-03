package com.projetmobile.mobile.data.room

/**
 * Statuts de synchronisation pour les entités Room.
 *
 * - SYNCED          : Donnée à jour avec le serveur.
 * - PENDING_CREATE  : Créée hors-ligne, en attente d'envoi au serveur.
 * - PENDING_UPDATE  : Modifiée hors-ligne, en attente de mise à jour serveur.
 * - PENDING_DELETE  : Marquée pour suppression, en attente de confirmation serveur.
 * - ERROR           : Dernière tentative de sync échouée (retry possible).
 */
object SyncStatus {
    const val SYNCED         = "SYNCED"
    const val PENDING_CREATE = "PENDING_CREATE"
    const val PENDING_UPDATE = "PENDING_UPDATE"
    const val PENDING_DELETE = "PENDING_DELETE"
    const val ERROR          = "ERROR"
}
