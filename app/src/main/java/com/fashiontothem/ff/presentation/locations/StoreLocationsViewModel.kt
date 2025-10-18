package com.fashiontothem.ff.presentation.locations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.domain.model.StoreLocation
import com.fashiontothem.ff.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F&F Tothem - Store Locations ViewModel
 */
@HiltViewModel
class StoreLocationsViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val TAG = "FFTothem_Locations"
    private val _uiState = MutableStateFlow(StoreLocationsUiState())
    val uiState: StateFlow<StoreLocationsUiState> = _uiState.asStateFlow()
    
    init {
        loadLocations()
    }

    fun loadLocations() {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            locationRepository.getStoreLocations().fold(
                onSuccess = { locations ->
                    // Filter only active stores with valid city
                    val activeLocations = locations.filter { 
                        it.city.isNotEmpty() && it.isActive
                    }
                    
                    val groupedByCity = activeLocations
                        .groupBy { it.city!! }
                        .toSortedMap() // Sort cities alphabetically
                    
                    val cities = groupedByCity.keys.toList()
                    val firstCity = cities.firstOrNull() ?: ""
                    
                    _uiState.update { 
                        it.copy(
                            allLocations = activeLocations,
                            locationsByCity = groupedByCity,
                            cities = cities,
                            selectedCity = firstCity, // Auto-select first city
                            isLoading = false
                        )
                    }
                    
                    Log.d(TAG, "✅ Loaded ${activeLocations.size} active locations (from ${locations.size} total) in ${cities.size} cities")
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load store locations"
                        )
                    }
                    Log.e(TAG, "❌ Failed to load locations: ${exception.message}", exception)
                }
            )
        }
    }
    
    fun selectCity(city: String) {
        _uiState.update { it.copy(selectedCity = city) }
        Log.d(TAG, "📍 Selected city: $city")
    }

    fun selectStore(store: StoreLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                locationRepository.saveSelectedLocation(
                    storeId = store.id,
                    storeName = store.name,
                    storeCity = store.city
                )
                
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        locationSelected = true
                    )
                }
                
                Log.d(TAG, "✅ Store location saved: ${store.name}")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save location: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class StoreLocationsUiState(
    val allLocations: List<StoreLocation> = emptyList(),
    val locationsByCity: Map<String, List<StoreLocation>> = emptyMap(),
    val cities: List<String> = emptyList(),
    val selectedCity: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val locationSelected: Boolean = false
)

