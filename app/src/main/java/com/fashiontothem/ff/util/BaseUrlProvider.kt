package com.fashiontothem.ff.util

import android.util.Log
import com.fashiontothem.ff.data.local.preferences.EnvironmentPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Base URL Provider
 * 
 * Provides the base URL based on selected environment (production/development).
 */
@Singleton
class BaseUrlProvider @Inject constructor(
    private val environmentPreferences: EnvironmentPreferences
) {
    
    private val TAG = "FFTothem_BaseUrl"
    
    companion object {
        const val PRODUCTION_BASE_URL = "https://www.fashionandfriends.com/"
        const val DEVELOPMENT_BASE_URL = "https://develop-pm.fashionandfriends.com/"
    }
    
    /**
     * Get the base URL for the current environment.
     * Defaults to production if environment is not set.
     */
    suspend fun getBaseUrl(): String {
        val environment = environmentPreferences.environment.first()
        val baseUrl = when (environment) {
            EnvironmentPreferences.ENVIRONMENT_DEVELOPMENT -> DEVELOPMENT_BASE_URL
            EnvironmentPreferences.ENVIRONMENT_PRODUCTION -> PRODUCTION_BASE_URL
            else -> PRODUCTION_BASE_URL // Default to production
        }
        Log.d(TAG, "Getting base URL for environment: $environment → $baseUrl")
        return baseUrl
    }
    
    /**
     * Get the base URL for a specific environment.
     */
    fun getBaseUrlForEnvironment(environment: String): String {
        return when (environment) {
            EnvironmentPreferences.ENVIRONMENT_DEVELOPMENT -> DEVELOPMENT_BASE_URL
            EnvironmentPreferences.ENVIRONMENT_PRODUCTION -> PRODUCTION_BASE_URL
            else -> PRODUCTION_BASE_URL
        }
    }
}

