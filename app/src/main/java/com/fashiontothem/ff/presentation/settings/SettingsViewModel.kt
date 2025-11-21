package com.fashiontothem.ff.presentation.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.fashiontothem.ff.data.cache.BrandImageCache
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.local.preferences.CategoryPreferences
import com.fashiontothem.ff.data.local.preferences.EnvironmentPreferences
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.data.remote.DynamicAthenaApiService
import com.fashiontothem.ff.data.repository.StoreRepositoryImpl
import com.fashiontothem.ff.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F&F Tothem - Settings ViewModel
 * 
 * ViewModel for managing settings including environment selection.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val environmentPreferences: EnvironmentPreferences,
    private val storePreferences: StorePreferences,
    private val locationPreferences: LocationPreferences,
    private val athenaPreferences: AthenaPreferences,
    private val categoryPreferences: CategoryPreferences,
    private val brandImageCache: BrandImageCache,
    private val dynamicAthenaApiService: DynamicAthenaApiService,
    private val storeRepositoryImpl: StoreRepositoryImpl,
    private val storeRepository: StoreRepository
) : ViewModel() {
    
    private val TAG = "FFTothem_Settings"
    
    private val _selectedEnvironment = MutableStateFlow<String>(EnvironmentPreferences.ENVIRONMENT_PRODUCTION)
    val selectedEnvironment: StateFlow<String> = _selectedEnvironment.asStateFlow()
    
    init {
        // Load current environment
        viewModelScope.launch {
            environmentPreferences.environment.collect { environment ->
                _selectedEnvironment.value = environment
            }
        }
    }
    
    /**
     * Change environment and refresh store config with new base URL.
     */
    fun changeEnvironment(newEnvironment: String, context: Context) {
        viewModelScope.launch {
            try {
                val currentEnvironment = _selectedEnvironment.value
                
                Log.d(TAG, "🔄 Switching environment from $currentEnvironment to $newEnvironment...")
                
                // Save new environment
                environmentPreferences.saveEnvironment(newEnvironment)
                Log.d(TAG, "✅ Environment saved: $newEnvironment")
                
                // Clear store config cache so it will be fetched with new base URL
                storeRepositoryImpl.clearCache()
                Log.d(TAG, "✅ Store config cache cleared")
                
                // Clear DynamicAthenaApiService cache so it will use new base URL
                dynamicAthenaApiService.clearCache()
                Log.d(TAG, "✅ DynamicAthenaApiService cache cleared")
                
                // If store is selected, refresh store config with new base URL
                val storeCode = storePreferences.selectedStoreCode.first()
                val countryCode = storePreferences.selectedCountryCode.first()
                
                if (!storeCode.isNullOrEmpty() && !countryCode.isNullOrEmpty()) {
                    Log.d(TAG, "🔄 Refreshing store config for $countryCode/$storeCode with new base URL...")
                    val refreshResult = storeRepository.refreshStoreConfigAndInitAthena()
                    if (refreshResult.isSuccess) {
                        Log.d(TAG, "✅ Store config refreshed successfully with new base URL")
                    } else {
                        Log.e(TAG, "❌ Failed to refresh store config: ${refreshResult.exceptionOrNull()?.message}")
                    }
                } else {
                    Log.d(TAG, "ℹ️ No store selected - skipping store config refresh")
                }
                
                Log.d(TAG, "✅ Environment changed to: $newEnvironment")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error changing environment: ${e.message}", e)
            }
        }
    }
    
}

