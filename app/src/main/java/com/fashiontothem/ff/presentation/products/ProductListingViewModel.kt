package com.fashiontothem.ff.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.domain.model.Product
import com.fashiontothem.ff.domain.repository.ProductRepository
import com.fashiontothem.ff.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F&F Tothem - Product Listing ViewModel
 */
@HiltViewModel
class ProductListingViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListingUiState())
    val uiState: StateFlow<ProductListingUiState> = _uiState.asStateFlow()

    private var currentCategoryId: String? = null
    private var currentCategoryLevel: String? = null
    private var currentPage = 1
    private var isLoadingMore = false
    private var hasReachedEnd = false

    fun loadProducts(categoryId: String, categoryLevel: String) {
        if (currentCategoryId == categoryId && currentCategoryLevel == categoryLevel && _uiState.value.products.isNotEmpty()) {
            return // Already loaded
        }

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

    fun loadMoreProducts() {
        if (isLoadingMore || hasReachedEnd || currentCategoryId == null || currentCategoryLevel == null) {
            return
        }

        isLoadingMore = true
        currentPage++

        viewModelScope.launch {
            loadProductsPage(currentCategoryId!!, currentCategoryLevel!!, currentPage, isLoadMore = true)
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
}

data class ProductListingUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
