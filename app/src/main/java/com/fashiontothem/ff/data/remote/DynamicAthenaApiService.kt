package com.fashiontothem.ff.data.remote

import android.util.Log
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.remote.auth.AthenaAuthInterceptor
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * F&F Tothem - Dynamic Athena API Service
 * 
 * Creates AthenaApiService with dynamic base URL from athenaSearchWebsiteUrl.
 * This ensures that API calls use the correct Athena endpoint for the selected store.
 */
class DynamicAthenaApiService(
    private val athenaPreferences: AthenaPreferences,
    private val loggingInterceptor: HttpLoggingInterceptor,
    private val athenaAuthInterceptor: AthenaAuthInterceptor,
    private val moshi: Moshi
) {
    
    private val TAG = "FFTothem_DynamicAthena"
    private var cachedApiService: AthenaApiService? = null
    private var lastBaseUrl: String? = null
    
    /**
     * Get AthenaApiService with dynamic base URL from store config.
     * Creates new service if base URL changed or service doesn't exist.
     */
    suspend fun getApiService(): AthenaApiService {
        val currentBaseUrl = athenaPreferences.websiteUrl.first()
        
        if (currentBaseUrl == null) {
            Log.e(TAG, "❌ No Athena website URL configured - using fallback")
            return getFallbackApiService()
        }
        
        // Check if we need to create new service (base URL changed)
        if (cachedApiService == null || lastBaseUrl != currentBaseUrl) {
            Log.d(TAG, "Creating new AthenaApiService with base URL: $currentBaseUrl")
            
            val athenaClient = OkHttpClient.Builder()
                .addInterceptor(athenaAuthInterceptor)  // Auto-add Bearer token
                .addInterceptor(loggingInterceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(athenaClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            
            cachedApiService = retrofit.create(AthenaApiService::class.java)
            lastBaseUrl = currentBaseUrl
            
            Log.d(TAG, "✅ AthenaApiService created with base URL: $currentBaseUrl")
        }
        
        return cachedApiService!!
    }
    
    /**
     * Get fallback AthenaApiService when no store is selected.
     */
    private fun getFallbackApiService(): AthenaApiService {
        if (cachedApiService == null) {
            Log.d(TAG, "Creating fallback AthenaApiService")
            
            val athenaClient = OkHttpClient.Builder()
                .addInterceptor(athenaAuthInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(com.fashiontothem.ff.util.Constants.ATHENA_DEFAULT_BASE_URL)
                .client(athenaClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            
            cachedApiService = retrofit.create(AthenaApiService::class.java)
            lastBaseUrl = com.fashiontothem.ff.util.Constants.ATHENA_DEFAULT_BASE_URL
        }
        
        return cachedApiService!!
    }
    
    /**
     * Clear cached service (call when store changes).
     */
    fun clearCache() {
        Log.d(TAG, "Clearing AthenaApiService cache")
        cachedApiService = null
        lastBaseUrl = null
    }
}
