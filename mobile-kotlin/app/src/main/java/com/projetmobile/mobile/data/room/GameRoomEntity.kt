/**
 * Rôle du fichier :
 * Structure abstraite (Entity) SQLite pour les Jeux (Games). 
 * Ce fichier modélise la table `games` afin que la bibliothèque Room puisse construire
 * efficacement les colonnes SQL locales en miroir avec notre API distante.
 * Il assure également le stockage d'informations temporaires pour un mode hors-ligne.
 */
package com.projetmobile.mobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Rôle : Entité Room représentant un jeu dans la base de données locale.
 *
 * Précondition : Utilisé par le compilateur Room pour générer la table `games`.
 * Postcondition : Structure abstraite (Entity) SQLite pour les Jeux (Games), capable de gérer
 * les variables d'état asynchrone hors-ligne.
 *
 * Convention des IDs :
 *  - id > 0  : ID serveur (item synchronisé ou en attente de mise à jour / suppression distante)
 *  - id < 0  : ID local temporaire (item créé hors-ligne, en attente de création serveur)
 */
@Entity(tableName = "games")
data class GameRoomEntity(
    // Identifiant principal unique de table.
    @PrimaryKey val id: Int,
    
    // Le nom officiel du produit / jeu.
    val title: String,
    
    // Le genre de jeu (ex: "Enfant", "Expert").
    val type: String,
    
    // Jointure faible (non gérée par ForeignKey via Room intentionnellement) vers l'Entité Editeur.
    val editorId: Int?,
    val editorName: String?,
    
    // Restrictions et métadonnées du gameplay
    val minAge: Int,
    val authors: String,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val prototype: Boolean,  // Si c'est un jeu pas encore édité commercialement
    val durationMinutes: Int?, // Durée approximative d'une session de jeu.
    val theme: String?,
    
    // Longue chaine contenant la description commerciale.
    val description: String?,
    
    // Liens externes (Pochettes boite, vidéos youtube des règles).
    val imageUrl: String?,
    val rulesVideoUrl: String?,
    
    // Sérialisation des informations sur la mécanique principale
    val mechanismsJson: String,
    
    // --- Partie Gestion Backend / Offline-First ---
    // Indicateur de synchronisation, par défaut défini comme reçu du serveur
    val syncStatus: String = SyncStatus.SYNCED,
    
    // Stockage des brouillons (Drafts) JSON pendant que l'appareil du visiteur capte de nouveau du réseau
    val pendingDraftJson: String? = null,
    
    // Marqueur "L'utilisateur voulait Update/Create/Delete, on ressaiera au réveil réseau !"
    val retryAction: String? = null,
    
    // Trace de la dernière exception levée (ex. "HTTP 409 Bad Gateway") visible pour débugguer. 
    val lastSyncErrorMessage: String? = null,
)
