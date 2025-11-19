package com.fashiontothem.ff.domain.repository

import com.fashiontothem.ff.domain.model.StoreLocation
import kotlinx.coroutines.flow.Flow

/**
 * F&F Tothem - Location Repository Interface
 */
interface LocationRepository {
    /**
     * Get store locations for current country.
     */
    suspend fun getStoreLocations(): Result<List<StoreLocation>>
    
    /**
     * Get selected store location ID.
     */
    fun getSelectedStoreId(): Flow<String?>
    
    /**
     * Get selected store location name.
     */
    fun getSelectedStoreName(): Flow<String?>
    
    /**
     * Get selected store location city.
     */
    fun getSelectedStoreCity(): Flow<String?>
    
    /**
     * Save selected store location.
     */
    suspend fun saveSelectedLocation(storeId: String, storeName: String, storeCity: String)
    
    /**
     * Clear selected store location.
     */
    suspend fun clearSelectedLocation()
}

