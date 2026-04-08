package com.projetmobile.mobile.data.room

/**
 * Rôle : Variables constantes servant d'attributs de statut à l'état de validation
 * d'une entité hors-ligne dans la synchronisation locale de Room.
 * 
 * - SYNCED         : Enregistrement identique avec celui existant sur le serveur.
 * - PENDING_CREATE : Créé exclusivement sur le téléphone, attente d'attribution finale d'ID.
 * - PENDING_UPDATE : Modifications internes au téléphone, nécessitant un report serveur.
 * - PENDING_DELETE : Marquée supprimée par l'application locale, le backend doit l'accepter.
 * - ERROR          : Échec d'une manœuvre : nécessitera que l'app retente à réception de net.
 * 
 * Précondition : S'insère dans la structure Room par le biais d'un champ String.
 * Postcondition : Établit un système distribué de type Eventual Consistency.
 */
object SyncStatus {
    const val SYNCED         = "SYNCED"
    const val PENDING_CREATE = "PENDING_CREATE"
    const val PENDING_UPDATE = "PENDING_UPDATE"
    const val PENDING_DELETE = "PENDING_DELETE"
    const val ERROR          = "ERROR"
}
