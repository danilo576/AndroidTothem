package com.fashiontothem.ff.data.remote

import com.fashiontothem.ff.data.remote.dto.AthenaTokenRequest
import com.fashiontothem.ff.data.remote.dto.AthenaTokenResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * F&F Tothem - Athena Search API Service
 * 
 * Separate service for Athena Search API (different base URL).
 */
interface AthenaApiService {
    
    /**
     * Get OAuth token for Athena Search API.
     * 
     * @param request Token request with client_id and client_secret
     * @return Access token response
     */
    @POST("oauth/token")
    suspend fun getAccessToken(
        @Body request: AthenaTokenRequest
    ): Response<AthenaTokenResponse>
    
    // Add more Athena Search endpoints here as needed
}

