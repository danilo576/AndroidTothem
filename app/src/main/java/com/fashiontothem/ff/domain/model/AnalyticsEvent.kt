package com.fashiontothem.ff.domain.model

/**
 * Represents an analytics event
 */
data class AnalyticsEvent(
    val name: String,
    val parameters: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Convert to Firebase Measurement Protocol format
     */
    fun toFirebaseFormat(): Map<String, Any> {
        val params = mutableMapOf<String, Any>()
        parameters.forEach { (key, value) ->
            params[key] = when (value) {
                is String -> value
                is Number -> value.toString()
                is Boolean -> if (value) "1" else "0"
                else -> value.toString()
            }
        }
        return mapOf("name" to name, "params" to params)
    }
}

// Predefined event names
object AnalyticsEvents {
    // Screen navigation
    const val SCREEN_VIEW = "screen_view"
    const val PAGE_VIEW = "page_view"
    
    // Visual search
    const val VISUAL_SEARCH_START = "visual_search_start"
    const val VISUAL_SEARCH_RESULTS = "visual_search_results"
    const val VISUAL_SEARCH_ERROR = "visual_search_error"
    
    // Product listing
    const val PRODUCT_LIST_VIEW = "product_list_view"
    const val PRODUCT_CLICK = "product_click"
    const val PRODUCT_FAVORITE = "product_favorite"
    const val PRODUCT_SHARE = "product_share"
    
    // Filters
    const val FILTER_APPLIED = "filter_applied"
    const val FILTER_CLEAR = "filter_clear"
    const val FILTER_CATEGORY = "filter_category"
    const val FILTER_BRAND = "filter_brand"
    const val FILTER_SIZE = "filter_size"
    const val FILTER_COLOR = "filter_color"
    
    // Store selection
    const val STORE_SELECTED = "store_selected"
    const val LOCATION_SELECTED = "location_selected"
    
    // Camera
    const val CAMERA_OPENED = "camera_opened"
    const val CAMERA_PHOTO_CAPTURED = "camera_photo_captured"
    
    // Navigation
    const val NAVIGATION_BACK = "navigation_back"
    const val NAVIGATION_HOME = "navigation_home"
    const val NAVIGATION_LOGO_CLICK = "navigation_logo_click"
}
