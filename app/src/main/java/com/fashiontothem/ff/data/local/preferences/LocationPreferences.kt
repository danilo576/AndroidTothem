package com.fashiontothem.ff.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.locationDataStore: DataStore<Preferences> by preferencesDataStore(name = "ff_tothem_location_prefs")

/**
 * F&F Tothem - Location Preferences
 * 
 * DataStore for saving selected store location (prodavnica).
 */
@Singleton
class LocationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.locationDataStore
    
    companion object {
        val SELECTED_STORE_ID = stringPreferencesKey("selected_store_id")
        val SELECTED_STORE_NAME = stringPreferencesKey("selected_store_name")
        val SELECTED_STORE_CITY = stringPreferencesKey("selected_store_city")
    }
    
    /**
     * Get selected store ID.
     */
    val selectedStoreId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SELECTED_STORE_ID]
    }
    
    /**
     * Get selected store name.
     */
    val selectedStoreName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SELECTED_STORE_NAME]
    }
    
    /**
     * Get selected store city.
     */
    val selectedStoreCity: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SELECTED_STORE_CITY]
    }
    
    /**
     * Save selected store location.
     */
    suspend fun saveSelectedLocation(storeId: String, storeName: String, storeCity: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_STORE_ID] = storeId
            preferences[SELECTED_STORE_NAME] = storeName
            preferences[SELECTED_STORE_CITY] = storeCity
        }
    }
    
    /**
     * Clear selected store location.
     */
    suspend fun clearSelectedLocation() {
        dataStore.edit { preferences ->
            preferences.remove(SELECTED_STORE_ID)
            preferences.remove(SELECTED_STORE_NAME)
            preferences.remove(SELECTED_STORE_CITY)
        }
    }
}

