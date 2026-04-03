package com.projetmobile.mobile.data.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RepositoryCallSupportTest {

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
        assertEquals("Payload invalide", repositoryException.message)
        assertEquals(listOf("title est requis", "min_age est requis"), repositoryException.details)
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
