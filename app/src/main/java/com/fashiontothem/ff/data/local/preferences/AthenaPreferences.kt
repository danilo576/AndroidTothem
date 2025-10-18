package com.fashiontothem.ff.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.athenaDataStore: DataStore<Preferences> by preferencesDataStore(name = "ff_tothem_athena_prefs")

/**
 * F&F Tothem - Athena Preferences
 * 
 * DataStore for saving Athena search token and expiration.
 */
@Singleton
class AthenaPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.athenaDataStore
    
    companion object {
        val ATHENA_ACCESS_TOKEN = stringPreferencesKey("athena_access_token")
        val ATHENA_TOKEN_EXPIRATION = longPreferencesKey("athena_token_expiration")  // Timestamp in millis
        val ATHENA_WEBSITE_URL = stringPreferencesKey("athena_website_url")
        val ATHENA_WTOKEN = stringPreferencesKey("athena_wtoken")
    }
    
    /**
     * Get Athena access token.
     */
    val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ATHENA_ACCESS_TOKEN]
    }
    
    /**
     * Get token expiration timestamp (in milliseconds).
     */
    val tokenExpiration: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[ATHENA_TOKEN_EXPIRATION]
    }
    
    /**
     * Get Athena website URL.
     */
    val websiteUrl: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ATHENA_WEBSITE_URL]
    }
    
    /**
     * Get Athena wtoken.
     */
    val wtoken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ATHENA_WTOKEN]
    }
    
    /**
     * Save Athena token with expiration.
     * 
     * @param accessToken The access token
     * @param expiresInSeconds How many seconds until expiration
     */
    suspend fun saveToken(accessToken: String, expiresInSeconds: Long) {
        val expirationTime = System.currentTimeMillis() + (expiresInSeconds * 1000)
        dataStore.edit { preferences ->
            preferences[ATHENA_ACCESS_TOKEN] = accessToken
            preferences[ATHENA_TOKEN_EXPIRATION] = expirationTime
        }
    }
    
    /**
     * Save Athena website URL and wtoken from store config.
     */
    suspend fun saveAthenaConfig(websiteUrl: String, wtoken: String) {
        dataStore.edit { preferences ->
            preferences[ATHENA_WEBSITE_URL] = websiteUrl
            preferences[ATHENA_WTOKEN] = wtoken
        }
    }
    
    /**
     * Check if token is expired or about to expire (within 5 minutes).
     */
    suspend fun isTokenExpiredOrExpiringSoon(): Boolean {
        val expiration = dataStore.data.map { preferences ->
            preferences[ATHENA_TOKEN_EXPIRATION] ?: 0L
        }.first()
        
        val now = System.currentTimeMillis()
        return (expiration - now) < (5 * 60 * 1000)  // Refresh if less than 5 minutes left
    }
    
    /**
     * Clear all Athena data.
     */
    suspend fun clearAthenaData() {
        dataStore.edit { preferences ->
            preferences.remove(ATHENA_ACCESS_TOKEN)
            preferences.remove(ATHENA_TOKEN_EXPIRATION)
            preferences.remove(ATHENA_WEBSITE_URL)
            preferences.remove(ATHENA_WTOKEN)
        }
    }
}

