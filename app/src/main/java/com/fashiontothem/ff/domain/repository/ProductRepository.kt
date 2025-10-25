package com.fashiontothem.ff.domain.repository

import com.fashiontothem.ff.domain.model.Product

/**
 * F&F Tothem - Product Repository Interface
 */
interface ProductRepository {
    suspend fun getProductsByCategory(
        token: String,
        categoryId: String,
        categoryLevel: String,
        page: Int
    ): Result<ProductPageResult>
    
    suspend fun getProductsByVisualSearch(
        token: String,
        image: String, // Base64 for page 1, image_cache for page > 1
        page: Int
    ): Result<ProductPageResult>
}

/**
 * Result of paginated product query
 */
data class ProductPageResult(
    val products: List<Product>,
    val hasNextPage: Boolean,
    val currentPage: Int,
    val lastPage: Int,
    val totalProducts: Int,
    val imageCache: String? = null // For visual search pagination
)
