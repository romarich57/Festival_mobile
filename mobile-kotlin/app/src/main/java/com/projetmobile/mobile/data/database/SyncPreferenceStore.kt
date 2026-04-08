/**
 * Rôle du fichier :
 * Service de persistance de préférence via le composant DataStore d'Android (modèle clé-valeur).
 * Ce fichier gère exclusivement l'enregistrement des horodatages (timestamps) de dernière synchronisation.
 * Son but est d'optimiser l'utilisation réseau en évitant de retélécharger les mêmes données si elles
 * sont considérées comme "fraîches" (en fonction d'un paramètre Time-To-Live).
 */
package com.projetmobile.mobile.data.database

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension déléguée sur Context : cela crée implicitement une instance unique de DataStore 
// stockée physiquement dans un fichier nommé "sync_preferences".
private val Context.syncPreferenceDataStore by preferencesDataStore(name = "sync_preferences")

/**
 * Rôle : DataStore dédié aux timestamps de dernière synchronisation par entité du domaine métier.
 * Suit le même pattern que [AuthPreferenceStore].
 *
 * Précondition : Le paramètre `context` d'application doit être présent et permettre l'accès au DataStore.
 * Postcondition : Les objets créés fournissent une interface asynchrone pour la persistance légère des dates de synchro.
 */
open class SyncPreferenceStore(private val context: Context) {

    /**
     * Objet compagnon (statique) comprenant les constantes globales pour éviter tout Hardcoding
     * (l'écriture en dur de chaînes de caractères) lorsqu'on a recours à cette classe.
     */
    companion object {
        // Définition des clés d'identifiants de domaines pour retrouver la date du bon type de téléchargement.
        const val KEY_GAMES        = "games"
        const val KEY_RESERVANTS   = "reservants"
        const val KEY_FESTIVALS    = "festivals"
        const val KEY_RESERVATIONS = "reservations"

        /** Durée de validité (Time-to-Live / TTL) du cache avant de forcer un nouvel appel réseau (ici : 5 minutes). */
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L
    }

    /** 
     * Rôle : Récupérer le "timestamp" (l'heure exacte d'une milliseconde) correspondant à la dernière mise à jour 
     * réseau réussie d'une fonctionnalité spécifique.
     * 
     * Précondition : Un `key` d'identifiant valide doit être fourni.
     * Postcondition : Retourne la date de la dernière synchronisation, ou null si elle n'a jamais eu lieu.
     */
    open suspend fun getLastSyncedAt(key: String): Long? {
        // Extraction depuis le DataStore de la valeur Long correspondante.
        return context.syncPreferenceDataStore.data
            .map { prefs -> prefs[longPreferencesKey(key)] }
            .first()
    }

    /** 
     * Rôle : Met à jour et sauvegarde la date courante (timestamp en millisecondes) liée au rafraîchissement
     * récent d'une donnée d'un domaine métier précis.
     * 
     * Précondition : Un `key` valide et un `timestamp` (généré ou explicite).
     * Postcondition : Écrit ou écrase le timestamp correspondant de la clé pour marquer la date de validité.
     */
    open suspend fun setLastSyncedAt(
        key: String,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        // Ouvre en transaction le fichier et écrit ou écrase la précédente valeur.
        context.syncPreferenceDataStore.edit { prefs ->
            prefs[longPreferencesKey(key)] = timestamp
        }
    }

    /**
     * Rôle : Indique, en comparant les timestamps, s'il est temps de rappeler l'API distance pour
     * rafraîchir les données en local, protégeant ainsi l'application contre les appels inutiles en rafale.
     *
     * Précondition : Un `key` pertinent et la durée du Time-To-Live (`ttlMs`) voulue.
     * Postcondition : Renvoie vrai si un appel réseau est nécessaire, faux sinon.
     */
    open suspend fun needsRefresh(key: String, ttlMs: Long = DEFAULT_TTL_MS): Boolean {
        // On récupère le timestamp. S'il est inexistant, on en déduit que l'app n'a pas été synchronisée.
        val lastSync = getLastSyncedAt(key) ?: return true
        
        // Comparaison : On calcule le delta entre le présent et l'horodatage stocké, et on le compare au TTL imposé.
        return (System.currentTimeMillis() - lastSync) > ttlMs
    }

    /** 
     * Rôle : Force délibérément l'invalidité des données du cache sur le domaine concerné,
     * contraignant ainsi le prochain appel réseau à ignorer le cache.
     * 
     * Précondition : Fournir l'identifiant pour lequel le cache doit être effacé.
     * Postcondition : Supprime l'entrée mémoire correspondante dans le fichier DataStore.
     */
    open suspend fun invalidate(key: String) {
        // Enlève l'entrée correspondante en forçant le block "edit()".
        context.syncPreferenceDataStore.edit { prefs ->
            prefs.remove(longPreferencesKey(key))
        }
    }
}
