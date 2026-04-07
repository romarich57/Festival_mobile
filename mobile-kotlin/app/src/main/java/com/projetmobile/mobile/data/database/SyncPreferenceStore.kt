package com.projetmobile.mobile.data.database

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.syncPreferenceDataStore by preferencesDataStore(name = "sync_preferences")

/**
 * DataStore pour les timestamps de dernière synchronisation par entité.
 *
 * Suit le même pattern que [AuthPreferenceStore] (déjà présent dans le projet).
 *
 * Clés recommandées : "games", "reservants", "festivals", "reservations".
 * TTL par défaut : 5 minutes.
 */
open class SyncPreferenceStore(private val context: Context) {

    companion object {
        const val KEY_GAMES        = "games"
        const val KEY_RESERVANTS   = "reservants"
        const val KEY_FESTIVALS    = "festivals"
        const val KEY_RESERVATIONS = "reservations"

        /** Durée de validité du cache avant nouvel appel réseau (5 minutes). */
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L
    }

    /** Retourne le timestamp de la dernière sync, ou null si jamais synchronisé. */
    open suspend fun getLastSyncedAt(key: String): Long? {
        return context.syncPreferenceDataStore.data
            .map { prefs -> prefs[longPreferencesKey(key)] }
            .first()
    }

    /** Enregistre le timestamp de la dernière sync réussie. */
    open suspend fun setLastSyncedAt(
        key: String,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        context.syncPreferenceDataStore.edit { prefs ->
            prefs[longPreferencesKey(key)] = timestamp
        }
    }

    /**
     * Retourne true si le cache doit être rafraîchi (jamais sync, ou TTL dépassé).
     *
     * @param ttlMs Durée de validité en millisecondes (défaut : [DEFAULT_TTL_MS]).
     */
    open suspend fun needsRefresh(key: String, ttlMs: Long = DEFAULT_TTL_MS): Boolean {
        val lastSync = getLastSyncedAt(key) ?: return true
        return (System.currentTimeMillis() - lastSync) > ttlMs
    }

    /** Force le rafraîchissement en supprimant le timestamp (prochain appel ira au réseau). */
    open suspend fun invalidate(key: String) {
        context.syncPreferenceDataStore.edit { prefs ->
            prefs.remove(longPreferencesKey(key))
        }
    }
}
