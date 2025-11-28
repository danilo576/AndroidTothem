package com.fashiontothem.ff.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * F&F Tothem - Product Details API Response
 * 
 * Response from GET /rs/rest/V1/barcode/find/in/store/{barcode_or_sku}
 */
@JsonClass(generateAdapter = true)
data class ProductDetailsResponse(
    @Json(name = "productDetails") val productDetails: ProductDetailsDto?,
    @Json(name = "stores") val stores: List<StoreDto>?
)

/**
 * Product Details DTO
 * Note: id, type, and name are optional to handle incomplete responses
 * where only sku is present (retail-only products)
 */
@JsonClass(generateAdapter = true)
data class ProductDetailsDto(
    @Json(name = "id") val id: String?,
    @Json(name = "sku") val sku: String,
    @Json(name = "type") val type: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "short_description") val shortDescription: String?,
    @Json(name = "more_information") val moreInformation: MoreInformationDto?,
    @Json(name = "options") val options: ProductOptionsDto?,
    @Json(name = "images") val images: ProductImagesDto?,
    @Json(name = "prices") val prices: ProductPricesDto?
)

/**
 * More Information DTO (contains additional product details like brand, season, etc.)
 */
@JsonClass(generateAdapter = true)
data class MoreInformationDto(
    @Json(name = "Brend") val brend: String?,
)

/**
 * Product Options (Size, Color, Color Shade, etc.)
 */
@JsonClass(generateAdapter = true)
data class ProductOptionsDto(
    @Json(name = "size") val size: OptionAttributeDto?,
    @Json(name = "color") val color: OptionAttributeDto?,
    @Json(name = "color_shade") val colorShade: OptionAttributeDto?
)

/**
 * Option Attribute (e.g., Size or Color)
 */
@JsonClass(generateAdapter = true)
data class OptionAttributeDto(
    @Json(name = "label") val label: String,
    @Json(name = "attribute_id") val attributeId: String,
    @Json(name = "options") val options: List<OptionValueDto>
)

/**
 * Option Value (e.g., "L", "M", "Pink", "Crna")
 */
@JsonClass(generateAdapter = true)
data class OptionValueDto(
    @Json(name = "label") val label: String,
    @Json(name = "value") val value: String
)

/**
 * Product Images
 */
@JsonClass(generateAdapter = true)
data class ProductImagesDto(
    @Json(name = "base_img") val baseImg: String?,
    @Json(name = "list") val list: List<String>?
)

/**
 * Product Prices
 */
@JsonClass(generateAdapter = true)
data class ProductPricesDto(
    @Json(name = "is_additional_loyalty_discount_allowed") val isAdditionalLoyaltyDiscountAllowed: Boolean?,
    @Json(name = "parent_id") val parentId: String?,
    @Json(name = "fictional") val fictional: String?,
    @Json(name = "base") val base: String?,
    @Json(name = "special") val special: String?,
    @Json(name = "loyalty") val loyalty: String?,
    @Json(name = "id") val id: String?
)

/**
 * Store DTO
 */
@JsonClass(generateAdapter = true)
data class StoreDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "phone_number1") val phoneNumber1: String?,
    @Json(name = "phone_number2") val phoneNumber2: String?,
    @Json(name = "lat") val lat: String?,
    @Json(name = "lng") val lng: String?,
    @Json(name = "fax") val fax: String?,
    @Json(name = "website") val website: String?,
    @Json(name = "street_address") val streetAddress: String?,
    @Json(name = "country") val country: String?,
    @Json(name = "zipcode") val zipcode: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "trading_hours") val tradingHours: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "store_code") val storeCode: String?,
    @Json(name = "brand_id") val brandId: String?,
    @Json(name = "city") val city: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "variants") val variants: List<StoreVariantDto>?
)

/**
 * Store Variant (available sizes/colors in store)
 */
@JsonClass(generateAdapter = true)
data class StoreVariantDto(
    @Json(name = "ItemNo") val itemNo: String,
    @Json(name = "Size") val size: String,
    @Json(name = "Qty") val qty: Int,
    @Json(name = "StoreName") val storeName: String,
    @Json(name = "StoreCode") val storeCode: String,
    @Json(name = "Shade") val shade: String?,
    @Json(name = "SuperAttribute") val superAttribute: SuperAttributeDto?
)

/**
 * Super Attribute (combination of size, color, and color_shade)
 */
@JsonClass(generateAdapter = true)
data class SuperAttributeDto(
    @Json(name = "size") val size: String?,
    @Json(name = "color") val color: String?,
    @Json(name = "color_shade") val colorShade: String?
)

/**
 * Extension functions to convert DTOs to domain models
 */
fun ProductDetailsResponse.toDomain(): com.fashiontothem.ff.domain.model.ProductDetails? {
    val product = productDetails ?: return null
    
    // Check if this is a retail-only product (incomplete product details)
    val isRetailOnly = product.id == null || product.name == null || product.type == null
    
    // If retail-only, extract options from variants
    val options = if (isRetailOnly && product.options == null) {
        extractOptionsFromVariants(this.stores)
    } else {
        product.options?.toDomain()
    }
    
    // For retail-only products, use default values for missing fields
    val defaultPrices = ProductPricesDto(
        isAdditionalLoyaltyDiscountAllowed = false,
        parentId = null,
        fictional = null,
        base = "0",
        special = null,
        loyalty = null,
        id = null
    )
    val prices = product.prices ?: defaultPrices
    
    return com.fashiontothem.ff.domain.model.ProductDetails(
        id = product.id ?: product.sku, // Use SKU as fallback for ID
        sku = product.sku,
        type = product.type ?: "simple", // Default to simple for retail-only
        name = product.name ?: product.sku, // Use SKU as fallback for name
        shortDescription = product.shortDescription,
        brandName = product.moreInformation?.brend,
        options = options,
        images = product.images?.toDomain() ?: com.fashiontothem.ff.domain.model.ProductDetailsImages(
            baseImg = null,
            imageList = emptyList()
        ),
        prices = prices.toDomain(),
        isRetailOnly = isRetailOnly
    )
}

/**
 * Extract product options (size, color, shade) from store variants
 * This is used when product details are incomplete (retail-only products)
 */
private fun extractOptionsFromVariants(stores: List<StoreDto>?): com.fashiontothem.ff.domain.model.ProductDetailsOptions? {
    if (stores.isNullOrEmpty()) return null
    
    // Collect all unique sizes, colors, and shades from variants
    val sizes = mutableSetOf<String>()
    val colors = mutableSetOf<String>()
    val shades = mutableSetOf<String>()
    
    stores.forEach { store ->
        store.variants?.forEach { variant ->
            // Check SuperAttribute first, then fallback to direct fields
            val size = variant.superAttribute?.size ?: variant.size
            val color = variant.superAttribute?.color
            val shade = variant.superAttribute?.colorShade ?: variant.shade
            
            size?.takeIf { it.isNotBlank() }?.let { sizes.add(it) }
            color?.takeIf { it.isNotBlank() }?.let { colors.add(it) }
            shade?.takeIf { it.isNotBlank() }?.let { shades.add(it) }
        }
    }
    
    // Build options only if we have data
    val sizeOptions = if (sizes.isNotEmpty()) {
        com.fashiontothem.ff.domain.model.OptionAttribute(
            label = "Veličina",
            attributeId = "242", // Default size attribute ID
            options = sizes.sorted().map { size ->
                com.fashiontothem.ff.domain.model.OptionValue(
                    label = size,
                    value = size // Use size as both label and value for retail-only
                )
            }
        )
    } else null
    
    val colorOptions = if (colors.isNotEmpty()) {
        com.fashiontothem.ff.domain.model.OptionAttribute(
            label = "Boja",
            attributeId = "93", // Default color attribute ID
            options = colors.sorted().map { color ->
                com.fashiontothem.ff.domain.model.OptionValue(
                    label = color,
                    value = color
                )
            }
        )
    } else null
    
    val shadeOptions = if (shades.isNotEmpty()) {
        com.fashiontothem.ff.domain.model.OptionAttribute(
            label = "Nijansa",
            attributeId = "180", // Default shade attribute ID
            options = shades.sorted().map { shade ->
                com.fashiontothem.ff.domain.model.OptionValue(
                    label = shade,
                    value = shade
                )
            }
        )
    } else null
    
    return if (sizeOptions != null || colorOptions != null || shadeOptions != null) {
        com.fashiontothem.ff.domain.model.ProductDetailsOptions(
            size = sizeOptions,
            color = colorOptions,
            colorShade = shadeOptions
        )
    } else null
}

fun ProductOptionsDto.toDomain(): com.fashiontothem.ff.domain.model.ProductDetailsOptions? {
    return com.fashiontothem.ff.domain.model.ProductDetailsOptions(
        size = size?.toDomain(),
        color = color?.toDomain(),
        colorShade = colorShade?.toDomain()
    )
}

fun OptionAttributeDto.toDomain(): com.fashiontothem.ff.domain.model.OptionAttribute {
    return com.fashiontothem.ff.domain.model.OptionAttribute(
        label = label,
        attributeId = attributeId,
        options = options.map { it.toDomain() }
    )
}

fun OptionValueDto.toDomain(): com.fashiontothem.ff.domain.model.OptionValue {
    return com.fashiontothem.ff.domain.model.OptionValue(
        label = label,
        value = value
    )
}

fun ProductImagesDto.toDomain(): com.fashiontothem.ff.domain.model.ProductDetailsImages {
    return com.fashiontothem.ff.domain.model.ProductDetailsImages(
        baseImg = baseImg,
        imageList = list ?: emptyList()
    )
}

fun ProductPricesDto.toDomain(): com.fashiontothem.ff.domain.model.ProductDetailsPrices {
    return com.fashiontothem.ff.domain.model.ProductDetailsPrices(
        isAdditionalLoyaltyDiscountAllowed = isAdditionalLoyaltyDiscountAllowed ?: false,
        parentId = parentId,
        fictional = fictional,
        base = base ?: "0",
        special = special,
        loyalty = loyalty,
        id = id
    )
}

fun StoreDto.toDomain(): com.fashiontothem.ff.domain.model.Store {
    return com.fashiontothem.ff.domain.model.Store(
        id = id,
        name = name,
        image = image,
        email = email,
        phoneNumber1 = phoneNumber1,
        phoneNumber2 = phoneNumber2,
        lat = lat,
        lng = lng,
        fax = fax,
        website = website,
        streetAddress = streetAddress,
        country = country,
        zipcode = zipcode,
        description = description,
        tradingHours = tradingHours,
        status = status,
        storeCode = storeCode,
        brandId = brandId,
        city = city,
        imageUrl = imageUrl,
        variants = variants?.map { it.toDomain() }
    )
}

fun StoreVariantDto.toDomain(): com.fashiontothem.ff.domain.model.StoreVariant {
    return com.fashiontothem.ff.domain.model.StoreVariant(
        itemNo = itemNo,
        size = size,
        qty = qty,
        storeName = storeName,
        storeCode = storeCode,
        shade = shade,
        superAttribute = superAttribute?.toDomain()
    )
}

fun SuperAttributeDto.toDomain(): com.fashiontothem.ff.domain.model.SuperAttribute {
    return com.fashiontothem.ff.domain.model.SuperAttribute(
        size = size,
        color = color,
        colorShade = colorShade
    )
}

/**
 * Guest Product Details DTO
 * Response from GET /{country_code}/rest/V1/guest-product/{sku}/details
 * This is a direct list response (not wrapped in productDetails)
 */
@JsonClass(generateAdapter = true)
data class GuestProductDetailsDto(
    @Json(name = "id") val id: String?,
    @Json(name = "sku") val sku: String,
    @Json(name = "type") val type: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "short_description") val shortDescription: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "model_code") val modelCode: String?,
    @Json(name = "more_information") val moreInformation: MoreInformationDto?,
    @Json(name = "options") val options: ProductOptionsDto?,
    @Json(name = "images") val images: ProductImagesDto?,
    @Json(name = "prices") val prices: ProductPricesDto?,
    @Json(name = "item_category") val itemCategory: List<String>?
)

/**
 * Extension function to convert GuestProductDetailsDto to domain model
 */
fun GuestProductDetailsDto.toDomain(): com.fashiontothem.ff.domain.model.ProductDetails {
    val prices = this.prices ?: ProductPricesDto(
        isAdditionalLoyaltyDiscountAllowed = false,
        parentId = null,
        fictional = null,
        base = "0",
        special = null,
        loyalty = null,
        id = null
    )
    
    return com.fashiontothem.ff.domain.model.ProductDetails(
        id = id ?: sku,
        sku = sku,
        type = type ?: "simple",
        name = name ?: sku,
        shortDescription = shortDescription,
        brandName = moreInformation?.brend,
        options = options?.toDomain(),
        images = images?.toDomain() ?: com.fashiontothem.ff.domain.model.ProductDetailsImages(
            baseImg = null,
            imageList = emptyList()
        ),
        prices = prices.toDomain(),
        isRetailOnly = false
    )
}

