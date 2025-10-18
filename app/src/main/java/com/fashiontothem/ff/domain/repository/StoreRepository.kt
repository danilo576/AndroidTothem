package com.fashiontothem.ff.domain.repository

import com.fashiontothem.ff.domain.model.CountryStore
import com.fashiontothem.ff.domain.model.StoreConfig
import kotlinx.coroutines.flow.Flow

/**
 * F&F Tothem - Store Repository Interface
 */
interface StoreRepository {
    /**
     * Get all available store configurations from API.
     */
    suspend fun getStoreConfigs(): Result<List<CountryStore>>
    
    /**
     * Get currently selected store code.
     */
    fun getSelectedStoreCode(): Flow<String?>
    
    /**
     * Get currently selected country code.
     */
    fun getSelectedCountryCode(): Flow<String?>
    
    /**
     * Save selected store.
     */
    suspend fun saveSelectedStore(storeCode: String, countryCode: String)
    
    /**
     * Clear selected store.
     */
    suspend fun clearSelectedStore()
}

