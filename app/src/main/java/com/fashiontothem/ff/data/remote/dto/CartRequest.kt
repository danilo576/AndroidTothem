package com.fashiontothem.ff.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * F&F Tothem - Cart Request DTOs for adding items to cart
 */

@JsonClass(generateAdapter = true)
data class CartRequest(
    @Json(name = "cartData") val cartData: CartData,
)

@JsonClass(generateAdapter = true)
data class CartData(
    @Json(name = "loyalty_card_number") val loyaltyCardNumber: String,
    @Json(name = "cartItem") val cartItem: CartItem,
)

@JsonClass(generateAdapter = true)
data class CartItem(
    @Json(name = "product_option") val productOption: ProductOption,
    @Json(name = "qty") val quantity: Int,
    @Json(name = "sku") val sku: String,
)

@JsonClass(generateAdapter = true)
data class ProductOption(
    @Json(name = "extension_attributes") val extensionAttributes: ExtensionAttributes,
)

@JsonClass(generateAdapter = true)
data class ExtensionAttributes(
    @Json(name = "configurable_item_options") val configurableItemOptions: List<ConfigurableItemOption>,
)

@JsonClass(generateAdapter = true)
data class ConfigurableItemOption(
    @Json(name = "option_id") val optionId: String,
    @Json(name = "option_value") val optionValue: Int,
)

