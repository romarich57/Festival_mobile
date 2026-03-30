package com.projetmobile.mobile.data.repository.festival

import com.projetmobile.mobile.data.database.festival.FestivalDao
import com.projetmobile.mobile.data.database.festival.toEntity
import com.projetmobile.mobile.data.database.festival.toSummary
import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.mapper.festival.toFestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implémentation offline-first du FestivalRepository.
 *
 * Stratégie "Room as single source of truth" :
 *  1. [observeFestivals] expose un Flow depuis Room → l'UI réagit automatiquement.
 *  2. [getFestivals] tente l'API → si succès, écrit dans Room → Flow notifie l'UI.
 *  3. Si pas de réseau → retourne le cache Room → pas d'écran blanc.
 */
class FestivalRepositoryImpl(
    private val festivalApiService: FestivalApiService,
    private val festivalDao: FestivalDao,
) : FestivalRepository {

    /**
     * Flow continu depuis Room — collecté par FestivalViewModel.
     * Se met à jour automatiquement quand Room change (après un refresh API).
     */
    fun observeFestivals(): Flow<List<FestivalSummary>> =
        festivalDao.observeAll().map { entities -> entities.map { it.toSummary() } }

    /**
     * Tente un refresh API et met à jour Room.
     * En cas d'échec réseau, retourne le cache local sans erreur visible
     * (sauf si le cache est vide → l'utilisateur voit un message d'erreur).
     */
    override suspend fun getFestivals(): Result<List<FestivalSummary>> {
        return try {
            // ── Réseau disponible : refresh et mise en cache ──────────────────
            val fresh = festivalApiService.getFestivals()
            val summaries = fresh.map { it.toFestivalSummary() }
            festivalDao.replaceAll(summaries.map { it.toEntity() })
            Result.success(summaries)
        } catch (networkError: Exception) {
            // ── Pas de réseau : retourne le cache Room ────────────────────────
            val cached = festivalDao.observeAll().first().map { it.toSummary() }
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(networkError)
            }
        }
    }
}