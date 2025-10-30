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
     * Get store locations for selected country.
     * OAuth1 authorized endpoint.
     * 
     * @param countryCode Country code (lowercase, e.g., "rs", "ba", "me", "hr")
     * @return List of store locations
     */
    @GET
    suspend fun getStoreLocations(
        @Url url: String  // Full URL: https://www.fashionandfriends.com/rs/rest/V1/store-locator/locations/rs
    ): Response<List<com.fashiontothem.ff.data.remote.dto.StoreLocationDto>>
    
    /**
     * Get brand images for filter UI.
     * POST endpoint to get brand logos/images.
     * 
     * @return List of brand images with URLs
     */
    @POST
    suspend fun getBrandImages(
        @Url url: String  // Full URL: https://www.fashionandfriends.com/rest/V1/brands-info
    ): Response<List<com.fashiontothem.ff.data.remote.dto.BrandImageDto>>
    
    // Add more Fashion & Friends API endpoints here as needed
}

