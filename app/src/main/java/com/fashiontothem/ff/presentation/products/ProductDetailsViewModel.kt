package com.fashiontothem.ff.presentation.products

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.domain.repository.ProductRepository
import com.fashiontothem.ff.domain.repository.ProductUnavailableException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F&F Tothem - Product Details ViewModel
 */
@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val storePreferences: StorePreferences,
    private val locationPreferences: LocationPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    private val TAG = "ProductDetailsViewModel"

    init {
        viewModelScope.launch {
            storePreferences.selectedStoreCode.collect { storeCode ->
                _uiState.update { it.copy(selectedStoreCode = storeCode) }
            }
        }

        viewModelScope.launch {
            locationPreferences.selectedStoreId.collect { storeId ->
                _uiState.update { it.copy(selectedStoreId = storeId) }
            }
        }

        viewModelScope.launch {
            locationPreferences.pickupPointEnabled.collect { enabled ->
                _uiState.update { it.copy(isPickupPointEnabled = enabled) }
            }
        }
        
        // Load secureBaseMediaUrl from preferences
        viewModelScope.launch {
            storePreferences.secureBaseMediaUrl.collect { secureBaseMediaUrl ->
                _uiState.update { it.copy(secureBaseMediaUrl = secureBaseMediaUrl) }
            }
        }
    }

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
                isProductUnavailable = false,
                selectedSize = null,  // Reset selections when loading new product
                selectedColor = null   // Reset selections when loading new product
            )

            try {
                // Encode SKU to base64
                val encodedSku = Base64.encodeToString(sku.toByteArray(), Base64.NO_WRAP)
                
                val result = productRepository.getProductDetails(encodedSku)
                
                result.fold(
                    onSuccess = { productDetailsResult ->
                        val productDetails = productDetailsResult.productDetails
                        
                        // Auto-select size if only one option exists
                        val autoSelectedSize = productDetails?.options?.size?.options?.takeIf { it.size == 1 }?.firstOrNull()?.value
                        
                        // Auto-select color if only one option exists (prioritize colorShade over color)
                        val autoSelectedColor = productDetails?.options?.colorShade?.options?.takeIf { it.size == 1 }?.firstOrNull()?.value
                            ?: productDetails?.options?.color?.options?.takeIf { it.size == 1 }?.firstOrNull()?.value
                        
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
                            selectedSize = autoSelectedSize,
                            selectedColor = autoSelectedColor,
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
                            // Determine error type based on exception message
                            val errorMessage = when {
                                exception.message?.contains("500") == true -> "SERVER_ERROR"
                                exception.message?.contains("404") == true -> "NOT_FOUND"
                                exception.message?.contains("network", ignoreCase = true) == true -> "NETWORK_ERROR"
                                exception.message?.contains("timeout", ignoreCase = true) == true -> "TIMEOUT_ERROR"
                                else -> "GENERIC_ERROR"
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = errorMessage,
                                isProductUnavailable = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading product details", e)
                val errorMessage = when {
                    e.message?.contains("500") == true -> "SERVER_ERROR"
                    e.message?.contains("404") == true -> "NOT_FOUND"
                    e.message?.contains("network", ignoreCase = true) == true -> "NETWORK_ERROR"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "TIMEOUT_ERROR"
                    else -> "GENERIC_ERROR"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage,
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
                isProductUnavailable = false,
                selectedSize = null,  // Reset selections when loading new product
                selectedColor = null   // Reset selections when loading new product
            )

            try {
                val result = productRepository.getProductDetails(barcode)
                
                result.fold(
                    onSuccess = { productDetailsResult ->
                        val productDetails = productDetailsResult.productDetails
                        
                        // Auto-select size if only one option exists
                        val autoSelectedSize = productDetails?.options?.size?.options?.takeIf { it.size == 1 }?.firstOrNull()?.value
                        
                        // Auto-select color if only one option exists (prioritize colorShade over color)
                        val autoSelectedColor = productDetails?.options?.colorShade?.options?.takeIf { it.size == 1 }?.firstOrNull()?.value
                            ?: productDetails?.options?.color?.options?.takeIf { it.size == 1 }?.firstOrNull()?.value
                        
                        _uiState.value = _uiState.value.copy(
                            productDetails = productDetails,
                            stores = productDetailsResult.stores,
                            selectedSize = autoSelectedSize,
                            selectedColor = autoSelectedColor,
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
                            // Determine error type based on exception message
                            val errorMessage = when {
                                exception.message?.contains("500") == true -> "SERVER_ERROR"
                                exception.message?.contains("404") == true -> "NOT_FOUND"
                                exception.message?.contains("network", ignoreCase = true) == true -> "NETWORK_ERROR"
                                exception.message?.contains("timeout", ignoreCase = true) == true -> "TIMEOUT_ERROR"
                                else -> "GENERIC_ERROR"
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = errorMessage,
                                isProductUnavailable = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading product details", e)
                val errorMessage = when {
                    e.message?.contains("500") == true -> "SERVER_ERROR"
                    e.message?.contains("404") == true -> "NOT_FOUND"
                    e.message?.contains("network", ignoreCase = true) == true -> "NETWORK_ERROR"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "TIMEOUT_ERROR"
                    else -> "GENERIC_ERROR"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage,
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
    
    /**
     * Add product to cart with loyalty card
     */
    suspend fun addToCart(
        loyaltyScannedBarcode: String,
        sku: String,
        sizeAttributeId: String,
        sizeOptionValue: String,
        colorAttributeId: String,
        colorOptionValue: String,
    ): Result<Boolean> {
        return productRepository.addToCart(
            loyaltyScannedBarcode = loyaltyScannedBarcode,
            sku = sku,
            sizeAttributeId = sizeAttributeId,
            sizeOptionValue = sizeOptionValue,
            colorAttributeId = colorAttributeId,
            colorOptionValue = colorOptionValue,
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
    val selectedColor: String? = null,
    val selectedStoreCode: String? = null, // From StorePreferences (for Athena API)
    val selectedStoreId: String? = null, // From LocationPreferences (for pickup point)
    val isPickupPointEnabled: Boolean = false,
    val secureBaseMediaUrl: String? = null, // From selected store config
)

