package com.fashiontothem.ff.domain.model

/**
 * F&F Tothem - Product Details Domain Models
 */

data class ProductDetails(
    val id: String,
    val sku: String,
    val type: String,
    val name: String,
    val shortDescription: String?,
    val brandName: String?,
    val options: ProductDetailsOptions?,
    val images: ProductDetailsImages?,
    val prices: ProductDetailsPrices
)

data class ProductDetailsOptions(
    val size: OptionAttribute?,
    val color: OptionAttribute?,
    val colorShade: OptionAttribute?
)

data class OptionAttribute(
    val label: String,
    val attributeId: String,
    val options: List<OptionValue>
)

data class OptionValue(
    val label: String,
    val value: String
)

data class ProductDetailsImages(
    val baseImg: String?,
    val imageList: List<String>
)

data class ProductDetailsPrices(
    val isAdditionalLoyaltyDiscountAllowed: Boolean,
    val parentId: String?,
    val fictional: String?,
    val base: String,
    val special: String?,
    val loyalty: String?,
    val id: String?
)

data class Store(
    val id: String,
    val name: String,
    val image: String?,
    val email: String?,
    val phoneNumber1: String?,
    val phoneNumber2: String?,
    val lat: String?,
    val lng: String?,
    val fax: String?,
    val website: String?,
    val streetAddress: String?,
    val country: String?,
    val zipcode: String?,
    val description: String?,
    val tradingHours: String?,
    val status: String?,
    val storeCode: String?,
    val brandId: String?,
    val city: String?,
    val imageUrl: String?,
    val variants: List<StoreVariant>?
)

data class StoreVariant(
    val itemNo: String,
    val size: String,
    val qty: Int,
    val storeName: String,
    val storeCode: String,
    val shade: String?,
    val superAttribute: SuperAttribute?
)

data class SuperAttribute(
    val size: String?,
    val color: String?,
    val colorShade: String?
)

