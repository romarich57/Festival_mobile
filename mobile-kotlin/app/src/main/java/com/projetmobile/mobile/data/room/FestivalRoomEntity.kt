/**
 * Rôle du fichier :
 * Définit la structure d'une table SQL Room (`Company`/`Festival`) et ce que fait chaque colonne. 
 * Concrètement, cette "Data class" est utilisée par Room (la base de données locale d'Android) 
 * pour générer et comprendre la table SQL `festivals`.
 * C'est l'équivalent local du modèle de données (DTO) réseau.
 */
package com.projetmobile.mobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Rôle : Entité Room représentant de manière forte un festival dans la base de données locale du téléphone.
 * Les festivals restent pilotés par le serveur distant, mais la suppression locale planifiée 
 * s'effectue grâce aux variables de persistance `syncStatus` et `retryAction` (offline-first).
 *
 * Précondition : Le compilateur Room utilise l'annotation @Entity pour allouer la table `festivals`.
 * Postcondition : `FestivalRoomEntity` garantit l'intégrité du mapping local, prêt à être inséré ou altéré en SQLite.
 */
@Entity(tableName = "festivals")
data class FestivalRoomEntity(
    // Indique à SQL que l'Id est la clé primaire unique identifiant cette ligne (non-auto-générée ici).
    @PrimaryKey val id: Int,
    
    // Titre/nom du festival (ex: "Festival de la BD 2024").
    val name: String,
    
    // Dates stockées souvent sous format ISO8601 en String par facilité SQLite.
    val startDate: String,
    val endDate: String,
    
    // Informations métier sur les dotations d'exposition d'un festival
    val stockTablesStandard: Int,
    val stockTablesGrande: Int,
    val stockTablesMairie: Int,
    val stockChaises: Int,
    
    // Le prix global affecté...
    val prixPrises: Double,
    
    // --- Champs de métadonnées pour gestion asynchrone (Background Syncs) ---
    // Définit si l'enregistrement a été envoyé à l'API ou modifié hors connexion. (Défaut : SYNCED)
    val syncStatus: String = SyncStatus.SYNCED,
    
    // Stocke l'action différée ("DELETE") si l'utilisateur a jeté depuis l'application hors connexion.
    val retryAction: String? = null,
    
    // Pour des raisons d'interface, permet d'afficher pourquoi la publication ou suppression échoue.
    val lastSyncErrorMessage: String? = null,
)
