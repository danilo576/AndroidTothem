package com.fashiontothem.ff.data.remote

import com.fashiontothem.ff.data.remote.dto.BrandImageDto
import com.fashiontothem.ff.data.remote.dto.ProductDetailsResponse
import com.fashiontothem.ff.data.remote.dto.StoreConfigResponse
import com.fashiontothem.ff.data.remote.dto.StoreLocationDto
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
    suspend fun getStoreConfigs(): Response<List<StoreConfigResponse>>
    
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
    ): Response<List<StoreLocationDto>>
    
    /**
     * Get brand images for filter UI.
     * POST endpoint to get brand logos/images.
     * 
     * @return List of brand images with URLs
     */
    @POST
    suspend fun getBrandImages(
        @Url url: String  // Full URL: https://www.fashionandfriends.com/rest/V1/brands-info
    ): Response<List<BrandImageDto>>
    
    /**
     * Get product details by barcode or SKU.
     * GET endpoint to find product in store.
     * 
     * @param url Full URL: https://www.fashionandfriends.com/rs/rest/V1/barcode/find/in/store/{barcode_or_base64_sku}
     * @return Product details with store availability
     */
    @GET
    suspend fun getProductDetails(
        @Url url: String  // Full URL: https://www.fashionandfriends.com/rs/rest/V1/barcode/find/in/store/{barcode_or_base64_sku}
    ): Response<List<ProductDetailsResponse>>
    
    // Add more Fashion & Friends API endpoints here as needed
}

