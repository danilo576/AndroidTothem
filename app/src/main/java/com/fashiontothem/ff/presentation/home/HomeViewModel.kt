package com.fashiontothem.ff.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import humer.UvcCamera.BuildConfig
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for HomeScreen
 * Handles preloading brand images on screen display
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    fun preloadBrandImages() {
        viewModelScope.launch {
            try {
                if (BuildConfig.DEBUG) {
                    Log.d("HomeViewModel", "🖼️ Preloading brand images...")
                }
                
                val result = productRepository.getBrandImages()
                result.fold(
                    onSuccess = { brandImages ->
                        if (BuildConfig.DEBUG) {
                            Log.d("HomeViewModel", "✅ Brand images preloaded: ${brandImages.size} images cached")
                        }
                    },
                    onFailure = { error ->
                        Log.e("HomeViewModel", "❌ Failed to preload brand images: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Exception while preloading brand images: ${e.message}", e)
            }
        }
    }
}

