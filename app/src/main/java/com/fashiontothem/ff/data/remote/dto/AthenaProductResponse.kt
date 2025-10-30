package com.fashiontothem.ff.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * F&F Tothem - Athena Product API Response DTOs
 */

@JsonClass(generateAdapter = true)
data class AthenaProductResponse(
    @Json(name = "data") val data: AthenaProductData
)

@JsonClass(generateAdapter = true)
data class AthenaProductData(
    @Json(name = "products") val products: AthenaProducts,
    @Json(name = "image_cache") val imageCache: String? = null // For visual search
)

@JsonClass(generateAdapter = true)
data class AthenaProducts(
    @Json(name = "results") val results: List<AthenaProductDto>,
    @Json(name = "amounts") val amounts: AthenaAmounts?,
    @Json(name = "filters") val filters: List<AthenaFilter>?,
    @Json(name = "active_filters") val activeFilters: List<AthenaActiveFilter>?
)

@JsonClass(generateAdapter = true)
data class AthenaAmounts(
    @Json(name = "current_page") val currentPage: Int,
    @Json(name = "last_page") val lastPage: Int,
    @Json(name = "next_page") val nextPage: Int?,
    @Json(name = "total") val total: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class AthenaProductDto(
    @Json(name = "id") val id: Int,
    @Json(name = "sku") val sku: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "short_description") val shortDescription: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "hover_image") val hoverImage: String?,
    @Json(name = "link") val link: String?,
    @Json(name = "price") val price: AthenaProductPrice?,
    @Json(name = "brand") val brand: AthenaProductBrand?,
    @Json(name = "availability") val availability: Int?,
    @Json(name = "salable_qty") val salableQty: Int?,
    @Json(name = "discount_percentage") val discountPercentage: Int?,
    @Json(name = "configurable_options") val configurableOptions: List<AthenaConfigurableOption>?,
    @Json(name = "category_names") val categoryNames: List<String>?,
    @Json(name = "attributes") val attributes: List<AthenaProductAttribute>?,
    @Json(name = "child_products") val childProducts: List<AthenaChildProduct>?,
    @Json(name = "gallery_images") val galleryImages: List<String>?,
    @Json(name = "product_combinations") val productCombinations: List<AthenaProductCombination>?,
    @Json(name = "total_reviews") val totalReviews: Int?,
    @Json(name = "views") val views: Int?,
    @Json(name = "product_score") val productScore: Int?,
    @Json(name = "product_type_id") val productTypeId: String?,
    @Json(name = "meta_title") val metaTitle: String?,
    @Json(name = "meta_description") val metaDescription: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "category_ids") val categoryIds: List<Int>?
)

@JsonClass(generateAdapter = true)
data class AthenaProductPrice(
    @Json(name = "regular_price") val regularPrice: Double?,
    @Json(name = "regular_price_with_currency") val regularPriceWithCurrency: String?,
    @Json(name = "special_price") val specialPrice: Double?,
    @Json(name = "special_price_with_currency") val specialPriceWithCurrency: String?,
    @Json(name = "loyalty_price") val loyaltyPrice: Double?,
    @Json(name = "loyalty_price_with_currency") val loyaltyPriceWithCurrency: String?,
    @Json(name = "discount_percentage") val discountPercentage: Int?,
    @Json(name = "discount_value") val discountValue: Double?,
    @Json(name = "best_month_price") val bestMonthPrice: Double?,
    @Json(name = "best_month_price_with_currency") val bestMonthPriceWithCurrency: String?,
    @Json(name = "customer_group_id") val customerGroupId: Int?
)

@JsonClass(generateAdapter = true)
data class AthenaProductBrand(
    @Json(name = "id") val id: Int,
    @Json(name = "label") val label: String,
    @Json(name = "option_id") val optionId: String,
    @Json(name = "attribute_code") val attributeCode: String
)

@JsonClass(generateAdapter = true)
data class AthenaConfigurableOption(
    @Json(name = "attribute_id") val attributeId: Int,
    @Json(name = "attribute_code") val attributeCode: String,
    @Json(name = "options") val options: List<AthenaOption>
)

@JsonClass(generateAdapter = true)
data class AthenaOption(
    @Json(name = "option_id") val optionId: String,
    @Json(name = "option_label") val optionLabel: String,
    @Json(name = "option_type") val optionType: String,
    @Json(name = "hash_code") val hashCode: String?,
    @Json(name = "seo_value") val seoValue: String
)

@JsonClass(generateAdapter = true)
data class AthenaProductAttribute(
    @Json(name = "name") val name: String,
    @Json(name = "label") val label: String,
    @Json(name = "value") val value: String,
    @Json(name = "seo_url") val seoUrl: String,
    @Json(name = "hash_code") val hashCode: String?,
    @Json(name = "original_value") val originalValue: String,
    @Json(name = "parent") val parent: String?,
    @Json(name = "original_parent") val originalParent: String?
)

@JsonClass(generateAdapter = true)
data class AthenaChildProduct(
    @Json(name = "entity_id") val entityId: String,
    @Json(name = "sku") val sku: String,
    @Json(name = "image") val image: String,
    @Json(name = "hover_image") val hoverImage: String,
    @Json(name = "stock_status") val stockStatus: Boolean,
    @Json(name = "color") val color: String,
    @Json(name = "size") val size: String,
    @Json(name = "configurable_options") val configurableOptions: List<AthenaChildProductOption>
)

@JsonClass(generateAdapter = true)
data class AthenaChildProductOption(
    @Json(name = "type") val type: String,
    @Json(name = "value") val value: String
)

@JsonClass(generateAdapter = true)
data class AthenaProductCombination(
    @Json(name = "color") val color: String,
    @Json(name = "size") val size: String
)

@JsonClass(generateAdapter = true)
data class AthenaCategoryRequest(
    @Json(name = "token") val token: String,
    @Json(name = "category") val category: String,
    @Json(name = "level") val level: String,
    @Json(name = "customer_group_id") val customerGroupId: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "color") val color: String? = null,
    @Json(name = "size") val size: String? = null,
    @Json(name = "brand") val brand: String? = null,
    @Json(name = "category2") val category2: String? = null
)

@JsonClass(generateAdapter = true)
data class AthenaVisualSearchRequest(
    @Json(name = "token") val token: String,
    @Json(name = "image") val image: String, // Base64 for page 1, image_cache for page > 1
    @Json(name = "customer_group_id") val customerGroupId: Int,
    @Json(name = "page") val page: Int
)

// Filter Options DTOs
@JsonClass(generateAdapter = true)
data class AthenaFilter(
    @Json(name = "title") val title: String,
    @Json(name = "type") val type: String,
    @Json(name = "array") val array: List<AthenaFilterOption>?
)

@JsonClass(generateAdapter = true)
data class AthenaFilterOption(
    @Json(name = "option_value") val optionValue: String,
    @Json(name = "option_key") val optionKey: String,
    @Json(name = "option_id") val optionId: String?,
    @Json(name = "option_label") val optionLabel: String,
    @Json(name = "count") val count: Int?,
    @Json(name = "type_id") val typeId: String?,
    @Json(name = "hax_code") val haxCode: String?
)

@JsonClass(generateAdapter = true)
data class AthenaActiveFilter(
    @Json(name = "name") val name: String?,
    @Json(name = "id") val id: String,
    @Json(name = "label") val label: String?,
    @Json(name = "type") val type: String,
    @Json(name = "url") val url: String?,
    @Json(name = "url_path") val urlPath: String?,
    @Json(name = "url_params") val urlParams: Map<String, String>?
)
