package com.fashiontothem.ff.data.repository

import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.data.remote.ApiService
import com.fashiontothem.ff.domain.model.CountryStore
import com.fashiontothem.ff.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Store Repository Implementation
 */
@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val storePreferences: StorePreferences
) : StoreRepository {
    
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
    }
}

