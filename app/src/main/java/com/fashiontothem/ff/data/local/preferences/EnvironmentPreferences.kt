package com.fashiontothem.ff.data.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.environmentDataStore: DataStore<Preferences> by preferencesDataStore(name = "ff_tothem_environment_prefs")

/**
 * F&F Tothem - Environment Preferences
 * 
 * DataStore for saving selected environment (production/development).
 */
@Singleton
class EnvironmentPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.environmentDataStore
    private val TAG = "FFTothem_EnvPrefs"
    
    companion object {
        val ENVIRONMENT = stringPreferencesKey("environment")
        
        const val ENVIRONMENT_PRODUCTION = "production"
        const val ENVIRONMENT_DEVELOPMENT = "development"
    }
    
    /**
     * Get selected environment.
     * Returns production by default if not set.
     */
    val environment: Flow<String> = dataStore.data.map { preferences ->
        val env = preferences[ENVIRONMENT] ?: ENVIRONMENT_PRODUCTION
        Log.d(TAG, "Reading environment from preferences: $env")
        env
    }
    
    /**
     * Save selected environment.
     * 
     * @param environment Environment string (production or development)
     */
    suspend fun saveEnvironment(environment: String) {
        Log.d(TAG, "Saving environment: $environment")
        dataStore.edit { preferences ->
            preferences[ENVIRONMENT] = environment
        }
        // Verify it was saved
        val saved = dataStore.data.first().get(ENVIRONMENT) ?: ENVIRONMENT_PRODUCTION
        Log.d(TAG, "Environment saved and verified: $saved")
    }
    
    /**
     * Clear environment preference.
     */
    suspend fun clearEnvironment() {
        dataStore.edit { preferences ->
            preferences.remove(ENVIRONMENT)
        }
    }
}

