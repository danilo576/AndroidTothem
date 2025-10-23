package com.fashiontothem.ff.data.manager

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.domain.repository.StoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Athena Token Manager
 * 
 * Manages Athena Search API token lifecycle:
 * - Automatic token refresh when expired
 * - Thread-safe token acquisition
 * - Stores token in DataStore
 */
@Singleton
class AthenaTokenManager @Inject constructor(
    private val athenaPreferences: AthenaPreferences,
    private val storePreferences: StorePreferences,
    private val storeRepository: StoreRepository
) {
    private val TAG = "FFTothem_AthenaToken"
    private val mutex = Mutex()  // Thread-safe token refresh
    
    // Cache for JWT expiration times to avoid repeated decoding
    private val jwtExpirationCache = mutableMapOf<String, Long>()
    
    /**
     * Get valid Athena access token from store config.
     * Automatically refreshes store config if token is expired or missing.
     * 
     * @return Valid access token or null if store config refresh failed
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getValidToken(): String? = mutex.withLock {
        // Check if token exists and is still valid
        val currentToken = athenaPreferences.accessToken.first()
        val isExpired = athenaPreferences.isTokenExpiredOrExpiringSoon()
        
        if (currentToken != null && !isExpired) {
            Log.d(TAG, "Using existing valid token")
            return currentToken
        }
        
        // Token expired or doesn't exist - refresh from store config
        Log.d(TAG, "Token expired or missing. Refreshing from store config...")
        return refreshTokenFromStoreConfig()
    }
    
    /**
     * Force refresh token (e.g., on 401 response).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun forceRefreshToken(): String? = mutex.withLock {
        Log.d(TAG, "Force refreshing token from store config...")
        return refreshTokenFromStoreConfig()
    }
    
    /**
     * Refresh Athena token from store config.
     * Gets the latest store config for the selected store and uses its access token.
     * Also updates Athena config (websiteUrl + wtoken) for dynamic API calls.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun refreshTokenFromStoreConfig(): String? {
        return try {
            // Get selected store code
            val selectedStoreCode = storePreferences.selectedStoreCode.first()
            val selectedCountryCode = storePreferences.selectedCountryCode.first()
            
            if (selectedStoreCode.isNullOrEmpty() || selectedCountryCode.isNullOrEmpty()) {
                Log.e(TAG, "❌ No store selected")
                return null
            }
            
            Log.d(TAG, "Refreshing store config for store: $selectedCountryCode/$selectedStoreCode")
            
            // Get fresh store config
            val storeConfigsResult = storeRepository.getStoreConfigs()
            if (storeConfigsResult.isFailure) {
                Log.e(TAG, "❌ Failed to get store configs: ${storeConfigsResult.exceptionOrNull()?.message}")
                return null
            }
            
            val storeConfigs = storeConfigsResult.getOrThrow()
            val selectedStore = storeConfigs
                .find { it.countryCode == selectedCountryCode }
                ?.stores?.find { it.code == selectedStoreCode }
            
            if (selectedStore == null) {
                Log.e(TAG, "❌ Store not found in config: $selectedCountryCode/$selectedStoreCode")
                return null
            }
            
            val accessToken = selectedStore.athenaSearchAccessToken
            if (accessToken.isEmpty()) {
                Log.e(TAG, "❌ No access token in store config for: $selectedStoreCode")
                return null
            }
            
            // Save Athena config (websiteUrl + wtoken) for dynamic API calls
            athenaPreferences.saveAthenaConfig(
                websiteUrl = selectedStore.athenaSearchWebsiteUrl,
                wtoken = selectedStore.athenaSearchWtoken
            )
            
            Log.d(TAG, "✅ Athena config updated: ${selectedStore.athenaSearchWebsiteUrl}")
            
            // Decode JWT to get actual expiration time
            val expirationSeconds = getJwtExpirationTime(accessToken)
            
            // Save token with actual expiration time
            athenaPreferences.saveToken(
                accessToken = accessToken,
                expiresInSeconds = expirationSeconds
            )
            
            Log.d(TAG, "✅ Token refreshed from store config for store: $selectedStoreCode")
            return accessToken
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception refreshing token from store config: ${e.message}", e)
            null
        }
    }
    
    /**
     * Clear token (e.g., on logout or store change).
     */
    suspend fun clearToken() {
        athenaPreferences.clearAthenaData()
        jwtExpirationCache.clear() // Clear JWT cache too
        Log.d(TAG, "Token and JWT cache cleared")
    }
    
    /**
     * Decode JWT token to get expiration time.
     * Uses cache to avoid repeated decoding of the same token.
     * 
     * @param jwtToken JWT token string
     * @return Expiration time in seconds from epoch, or fallback to 1 year if decoding fails
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getJwtExpirationTime(jwtToken: String): Long {
        // Check cache first
        jwtExpirationCache[jwtToken]?.let { cachedExpiration ->
            Log.d(TAG, "Using cached JWT expiration time")
            return cachedExpiration
        }
        
        return try {
            // JWT has 3 parts separated by dots: header.payload.signature
            val parts = jwtToken.split(".")
            if (parts.size != 3) {
                Log.w(TAG, "⚠️ Invalid JWT format - using fallback expiration")
                return 365L * 24 * 60 * 60 // 1 year fallback
            }
            
            // Decode payload (second part)
            val payload = parts[1]
            
            // Add padding if needed for Base64 decoding
            val paddedPayload = when (payload.length % 4) {
                2 -> "$payload=="
                3 -> "$payload="
                else -> payload
            }
            
            val decodedBytes = Base64.getDecoder().decode(paddedPayload)
            val payloadJson = String(decodedBytes)
            
            // Parse JSON to get 'exp' claim
            val expStart = payloadJson.indexOf("\"exp\":")
            if (expStart == -1) {
                Log.w(TAG, "⚠️ No 'exp' claim found in JWT - using fallback expiration")
                return 365L * 24 * 60 * 60 // 1 year fallback
            }
            
            val expValueStart = expStart + 6 // Skip '"exp":'
            val expValueEnd = payloadJson.indexOf(',', expValueStart).let { 
                if (it == -1) payloadJson.indexOf('}', expValueStart) else it 
            }
            
            val expValue = payloadJson.substring(expValueStart, expValueEnd).trim()
            val expTimestamp = expValue.toDoubleOrNull()?.toLong()
            
            if (expTimestamp == null) {
                Log.w(TAG, "⚠️ Invalid 'exp' value in JWT: $expValue - using fallback expiration")
                return 365L * 24 * 60 * 60 // 1 year fallback
            }
            
            // Convert to seconds from now
            val currentTimeSeconds = System.currentTimeMillis() / 1000
            val expirationSeconds = expTimestamp - currentTimeSeconds
            
            if (expirationSeconds <= 0) {
                Log.w(TAG, "⚠️ JWT already expired - using fallback expiration")
                return 365L * 24 * 60 * 60 // 1 year fallback
            }
            
            // Calculate exact expiration date and time
            val expirationDate = Date(expTimestamp * 1000) // Convert to milliseconds
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val expirationString = dateFormat.format(expirationDate)
            
            Log.d(TAG, "✅ JWT decoded - expires in ${expirationSeconds}s (${expirationSeconds / 3600}h)")
            Log.d(TAG, "📅 Token expires on: $expirationString")
            
            // Cache the result
            jwtExpirationCache[jwtToken] = expirationSeconds
            
            expirationSeconds
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error decoding JWT: ${e.message} - using fallback expiration", e)
            365L * 24 * 60 * 60 // 1 year fallback
        }
    }
}

