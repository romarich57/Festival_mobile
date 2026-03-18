package com.projetmobile.mobile.data.database

import android.content.Context
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

    private val sharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "festival_mobile_cookies",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

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
        val serializedCookies = sharedPreferences.getString(COOKIE_STORE_KEY, null) ?: return emptyList()
        val jsonArray = JSONArray(serializedCookies)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val cookieJson = jsonArray.optJSONObject(index) ?: continue
                deserializeCookie(cookieJson)?.let(::add)
            }
        }
    }

    private fun writeCookies(cookies: List<Cookie>) {
        val jsonArray = JSONArray()
        cookies.forEach { cookie ->
            jsonArray.put(serializeCookie(cookie))
        }

        sharedPreferences.edit()
            .putString(COOKIE_STORE_KEY, jsonArray.toString())
            .apply()
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

    private companion object {
        private const val COOKIE_STORE_KEY = "cookies_json"
    }
}
