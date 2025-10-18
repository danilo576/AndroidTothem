package com.fashiontothem.ff.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * F&F Tothem - API Service
 * 
 * Retrofit interface for F&F Tothem fashion gallery API endpoints.
 */
interface ApiService {
    
    /**
     * Get fashion images with pagination.
     * 
     * @param page Page number (0-based)
     * @param pageSize Number of images per page
     * @return List of fashion images
     */
    @GET("fashion/images") // TODO: Update with actual F&F Tothem API endpoint
    suspend fun getFashionImages(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20
    ): Response<List<Any>> // TODO: Replace with actual DTO (e.g., FashionImageDto)
    
    // Add more F&F Tothem API endpoints here as needed
}

