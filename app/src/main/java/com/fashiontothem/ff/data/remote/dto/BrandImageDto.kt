package com.fashiontothem.ff.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BrandImageDto(
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "option_value") val optionValue: String?,
    @Json(name = "option_label") val optionLabel: String?
)

fun BrandImageDto.toDomain(): com.fashiontothem.ff.domain.model.BrandImage? {
    // Only return valid brand images with all required fields
    return if (!imageUrl.isNullOrEmpty() && !optionValue.isNullOrEmpty() && !optionLabel.isNullOrEmpty()) {
        com.fashiontothem.ff.domain.model.BrandImage(
            imageUrl = imageUrl,
            optionValue = optionValue,
            optionLabel = optionLabel
        )
    } else {
        null
    }
}

