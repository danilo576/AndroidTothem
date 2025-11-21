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
        page: Int,
        filters: ProductFilters? = null,
        filterOptions: com.fashiontothem.ff.domain.model.FilterOptions? = null, // Pass previous filter options for param names
        activeFilters: Map<String, Set<String>> = emptyMap(), // Pass previous active filters for category level tracking
        preferConsolidatedCategories: Boolean = false // Prefer 'kategorije' group for category tab
    ): Result<ProductPageResult>
    
    suspend fun getProductsByVisualSearch(
        token: String,
        image: String, // Base64 for page 1, image_cache for page > 1
        page: Int,
        filters: ProductFilters? = null,
        filterOptions: com.fashiontothem.ff.domain.model.FilterOptions? = null,
        activeFilters: Map<String, Set<String>> = emptyMap() // Pass previous active filters for category level tracking
    ): Result<ProductPageResult>
    
    suspend fun getBrandImages(): Result<List<com.fashiontothem.ff.domain.model.BrandImage>>
    
    /**
     * Get product details by barcode or SKU.
     * 
     * @param barcodeOrSku Barcode (e.g., "8057338370499") or base64 encoded SKU
     * @return Result containing product details and stores, or error
     */
    suspend fun getProductDetails(barcodeOrSku: String): Result<ProductDetailsResult>
    
    /**
     * Add product to cart with loyalty card.
     * 
     * @param loyaltyScannedBarcode Scanned loyalty card barcode (will be sanitized)
     * @param sku Product SKU
     * @param sizeAttributeId Size attribute ID (from productDetails.options.size.attributeId)
     * @param sizeOptionValue Size option value (from productDetails.options.size.options[selected].value)
     * @param colorAttributeId Color attribute ID (from productDetails.options.colorShade.attributeId, if exists)
     * @param colorOptionValue Color option value (from productDetails.options.colorShade.options[selected].value, if exists)
     * @return Result<Boolean> - true if successful, error otherwise
     */
    suspend fun addToCart(
        loyaltyScannedBarcode: String,
        sku: String,
        sizeAttributeId: String,
        sizeOptionValue: String,
        colorAttributeId: String,
        colorOptionValue: String,
    ): Result<Boolean>
}

/**
 * Product details result
 */
data class ProductDetailsResult(
    val productDetails: com.fashiontothem.ff.domain.model.ProductDetails,
    val stores: List<com.fashiontothem.ff.domain.model.Store>
)

/**
 * Product filter parameters
 */
data class ProductFilters(
    val genders: Set<String> = emptySet(),
    val categories: Set<String> = emptySet(),
    val brands: Set<String> = emptySet(),
    val sizes: Set<String> = emptySet(),
    val colors: Set<String> = emptySet()
) {
    fun isEmpty(): Boolean = genders.isEmpty() && categories.isEmpty() && brands.isEmpty() && sizes.isEmpty() && colors.isEmpty()
    
    // Convert to API params using option_key from API response
    fun toApiParams(
        filterOptions: com.fashiontothem.ff.domain.model.FilterOptions?,
        activeFilters: Map<String, Set<String>> = emptyMap()
    ): Map<String, String> {
        val params = mutableMapOf<String, String>()
        
        // Use option_key from API response if available, otherwise fall back to defaults
        if (genders.isNotEmpty()) {
            val paramName = filterOptions?.genderParamName ?: "gender"
            params[paramName] = genders.joinToString("_")
        }
        if (colors.isNotEmpty()) {
            val paramName = filterOptions?.colorParamName ?: "color"
            params[paramName] = colors.joinToString("_")
        }
        if (sizes.isNotEmpty()) {
            val paramName = filterOptions?.sizeParamName ?: "size"
            params[paramName] = sizes.joinToString("_")
        }
        if (brands.isNotEmpty()) {
            val paramName = filterOptions?.brandParamName ?: "brand"
            params[paramName] = brands.joinToString("_")
        }
        if (categories.isNotEmpty()) {
            // ✅ Map each category to its correct level
            // Strategy: Use active filters to find existing category levels
            val categoriesByLevel = mutableMapOf<String, MutableSet<String>>()
            
            categories.forEach { category ->
                // Find level for this category from activeFilters
                val existingLevel = activeFilters.keys
                    .filter { it.startsWith("category") }
                    .find { activeFilters[it]?.contains(category) == true }
                
                if (existingLevel != null) {
                    // This category is already active, use its level
                    categoriesByLevel.getOrPut(existingLevel) { mutableSetOf() }.add(category)
                } else {
                    // This is a new category, use categoryParamName from filterOptions
                    val defaultLevel = filterOptions?.categoryParamName ?: "category2"
                    categoriesByLevel.getOrPut(defaultLevel) { mutableSetOf() }.add(category)
                }
            }
            
            // Add params for each level
            categoriesByLevel.forEach { (level, values) ->
                params[level] = values.joinToString("_")
            }
        }
        
        return params
    }
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
    val imageCache: String? = null, // For visual search pagination
    val filterOptions: com.fashiontothem.ff.domain.model.FilterOptions? = null, // Available filter options from API
    val activeFilters: Map<String, Set<String>> = emptyMap(), // Currently active filters from API (type -> ids)
    val isEmpty: Boolean = false, // True if category exists but has no products
    val categoryNotFound: Boolean = false, // True if category doesn't exist
    val errorMessage: String? = null // Error message from API (e.g., "Category doesn't exist.")
)
