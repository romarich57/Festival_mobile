package com.projetmobile.mobile.data.remote

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
