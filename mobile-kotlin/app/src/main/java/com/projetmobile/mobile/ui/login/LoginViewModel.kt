package com.projetmobile.mobile.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.network.models.LoginRequest
import com.projetmobile.mobile.ui.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel(private val repository: ApiRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Veuillez remplir tous les champs")
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = repository.login(LoginRequest(identifier, password))

            result.fold(
                onSuccess = { response ->
                    _uiState.value = LoginUiState.Success(response.message)
                },
                onFailure = { exception ->
                    val errorMessage = extractErrorMessage(exception)
                    _uiState.value = LoginUiState.Error(errorMessage)
                }
            )
        }
    }

    private fun extractErrorMessage(exception: Throwable): String {
        return try {
            if (exception is retrofit2.HttpException) {
                val errorBody = exception.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    val jsonObject = JSONObject(errorBody)
                    if (jsonObject.has("error")) {
                        return jsonObject.getString("error")
                    }
                }
            }
            exception.localizedMessage ?: "Erreur de connexion"
        } catch (e: Exception) {
            "Erreur inattendue"
        }
    }
}
