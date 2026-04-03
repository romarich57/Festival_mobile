package com.projetmobile.mobile.data.remote.auth

import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrl

class AuthRefreshInterceptor(
    baseUrl: String,
    cookieJar: CookieJar,
) : Interceptor {
    private val refreshUrl = checkNotNull(baseUrl.toHttpUrl().resolve("auth/refresh")) {
        "Impossible de résoudre l'endpoint auth/refresh depuis $baseUrl"
    }

    private val refreshClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!shouldAttemptRefresh(request, response)) {
            return response
        }

        val refreshSucceeded = synchronized(refreshLock) {
            executeRefresh()
        }

        if (!refreshSucceeded) {
            return response
        }

        response.close()
        return chain.proceed(
            request.newBuilder()
                .header(RETRY_HEADER, RETRY_HEADER_VALUE)
                .build(),
        )
    }

    private fun executeRefresh(): Boolean {
        val refreshRequest = Request.Builder()
            .url(refreshUrl)
            .post(ByteArray(0).toRequestBody(null))
            .build()

        return runCatching {
            refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                refreshResponse.isSuccessful
            }
        }.getOrDefault(false)
    }

    private fun shouldAttemptRefresh(request: Request, response: Response): Boolean {
        if (response.code != 401 && response.code != 403) {
            return false
        }
        if (request.header(RETRY_HEADER) == RETRY_HEADER_VALUE) {
            return false
        }

        val path = request.url.encodedPath
        if (path.endsWith("/auth/refresh")) {
            return false
        }
        if (path.contains("/auth/") && !path.endsWith("/auth/whoami")) {
            return false
        }

        return true
    }

    private companion object {
        private const val RETRY_HEADER = "X-Auth-Refresh-Retry"
        private const val RETRY_HEADER_VALUE = "1"
    }
}
