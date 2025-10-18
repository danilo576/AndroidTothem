package com.fashiontothem.ff.data.remote.dto

import com.fashiontothem.ff.domain.model.CountryStore
import com.fashiontothem.ff.domain.model.StoreConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * F&F Tothem - Store Config Response DTO
 */
@JsonClass(generateAdapter = true)
data class StoreConfigResponse(
    @Json(name = "country_code") val countryCode: String,
    @Json(name = "country_name") val countryName: String,
    @Json(name = "storeConfigs") val storeConfigs: List<StoreConfigDto>
) {
    fun toDomain(): CountryStore {
        return CountryStore(
            countryCode = countryCode,
            countryName = countryName,
            stores = storeConfigs.map { it.toDomain() }
        )
    }
}

@JsonClass(generateAdapter = true)
data class StoreConfigDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "code") val code: String,
    @Json(name = "website_id") val websiteId: String,
    @Json(name = "base_url") val baseUrl: String,
    @Json(name = "secure_base_url") val secureBaseUrl: String,
    @Json(name = "base_link_url") val baseLinkUrl: String,
    @Json(name = "secure_base_link_url") val secureBaseLinkUrl: String,
    @Json(name = "base_static_url") val baseStaticUrl: String,
    @Json(name = "secure_base_static_url") val secureBaseStaticUrl: String,
    @Json(name = "base_media_url") val baseMediaUrl: String,
    @Json(name = "secure_base_media_url") val secureBaseMediaUrl: String,
    @Json(name = "chat_enabled") val chatEnabled: Boolean,
    @Json(name = "locale") val locale: String,
    @Json(name = "base_currency_code") val baseCurrencyCode: String,
    @Json(name = "default_display_currency_code") val defaultDisplayCurrencyCode: String,
    @Json(name = "timezone") val timezone: String,
    @Json(name = "athena_search_active") val athenaSearchActive: String,
    @Json(name = "athena_search_website_url") val athenaSearchWebsiteUrl: String,
    @Json(name = "athena_search_wtoken") val athenaSearchWtoken: String,
    @Json(name = "athena_search_access_token") val athenaSearchAccessToken: String
) {
    fun toDomain(): StoreConfig {
        return StoreConfig(
            id = id,
            name = name,
            code = code,
            websiteId = websiteId,
            baseUrl = baseUrl,
            secureBaseUrl = secureBaseUrl,
            baseMediaUrl = baseMediaUrl,
            secureBaseMediaUrl = secureBaseMediaUrl,
            locale = locale,
            baseCurrencyCode = baseCurrencyCode,
            defaultDisplayCurrencyCode = defaultDisplayCurrencyCode,
            timezone = timezone,
            athenaSearchWebsiteUrl = athenaSearchWebsiteUrl,
            athenaSearchWtoken = athenaSearchWtoken,
            athenaSearchAccessToken = athenaSearchAccessToken
        )
    }
}

