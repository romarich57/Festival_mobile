package com.projetmobile.mobile.ui

import com.projetmobile.mobile.network.ApiService
import com.projetmobile.mobile.network.HealthResponse
import com.projetmobile.mobile.network.models.LoginRequest
import com.projetmobile.mobile.network.models.LoginResponse

class ApiRepository(
    private val apiService: ApiService,
) {
    suspend fun checkHealth(): Result<HealthResponse> = runCatching {
        apiService.getHealth()
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> = runCatching {
        apiService.login(request)
    }
}
