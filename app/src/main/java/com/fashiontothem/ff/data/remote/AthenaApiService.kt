package com.fashiontothem.ff.data.remote

import com.fashiontothem.ff.data.remote.dto.AthenaProductResponse
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
     * Get products by category with dynamic filter params (Map version)
     * Used to send only non-null filter parameters
     */
    @POST("api/v2/category/data")
    suspend fun getProductsByCategoryDynamic(
        @Body request: Map<String, @JvmSuppressWildcards Any>,
    ): Response<AthenaProductResponse>

    /**
     * Get products by visual similarity search
     * POST {athenaSearchWebsiteUrl}/api/v2/visual-similarity-search
     */
    @POST("api/v2/visual-similarity-search")
    suspend fun getProductsByVisualSearch(
        @Body request: Map<String, @JvmSuppressWildcards Any>,
    ): Response<AthenaProductResponse>
}

