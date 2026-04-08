package com.projetmobile.mobile.data.repository

import java.net.ConnectException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import org.junit.After
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RepositoryCallSupportTest {

    @After
    fun tearDown() {
        RepositoryNetworkStatus.resetForTests()
    }

    @Test
    fun toRepositoryException_preservesStatusAndDetailsFromBackend() {
        val exception = httpException(
            statusCode = 400,
            body = """
                {
                  "error": "Payload invalide",
                  "details": ["title est requis", "min_age est requis"]
                }
            """.trimIndent(),
        )

        val repositoryException = exception.toRepositoryException("Message par défaut")

        assertTrue(repositoryException is RepositoryException)
        repositoryException as RepositoryException
        assertEquals(400, repositoryException.statusCode)
        assertEquals(RepositoryFailureKind.Validation, repositoryException.kind)
        assertEquals("Payload invalide", repositoryException.message)
        assertEquals(listOf("title est requis", "min_age est requis"), repositoryException.details)
    }

    @Test
    fun toRepositoryException_mapsUnknownHostToOfflineFailure() {
        RepositoryNetworkStatus.initialize(NetworkStatusProvider { false })
        val repositoryException = UnknownHostException("mobile.romdev.cloud")
            .toRepositoryException("Message par défaut") as RepositoryException

        assertEquals(RepositoryFailureKind.Offline, repositoryException.kind)
        assertEquals(
            "Aucune connexion internet. Réessayez lorsque vous serez de nouveau en ligne.",
            repositoryException.message,
        )
    }

    @Test
    fun toRepositoryException_mapsConnectFailureToBackendUnreachableWhenNetworkIsValidated() {
        RepositoryNetworkStatus.initialize(NetworkStatusProvider { true })

        val repositoryException = ConnectException("Connection refused")
            .toRepositoryException("Message par défaut") as RepositoryException

        assertEquals(RepositoryFailureKind.BackendUnreachable, repositoryException.kind)
        assertEquals(
            "Serveur inaccessible pour le moment. Réessayez plus tard.",
            repositoryException.message,
        )
    }

    @Test
    fun toRepositoryException_mapsSslFailureToBackendUnreachableWhenNetworkIsValidated() {
        RepositoryNetworkStatus.initialize(NetworkStatusProvider { true })

        val repositoryException = SSLException("Handshake failed")
            .toRepositoryException("Message par défaut") as RepositoryException

        assertEquals(RepositoryFailureKind.BackendUnreachable, repositoryException.kind)
        assertEquals(
            "Serveur inaccessible pour le moment. Réessayez plus tard.",
            repositoryException.message,
        )
    }

    private fun httpException(
        statusCode: Int,
        body: String,
    ): HttpException {
        return HttpException(
            Response.error<Any>(
                statusCode,
                body.toResponseBody("application/json".toMediaType()),
            ),
        )
    }
}
