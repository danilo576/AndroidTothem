package com.fashiontothem.ff.domain.repository

/**
 * F&F Tothem - Product Repository Interface
 */
interface ProductRepository {
    suspend fun getProductsByCategory(
        token: String,
        categoryId: String,
        categoryLevel: String,
        page: Int
    ): Result<List<com.fashiontothem.ff.domain.model.Product>>
}
