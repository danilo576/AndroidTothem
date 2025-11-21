package com.fashiontothem.ff.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.data.local.preferences.FilterPreferences
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.domain.model.Product
import com.fashiontothem.ff.domain.repository.ProductFilters
import com.fashiontothem.ff.domain.repository.ProductRepository
import com.fashiontothem.ff.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F&F Tothem - Product Listing ViewModel
 */
@HiltViewModel
class ProductListingViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val locationPreferences: LocationPreferences,
    private val filterPreferences: FilterPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListingUiState())
    val uiState: StateFlow<ProductListingUiState> = _uiState.asStateFlow()
    
    private val _isLoadingFilters = MutableStateFlow(false)
    val isLoadingFilters: StateFlow<Boolean> = _isLoadingFilters.asStateFlow()
    
    // ✅ Grid columns state (preserved across navigation)
    private val _gridColumns = MutableStateFlow(2)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()
    
    // ✅ Trigger for scroll reset (only when filters are applied)
    private val _shouldResetScroll = MutableStateFlow(false)
    val shouldResetScroll: StateFlow<Boolean> = _shouldResetScroll.asStateFlow()

    private var currentCategoryId: String? = null
    private var currentCategoryLevel: String? = null
    var currentFilters: ProductFilters? = null // Made public for filter screen access
        private set
    private var visualSearchImage: String? = null // Base64 image for visual search
    private var imageCache: String? = null // Image cache for visual search pagination
    private var currentPage = 1
    private var isLoadingMore = false
    private var hasReachedEnd = false
    private var loadMoreJob: Job? = null
    private var lastLoadMoreTime = 0L
    private val loadMoreDebounceMs = 300L // Prevent rapid-fire requests

    // Prefer consolidated 'kategorije' group for category tab when coming from Brand/Category flow
    private var preferConsolidatedCategories: Boolean = false

    fun loadProducts(categoryId: String, categoryLevel: String) {
        viewModelScope.launch {
            // Don't load saved filters - always start fresh
            currentFilters = null
            
            if (currentCategoryId == categoryId && 
                currentCategoryLevel == categoryLevel && 
                _uiState.value.products.isNotEmpty()) {
                return@launch // Already loaded
            }

            // Reset visual search mode
            visualSearchImage = null
            imageCache = null
            
            currentCategoryId = categoryId
            currentCategoryLevel = categoryLevel
            currentPage = 1
            isLoadingMore = false
            hasReachedEnd = false

            _uiState.value = _uiState.value.copy(
                products = emptyList(),
                isLoading = true,
                error = null
            )

            loadProductsPage(categoryId, categoryLevel, 1, isLoadMore = false)
        }
    }
    
    fun applyFilters(filters: ProductFilters) {
        viewModelScope.launch {
            // Show loading on filter screen
            _isLoadingFilters.value = true
            
            // ✅ Trigger scroll reset when filters are applied
            _shouldResetScroll.value = true
            
            // Don't save filters - just apply them for this request
            currentFilters = if (filters.isEmpty()) null else filters
            
            // Reload products with new filters
            if (visualSearchImage != null) {
                // Visual search mode with filters
                currentPage = 1
                isLoadingMore = false
                hasReachedEnd = false
                
                _uiState.value = _uiState.value.copy(
                    products = emptyList(),
                    isLoading = true,
                    error = null
                )
                
                loadVisualSearchPage(visualSearchImage!!, imageCache, 1, isLoadMore = false)
            } else if (currentCategoryId != null && currentCategoryLevel != null) {
                // Category mode with filters
                currentPage = 1
                isLoadingMore = false
                hasReachedEnd = false
                
                _uiState.value = _uiState.value.copy(
                    products = emptyList(),
                    isLoading = true,
                    error = null
                )
                
                loadProductsPage(currentCategoryId!!, currentCategoryLevel!!, 1, isLoadMore = false)
            }
            
            // Loading will be set to false in load methods when response arrives
        }
    }
    
    fun clearFilters() {
        viewModelScope.launch {
            // Don't clear DataStore - just clear current filters
            currentFilters = null
            
            // Reload products without filters
            if (visualSearchImage != null) {
                // Visual search mode
                currentPage = 1
                isLoadingMore = false
                hasReachedEnd = false
                
                _uiState.value = _uiState.value.copy(
                    products = emptyList(),
                    isLoading = true,
                    error = null
                )
                
                loadVisualSearchPage(visualSearchImage!!, imageCache, 1, isLoadMore = false)
            } else if (currentCategoryId != null && currentCategoryLevel != null) {
                // Category mode
                currentPage = 1
                isLoadingMore = false
                hasReachedEnd = false
                
                _uiState.value = _uiState.value.copy(
                    products = emptyList(),
                    isLoading = true,
                    error = null
                )
                
                loadProductsPage(currentCategoryId!!, currentCategoryLevel!!, 1, isLoadMore = false)
            }
        }
    }
    
    fun loadProductsByVisualSearch(imageBase64: String) {
        // Reset category mode
        currentCategoryId = null
        currentCategoryLevel = null
        
        visualSearchImage = imageBase64
        imageCache = null // Will be set from first response
        currentPage = 1
        isLoadingMore = false
        hasReachedEnd = false

        _uiState.value = _uiState.value.copy(
            products = emptyList(),
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            loadVisualSearchPage(imageBase64, null, 1, isLoadMore = false)
        }
    }
    
    fun checkAndLoadVisualSearchOrCategory(categoryId: String?, categoryLevel: String?, filterType: String) {
        viewModelScope.launch {
            // Decide preference for consolidated categories based on entry point
            preferConsolidatedCategories = (filterType == "brand" || filterType == "category")
            if (filterType == "visual") {
                // Visual search mode
                
                // If already in visual search mode with image, don't reload
                if (visualSearchImage != null) {
                    return@launch
                }
                
                // Get image from DataStore
                val imageBase64 = locationPreferences.visualSearchImage.firstOrNull()
                if (!imageBase64.isNullOrEmpty()) {
                    loadProductsByVisualSearch(imageBase64)
                    // Clear the visual search image after using it
                    locationPreferences.clearVisualSearchImage()
                } else {
                    // No image found, show error
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Visual search image not found"
                    )
                }
            } else if (!categoryId.isNullOrEmpty() && !categoryLevel.isNullOrEmpty()) {
                // Regular category mode - only load if not already loaded
                if (currentCategoryId != categoryId || currentCategoryLevel != categoryLevel) {
                    loadProducts(categoryId, categoryLevel)
                }
            } else {
                // Invalid state
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Invalid product listing parameters"
                )
            }
        }
    }

    fun loadMoreProducts() {
        // Debounce rapid scroll triggers
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLoadMoreTime < loadMoreDebounceMs) {
            return
        }
        
        if (isLoadingMore || hasReachedEnd) {
            return
        }

        lastLoadMoreTime = currentTime
        isLoadingMore = true
        currentPage++

        // Cancel any pending load more job
        loadMoreJob?.cancel()
        
        loadMoreJob = viewModelScope.launch {
            if (visualSearchImage != null) {
                // Visual search mode - use image_cache for page > 1
                loadVisualSearchPage(visualSearchImage!!, imageCache, currentPage, isLoadMore = true)
            } else if (currentCategoryId != null && currentCategoryLevel != null) {
                // Category mode
                loadProductsPage(currentCategoryId!!, currentCategoryLevel!!, currentPage, isLoadMore = true)
            }
        }
    }

    private suspend fun loadProductsPage(
        categoryId: String,
        categoryLevel: String,
        page: Int,
        isLoadMore: Boolean
    ) {
        try {
            // Get the cached Athena token (no API call)
            val token = storeRepository.getCachedAthenaToken() ?: ""
            
            if (token.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Athena token nije dostupan. Molimo pokrenite aplikaciju ponovo."
                )
                return
            }

            val result = productRepository.getProductsByCategory(
                token = token,
                categoryId = categoryId,
                categoryLevel = categoryLevel,
                page = page,
                filters = currentFilters,
                filterOptions = _uiState.value.availableFilters, // Pass current filter options for param names
                activeFilters = _uiState.value.activeFilters, // ✅ Pass current active filters for category level tracking
                preferConsolidatedCategories = preferConsolidatedCategories
            )

            result.fold(
                onSuccess = { pageResult ->
                    hasReachedEnd = !pageResult.hasNextPage
                    
                _uiState.value = _uiState.value.copy(
                    products = if (isLoadMore) {
                        _uiState.value.products + pageResult.products
                    } else {
                        pageResult.products
                    },
                    isLoading = false,
                    error = null,
                    availableFilters = pageResult.filterOptions ?: _uiState.value.availableFilters,
                    activeFilters = pageResult.activeFilters, // Update active filters from API
                    isEmpty = pageResult.isEmpty,
                    categoryNotFound = pageResult.categoryNotFound,
                    errorMessage = pageResult.errorMessage
                )
                
                // Stop filter loading when new filters arrive
                _isLoadingFilters.value = false
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                    _isLoadingFilters.value = false // Ensure overlay hides on error
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Unknown error occurred"
            )
            _isLoadingFilters.value = false // Ensure overlay hides on exception
        } finally {
            isLoadingMore = false
        }
    }
    
    private suspend fun loadVisualSearchPage(
        imageBase64: String,
        cachedImageHash: String?,
        page: Int,
        isLoadMore: Boolean
    ) {
        try {
            // Get the cached Athena token (no API call)
            val token = storeRepository.getCachedAthenaToken() ?: ""
            
            if (token.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Athena token nije dostupan. Molimo pokrenite aplikaciju ponovo."
                )
                return
            }

            // Use image_cache if available (more efficient when applying filters), otherwise use base64
            val imageParam = cachedImageHash ?: imageBase64
            
            val result = productRepository.getProductsByVisualSearch(
                token = token,
                image = imageParam,
                page = page,
                filters = currentFilters,
                filterOptions = _uiState.value.availableFilters, // Pass current filter options for param names
                activeFilters = _uiState.value.activeFilters // ✅ Pass current active filters for category level tracking
            )

            result.fold(
                onSuccess = { pageResult ->
                    hasReachedEnd = !pageResult.hasNextPage
                    
                    // Store image_cache for subsequent pages
                    if (pageResult.imageCache != null) {
                        imageCache = pageResult.imageCache
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        products = if (isLoadMore) {
                            _uiState.value.products + pageResult.products
                        } else {
                            pageResult.products
                        },
                        isLoading = false,
                        error = null,
                        availableFilters = pageResult.filterOptions ?: _uiState.value.availableFilters,
                        activeFilters = pageResult.activeFilters, // Update active filters from API
                        isEmpty = pageResult.isEmpty,
                        categoryNotFound = pageResult.categoryNotFound,
                        errorMessage = pageResult.errorMessage
                    )
                    _isLoadingFilters.value = false // Stop filter loading when new filters arrive
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                    _isLoadingFilters.value = false // Stop filter loading on error
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Unknown error occurred"
            )
            _isLoadingFilters.value = false // Stop filter loading on exception
        } finally {
            isLoadingMore = false
        }
    }
    
    /**
     * Toggle grid columns between 2 and 3
     */
    fun toggleGridColumns() {
        _gridColumns.value = if (_gridColumns.value == 2) 3 else 2
    }
    
    /**
     * Mark scroll reset as complete
     * Should be called after scroll reset is performed in UI
     */
    fun onScrollResetComplete() {
        _shouldResetScroll.value = false
    }
}

/**
 * UI State for Product Listing
 * Immutable state for better Compose performance
 */
data class ProductListingUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableFilters: com.fashiontothem.ff.domain.model.FilterOptions? = null,
    val activeFilters: Map<String, Set<String>> = emptyMap(), // Currently active filters from API
    val isEmpty: Boolean = false, // True if category exists but has no products
    val categoryNotFound: Boolean = false, // True if category doesn't exist
    val errorMessage: String? = null // Error message from API (e.g., "Category doesn't exist.")
) {
    // Helper to check if we have any content
    val hasContent: Boolean get() = products.isNotEmpty() && !isLoading && error == null
}
