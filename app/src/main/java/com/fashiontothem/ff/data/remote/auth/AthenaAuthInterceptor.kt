package com.fashiontothem.ff.data.remote.auth

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.manager.AthenaTokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * F&F Tothem - Athena Auth Interceptor
 * 
 * Automatically adds Bearer token to Athena API requests.
 * If 401 is received, automatically refreshes token from store config and retries request.
 */
class AthenaAuthInterceptor(
    private val athenaPreferences: AthenaPreferences,
    private val athenaTokenManager: AthenaTokenManager
) : Interceptor {
    
    private val TAG = "FFTothem_AthenaAuth"
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get current token from DataStore
        val token = runBlocking {
            athenaPreferences.accessToken.first()
        }
        
        if (token.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ No Athena token available - proceeding without auth")
            return chain.proceed(originalRequest)
        }
        
        // Add Bearer token to request
        val requestWithToken = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        Log.d(TAG, "Added Bearer token to ${originalRequest.url.encodedPath}")
        
        // Execute request
        val response = chain.proceed(requestWithToken)
        
        // Handle 401 - refresh token and retry
        if (response.code == 401) {
            Log.w(TAG, "⚠️ 401 Unauthorized - Refreshing token from store config...")
            
            // Refresh token from store config
            val newToken = runBlocking {
                athenaTokenManager.forceRefreshToken()
            }
            
            if (newToken != null && newToken != token) {
                Log.d(TAG, "✅ Token refreshed - retrying request with new token")
                
                // Retry request with new token
                val retryRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                
                return chain.proceed(retryRequest)
            } else {
                Log.e(TAG, "❌ Failed to refresh token - returning 401 response")
            }
        }
        
        return response
    }
}

