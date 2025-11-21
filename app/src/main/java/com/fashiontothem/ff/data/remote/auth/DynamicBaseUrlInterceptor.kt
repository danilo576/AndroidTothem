package com.fashiontothem.ff.data.remote.auth

import android.util.Log
import com.fashiontothem.ff.util.BaseUrlProvider
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Dynamic Base URL Interceptor
 * 
 * Intercepts HTTP requests and dynamically changes the base URL based on the current environment.
 * This ensures that API calls use the correct base URL even after environment changes.
 */
@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val baseUrlProvider: BaseUrlProvider
) : Interceptor {
    
    private val TAG = "FFTothem_DynamicBaseUrl"
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        
        // Get current base URL from provider
        val currentBaseUrl = runBlocking { baseUrlProvider.getBaseUrl() }
        
        // Check if the request URL needs to be changed
        val shouldChangeBaseUrl = originalUrl.host.contains("fashionandfriends.com")
        
        if (shouldChangeBaseUrl) {
            // Parse the current base URL
            val newBaseUrl = currentBaseUrl.toHttpUrlOrNull()
                ?: throw IllegalStateException("Invalid base URL: $currentBaseUrl")
            
            // Build new URL with the correct base
            val newUrl = originalUrl.newBuilder()
                .scheme(newBaseUrl.scheme)
                .host(newBaseUrl.host)
                .port(newBaseUrl.port)
                .build()
            
            // Create new request with updated URL
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()
            
            Log.d(TAG, "Changed base URL: ${originalUrl.host} → ${newUrl.host}")
            
            return chain.proceed(newRequest)
        }
        
        // No change needed, proceed with original request
        return chain.proceed(originalRequest)
    }
}

