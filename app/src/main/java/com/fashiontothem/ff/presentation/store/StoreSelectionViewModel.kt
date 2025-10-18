package com.fashiontothem.ff.presentation.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.domain.model.CountryStore
import com.fashiontothem.ff.domain.usecase.GetStoreConfigsUseCase
import com.fashiontothem.ff.domain.usecase.SaveSelectedStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F&F Tothem - Store Selection ViewModel
 */
@HiltViewModel
class StoreSelectionViewModel @Inject constructor(
    private val getStoreConfigsUseCase: GetStoreConfigsUseCase,
    private val saveSelectedStoreUseCase: SaveSelectedStoreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreSelectionUiState())
    val uiState: StateFlow<StoreSelectionUiState> = _uiState.asStateFlow()

    init {
        loadStores()
    }

    fun loadStores() {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getStoreConfigsUseCase().fold(
                onSuccess = { stores ->
                    _uiState.update {
                        it.copy(
                            stores = stores,
                            isLoading = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load stores"
                        )
                    }
                }
            )
        }
    }

    fun selectStore(countryCode: String, storeCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                saveSelectedStoreUseCase(storeCode, countryCode)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        storeSelected = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save store: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class StoreSelectionUiState(
    val stores: List<CountryStore> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val storeSelected: Boolean = false
)

