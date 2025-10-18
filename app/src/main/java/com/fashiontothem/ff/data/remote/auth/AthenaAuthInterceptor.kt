package com.fashiontothem.ff.data.remote.auth

import android.util.Log
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * F&F Tothem - Athena Auth Interceptor
 * 
 * Automatically adds Bearer token to Athena API requests.
 * If 401 is received, token should be refreshed by TokenManager and request retried manually.
 */
class AthenaAuthInterceptor(
    private val athenaPreferences: AthenaPreferences
) : Interceptor {
    
    private val TAG = "FFTothem_AthenaAuth"
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip token endpoint itself (no auth needed)
        if (originalRequest.url.encodedPath.contains("/oauth/token")) {
            return chain.proceed(originalRequest)
        }
        
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
        
        // Log 401 for debugging (token refresh will be handled by TokenManager)
        if (response.code == 401) {
            Log.w(TAG, "⚠️ 401 Unauthorized - Token may be expired. TokenManager will refresh on next call.")
        }
        
        return response
    }
}

