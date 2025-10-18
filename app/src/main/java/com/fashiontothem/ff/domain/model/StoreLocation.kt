package com.fashiontothem.ff.domain.model

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * F&F Tothem - Store Location Model
 */
data class StoreLocation(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val email: String?,
    val phoneNumber1: String?,
    val phoneNumber2: String?,
    val latitude: Double?,
    val longitude: Double?,
    val streetAddress: String,
    val country: String,
    val zipcode: String?,
    val city: String,
    val tradingHours: String?,
    val isActive: Boolean
)

/**
 * Calculate distance between two coordinates in kilometers.
 */
fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val earthRadius = 6371.0 // km
    
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)
    val lon1Rad = Math.toRadians(lon1)
    val lon2Rad = Math.toRadians(lon2)
    
    val angle = acos(
        sin(lat1Rad) * sin(lat2Rad) +
        cos(lat1Rad) * cos(lat2Rad) * cos(lon2Rad - lon1Rad)
    )
    
    return earthRadius * angle
}

/**
 * Find nearest store to given coordinates.
 */
fun List<StoreLocation>.findNearest(latitude: Double, longitude: Double): StoreLocation? {
    return this
        .filter { it.latitude != null && it.longitude != null }
        .minByOrNull { store ->
            calculateDistance(latitude, longitude, store.latitude!!, store.longitude!!)
        }
}

/**
 * Group stores by city.
 */
fun List<StoreLocation>.groupByCity(): Map<String, List<StoreLocation>> {
    return this
        .filter { it.isActive }  // Only active stores
        .groupBy { it.city }
        .toSortedMap()  // Sort cities alphabetically
}

