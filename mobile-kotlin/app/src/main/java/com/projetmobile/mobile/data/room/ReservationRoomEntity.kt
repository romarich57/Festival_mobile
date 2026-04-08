/**
 * Rôle du fichier :
 * Structure abstraite (Entity) d'une Réservation. Ce script décrit la constitution précise
 * de la table SQLite `reservations` au niveau local.
 * L'objectif est double : garder la mémoire d'un stand vendu et autoriser une application en hors-ligne 
 * via le système de 'pendingDraftJson' propre aux composants en création attente.
 */
package com.projetmobile.mobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Rôle : Entité Room représentant une ligne du tableau de bord des réservations.
 *
 * Précondition : Utilisé par le système SQLite de Room pour cartographier la table `reservations`.
 * Postcondition : Persiste les métadonnées de réservation et conserve l'historique local et asynchrone des modifications.
 * 
 * Convention des IDs : 
 * - id > 0 = Le serveur a acté l'espace ;
 * - id < 0 = L'utilisateur a préparé cette place localement mais l'envoi API n'a pas été reçu.
 */
@Entity(tableName = "reservations")
data class ReservationRoomEntity(
    // Clé unique et primitive de la table SQLite
    @PrimaryKey val id: Int,
    
    // Référence au festival parent (clé étrangère implicite pour le filtrage local d'Event).
    val festivalId: Int,
    
    // Nom lisible du bénéficiaire (Association/Éditeur) affiché à l'écran.
    val reservantName: String,
    
    // Typlogie (EXPOSANT, EDITEUR...).
    val reservantType: String,
    
    // Workflow étatique : de EN_ATTENTE à CONFIRME, et finalement PRESENT.
    val workflowState: String,
    
    // État réseau standard : Sync vs Non-sync (Défaut: SYNCED)
    val syncStatus: String = SyncStatus.SYNCED,
    
    // Données de création de Réservation (ReservationCreatePayloadDto) encodées en JSON string
    // Permet d'envoyer l'objet initial après un retour réseau si créé et PENDING_CREATE.
    val pendingDraftJson: String? = null,
    
    // Le code action bloquée, en attente d'un "réessayer" (DELETE/UPDATE/CREATE).
    val retryAction: String? = null,
    
    // Chaine de caractères destinée à loguer la ou les causes d'un envoi raté (Code retour 500 par ex.)
    val lastSyncErrorMessage: String? = null,
)
