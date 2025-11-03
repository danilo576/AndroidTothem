package com.fashiontothem.ff.domain.repository

import com.fashiontothem.ff.domain.model.AnalyticsEvent

/**
 * Repository interface for analytics tracking
 * Works without Google Play Services by using Firebase REST API
 */
interface AnalyticsRepository {
    /**
     * Log a custom event (queued if offline, sent immediately if online)
     */
    suspend fun logEvent(event: AnalyticsEvent)
    
    /**
     * Set user properties (e.g., device type, location)
     */
    suspend fun setUserProperty(name: String, value: String)
    
    /**
     * Get current user ID (generated per-installation)
     */
    suspend fun getUserId(): String
    
    /**
     * Flush any pending events (called when network becomes available)
     */
    suspend fun flushEvents()
}
