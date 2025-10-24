package com.fashiontothem.ff.data.repository

import android.util.Log
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
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
    private val storePreferences: StorePreferences,
    private val athenaPreferences: AthenaPreferences
) : StoreRepository {
    
    private val TAG = "FFTothem_StoreRepo"
    
    // Cache for store configs to avoid unnecessary API calls
    private var cachedStoreConfigs: List<CountryStore>? = null
    private var lastConfigFetchTime: Long = 0
    private val CONFIG_CACHE_DURATION = 5 * 60 * 1000L // 5 minutes
    
    override suspend fun getStoreConfigs(): Result<List<CountryStore>> {
        return try {
            // Check if we have cached configs that are still fresh
            val currentTime = System.currentTimeMillis()
            if (cachedStoreConfigs != null && (currentTime - lastConfigFetchTime) < CONFIG_CACHE_DURATION) {
                Log.d(TAG, "Using cached store configs (age: ${(currentTime - lastConfigFetchTime) / 1000}s)")
                return Result.success(cachedStoreConfigs!!)
            }
            
            Log.d(TAG, "Fetching fresh store configs from API...")
            val response = apiService.getStoreConfigs()
            if (response.isSuccessful) {
                val stores = response.body()?.map { it.toDomain() } ?: emptyList()
                
                // Cache the results
                cachedStoreConfigs = stores
                lastConfigFetchTime = currentTime
                
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
        athenaPreferences.clearAthenaData()  // Clear Athena token too
    }
    
    /**
     * Refresh store config and initialize Athena token.
     * Call this on app start if store is already selected.
     * Optimized to avoid unnecessary API calls.
     */
    override suspend fun refreshStoreConfigAndInitAthena(): Result<StoreConfig> {
        return try {
            val storeCode = storePreferences.selectedStoreCode.first()
            val countryCode = storePreferences.selectedCountryCode.first()
            
            if (storeCode == null || countryCode == null) {
                return Result.failure(Exception("No store selected"))
            }
            
            Log.d(TAG, "Checking if store config refresh is needed for $countryCode / $storeCode")
            
            // Check if we already have valid Athena config and token
            val currentWebsiteUrl = athenaPreferences.websiteUrl.first()
            val currentWtoken = athenaPreferences.wtoken.first()
            val currentToken = athenaPreferences.accessToken.first()
            val isTokenExpired = athenaPreferences.isTokenExpiredOrExpiringSoon()
            
            // If we have valid config and token, skip API call
            if (currentWebsiteUrl != null && currentWtoken != null && 
                currentToken != null && !isTokenExpired) {
                Log.d(TAG, "✅ Athena config and token are still valid - skipping API call")
                
                // Find cached store config to return
                val cachedStores = cachedStoreConfigs
                if (cachedStores != null) {
                    val selectedStore = cachedStores
                        .find { it.countryCode == countryCode }
                        ?.stores?.find { it.code == storeCode }
                    
                    if (selectedStore != null) {
                        return Result.success(selectedStore)
                    }
                }
            }
            
            Log.d(TAG, "Refreshing store config from API...")
            
            // Get fresh store configs from API (will use cache if available)
            val storeConfigsResult = getStoreConfigs()
            if (storeConfigsResult.isFailure) {
                return Result.failure(Exception("Failed to get store configs: ${storeConfigsResult.exceptionOrNull()?.message}"))
            }
            
            val allStores = storeConfigsResult.getOrThrow()
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
            
            // Save Athena access token from store config
            if (selectedStore.athenaSearchAccessToken.isNotEmpty()) {
                athenaPreferences.saveToken(
                    accessToken = selectedStore.athenaSearchAccessToken,
                    expiresInSeconds = 365L * 24 * 60 * 60 // 1 year
                )
                Log.d(TAG, "✅ Athena token saved from store config")
            } else {
                Log.w(TAG, "⚠️ No Athena token in store config")
            }
            
            Result.success(selectedStore)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error refreshing store config: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get cached Athena wtoken without any API calls.
     * This is used for product API calls to avoid unnecessary store config refreshes.
     */
    override suspend fun getCachedAthenaToken(): String? {
        return try {
            val token = athenaPreferences.wtoken.first()
            if (token.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ No cached Athena token found")
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting cached Athena token: ${e.message}", e)
            null
        }
    }
}


