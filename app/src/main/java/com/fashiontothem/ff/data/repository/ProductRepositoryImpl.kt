package com.fashiontothem.ff.data.repository

import com.fashiontothem.ff.data.remote.AthenaApiService
import com.fashiontothem.ff.data.remote.dto.AthenaCategoryRequest
import com.fashiontothem.ff.data.remote.dto.AthenaChildProduct
import com.fashiontothem.ff.data.remote.dto.AthenaChildProductOption
import com.fashiontothem.ff.data.remote.dto.AthenaConfigurableOption
import com.fashiontothem.ff.data.remote.dto.AthenaOption
import com.fashiontothem.ff.data.remote.dto.AthenaProductAttribute
import com.fashiontothem.ff.data.remote.dto.AthenaProductBrand
import com.fashiontothem.ff.data.remote.dto.AthenaProductCombination
import com.fashiontothem.ff.data.remote.dto.AthenaProductDto
import com.fashiontothem.ff.data.remote.dto.AthenaProductPrice
import com.fashiontothem.ff.domain.model.ChildProduct
import com.fashiontothem.ff.domain.model.ChildProductOption
import com.fashiontothem.ff.domain.model.ConfigurableOption
import com.fashiontothem.ff.domain.model.Option
import com.fashiontothem.ff.domain.model.Product
import com.fashiontothem.ff.domain.model.ProductAttribute
import com.fashiontothem.ff.domain.model.ProductBrand
import com.fashiontothem.ff.domain.model.ProductCombination
import com.fashiontothem.ff.domain.model.ProductPrice
import com.fashiontothem.ff.domain.repository.ProductPageResult
import com.fashiontothem.ff.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Product Repository Implementation
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val athenaApiService: AthenaApiService
) : ProductRepository {

    override suspend fun getProductsByCategory(
        token: String,
        categoryId: String,
        categoryLevel: String,
        page: Int
    ): Result<ProductPageResult> {
        return try {
            val request = AthenaCategoryRequest(
                token = token,
                category = categoryId,
                level = categoryLevel,
                customerGroupId = 0,
                page = page
            )
            
            val response = athenaApiService.getProductsByCategory(request)
            
            if (response.isSuccessful) {
                val productsData = response.body()?.data?.products
                val products = productsData?.results?.map { it.toDomain() } ?: emptyList()
                val amounts = productsData?.amounts
                
                val result = ProductPageResult(
                    products = products,
                    hasNextPage = amounts?.nextPage != null || (amounts != null && amounts.currentPage < amounts.lastPage),
                    currentPage = amounts?.currentPage ?: page,
                    lastPage = amounts?.lastPage ?: page,
                    totalProducts = amounts?.total ?: 0
                )
                
                Result.success(result)
            } else {
                Result.failure(Exception("Failed to fetch products: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension function to convert DTO to Domain model
 * All fields are now nullable to prevent crashes from API inconsistencies
 */
private fun AthenaProductDto.toDomain(): Product {
    return Product(
        id = id,
        sku = sku,
        name = name,
        shortDescription = shortDescription,
        description = description,
        imageUrl = image,
        hoverImageUrl = hoverImage,
        link = link,
        price = price?.toDomain(),
        brand = brand?.toDomain(),
        availability = availability,
        salableQty = salableQty,
        discountPercentage = discountPercentage,
        configurableOptions = configurableOptions?.map { it.toDomain() },
        categoryNames = categoryNames,
        attributes = attributes?.map { it.toDomain() },
        childProducts = childProducts?.map { it.toDomain() },
        galleryImages = galleryImages,
        productCombinations = productCombinations?.map { it.toDomain() },
        totalReviews = totalReviews,
        views = views,
        productScore = productScore,
        productTypeId = productTypeId,
        metaTitle = metaTitle,
        metaDescription = metaDescription,
        type = type,
        categoryIds = categoryIds
    )
}

private fun AthenaProductPrice.toDomain(): ProductPrice {
    return ProductPrice(
        regularPrice = regularPrice,
        regularPriceWithCurrency = regularPriceWithCurrency,
        specialPrice = specialPrice,
        specialPriceWithCurrency = specialPriceWithCurrency,
        loyaltyPrice = loyaltyPrice,
        loyaltyPriceWithCurrency = loyaltyPriceWithCurrency,
        discountPercentage = discountPercentage,
        discountValue = discountValue,
        bestMonthPrice = bestMonthPrice,
        bestMonthPriceWithCurrency = bestMonthPriceWithCurrency,
        customerGroupId = customerGroupId
    )
}

private fun AthenaProductBrand.toDomain(): ProductBrand {
    return ProductBrand(
        id = id,
        label = label,
        optionId = optionId,
        attributeCode = attributeCode
    )
}

private fun AthenaConfigurableOption.toDomain(): ConfigurableOption {
    return ConfigurableOption(
        attributeId = attributeId,
        attributeCode = attributeCode,
        options = options.map { it.toDomain() }
    )
}

private fun AthenaOption.toDomain(): Option {
    return Option(
        optionId = optionId,
        optionLabel = optionLabel,
        optionType = optionType,
        hashCode = hashCode,
        seoValue = seoValue
    )
}

private fun AthenaProductAttribute.toDomain(): ProductAttribute {
    return ProductAttribute(
        name = name,
        label = label,
        value = value,
        seoUrl = seoUrl,
        hashCode = hashCode,
        originalValue = originalValue,
        parent = parent,
        originalParent = originalParent
    )
}

private fun AthenaChildProduct.toDomain(): ChildProduct {
    return ChildProduct(
        entityId = entityId,
        sku = sku,
        imageUrl = image,
        hoverImageUrl = hoverImage,
        stockStatus = stockStatus,
        color = color,
        size = size,
        configurableOptions = configurableOptions.map { it.toDomain() }
    )
}

private fun AthenaChildProductOption.toDomain(): ChildProductOption {
    return ChildProductOption(
        type = type,
        value = value
    )
}

private fun AthenaProductCombination.toDomain(): ProductCombination {
    return ProductCombination(
        color = color,
        size = size
    )
}
