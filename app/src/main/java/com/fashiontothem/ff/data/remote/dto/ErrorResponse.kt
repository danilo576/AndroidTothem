package com.fashiontothem.ff.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * F&F Tothem - Error Response DTO for API error messages
 */
@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "message") val message: String?,
)

