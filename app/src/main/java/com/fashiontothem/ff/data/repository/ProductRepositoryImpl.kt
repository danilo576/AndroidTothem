package com.fashiontothem.ff.data.repository

import com.fashiontothem.ff.data.cache.BrandImageCache
import com.fashiontothem.ff.data.remote.AthenaApiService
import com.fashiontothem.ff.data.remote.dto.AthenaChildProduct
import com.fashiontothem.ff.data.remote.dto.AthenaChildProductOption
import com.fashiontothem.ff.data.remote.dto.AthenaConfigurableOption
import com.fashiontothem.ff.data.remote.dto.AthenaOption
import com.fashiontothem.ff.data.remote.dto.AthenaProductAttribute
import com.fashiontothem.ff.data.remote.dto.AthenaProductBrand
import com.fashiontothem.ff.data.remote.dto.AthenaProductCombination
import com.fashiontothem.ff.data.remote.dto.AthenaProductDto
import com.fashiontothem.ff.data.remote.dto.AthenaProductPrice
import com.fashiontothem.ff.data.remote.dto.CartData
import com.fashiontothem.ff.data.remote.dto.CartItem
import com.fashiontothem.ff.data.remote.dto.CartRequest
import com.fashiontothem.ff.data.remote.dto.ConfigurableItemOption
import com.fashiontothem.ff.data.remote.dto.ErrorResponse
import com.fashiontothem.ff.data.remote.dto.ExtensionAttributes
import com.fashiontothem.ff.data.remote.dto.ProductOption
import com.squareup.moshi.Moshi
import com.fashiontothem.ff.data.remote.dto.toDomain
import com.fashiontothem.ff.domain.model.BrandImage
import com.fashiontothem.ff.domain.model.ChildProduct
import com.fashiontothem.ff.domain.model.ChildProductOption
import com.fashiontothem.ff.domain.model.ConfigurableOption
import com.fashiontothem.ff.domain.model.FilterOption
import com.fashiontothem.ff.domain.model.FilterOptions
import com.fashiontothem.ff.domain.model.Option
import com.fashiontothem.ff.domain.model.Product
import com.fashiontothem.ff.domain.model.ProductAttribute
import com.fashiontothem.ff.domain.model.ProductBrand
import com.fashiontothem.ff.domain.model.ProductCombination
import com.fashiontothem.ff.domain.model.ProductPrice
import com.fashiontothem.ff.domain.repository.ProductPageResult
import com.fashiontothem.ff.domain.repository.ProductRepository
import com.fashiontothem.ff.domain.repository.ProductUnavailableException
import com.fashiontothem.ff.domain.repository.QuantityNotAvailableException
import com.fashiontothem.ff.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Product Repository Implementation
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val athenaApiService: AthenaApiService,
    private val apiService: com.fashiontothem.ff.data.remote.ApiService,
    private val brandImageCache: BrandImageCache,
    private val storePreferences: com.fashiontothem.ff.data.local.preferences.StorePreferences,
    private val moshi: Moshi,
) : ProductRepository {

    // Mutex to ensure only one API call happens at a time
    private val brandImagesMutex = Mutex()

    /**
     * Get brand images with singleton cache (only one API call per app session)
     */
    private suspend fun getBrandImagesWithCache(): List<BrandImage> {
        // Return cached if available
        brandImageCache.getCachedBrandImages()?.let { return it }

        // Use mutex to ensure only one API call happens even with concurrent requests
        return brandImagesMutex.withLock {
            // Double-check after acquiring lock
            brandImageCache.getCachedBrandImages()?.let { return@withLock it }

            // Check if already loading
            if (brandImageCache.isLoading()) {
                // Wait a bit and check cache again
                kotlinx.coroutines.delay(100)
                brandImageCache.getCachedBrandImages()?.let { return@withLock it }
            }

            // Mark as loading
            brandImageCache.setLoading(true)

            // Fetch from API directly (without calling getBrandImages() to avoid mutex deadlock)
            return@withLock try {
                val response = apiService.getBrandImages(Constants.FASHION_BRANDS_INFO_URL)

                if (response.isSuccessful) {
                    val brandImages = response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
                    brandImageCache.setBrandImages(brandImages)
                    brandImages
                } else {
                    Timber.tag("ProductRepository")
                        .e("Failed to fetch brand images: ${response.code()} ${response.message()}")
                    brandImageCache.setLoading(false)
                    emptyList()
                }
            } catch (e: Exception) {
                Timber.tag("ProductRepository").e("Failed to fetch brand images: ${e.message}")
                brandImageCache.setLoading(false)
                emptyList()
            }
        }
    }

    override suspend fun getProductsByCategory(
        token: String,
        categoryId: String,
        categoryLevel: String,
        page: Int,
        filters: com.fashiontothem.ff.domain.repository.ProductFilters?,
        filterOptions: FilterOptions?, // Pass previous filter options for param names
        activeFilters: Map<String, Set<String>>, // Pass previous active filters for category level tracking
        preferConsolidatedCategories: Boolean,
    ): Result<ProductPageResult> {
        return try {
            // Build request with only non-null filter params
            val filterParams = filters?.toApiParams(filterOptions, activeFilters) ?: emptyMap()

            // Create base request map (send only what's needed)
            val requestMap = mutableMapOf<String, Any>(
                "token" to token,
                "category" to categoryId,
                "level" to categoryLevel,
                "customer_group_id" to 0,
                "page" to page
            )

            // Add filter params only if present
            filterParams.forEach { (key, value) ->
                requestMap[key] = value
            }

            // Use dynamic Map-based request to avoid null serialization
            val response = athenaApiService.getProductsByCategoryDynamic(requestMap)

            if (response.isSuccessful) {
                val responseData = response.body()?.data
                val productsData = responseData?.products
                val products = productsData?.results?.map { it.toDomain() } ?: emptyList()
                val amounts = productsData?.amounts
                val availableFilters = productsData?.filters

                // Log raw filters from API
                Timber.tag("ProductRepository").d("=== FILTER RESPONSE ===")
                Timber.tag("ProductRepository")
                    .d("Available filters count: ${availableFilters?.size}")
                availableFilters?.forEach { filter ->
                    Timber.tag("ProductRepository")
                        .d("Filter type: ${filter.type}, options count: ${filter.array?.size}")
                    filter.array?.forEach { option ->
                        Timber.tag("ProductRepository")
                            .d("  - ${option.optionLabel} (key: ${option.optionKey}, value: ${option.optionValue}, count: ${option.count})")
                    }
                }

                // Log active filters
                Timber.tag("ProductRepository")
                    .d("Active filters count: ${productsData?.activeFilters?.size}")
                productsData?.activeFilters?.forEach { activeFilter ->
                    Timber.tag("ProductRepository")
                        .d("Active: type=${activeFilter.type}, id=${activeFilter.id}, label=${activeFilter.label}")
                }
                Timber.tag("ProductRepository").d("======================")

                // Fetch brand images for filter UI
                val brandImages = getBrandImagesWithCache()
                Timber.tag("ProductRepository")
                    .d("Loaded ${brandImages.size} brand images for filters")

                // Convert available filters to FilterOptions, including active filters and brand images
                val filterOptionsResult =
                    availableFilters?.toFilterOptions(
                        productsData?.activeFilters,
                        brandImages,
                        preferConsolidatedCategories
                    )

                val result = ProductPageResult(
                    products = products,
                    hasNextPage = amounts?.nextPage != null || (amounts != null && amounts.currentPage < amounts.lastPage),
                    currentPage = amounts?.currentPage ?: page,
                    lastPage = amounts?.lastPage ?: page,
                    totalProducts = amounts?.total ?: 0,
                    imageCache = null, // Not used in category search
                    filterOptions = filterOptionsResult,
                    activeFilters = productsData?.activeFilters?.toActiveFiltersMap() ?: emptyMap()
                )

                Result.success(result)
            } else {
                Result.failure(Exception("Failed to fetch products: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductsByVisualSearch(
        token: String,
        image: String,
        page: Int,
        filters: com.fashiontothem.ff.domain.repository.ProductFilters?,
        filterOptions: FilterOptions?,
        activeFilters: Map<String, Set<String>>,
    ): Result<ProductPageResult> {
        return try {
            // Build request with filter params
            val filterParams = filters?.toApiParams(filterOptions, activeFilters) ?: emptyMap()

            val requestMap = mutableMapOf<String, Any>(
                "token" to token,
                "image" to image, // Base64 for page 1, image_cache for page > 1
                "customer_group_id" to 0,
                "page" to page
            )

            // Add filter parameters to request
            requestMap.putAll(filterParams)

            val response = athenaApiService.getProductsByVisualSearch(requestMap)

            if (response.isSuccessful) {
                val responseData = response.body()?.data
                val productsData = responseData?.products
                val products = productsData?.results?.map { it.toDomain() } ?: emptyList()
                val amounts = productsData?.amounts
                val receivedImageCache = responseData?.imageCache // Get image_cache from response
                val availableFilters = productsData?.filters

                // Log raw filters from API for Visual Search
                Timber.tag("ProductRepository").d("=== VISUAL SEARCH FILTER RESPONSE ===")
                Timber.tag("ProductRepository")
                    .d("Available filters count: ${availableFilters?.size}")
                availableFilters?.forEach { filter ->
                    Timber.tag("ProductRepository")
                        .d("Filter type: ${filter.type}, options count: ${filter.array?.size}")
                    filter.array?.forEach { option ->
                        Timber.tag("ProductRepository")
                            .d("  - ${option.optionLabel} (key: ${option.optionKey}, value: ${option.optionValue}, count: ${option.count})")
                    }
                }

                // Log active filters
                Timber.tag("ProductRepository")
                    .d("Active filters count: ${productsData?.activeFilters?.size}")
                productsData?.activeFilters?.forEach { activeFilter ->
                    Timber.tag("ProductRepository")
                        .d("Active: type=${activeFilter.type}, id=${activeFilter.id}, label=${activeFilter.label}")
                }
                Timber.tag("ProductRepository").d("======================================")

                // Fetch brand images for filter UI
                val brandImages = getBrandImagesWithCache()
                Timber.tag("ProductRepository")
                    .d("Loaded ${brandImages.size} brand images for visual search filters")
                brandImages.forEach {
                    Timber.tag("ProductRepository")
                        .d("  Brand Image: label=${it.optionLabel}, value=${it.optionValue}, url=${it.imageUrl}")
                }

                // Convert available filters to FilterOptions, including active filters and brand images
                val filterOptionsResult =
                    availableFilters?.toFilterOptions(productsData?.activeFilters, brandImages)

                // Log mapped brands with images
                Timber.tag("ProductRepository")
                    .d("Mapped brands: ${filterOptionsResult?.brands?.size}")
                filterOptionsResult?.brands?.forEach { brand ->
                    Timber.tag("ProductRepository")
                        .d("  Mapped Brand: label=${brand.label}, key=${brand.key}, hasImage=${brand.imageUrl != null}, url=${brand.imageUrl}")
                }

                val result = ProductPageResult(
                    products = products,
                    hasNextPage = amounts?.nextPage != null || (amounts != null && amounts.currentPage < amounts.lastPage),
                    currentPage = amounts?.currentPage ?: page,
                    lastPage = amounts?.lastPage ?: page,
                    totalProducts = amounts?.total ?: 0,
                    imageCache = receivedImageCache, // Store for next page
                    filterOptions = filterOptionsResult,
                    activeFilters = productsData?.activeFilters?.toActiveFiltersMap() ?: emptyMap()
                )

                Result.success(result)
            } else {
                Result.failure(Exception("Failed to fetch products by visual search: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBrandImages(): Result<List<BrandImage>> {
        // Check cache first
        brandImageCache.getCachedBrandImages()?.let {
            return Result.success(it)
        }

        // Use mutex to ensure only one API call happens at a time
        return brandImagesMutex.withLock {
            // Double-check after acquiring lock
            brandImageCache.getCachedBrandImages()?.let {
                return@withLock Result.success(it)
            }

            // Check if already loading
            if (brandImageCache.isLoading()) {
                // Wait a bit and check cache again
                kotlinx.coroutines.delay(100)
                brandImageCache.getCachedBrandImages()?.let {
                    return@withLock Result.success(it)
                }
            }

            // Mark as loading
            brandImageCache.setLoading(true)

            // If not cached, fetch from API
            return@withLock try {
                val response = apiService.getBrandImages(Constants.FASHION_BRANDS_INFO_URL)

                if (response.isSuccessful) {
                    val brandImages = response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
                    // Cache the result
                    brandImageCache.setBrandImages(brandImages)
                    Result.success(brandImages)
                } else {
                    brandImageCache.setLoading(false)
                    Result.failure(Exception("Failed to fetch brand images: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                brandImageCache.setLoading(false)
                Result.failure(e)
            }
        }
    }

    override suspend fun getProductDetails(barcodeOrSku: String): Result<com.fashiontothem.ff.domain.repository.ProductDetailsResult> {
        return try {
            val url =
                "${Constants.FASHION_AND_FRIENDS_BASE_URL}rs/rest/V1/barcode/find/in/store/$barcodeOrSku"
            val response = apiService.getProductDetails(url)

            when {
                response.isSuccessful -> {
                    val responseList = response.body()
                    if (responseList != null && responseList.isNotEmpty()) {
                        val firstResponse = responseList.first()
                        val productDetails = firstResponse.toDomain()
                        val stores = firstResponse.stores?.map { it.toDomain() } ?: emptyList()

                        if (productDetails != null) {
                            Result.success(
                                com.fashiontothem.ff.domain.repository.ProductDetailsResult(
                                    productDetails = productDetails,
                                    stores = stores
                                )
                            )
                        } else {
                            Result.failure(Exception("Product details not found"))
                        }
                    } else {
                        Result.failure(Exception("Product not found"))
                    }
                }

                response.code() == 404 -> {
                    // Product is no longer available (or available only in physical stores)
                    Result.failure(ProductUnavailableException())
                }

                else -> {
                    Result.failure(Exception("Failed to fetch product details: ${response.code()} ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addToCart(
        loyaltyScannedBarcode: String,
        sku: String,
        sizeAttributeId: String,
        sizeOptionValue: String,
        colorAttributeId: String,
        colorOptionValue: String,
    ): Result<Boolean> {
        return try {
            // Sanitize barcode - remove whitespace characters and check for duplicate halves
            val sanitizedBarcode = loyaltyScannedBarcode
                .trim()  // Remove leading/trailing whitespace
                .replace("\r", "")  // Remove carriage return
                .replace("\n", "")  // Remove newline
                .let { barcode ->
                    val mid = barcode.length / 2
                    if (mid > 0 && barcode.take(mid) == barcode.substring(mid)) {
                        barcode.take(mid)
                    } else {
                        barcode
                    }
                }

            // Build configurable options list
            val configurableOptions = listOfNotNull(
                sizeAttributeId.takeIf { it.isNotBlank() }?.let {
                    sizeOptionValue.toIntOrNull()?.takeIf { it != -1 }?.let { value ->
                        ConfigurableItemOption(optionId = sizeAttributeId, optionValue = value)
                    }
                },
                colorAttributeId.takeIf { it.isNotBlank() }?.let {
                    colorOptionValue.toIntOrNull()?.takeIf { it != -1 }?.let { value ->
                        ConfigurableItemOption(optionId = colorAttributeId, optionValue = value)
                    }
                }
            )

            // Build cart request
            val cartRequest = CartRequest(
                cartData = CartData(
                    loyaltyCardNumber = sanitizedBarcode,
                    cartItem = CartItem(
                        quantity = 1,
                        sku = sku,
                        productOption = ProductOption(
                            extensionAttributes = ExtensionAttributes(
                                configurableItemOptions = configurableOptions
                            )
                        )
                    )
                )
            )

            // Call API
            // Get country code from StorePreferences and use it in URL (e.g., /rs/, /ba/, /me/, /hr/)
            val countryCode = storePreferences.selectedCountryCode.first()?.lowercase() ?: "rs"
            val url = "${Constants.FASHION_AND_FRIENDS_BASE_URL}${countryCode}/rest/V1/carts/mine-items"
            val response = apiService.addToCart(url, cartRequest)

            when {
                response.isSuccessful && response.body() == true -> {
                    Result.success(true)
                }

                response.isSuccessful -> {
                    // Response is successful but body is not true
                    Result.failure(Exception("Failed to add item to cart: response was not true"))
                }

                else -> {
                    // Try to parse error body
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrBlank()) {
                            val errorResponse = moshi.adapter(ErrorResponse::class.java).fromJson(errorBody)
                            errorResponse?.message
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                    
                    // Check if it's a quantity not available error
                    val isQuantityError = errorMessage?.contains("The requested qty is not available", ignoreCase = true) == true
                    
                    if (isQuantityError) {
                        Result.failure(QuantityNotAvailableException(errorMessage ?: "The requested qty is not available"))
                    } else {
                        Result.failure(Exception("Failed to add item to cart: ${response.code()} ${response.message()}. ${errorMessage ?: ""}"))
                    }
                }
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
        options = options?.map { it.toDomain() }
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
        configurableOptions = configurableOptions?.map { it.toDomain() }
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

/**
 * Extension function to convert Filter list DTO to FilterOptions domain model
 */
private fun List<com.fashiontothem.ff.data.remote.dto.AthenaFilter>.toFilterOptions(
    activeFilters: List<com.fashiontothem.ff.data.remote.dto.AthenaActiveFilter>? = null,
    brandImages: List<BrandImage> = emptyList(),
    preferConsolidatedCategories: Boolean = false,
): FilterOptions {
    val gendersFilter = this.find { it.type == "pol" }
    val brandsFilter = this.find { it.type == "brend" }
    val sizesFilter = this.find { it.type == "velicina" }
    val colorsFilter = this.find { it.type == "boja" }

    // Prefer consolidated 'kategorije' group when present; otherwise use category1/category2/...
    val kategorijeFilter = this.find { it.type == "kategorije" }
    val categoryFilters = this.filter { it.type.startsWith("category") }

    // Helper function to merge active filters with available filters
    fun mergeFilters(
        availableOptions: List<FilterOption>,
        activeFiltersOfType: List<com.fashiontothem.ff.data.remote.dto.AthenaActiveFilter>,
        isBrandFilter: Boolean = false,
        isColorFilter: Boolean = false,
    ): List<FilterOption> {
        val availableKeys = availableOptions.map { it.key }.toSet()
        val activeOptions = activeFiltersOfType
            .filter { it.id !in availableKeys } // Only add active filters not already in available
            .map { activeFilter ->
                // For brand filters, try to find matching image by optionLabel (display name)
                val brandImage = if (isBrandFilter) {
                    brandImages.find { it.optionLabel == activeFilter.label }
                } else null

                // For color filters, try to find matching hexCode from original API data
                val colorHexCode = if (isColorFilter) {
                    colorsFilter?.array?.find { it.optionValue == activeFilter.id }?.haxCode
                } else null

                FilterOption(
                    key = activeFilter.id,
                    label = activeFilter.label ?: activeFilter.id,
                    count = 0, // Active filters don't have count
                    imageUrl = brandImage?.imageUrl,
                    hexCode = colorHexCode
                )
            }
        return availableOptions + activeOptions
    }

    // Get active filters by type
    val activeGenders = activeFilters?.filter { it.type == "pol" } ?: emptyList()
    val activeBrands = activeFilters?.filter { it.type == "brend" } ?: emptyList()
    val activeSizes = activeFilters?.filter { it.type == "velicina" } ?: emptyList()
    val activeColors = activeFilters?.filter { it.type == "boja" } ?: emptyList()
    val activeCategories = activeFilters?.filter {
        it.type.startsWith("category") || (preferConsolidatedCategories && it.type == "kategorije")
    } ?: emptyList()

    // Combine available category options, filtering out null values
    val availableCategories =
        if (preferConsolidatedCategories && kategorijeFilter?.array?.isNotEmpty() == true) {
            kategorijeFilter.array!!.mapNotNull { option ->
                val key = option.optionValue
                val label = option.optionLabel
                if (key != null && label != null) {
                    FilterOption(
                        key = key,
                        label = label,
                        count = option.count ?: 0
                    )
                } else null
            }
        } else {
            categoryFilters.flatMap { filter ->
                filter.array?.mapNotNull { option ->
                    val key = option.optionValue
                    val label = option.optionLabel
                    if (key != null && label != null) {
                        FilterOption(
                            key = key,
                            label = label,
                            count = option.count ?: 0
                        )
                    } else null
                } ?: emptyList()
            }
        }

    // Merge available and active categories, then remove duplicates by label
    val allCategories = mergeFilters(availableCategories, activeCategories)
        .distinctBy { it.label.lowercase().trim() } // Remove duplicates by label (case-insensitive)

    // Use option_key from 'kategorije' when present; else from first category filter
    val categoryParamName = if (preferConsolidatedCategories) {
        kategorijeFilter?.array?.firstOrNull { it.optionKey != null }?.optionKey
            ?: categoryFilters.firstOrNull()?.array?.firstOrNull { it.optionKey != null }?.optionKey
    } else {
        categoryFilters.firstOrNull()?.array?.firstOrNull { it.optionKey != null }?.optionKey
    }

    // Map available genders and merge with active, filtering out null values
    val availableGenders = gendersFilter?.array?.mapNotNull { option ->
        val key = option.optionValue
        val label = option.optionLabel
        if (key != null && label != null) {
            FilterOption(
                key = key,
                label = label,
                count = option.count ?: 0
            )
        } else null
    } ?: emptyList()

    // Map available brands and merge with active, including brand images, filtering out null values
    val availableBrands = brandsFilter?.array?.mapNotNull { option ->
        val key = option.optionValue
        val label = option.optionLabel
        if (key != null && label != null) {
            // Find matching brand image by optionLabel (display name) - Athena uses numeric optionValue, brands-info uses string slug
            val brandImage = brandImages.find { it.optionLabel == label }
            FilterOption(
                key = key,
                label = label,
                count = option.count ?: 0,
                imageUrl = brandImage?.imageUrl // ✅ Now correctly mapped
            )
        } else null
    } ?: emptyList()

    // Map available sizes and merge with active, filtering out null values
    val availableSizes = sizesFilter?.array?.mapNotNull { option ->
        val key = option.optionValue
        val label = option.optionLabel
        if (key != null && label != null) {
            FilterOption(
                key = key,
                label = label,
                count = option.count ?: 0
            )
        } else null
    } ?: emptyList()

    // Map available colors and merge with active, filtering out null values
    val availableColors = colorsFilter?.array?.mapNotNull { option ->
        val key = option.optionValue
        val label = option.optionLabel
        if (key != null && label != null) {
            FilterOption(
                key = key,
                label = label,
                count = option.count ?: 0,
                hexCode = option.haxCode // Map haxCode from API for color display
            )
        } else null
    } ?: emptyList()

    return FilterOptions(
        genders = mergeFilters(availableGenders, activeGenders),
        genderParamName = gendersFilter?.array?.firstOrNull { it.optionKey != null }?.optionKey,
        brands = mergeFilters(availableBrands, activeBrands, isBrandFilter = true),
        brandParamName = brandsFilter?.array?.firstOrNull { it.optionKey != null }?.optionKey,
        sizes = mergeFilters(availableSizes, activeSizes),
        sizeParamName = sizesFilter?.array?.firstOrNull { it.optionKey != null }?.optionKey,
        colors = mergeFilters(availableColors, activeColors, isColorFilter = true),
        colorParamName = colorsFilter?.array?.firstOrNull { it.optionKey != null }?.optionKey,
        categories = allCategories,
        categoryParamName = categoryParamName
    )
}

/**
 * Extension function to convert active filters to Map
 * Maps active_filters from API to format: { "velicina": ["m", "s"], "boja": ["blue"] }
 */
private fun List<com.fashiontothem.ff.data.remote.dto.AthenaActiveFilter>.toActiveFiltersMap(): Map<String, Set<String>> {
    val map = mutableMapOf<String, MutableSet<String>>()

    forEach { activeFilter ->
        // Use 'type' field (velicina, boja, brend) as key and 'id' as value
        val values = map.getOrPut(activeFilter.type) { mutableSetOf() }
        values.add(activeFilter.id)
    }

    return map
}