package com.projetmobile.mobile.ui.screens.festival

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FestivalViewModel(
    private val festivalRepository: FestivalRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FestivalUiState())
    val uiState: StateFlow<FestivalUiState> = _uiState.asStateFlow()

    init {
        loadFestivals()
    }

    fun loadFestivals() {
        viewModelScope.launch {
            _uiState.value = FestivalUiState(isLoading = true)
            festivalRepository.getFestivals()
                .onSuccess { festivals ->
                    _uiState.value = FestivalUiState(
                        isLoading = false,
                        festivals = festivals,
                        errorMessage = null,
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = FestivalUiState(
                        isLoading = false,
                        festivals = emptyList(),
                        errorMessage = throwable.localizedMessage ?: "Impossible de charger les festivals.",
                    )
                }
        }
    }

    companion object {
        fun factory(festivalRepository: FestivalRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    FestivalViewModel(festivalRepository)
                }
            }
        }
    }
}
