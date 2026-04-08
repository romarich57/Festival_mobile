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

private val Context.authPreferenceDataStore by preferencesDataStore(name = "auth_preferences")

open class AuthPreferenceStore(private val context: Context) {
    private object Keys {
        val pendingVerificationEmail: Preferences.Key<String> =
            stringPreferencesKey("pending_verification_email")
        val lastLoginIdentifier: Preferences.Key<String> =
            stringPreferencesKey("last_login_identifier")
        val cachedUser: Preferences.Key<String> =
            stringPreferencesKey("cached_user")
    }

    open suspend fun getPendingVerificationEmail(): String? {
        return context.authPreferenceDataStore.data
            .map { preferences -> preferences[Keys.pendingVerificationEmail] }
            .first()
    }

    open suspend fun setPendingVerificationEmail(email: String) {
        context.authPreferenceDataStore.edit { preferences ->
            preferences[Keys.pendingVerificationEmail] = email
        }
    }

    open suspend fun clearPendingVerificationEmail() {
        context.authPreferenceDataStore.edit { preferences ->
            preferences.remove(Keys.pendingVerificationEmail)
        }
    }

    open suspend fun getLastLoginIdentifier(): String? {
        return context.authPreferenceDataStore.data
            .map { preferences -> preferences[Keys.lastLoginIdentifier] }
            .first()
    }

    open suspend fun setLastLoginIdentifier(identifier: String) {
        context.authPreferenceDataStore.edit { preferences ->
            preferences[Keys.lastLoginIdentifier] = identifier
        }
    }

    open suspend fun getCachedUser(): AuthUser? {
        return context.authPreferenceDataStore.data
            .map { preferences -> preferences[Keys.cachedUser] }
            .first()
            ?.let { serializedUser ->
                runCatching {
                    ApiJson.instance.decodeFromString(AuthUser.serializer(), serializedUser)
                }.getOrNull()
            }
    }

    open suspend fun setCachedUser(user: AuthUser) {
        context.authPreferenceDataStore.edit { preferences ->
            preferences[Keys.cachedUser] = ApiJson.instance.encodeToString(
                AuthUser.serializer(),
                user,
            )
        }
    }

    open suspend fun clearCachedUser() {
        context.authPreferenceDataStore.edit { preferences ->
            preferences.remove(Keys.cachedUser)
        }
    }
}
