package com.fashiontothem.ff.domain.model

/**
 * F&F Tothem - Product Domain Models
 */

data class Product(
    val id: Int,
    val sku: String?,
    val name: String?,
    val shortDescription: String?,
    val description: String?,
    val imageUrl: String?,
    val hoverImageUrl: String?,
    val link: String?,
    val price: ProductPrice?,
    val brand: ProductBrand?,
    val availability: Int?,
    val salableQty: Int?,
    val discountPercentage: Int?,
    val configurableOptions: List<ConfigurableOption>?,
    val categoryNames: List<String>?,
    val attributes: List<ProductAttribute>?,
    val childProducts: List<ChildProduct>?,
    val galleryImages: List<String>?,
    val productCombinations: List<ProductCombination>?,
    val totalReviews: Int?,
    val views: Int?,
    val productScore: Int?,
    val productTypeId: String?,
    val metaTitle: String?,
    val metaDescription: String?,
    val type: String?,
    val categoryIds: List<Int>?
)

data class ProductPrice(
    val regularPrice: Double?,
    val regularPriceWithCurrency: String?,
    val specialPrice: Double?,
    val specialPriceWithCurrency: String?,
    val loyaltyPrice: Double?,
    val loyaltyPriceWithCurrency: String?,
    val discountPercentage: Int?,
    val discountValue: Double?,
    val bestMonthPrice: Double?,
    val bestMonthPriceWithCurrency: String?,
    val customerGroupId: Int?
)

data class ProductBrand(
    val id: Int,
    val label: String,
    val optionId: String,
    val attributeCode: String
)

data class ConfigurableOption(
    val attributeId: Int,
    val attributeCode: String,
    val options: List<Option>
)

data class Option(
    val optionId: String,
    val optionLabel: String,
    val optionType: String,
    val hashCode: String?,
    val seoValue: String
)

data class ProductAttribute(
    val name: String,
    val label: String,
    val value: String,
    val seoUrl: String,
    val hashCode: String?,
    val originalValue: String,
    val parent: String?,
    val originalParent: String?
)

data class ChildProduct(
    val entityId: String,
    val sku: String,
    val imageUrl: String,
    val hoverImageUrl: String,
    val stockStatus: Boolean,
    val color: String,
    val size: String,
    val configurableOptions: List<ChildProductOption>
)

data class ChildProductOption(
    val type: String,
    val value: String
)

data class ProductCombination(
    val color: String,
    val size: String
)
