package com.fashiontothem.ff.data.repository

import android.util.Log
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.data.remote.ApiService
import com.fashiontothem.ff.domain.model.StoreLocation
import com.fashiontothem.ff.domain.repository.LocationRepository
import com.fashiontothem.ff.domain.repository.StoreRepository
import com.fashiontothem.ff.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Location Repository Implementation
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val storeRepository: StoreRepository,
    private val storePreferences: StorePreferences,
    private val locationPreferences: LocationPreferences
) : LocationRepository {
    
    private val TAG = "FFTothem_LocationRepo"
    
    override suspend fun getStoreLocations(): Result<List<StoreLocation>> {
        return try {
            // Get selected country code
            val countryCode = storePreferences.selectedCountryCode.first()
            if (countryCode == null) {
                return Result.failure(Exception("No country selected"))
            }
            
            // Build URL dynamically based on selected country
            val url = "${Constants.FASHION_AND_FRIENDS_BASE_URL}${countryCode.lowercase()}/rest/V1/store-locator/locations/${countryCode.lowercase()}"
            
            Log.d(TAG, "Fetching store locations from: $url")
            
            val response = apiService.getStoreLocations(url)
            
            if (response.isSuccessful) {
                // Get base media URL from refreshed store config
                val storeConfigResult = storeRepository.refreshStoreConfigAndInitAthena()
                val baseMediaUrl = storeConfigResult.getOrNull()?.secureBaseMediaUrl 
                    ?: "https://fashion-assets.fashionandfriends.com/media/"  // Fallback
                
                val locations = response.body()?.map { it.toDomain(baseMediaUrl) } ?: emptyList()
                
                Log.d(TAG, "✅ Loaded ${locations.size} store locations (base media: $baseMediaUrl)")
                Result.success(locations)
            } else {
                Log.e(TAG, "❌ Failed to load locations: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to load store locations: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception loading locations: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override fun getSelectedStoreId(): Flow<String?> {
        return locationPreferences.selectedStoreId
    }
    
    override fun getSelectedStoreName(): Flow<String?> {
        return locationPreferences.selectedStoreName
    }
    
    override suspend fun saveSelectedLocation(storeId: String, storeName: String, storeCity: String) {
        locationPreferences.saveSelectedLocation(storeId, storeName, storeCity)
        Log.d(TAG, "✅ Saved selected location: $storeName ($storeCity)")
    }
    
    override suspend fun clearSelectedLocation() {
        locationPreferences.clearSelectedLocation()
        Log.d(TAG, "Cleared selected location")
    }
}

