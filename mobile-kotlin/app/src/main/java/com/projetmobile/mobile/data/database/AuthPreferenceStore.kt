package com.projetmobile.mobile.data.database

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authPreferenceDataStore by preferencesDataStore(name = "auth_preferences")

class AuthPreferenceStore(private val context: Context) {
    private object Keys {
        val pendingVerificationEmail: Preferences.Key<String> =
            stringPreferencesKey("pending_verification_email")
        val lastLoginIdentifier: Preferences.Key<String> =
            stringPreferencesKey("last_login_identifier")
    }

    suspend fun getPendingVerificationEmail(): String? {
        return context.authPreferenceDataStore.data
            .map { preferences -> preferences[Keys.pendingVerificationEmail] }
            .first()
    }

    suspend fun setPendingVerificationEmail(email: String) {
        context.authPreferenceDataStore.edit { preferences ->
            preferences[Keys.pendingVerificationEmail] = email
        }
    }

    suspend fun clearPendingVerificationEmail() {
        context.authPreferenceDataStore.edit { preferences ->
            preferences.remove(Keys.pendingVerificationEmail)
        }
    }

    suspend fun getLastLoginIdentifier(): String? {
        return context.authPreferenceDataStore.data
            .map { preferences -> preferences[Keys.lastLoginIdentifier] }
            .first()
    }

    suspend fun setLastLoginIdentifier(identifier: String) {
        context.authPreferenceDataStore.edit { preferences ->
            preferences[Keys.lastLoginIdentifier] = identifier
        }
    }
}
