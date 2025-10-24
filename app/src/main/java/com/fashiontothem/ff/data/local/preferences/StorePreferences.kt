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

private val Context.storeDataStore: DataStore<Preferences> by preferencesDataStore(name = "ff_tothem_store_prefs")

/**
 * F&F Tothem - Store Preferences
 * 
 * DataStore for saving selected store configuration.
 */
@Singleton
class StorePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.storeDataStore
    
    companion object {
        val SELECTED_STORE_CODE = stringPreferencesKey("selected_store_code")
        val SELECTED_COUNTRY_CODE = stringPreferencesKey("selected_country_code")
        val SELECTED_LOCALE = stringPreferencesKey("selected_locale")
    }
    
    /**
     * Get selected store code.
     * Returns null if no store has been selected yet.
     */
    val selectedStoreCode: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SELECTED_STORE_CODE]
    }
    
    /**
     * Get selected country code.
     */
    val selectedCountryCode: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SELECTED_COUNTRY_CODE]
    }
    
    /**
     * Get selected locale.
     */
    val selectedLocale: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SELECTED_LOCALE]
    }
    
    /**
     * Save selected store.
     * 
     * @param storeCode Store code (e.g., "rs_SR", "ba_BS")
     * @param countryCode Country code (e.g., "RS", "BA")
     * @param locale Locale string (e.g., "sr_Cyrl_RS", "hr_HR")
     */
    suspend fun saveSelectedStore(storeCode: String, countryCode: String, locale: String? = null) {
        dataStore.edit { preferences ->
            preferences[SELECTED_STORE_CODE] = storeCode
            preferences[SELECTED_COUNTRY_CODE] = countryCode
            if (locale != null) {
                preferences[SELECTED_LOCALE] = locale
            }
        }
    }
    
    /**
     * Clear selected store.
     */
    suspend fun clearSelectedStore() {
        dataStore.edit { preferences ->
            preferences.remove(SELECTED_STORE_CODE)
            preferences.remove(SELECTED_COUNTRY_CODE)
            preferences.remove(SELECTED_LOCALE)
        }
    }
}

