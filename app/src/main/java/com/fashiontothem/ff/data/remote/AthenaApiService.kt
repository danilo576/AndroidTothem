package com.fashiontothem.ff.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * F&F Tothem - Athena Search API Service
 *
 * Dynamic service for Athena Search API using athenaSearchWebsiteUrl from store config.
 * Base URL is determined at runtime based on selected store.
 */
interface AthenaApiService {
    
    // Example endpoints - add your actual Athena API endpoints here
    // The base URL will be dynamically set to athenaSearchWebsiteUrl from store config
    
    /**
     * Example search endpoint
     * POST {athenaSearchWebsiteUrl}/api/search
     */
    // @POST("api/search")
    // suspend fun search(@Body request: AthenaSearchRequest): AthenaSearchResponse
    
    /**
     * Example product details endpoint  
     * POST {athenaSearchWebsiteUrl}/api/product
     */
    // @POST("api/product")
    // suspend fun getProduct(@Body request: AthenaProductRequest): AthenaProductResponse
}

