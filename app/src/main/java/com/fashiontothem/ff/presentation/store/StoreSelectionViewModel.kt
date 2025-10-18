package com.fashiontothem.ff.presentation.store

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.manager.AthenaTokenManager
import com.fashiontothem.ff.data.remote.AthenaApiService
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
    private val saveSelectedStoreUseCase: SaveSelectedStoreUseCase,
    private val athenaPreferences: AthenaPreferences,
    private val athenaTokenManager: AthenaTokenManager,
    private val athenaApiService: AthenaApiService
) : ViewModel() {

    private val TAG = "FFTothem_StoreSelection"
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
                // Save selected store
                saveSelectedStoreUseCase(storeCode, countryCode)
                
                // Find selected store in loaded stores
                val selectedStore = _uiState.value.stores
                    .find { it.countryCode == countryCode }
                    ?.stores?.find { it.code == storeCode }
                
                if (selectedStore != null) {
                    // Save Athena config from selected store
                    athenaPreferences.saveAthenaConfig(
                        websiteUrl = selectedStore.athenaSearchWebsiteUrl,
                        wtoken = selectedStore.athenaSearchWtoken
                    )
                    
                    Log.d(TAG, "✅ Athena config saved: ${selectedStore.athenaSearchWebsiteUrl}")
                    
                    // Get Athena access token immediately
                    val token = athenaTokenManager.getValidToken(athenaApiService)
                    if (token != null) {
                        Log.d(TAG, "✅ Athena token obtained")
                    } else {
                        Log.w(TAG, "⚠️ Failed to get Athena token")
                    }
                }
                
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


