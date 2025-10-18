package com.fashiontothem.ff.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * F&F Tothem - Athena Token Response
 */
@JsonClass(generateAdapter = true)
data class AthenaTokenResponse(
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Long,  // Seconds until expiration
    @Json(name = "access_token") val accessToken: String
)

/**
 * Request body for Athena token endpoint
 */
@JsonClass(generateAdapter = true)
data class AthenaTokenRequest(
    @Json(name = "client_id") val clientId: String,
    @Json(name = "client_secret") val clientSecret: String,
    @Json(name = "grant_type") val grantType: String = "client_credentials",
    @Json(name = "scope") val scope: String = "*"
)

