package com.projetmobile.mobile.data.database

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject

class PersistentCookieJar(context: Context) : CookieJar {
    private val cookieStore = SecureCookieStore(context.applicationContext)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.saveCookies(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.loadCookies(url)
    }
}

private class SecureCookieStore(context: Context) {
    private val lock = Any()
    private val applicationContext = context.applicationContext
    private var sharedPreferences: SharedPreferences? = null

    fun saveCookies(cookies: List<Cookie>) {
        synchronized(lock) {
            val mergedCookies = readCookies()
                .associateBy(::cookieIdentifier)
                .toMutableMap()

            cookies.forEach { cookie ->
                if (cookie.persistent && cookie.expiresAt < System.currentTimeMillis()) {
                    mergedCookies.remove(cookieIdentifier(cookie))
                } else {
                    mergedCookies[cookieIdentifier(cookie)] = cookie
                }
            }

            writeCookies(mergedCookies.values.toList())
        }
    }

    fun loadCookies(url: HttpUrl): List<Cookie> {
        synchronized(lock) {
            val currentTime = System.currentTimeMillis()
            val allCookies = readCookies()
            val activeCookies = allCookies.filter { cookie ->
                !cookie.persistent || cookie.expiresAt >= currentTime
            }

            if (activeCookies.size != allCookies.size) {
                writeCookies(activeCookies)
            }

            return activeCookies.filter { cookie -> cookie.matches(url) }
        }
    }

    private fun readCookies(): List<Cookie> {
        return runCatching {
            val serializedCookies = getSharedPreferences().getString(COOKIE_STORE_KEY, null) ?: return emptyList()
            val jsonArray = JSONArray(serializedCookies)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val cookieJson = jsonArray.optJSONObject(index) ?: continue
                    deserializeCookie(cookieJson)?.let(::add)
                }
            }
        }.getOrElse { throwable ->
            Log.w(TAG, "Cookie store corrompu, réinitialisation locale.", throwable)
            clearCorruptedStore()
            emptyList()
        }
    }

    private fun writeCookies(cookies: List<Cookie>) {
        val jsonArray = JSONArray()
        cookies.forEach { cookie ->
            jsonArray.put(serializeCookie(cookie))
        }

        runCatching {
            getSharedPreferences().edit()
                .putString(COOKIE_STORE_KEY, jsonArray.toString())
                .apply()
        }.getOrElse { throwable ->
            Log.w(TAG, "Impossible d'écrire le cookie store, réinitialisation locale.", throwable)
            clearCorruptedStore()
        }
    }

    private fun serializeCookie(cookie: Cookie): JSONObject {
        return JSONObject().apply {
            put("name", cookie.name)
            put("value", cookie.value)
            put("expiresAt", cookie.expiresAt)
            put("domain", cookie.domain)
            put("path", cookie.path)
            put("secure", cookie.secure)
            put("httpOnly", cookie.httpOnly)
            put("hostOnly", cookie.hostOnly)
            put("persistent", cookie.persistent)
        }
    }

    private fun deserializeCookie(jsonObject: JSONObject): Cookie? {
        val domain = jsonObject.optString("domain")
        val name = jsonObject.optString("name")
        val value = jsonObject.optString("value")
        if (domain.isBlank() || name.isBlank()) {
            return null
        }

        return runCatching {
            Cookie.Builder().apply {
                name(name)
                value(value)

                if (jsonObject.optBoolean("hostOnly")) {
                    hostOnlyDomain(domain)
                } else {
                    domain(domain)
                }

                path(jsonObject.optString("path", "/"))

                if (jsonObject.optBoolean("persistent")) {
                    expiresAt(jsonObject.optLong("expiresAt", Long.MAX_VALUE))
                }

                if (jsonObject.optBoolean("secure")) {
                    secure()
                }

                if (jsonObject.optBoolean("httpOnly")) {
                    httpOnly()
                }
            }.build()
        }.getOrNull()
    }

    private fun cookieIdentifier(cookie: Cookie): String {
        return "${cookie.name}|${cookie.domain}|${cookie.path}"
    }

    private fun getSharedPreferences(): SharedPreferences {
        sharedPreferences?.let { return it }

        return runCatching {
            createEncryptedPreferences()
        }.recoverCatching {
            Log.w(TAG, "Préférences chiffrées invalides, suppression du store local.", it)
            clearCorruptedStore()
            createEncryptedPreferences()
        }.getOrThrow().also { preferences ->
            sharedPreferences = preferences
        }
    }

    private fun createEncryptedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFERENCES_NAME,
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private fun clearCorruptedStore() {
        sharedPreferences = null
        applicationContext.deleteSharedPreferences(PREFERENCES_NAME)
    }

    private companion object {
        private const val TAG = "SecureCookieStore"
        private const val PREFERENCES_NAME = "festival_mobile_cookies"
        private const val COOKIE_STORE_KEY = "cookies_json"
    }
}
