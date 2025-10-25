package com.fashiontothem.ff.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.domain.model.Product
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
    private val locationPreferences: LocationPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListingUiState())
    val uiState: StateFlow<ProductListingUiState> = _uiState.asStateFlow()

    private var currentCategoryId: String? = null
    private var currentCategoryLevel: String? = null
    private var visualSearchImage: String? = null // Base64 image for visual search
    private var imageCache: String? = null // Image cache for visual search pagination
    private var currentPage = 1
    private var isLoadingMore = false
    private var hasReachedEnd = false
    private var loadMoreJob: Job? = null
    private var lastLoadMoreTime = 0L
    private val loadMoreDebounceMs = 300L // Prevent rapid-fire requests

    fun loadProducts(categoryId: String, categoryLevel: String) {
        if (currentCategoryId == categoryId && currentCategoryLevel == categoryLevel && _uiState.value.products.isNotEmpty()) {
            return // Already loaded
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

        viewModelScope.launch {
            loadProductsPage(categoryId, categoryLevel, 1, isLoadMore = false)
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
            if (filterType == "visual") {
                // Visual search mode - get image from DataStore
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
                // Regular category mode
                loadProducts(categoryId, categoryLevel)
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
                page = page
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
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Unknown error occurred"
            )
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

            // For page 1: send base64, for page > 1: send image_cache
            val imageParam = if (page == 1) imageBase64 else (cachedImageHash ?: imageBase64)
            
            val result = productRepository.getProductsByVisualSearch(
                token = token,
                image = imageParam,
                page = page
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
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Unknown error occurred"
            )
        } finally {
            isLoadingMore = false
        }
    }
}

/**
 * UI State for Product Listing
 * Immutable state for better Compose performance
 */
data class ProductListingUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    // Helper to check if we have any content
    val hasContent: Boolean get() = products.isNotEmpty() && !isLoading && error == null
}
