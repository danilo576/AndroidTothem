package com.fashiontothem.ff.data.repository

import android.util.Log
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.data.manager.AthenaTokenManager
import com.fashiontothem.ff.data.remote.AthenaApiService
import com.fashiontothem.ff.data.remote.ApiService
import com.fashiontothem.ff.domain.model.CountryStore
import com.fashiontothem.ff.domain.model.StoreConfig
import com.fashiontothem.ff.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Store Repository Implementation
 */
@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val athenaApiService: AthenaApiService,
    private val storePreferences: StorePreferences,
    private val athenaPreferences: AthenaPreferences,
    private val athenaTokenManager: AthenaTokenManager
) : StoreRepository {
    
    private val TAG = "FFTothem_StoreRepo"
    
    override suspend fun getStoreConfigs(): Result<List<CountryStore>> {
        return try {
            val response = apiService.getStoreConfigs()
            if (response.isSuccessful) {
                val stores = response.body()?.map { it.toDomain() } ?: emptyList()
                Result.success(stores)
            } else {
                Result.failure(Exception("Failed to load stores: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getSelectedStoreCode(): Flow<String?> {
        return storePreferences.selectedStoreCode
    }
    
    override fun getSelectedCountryCode(): Flow<String?> {
        return storePreferences.selectedCountryCode
    }
    
    override suspend fun saveSelectedStore(storeCode: String, countryCode: String) {
        storePreferences.saveSelectedStore(storeCode, countryCode)
    }
    
    override suspend fun clearSelectedStore() {
        storePreferences.clearSelectedStore()
        athenaTokenManager.clearToken()  // Clear Athena token too
    }
    
    /**
     * Refresh store config and initialize Athena token.
     * Call this on app start if store is already selected.
     */
    override suspend fun refreshStoreConfigAndInitAthena(): Result<StoreConfig> {
        return try {
            val storeCode = storePreferences.selectedStoreCode.first()
            val countryCode = storePreferences.selectedCountryCode.first()
            
            if (storeCode == null || countryCode == null) {
                return Result.failure(Exception("No store selected"))
            }
            
            Log.d(TAG, "Refreshing store config for $countryCode / $storeCode")
            
            // Get fresh store configs from API
            val response = apiService.getStoreConfigs()
            if (!response.isSuccessful) {
                return Result.failure(Exception("Failed to refresh store config: ${response.code()}"))
            }
            
            // Find selected store in response
            val allStores = response.body()?.map { it.toDomain() } ?: emptyList()
            val selectedStore = allStores
                .find { it.countryCode == countryCode }
                ?.stores?.find { it.code == storeCode }
            
            if (selectedStore == null) {
                return Result.failure(Exception("Selected store not found in API response"))
            }
            
            // Save Athena config (website URL and wtoken)
            athenaPreferences.saveAthenaConfig(
                websiteUrl = selectedStore.athenaSearchWebsiteUrl,
                wtoken = selectedStore.athenaSearchWtoken
            )
            
            Log.d(TAG, "✅ Store config refreshed. Athena URL: ${selectedStore.athenaSearchWebsiteUrl}")
            
            // Get or refresh Athena access token (with fallback to store config token)
            val token = athenaTokenManager.getValidToken(
                athenaApiService = athenaApiService,
                fallbackToken = selectedStore.athenaSearchAccessToken
            )
            if (token != null) {
                Log.d(TAG, "✅ Athena token ready")
            } else {
                Log.w(TAG, "⚠️ Failed to get Athena token (even with fallback)")
            }
            
            Result.success(selectedStore)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error refreshing store config: ${e.message}", e)
            Result.failure(e)
        }
    }
}


