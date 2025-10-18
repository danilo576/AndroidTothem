package com.fashiontothem.ff.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.domain.model.ImageItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for image gallery with pagination support.
 * 
 * Optimized for kiosk with aggressive caching:
 * - 1.8GB RAM cache (~450 high-res images)
 * - 500MB disk cache (~2500 thumbnails)
 * - Prefetching next page for smooth scrolling
 */
@HiltViewModel
class ImagePaginationViewModel @Inject constructor(
    // Inject your use cases here
    // private val getImagesUseCase: GetImagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImagePaginationUiState())
    val uiState: StateFlow<ImagePaginationUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var canLoadMore = true

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // TODO: Replace with actual API call
                // val result = getImagesUseCase(page = 0, pageSize = 20)
                
                // Example:
                val mockImages = generateMockImages(page = 0, pageSize = 20)
                
                _uiState.update {
                    it.copy(
                        images = mockImages,
                        isLoading = false,
                        currentPage = 0
                    )
                }
                
                currentPage = 0
                canLoadMore = mockImages.isNotEmpty()
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore || !canLoadMore) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            
            try {
                val nextPage = currentPage + 1
                
                // TODO: Replace with actual API call
                // val result = getImagesUseCase(page = nextPage, pageSize = 20)
                
                // Example:
                val newImages = generateMockImages(page = nextPage, pageSize = 20)
                
                _uiState.update { currentState ->
                    currentState.copy(
                        images = currentState.images + newImages,
                        isLoadingMore = false,
                        currentPage = nextPage
                    )
                }
                
                currentPage = nextPage
                canLoadMore = newImages.isNotEmpty()
                
                // Prefetch next page images in background
                // ImagePrefetchHelper.prefetchNextPage(context, nextPageImageUrls)
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = e.message ?: "Failed to load more"
                    )
                }
            }
        }
    }

    fun retry() {
        loadFirstPage()
    }

    // Mock data generator - replace with real API call
    private fun generateMockImages(page: Int, pageSize: Int): List<ImageItem> {
        return List(pageSize) { index ->
            val globalIndex = page * pageSize + index
            ImageItem(
                id = "image_$globalIndex",
                url = "https://picsum.photos/800/600?random=$globalIndex",
                title = "Image ${globalIndex + 1}",
                description = "Page $page, Item $index"
            )
        }
    }
}

/**
 * UI State for image pagination.
 */
data class ImagePaginationUiState(
    val images: List<ImageItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0
)

