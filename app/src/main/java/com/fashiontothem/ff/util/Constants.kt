package com.fashiontothem.ff.util

/**
 * F&F Tothem - Application Constants
 */
object Constants {
    
    /**
     * Fashion & Friends base URL - used for all F&F API calls
     */
    const val FASHION_AND_FRIENDS_BASE_URL = "https://www.fashionandfriends.com/"
    /**
     * Fashion & Friends base media URL
     */
    const val FASHION_AND_FRIENDS_MEDIA_BASE_URL = "https://fashion-assets.fashionandfriends.com/"

    /**
     * Fashion & Friends API base path for mobile endpoints
     */
    const val FASHION_API_PATH = "rest/V1/mobile/"
    
    /**
     * Full Fashion & Friends API base URL
     */
    const val FASHION_API_BASE_URL = FASHION_AND_FRIENDS_BASE_URL + FASHION_API_PATH
    
    /**
     * Default Athena Search base URL (EU region)
     */
    const val ATHENA_DEFAULT_BASE_URL = "https://eu-1.athenasearch.cloud/"
    
    /**
     * Fashion & Friends brand images API endpoint
     */
    const val FASHION_BRANDS_INFO_PATH = "rest/V1/brands-info"
    
    /**
     * Full Fashion & Friends brand images API URL
     */
    const val FASHION_BRANDS_INFO_URL = FASHION_AND_FRIENDS_BASE_URL + FASHION_BRANDS_INFO_PATH
    
    /**
     * Firebase Measurement Protocol endpoint
     */
    const val FIREBASE_MP_ENDPOINT = "https://www.google-analytics.com/mp/collect"
    
    /**
     * Firebase Measurement Protocol debug endpoint
     */
    const val FIREBASE_MP_DEBUG_ENDPOINT = "https://www.google-analytics.com/debug/mp/collect"
    
    /**
     * Firebase App ID for Analytics
     */
    const val FIREBASE_APP_ID = "1:989719399560:android:a7cfac50d3f9a0dc43e33d"
    
    /**
     * Firebase Analytics API Secret
     */
    const val FIREBASE_API_SECRET = "BmJR_a9-Sta_jX2DBo0lIQ"
}

