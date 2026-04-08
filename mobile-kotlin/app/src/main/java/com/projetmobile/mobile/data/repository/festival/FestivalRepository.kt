package com.projetmobile.mobile.data.repository.festival

import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalDto
import kotlinx.coroutines.flow.Flow

/**
 * Rôle : Contrat d'accès pour interagir avec les données des Festivals. 
 * Orchestrer la base de données locale (Room) comme Single Source of Truth, et le backend distant.
 * 
 * Précondition : Modules locaux initialisés.
 * Postcondition : Délivre des flux asynchrones Flow pour la réactivité, ainsi que 
 * des méthodes `suspend` pour les opérations ciblées/offlines.
 */
interface FestivalRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    /** 
     * Rôle : Flux live de tous les festivals depuis Room. 
     * 
     * Précondition : Données collectées dans la DAO locale.
     * Postcondition : Offre un flow ininterrompu de résumés d'entités.
     */
    fun observeFestivals(): Flow<List<FestivalSummary>>

    /** 
     * Rôle : Flux live d'un festival précis par son ID. 
     * 
     * Précondition : ID Festival correct.
     * Postcondition : Actualisation temps réel de ses informations pointées en locale.
     */
    fun observeFestival(id: Int): Flow<FestivalSummary?>

    // ── Déclenchement réseau ────────────────────────────────────────────────

    /** 
     * Rôle : Rafraîchit les festivals depuis le réseau et met à jour Room. 
     * 
     * Précondition : Appareil relié à Internet.
     * Postcondition : Base locale purgée des suppressions externes et complétée.
     */
    suspend fun refreshFestivals(): Result<List<FestivalSummary>>

    /** 
     * Rôle : Interroge via internet un festival précis pour le forcer en cache.
     * 
     * Précondition : ID valide côté distant.
     * Postcondition : Met à jour Room puis renvoie le résultat métier [FestivalSummary].
     */
    suspend fun getFestival(id: Int): Result<FestivalSummary>

    // ── Admin (réseau direct) ────────────────────────────────────────────────

    /** 
     * Rôle : Création explicite d'un festival, action typique d'administration.
     * 
     * Précondition : Perms Admin, DTO intègre.
     * Postcondition : API appelée puis Room synchronisé immédiatement.
     */
    suspend fun addFestival(festival: FestivalDto): Result<FestivalDto>

    /** 
     * Rôle : Pousse une demande de destruction du festival.
     * 
     * Précondition : ID exact.
     * Postcondition : Marqué en sync Pending hors-ligne ou exécuté et retiré.
     */
    suspend fun deleteFestival(id: Int): Result<Unit>
}
