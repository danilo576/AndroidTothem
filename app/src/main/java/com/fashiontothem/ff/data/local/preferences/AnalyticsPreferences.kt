package com.fashiontothem.ff.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fashiontothem.ff.domain.model.AnalyticsEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.analyticsDataStore: DataStore<Preferences> by preferencesDataStore(name = "analytics_prefs")

@Singleton
class AnalyticsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.analyticsDataStore
    
    private val userIdKey = stringPreferencesKey("user_id")
    private val eventQueueKey = stringPreferencesKey("event_queue")
    private val userPropertiesKey = stringPreferencesKey("user_properties")
    
    private val gson = Gson()
    
    // User ID (generated once per installation)
    suspend fun getUserId(): String? {
        return dataStore.data.first()[userIdKey]
    }
    
    suspend fun saveUserId(userId: String) {
        dataStore.edit { prefs ->
            prefs[userIdKey] = userId
        }
    }
    
    // Event queue for offline events
    suspend fun getEventQueue(): List<AnalyticsEvent> {
        val json = dataStore.data.first()[eventQueueKey] ?: return emptyList()
        val type = object : TypeToken<List<AnalyticsEvent>>() {}.type
        return gson.fromJson<List<AnalyticsEvent>>(json, type) ?: emptyList()
    }
    
    suspend fun addEventToQueue(event: AnalyticsEvent) {
        dataStore.edit { prefs ->
            val currentQueue = getEventQueue()
            val updatedQueue = currentQueue + event
            // Keep only last 100 events to prevent storage bloat
            val queue = if (updatedQueue.size > 100) {
                updatedQueue.takeLast(100)
            } else {
                updatedQueue
            }
            prefs[eventQueueKey] = gson.toJson(queue)
        }
    }
    
    suspend fun clearEventQueue() {
        dataStore.edit { prefs ->
            prefs.remove(eventQueueKey)
        }
    }
    
    // User properties
    suspend fun getUserProperties(): Map<String, String> {
        val json = dataStore.data.first()[userPropertiesKey] ?: return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson<Map<String, String>>(json, type) ?: emptyMap()
    }
    
    suspend fun saveUserProperties(properties: Map<String, String>) {
        dataStore.edit { prefs ->
            prefs[userPropertiesKey] = gson.toJson(properties)
        }
    }
}
