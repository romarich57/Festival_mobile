/**
 * Rôle du fichier :
 * Service de persistance légère local utilisant DataStore (jetPack) pour stocker les petites
 * données liées à l'authentification et au cache de l'utilisateur.
 * Ce fichier permet de persister l'email de connexion en attente, le dernier identifiant utilisé 
 * et les données (sérialisées) de l'utilisateur actif afin qu'il n'ait pas à se reconnecter
 * ou recharger son profil au prochain démarrage.
 */
package com.projetmobile.mobile.data.database

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.remote.common.ApiJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Création d'une extension sur Context pour générer un DataStore nommé "auth_preferences".
// Il fonctionnera comme un Singleton encapsulant le fichier SharedPreferences local.
private val Context.authPreferenceDataStore by preferencesDataStore(name = "auth_preferences")

/**
 * Rôle : Gérer le stockage et la récupération des informations de session de façon réactive.
 *
 * Précondition : Le contexte Android (Context) fourni doit permettre l'accès au DataStore.
 * Postcondition : Les méthodes de l'objet opèrent sur le fichier "auth_preferences" persistant.
 */
open class AuthPreferenceStore(private val context: Context) {
    
    /**
     * Objet interne regroupant les clés typées nécessaires pour accéder 
     * aux valeurs stockées dans le DataStore. 
     */
    private object Keys {
        // Clé pour mémoriser temporairement un email lors d'une validation/réinitialisation de compte.
        val pendingVerificationEmail: Preferences.Key<String> =
            stringPreferencesKey("pending_verification_email")
        
        // Clé pour sauvegarder le dernier identifiant entré (pour pré-remplir le champ de login).
        val lastLoginIdentifier: Preferences.Key<String> =
            stringPreferencesKey("last_login_identifier")
            
        // Clé pour stocker le JSON complet de l'utilisateur (AuthUser) actuellement connecté.
        val cachedUser: Preferences.Key<String> =
            stringPreferencesKey("cached_user")
    }

    /**
     * Rôle : Récupère l'email mis en attente de vérification.
     * 
     * Précondition : Le DataStore d'authentification a été préalablement initialisé.
     * Postcondition : Retourne l'adresse email en attente d'activation, ou null sinon.
     */
    open suspend fun getPendingVerificationEmail(): String? {
        // Accède au DataStore, extrait la valeur liée à la clé correspondante, et renvoie le premier élément émis.
        return context.authPreferenceDataStore.data
            .map { preferences -> preferences[Keys.pendingVerificationEmail] }
            .first()
    }

    /**
     * Rôle : Sauvegarde un email dans les préférences pour une vérification ultérieure.
     * 
     * Précondition : Une adresse email valide en format texte.
     * Postcondition : Insère l'adresse email dans la clé correspondante du DataStore de façon persistante.
     */
    open suspend fun setPendingVerificationEmail(email: String) {
        // Ouvre en transaction le DataStore et y assigne l'email.
        context.authPreferenceDataStore.edit { preferences ->
            preferences[Keys.pendingVerificationEmail] = email
        }
    }

    /**
     * Rôle : Supprime l'email en attente des préférences une fois que le flux de vérification est terminé.
     *
     * Précondition : -
     * Postcondition : La clé "pending_verification_email" n'existe plus dans le DataStore.
     */
    open suspend fun clearPendingVerificationEmail() {
        // Ouvre en transaction le DataStore et retire la clé associée.
        context.authPreferenceDataStore.edit { preferences ->
            preferences.remove(Keys.pendingVerificationEmail)
        }
    }

    /**
     * Rôle : Récupère le dernier identifiant utilisé pour se connecter au service.
     * 
     * Précondition : Le DataStore d'authentification a été préalablement initialisé.
     * Postcondition : Retourne la valeur de l'identifiant, ou null si inexistante.
     */
    open suspend fun getLastLoginIdentifier(): String? {
        return context.authPreferenceDataStore.data
            .map { preferences -> preferences[Keys.lastLoginIdentifier] }
            .first()
    }

    /**
     * Rôle : Mémorise l'identifiant renseigné par l'utilisateur lors du login
     * (pour l'aide à la reconnexion automatique/champ pré-rempli).
     * 
     * Précondition : L'utilisateur vient d'introduire un identifiant sur l'écran de login.
     * Postcondition : Sauvegarde cet identifiant dans la clé dédiée du DataStore.
     */
    open suspend fun setLastLoginIdentifier(identifier: String) {
        context.authPreferenceDataStore.edit { preferences ->
            preferences[Keys.lastLoginIdentifier] = identifier
        }
    }

    /**
     * Rôle : Lit la chaîne JSON représentant l'utilisateur connecté stockée localement
     * et essaie de la décoder dans un objet métier AuthUser.
     * 
     * Précondition : Le cache utilisateur doit potentiellement contenir une chaîne JSON valide.
     * Postcondition : Retourne l'utilisateur ([AuthUser]) désérialisé ou null si échec/absent.
     */
    open suspend fun getCachedUser(): AuthUser? {
        return context.authPreferenceDataStore.data
            // On extrait d'abord le JSON sous forme de String.
            .map { preferences -> preferences[Keys.cachedUser] }
            .first()
            // S'il est présent (non null), on le désérialise dynamiquement.
            ?.let { serializedUser ->
                // Utilisation d'un runCatching pour éviter les Crash (IllegalArgumentException) 
                // au cas où le format JSON de base de données serait corrompu/évolué.
                runCatching {
                    ApiJson.instance.decodeFromString(AuthUser.serializer(), serializedUser)
                }.getOrNull()
            }
    }

    /**
     * Rôle : Convertit et enregistre l'utilisateur actif dans les préférences locales pour le mode hors-ligne.
     * 
     * Précondition : Un utilisateur valablement authentifié [AuthUser].
     * Postcondition : Enregistre l'ensemble des données de cet utilisateur sous forme sérialisée (JSON) en local.
     */
    open suspend fun setCachedUser(user: AuthUser) {
        context.authPreferenceDataStore.edit { preferences ->
            // Encodage de l'objet en chaine de caractère JSON via la librairie standard Kotlinx Serialization.
            preferences[Keys.cachedUser] = ApiJson.instance.encodeToString(
                AuthUser.serializer(),
                user,
            )
        }
    }

    /**
     * Rôle : Efface complètement les données profil de l'utilisateur actif local (Utile en cas de déconnexion).
     * 
     * Précondition : L'utilisateur demande à se déconnecter de la session courante.
     * Postcondition : Le profil temporaire stocké localement est retiré du système de persistance (DataStore).
     */
    open suspend fun clearCachedUser() {
        context.authPreferenceDataStore.edit { preferences ->
            // Suppression de la clé associée au cache utilisateur.
            preferences.remove(Keys.cachedUser)
        }
    }
}
