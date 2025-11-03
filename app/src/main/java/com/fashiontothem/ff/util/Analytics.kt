package com.fashiontothem.ff.util

import com.fashiontothem.ff.domain.model.AnalyticsEvent
import com.fashiontothem.ff.domain.repository.AnalyticsRepository
import javax.inject.Inject

/**
 * Analytics helper class for easy event tracking throughout the app
 */
class Analytics @Inject constructor(
    private val repository: AnalyticsRepository
) {
    
    // Generic screen view (optional)
    suspend fun trackScreenView(screenName: String) {
        repository.logEvent(
            AnalyticsEvent(
                name = com.fashiontothem.ff.domain.model.AnalyticsEvents.SCREEN_VIEW,
                parameters = mapOf("screen_name" to screenName)
            )
        )
    }
    
    // Generic custom event
    suspend fun trackCustomEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        repository.logEvent(
            AnalyticsEvent(
                name = eventName,
                parameters = parameters
            )
        )
    }
}
