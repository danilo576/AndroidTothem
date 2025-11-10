package com.fashiontothem.ff.presentation.products

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.domain.repository.ProductRepository
import com.fashiontothem.ff.domain.repository.ProductUnavailableException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F&F Tothem - Product Details ViewModel
 */
@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    private val TAG = "ProductDetailsViewModel"

    /**
     * Load product details by SKU
     * @param sku Product SKU (will be base64 encoded)
     * @param shortDescription Optional short description passed from listing
     * @param brandLabel Optional brand label for matching brand image
     */
    fun loadProductDetails(sku: String, shortDescription: String? = null, brandLabel: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                isProductUnavailable = false
            )

            try {
                // Encode SKU to base64
                val encodedSku = Base64.encodeToString(sku.toByteArray(), Base64.NO_WRAP)
                
                val result = productRepository.getProductDetails(encodedSku)
                
                result.fold(
                    onSuccess = { productDetailsResult ->
                        val productDetails = productDetailsResult.productDetails
                        
                        // Use passed values if available, otherwise fallback to API values
                        val finalShortDescription = shortDescription ?: productDetails?.shortDescription
                        val finalBrandLabel = brandLabel ?: productDetails?.brandName
                        
                        // Load brand images and find matching brand
                        val brandImageUrl = finalBrandLabel?.let { label ->
                            findBrandImage(label)
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            productDetails = productDetails,
                            stores = productDetailsResult.stores,
                            brandImageUrl = brandImageUrl,
                            apiShortDescription = productDetails?.shortDescription,
                            apiBrandName = productDetails?.brandName,
                            isLoading = false,
                            error = null,
                            isProductUnavailable = false
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to load product details", exception)
                        if (exception is ProductUnavailableException) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                productDetails = null,
                                stores = emptyList(),
                                error = null,
                                isProductUnavailable = true
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load product details",
                                isProductUnavailable = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading product details", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred",
                    isProductUnavailable = false
                )
            }
        }
    }
    
    /**
     * Find brand image URL by matching brand label
     */
    private suspend fun findBrandImage(brandLabel: String): String? {
        return try {
            val brandImagesResult = productRepository.getBrandImages()
            brandImagesResult.getOrNull()?.firstOrNull { brandImage ->
                brandImage.optionLabel.equals(brandLabel, ignoreCase = true)
            }?.imageUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error finding brand image", e)
            null
        }
    }

    /**
     * Load product details by barcode
     * @param barcode Product barcode (e.g., "8057338370499")
     */
    fun loadProductDetailsByBarcode(barcode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                isProductUnavailable = false
            )

            try {
                val result = productRepository.getProductDetails(barcode)
                
                result.fold(
                    onSuccess = { productDetailsResult ->
                        _uiState.value = _uiState.value.copy(
                            productDetails = productDetailsResult.productDetails,
                            stores = productDetailsResult.stores,
                            isLoading = false,
                            error = null,
                            isProductUnavailable = false
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to load product details", exception)
                        if (exception is ProductUnavailableException) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                productDetails = null,
                                stores = emptyList(),
                                error = null,
                                isProductUnavailable = true
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load product details",
                                isProductUnavailable = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading product details", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred",
                    isProductUnavailable = false
                )
            }
        }
    }

    /**
     * Select a size option (or deselect if already selected)
     */
    fun selectSize(sizeValue: String) {
        _uiState.value = _uiState.value.copy(
            selectedSize = if (_uiState.value.selectedSize == sizeValue) null else sizeValue
        )
    }

    /**
     * Select a color option (or deselect if already selected)
     */
    fun selectColor(colorValue: String) {
        _uiState.value = _uiState.value.copy(
            selectedColor = if (_uiState.value.selectedColor == colorValue) null else colorValue
        )
    }
}

/**
 * Product Details UI State
 */
data class ProductDetailsUiState(
    val productDetails: com.fashiontothem.ff.domain.model.ProductDetails? = null,
    val stores: List<com.fashiontothem.ff.domain.model.Store> = emptyList(),
    val brandImageUrl: String? = null,
    val apiShortDescription: String? = null, // Short description from API (for fallback)
    val apiBrandName: String? = null, // Brand name from API (for fallback)
    val isLoading: Boolean = false,
    val error: String? = null,
    val isProductUnavailable: Boolean = false,
    val selectedSize: String? = null,
    val selectedColor: String? = null
)

