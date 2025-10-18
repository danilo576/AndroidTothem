package com.fashiontothem.ff.data.manager

import android.util.Log
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.remote.AthenaApiService
import com.fashiontothem.ff.data.remote.dto.AthenaTokenRequest
import humer.UvcCamera.BuildConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val athenaPreferences: AthenaPreferences
) {
    private val TAG = "FFTothem_AthenaToken"
    private val mutex = Mutex()  // Thread-safe token refresh
    
    /**
     * Get valid Athena access token.
     * Automatically refreshes if expired or about to expire.
     * Falls back to fallbackToken from store config if OAuth fails.
     * 
     * @param athenaApiService Athena API service for token refresh
     * @param fallbackToken Token from store config to use if OAuth fails
     * @return Valid access token or null if both methods failed
     */
    suspend fun getValidToken(
        athenaApiService: AthenaApiService,
        fallbackToken: String? = null
    ): String? = mutex.withLock {
        // Check if token exists and is still valid
        val currentToken = athenaPreferences.accessToken.first()
        val isExpired = athenaPreferences.isTokenExpiredOrExpiringSoon()
        
        if (currentToken != null && !isExpired) {
            Log.d(TAG, "Using existing valid token")
            return currentToken
        }
        
        // Token expired or doesn't exist - refresh
        Log.d(TAG, "Token expired or missing. Refreshing...")
        return refreshToken(athenaApiService, fallbackToken)
    }
    
    /**
     * Force refresh token (e.g., on 401 response).
     */
    suspend fun forceRefreshToken(
        athenaApiService: AthenaApiService,
        fallbackToken: String? = null
    ): String? = mutex.withLock {
        Log.d(TAG, "Force refreshing token...")
        return refreshToken(athenaApiService, fallbackToken)
    }
    
    /**
     * Refresh Athena token from API.
     * Falls back to fallbackToken if OAuth endpoint fails.
     */
    private suspend fun refreshToken(
        athenaApiService: AthenaApiService,
        fallbackToken: String?
    ): String? {
        return try {
            val request = AthenaTokenRequest(
                clientId = BuildConfig.ATHENA_CLIENT_ID,
                clientSecret = BuildConfig.ATHENA_CLIENT_SECRET,
                grantType = "client_credentials",
                scope = "*"
            )
            
            val response = athenaApiService.getAccessToken(request)
            
            if (response.isSuccessful) {
                val tokenResponse = response.body()
                if (tokenResponse != null) {
                    // Save token with expiration
                    athenaPreferences.saveToken(
                        accessToken = tokenResponse.accessToken,
                        expiresInSeconds = tokenResponse.expiresIn
                    )
                    
                    Log.d(TAG, "✅ Token refreshed from OAuth. Expires in ${tokenResponse.expiresIn}s")
                    return tokenResponse.accessToken
                }
            }
            
            // OAuth failed - use fallback token from store config if available
            Log.w(TAG, "⚠️ OAuth token refresh failed: ${response.code()} ${response.message()}")
            
            if (fallbackToken != null) {
                Log.d(TAG, "✅ Using fallback token from store config")
                
                // Save fallback token (assume 1 year expiration based on JWT)
                athenaPreferences.saveToken(
                    accessToken = fallbackToken,
                    expiresInSeconds = 365L * 24 * 60 * 60 // 1 year
                )
                
                return fallbackToken
            }
            
            Log.e(TAG, "❌ No fallback token available")
            null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception refreshing token: ${e.message}", e)
            
            // Network error - use fallback token if available
            if (fallbackToken != null) {
                Log.d(TAG, "✅ Using fallback token due to network error")
                
                athenaPreferences.saveToken(
                    accessToken = fallbackToken,
                    expiresInSeconds = 365L * 24 * 60 * 60
                )
                
                return fallbackToken
            }
            
            null
        }
    }
    
    /**
     * Clear token (e.g., on logout or store change).
     */
    suspend fun clearToken() {
        athenaPreferences.clearAthenaData()
        Log.d(TAG, "Token cleared")
    }
}

