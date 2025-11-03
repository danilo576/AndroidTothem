package com.fashiontothem.ff.data.repository

import android.content.Context
import android.util.Log
import com.fashiontothem.ff.data.local.preferences.AnalyticsPreferences
import com.fashiontothem.ff.domain.model.AnalyticsEvent
import com.fashiontothem.ff.domain.repository.AnalyticsRepository
import com.fashiontothem.ff.util.NetworkConnectivityObserver
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics repository implementation using Firebase Measurement Protocol REST API
 * Works without Google Play Services
 */
@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val analyticsPreferences: AnalyticsPreferences,
    private val okHttpClient: OkHttpClient
) : AnalyticsRepository {
    
    companion object {
        private const val TAG = "Analytics"
        
        // Firebase Measurement Protocol endpoint
        private const val FIREBASE_MP_ENDPOINT = "https://www.google-analytics.com/mp/collect"

        // Analytics credentials from Firebase Console
           private const val FIREBASE_APP_ID = "1:989719399560:android:a7cfac50d3f9a0dc43e33d"
           private const val API_SECRET = "BmJR_a9-Sta_jX2DBo0lIQ" // ✅ New API Secret for com.totem.ff stream
           
       }
    
    // Generate unique user ID once per installation
    private var userId: String? = null
    private val gson = Gson()
    
    private val networkObserver = NetworkConnectivityObserver(context)
    
    init {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🔥 AnalyticsRepositoryImpl INITIALIZING")
        Log.d(TAG, "Firebase App ID: $FIREBASE_APP_ID")
        Log.d(TAG, "═══════════════════════════════════════")
        
        // Try to get or generate user ID synchronously
        kotlinx.coroutines.runBlocking {
            userId = analyticsPreferences.getUserId()
            if (userId == null) {
                userId = generateInstallationId()
                analyticsPreferences.saveUserId(userId!!)
                Log.d(TAG, "✅ Generated new User ID: $userId")
            } else {
                Log.d(TAG, "✅ Using existing User ID: $userId")
            }
        }
        
        // Initialize network observer to flush events when online
        startNetworkMonitoring()
    }
    
    private fun generateInstallationId(): String {
        // Fallback unique ID per installation (only used if FID cannot be obtained)
        return UUID.randomUUID().toString().replace("-", "")
    }

    private suspend fun getFirebaseAppInstanceId(): String {
        // Prefer the Analytics app instance id (GA4) to ensure DebugView correlation
        try {
            val analyticsId = FirebaseAnalytics.getInstance(context).appInstanceId.await()
            if (!analyticsId.isNullOrBlank()) {
                return analyticsId
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to get Analytics app_instance_id, will try FID", t)
        }
        // Fallback to Firebase Installations ID (FID)
        return try {
            FirebaseInstallations.getInstance().id.await()
        } catch (t: Throwable) {
            // Final fallback to local ID
            Log.w(TAG, "Failed to get Firebase Installation ID, using local fallback.", t)
            generateInstallationId()
        }
    }
    
    override suspend fun getUserId(): String {
        return userId ?: generateInstallationId().also { userId = it }
    }
    
    override suspend fun logEvent(event: AnalyticsEvent) {
        Log.d(TAG, "===========================================")
        Log.d(TAG, "📊 Analytics Event Triggered")
        Log.d(TAG, "Event Name: ${event.name}")
        Log.d(TAG, "Event Parameters: ${event.parameters}")
        Log.d(TAG, "Timestamp: ${event.timestamp}")
        
        try {
            val isOnline = networkObserver.isNetworkAvailable()
            Log.d(TAG, "Network Status: ${if (isOnline) "✅ ONLINE" else "❌ OFFLINE"}")
            
            if (isOnline) {
                // Send immediately if online
                Log.d(TAG, "Sending event to Firebase immediately...")
                sendEventToFirebase(event)
            } else {
                // Queue for later if offline
                Log.d(TAG, "Queueing event for later (offline)")
                analyticsPreferences.addEventToQueue(event)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error logging event: ${event.name}", e)
            // Still queue on error to not lose events
            analyticsPreferences.addEventToQueue(event)
        }
        Log.d(TAG, "===========================================")
    }
    
    override suspend fun setUserProperty(name: String, value: String) {
        val currentProperties = analyticsPreferences.getUserProperties().toMutableMap()
        currentProperties[name] = value
        analyticsPreferences.saveUserProperties(currentProperties)
        
        // Send as event for tracking
        logEvent(
            AnalyticsEvent(
                name = "user_property_set",
                parameters = mapOf("property_name" to name, "property_value" to value)
            )
        )
    }
    
    override suspend fun flushEvents() {
        if (!networkObserver.isNetworkAvailable()) {
            Log.d(TAG, "Still offline - cannot flush events")
            return
        }
        
        val queue = analyticsPreferences.getEventQueue()
        if (queue.isEmpty()) {
            Log.d(TAG, "No events to flush")
            return
        }
        
        Log.d(TAG, "Flushing ${queue.size} queued events")
        
        queue.forEach { event ->
            try {
                sendEventToFirebase(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending queued event: ${event.name}", e)
            }
        }
        
        // Clear queue after successful send
        analyticsPreferences.clearEventQueue()
        Log.d(TAG, "Event queue cleared")
    }
    
    private suspend fun sendEventToFirebase(event: AnalyticsEvent) {
        try {
            val userId = getUserId()
            val appInstanceId = getFirebaseAppInstanceId()
            val baseEvent = event.toFirebaseFormat()
            val baseParams = (baseEvent["params"] as? Map<String, Any>)?.toMutableMap() ?: mutableMapOf()

            // Ensure MP debug events appear in GA4 DebugView (force during setup)
            baseParams["debug_mode"] = 1
            val finalEvent = mapOf(
                "name" to (baseEvent["name"] ?: event.name),
                "params" to baseParams
            )
            val eventData = mutableMapOf<String, Any>(
                "app_instance_id" to appInstanceId,
                "user_id" to userId,
                "events" to listOf(finalEvent)
            )
            
            val json = gson.toJson(eventData)
            val requestBody = json.toRequestBody("application/json".toMediaType())
            
            val baseUrl = FIREBASE_MP_ENDPOINT
            val url = "$baseUrl?api_secret=$API_SECRET&firebase_app_id=$FIREBASE_APP_ID"
            
            Log.d(TAG, "📡 Sending to Firebase:")
            Log.d(TAG, "URL: $baseUrl")
            Log.d(TAG, "Firebase App ID: $FIREBASE_APP_ID")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "App Instance ID: $appInstanceId")
            Log.d(TAG, "Request Body: $json")
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            Log.d(TAG, "Response Code: ${response.code}")
            Log.d(TAG, "Response Message: ${response.message}")
            
            val responseBody = response.body?.string()
            
            if (response.isSuccessful) {
                Log.d(TAG, "✅ SUCCESS: Event sent to Firebase: ${event.name}")
                Log.d(TAG, "GA4 Ingest OK → app_id=$FIREBASE_APP_ID, app_instance_id=$appInstanceId, user_id=$userId, event=${event.name}")
            } else {
                Log.e(TAG, "❌ FAILED to send event: ${response.code} - ${response.message}")
                if (responseBody != null) {
                    Log.e(TAG, "Error Body: $responseBody")
                }
            }
            
            response.close()

            // In debug builds, also call the validator endpoint to surface validation messages
            if (humer.UvcCamera.BuildConfig.DEBUG) {
                val debugUrl = "https://www.google-analytics.com/debug/mp/collect?api_secret=$API_SECRET&firebase_app_id=$FIREBASE_APP_ID"
                val debugRequest = Request.Builder()
                    .url(debugUrl)
                    .post(requestBody)
                    .build()
                try {
                    val debugResponse = okHttpClient.newCall(debugRequest).execute()
                    val debugBody = debugResponse.body?.string()
                    Log.d(TAG, "🧪 MP Debug Endpoint Code: ${debugResponse.code}")
                    Log.d(TAG, "🧪 MP Debug Endpoint Body: ${debugBody ?: "<empty>"}")
                    debugResponse.close()
                } catch (vt: Throwable) {
                    Log.w(TAG, "Validator call failed", vt)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception sending event to Firebase", e)
            throw e
        }
    }
    
    private fun startNetworkMonitoring() {
        // Monitor network and flush queue when online
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob()).launch {
            networkObserver.observe().collect { isConnected ->
                if (isConnected) {
                    Log.d(TAG, "Network connected - flushing event queue")
                    try {
                        flushEvents()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error flushing events", e)
                    }
                }
            }
        }
    }
    
}
