package com.fashiontothem.ff.domain.model

/**
 * F&F Tothem - Store Configuration Models
 */
data class CountryStore(
    val countryCode: String,
    val countryName: String,
    val stores: List<StoreConfig>
)

data class StoreConfig(
    val id: String,
    val name: String,
    val code: String,
    val websiteId: String,
    val baseUrl: String,
    val secureBaseUrl: String,
    val baseMediaUrl: String,
    val secureBaseMediaUrl: String,
    val locale: String,
    val baseCurrencyCode: String,
    val defaultDisplayCurrencyCode: String,
    val timezone: String
)

/**
 * Helper to get flag emoji for country code
 */
fun CountryStore.getFlagEmoji(): String {
    return when (countryCode) {
        "RS" -> "🇷🇸"
        "BA" -> "🇧🇦"
        "ME" -> "🇲🇪"
        "HR" -> "🇭🇷"
        else -> "🌍"
    }
}

