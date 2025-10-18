package com.fashiontothem.ff.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * F&F Tothem - API Service
 * 
 * Retrofit interface for Fashion & Friends API endpoints.
 */
interface ApiService {
    
    /**
     * Get store configurations for all countries.
     * OAuth1 authorized endpoint.
     * 
     * @return List of country stores with configurations
     */
    @GET("store/storeConfigs")
    suspend fun getStoreConfigs(): Response<List<com.fashiontothem.ff.data.remote.dto.StoreConfigResponse>>
    
    /**
     * Get fashion images with pagination.
     * 
     * @param page Page number (0-based)
     * @param pageSize Number of images per page
     * @return List of fashion images
     */
    @GET("fashion/images") // TODO: Update with actual endpoint
    suspend fun getFashionImages(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20
    ): Response<List<Any>> // TODO: Replace with actual DTO
    
    // Add more Fashion & Friends API endpoints here as needed
}

