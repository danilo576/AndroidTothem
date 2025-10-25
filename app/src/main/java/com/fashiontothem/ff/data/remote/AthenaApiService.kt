package com.fashiontothem.ff.data.remote

import com.fashiontothem.ff.data.remote.dto.AthenaCategoryRequest
import com.fashiontothem.ff.data.remote.dto.AthenaProductResponse
import com.fashiontothem.ff.data.remote.dto.AthenaVisualSearchRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * F&F Tothem - Athena Search API Service
 *
 * Dynamic service for Athena Search API using athenaSearchWebsiteUrl from store config.
 * Base URL is determined at runtime based on selected store.
 */
interface AthenaApiService {
    
    /**
     * Get products by category with pagination
     * POST {athenaSearchWebsiteUrl}/api/v2/category/data
     */
    @POST("api/v2/category/data")
    suspend fun getProductsByCategory(
        @Body request: AthenaCategoryRequest
    ): Response<AthenaProductResponse>
    
    /**
     * Get products by visual similarity search
     * POST {athenaSearchWebsiteUrl}/api/v2/visual-similarity-search
     */
    @POST("api/v2/visual-similarity-search")
    suspend fun getProductsByVisualSearch(
        @Body request: AthenaVisualSearchRequest
    ): Response<AthenaProductResponse>
}

