package com.fashiontothem.ff.data.remote.dto

import com.fashiontothem.ff.domain.model.StoreLocation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * F&F Tothem - Store Location Response DTO
 */
@JsonClass(generateAdapter = true)
data class StoreLocationDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "phone_number1") val phoneNumber1: String?,
    @Json(name = "phone_number2") val phoneNumber2: String?,
    @Json(name = "lat") val lat: String?,
    @Json(name = "lng") val lng: String?,
    @Json(name = "street_address") val streetAddress: String?,
    @Json(name = "country") val country: String,
    @Json(name = "zipcode") val zipcode: String?,
    @Json(name = "city") val city: String?,
    @Json(name = "trading_hours") val tradingHours: String?,
    @Json(name = "status") val status: String
) {
    fun toDomain(baseMediaUrl: String): StoreLocation {
        return StoreLocation(
            id = id,
            name = name,
            imageUrl = if (!image.isNullOrEmpty()) "$baseMediaUrl$image" else null,
            email = email,
            phoneNumber1 = phoneNumber1,
            phoneNumber2 = phoneNumber2,
            latitude = lat?.toDoubleOrNull(),
            longitude = lng?.toDoubleOrNull(),
            streetAddress = streetAddress ?: "",
            country = country,
            zipcode = zipcode,
            city = city ?: "",
            tradingHours = tradingHours,
            isActive = status == "1"
        )
    }
}

