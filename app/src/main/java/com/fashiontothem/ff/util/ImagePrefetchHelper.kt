package com.fashiontothem.ff.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * F&F Tothem - Image Prefetch Helper
 * 
 * Helper class for prefetching fashion images in pagination scenarios.
 * Use this to preload images from the next page while user is viewing current page,
 * ensuring smooth scrolling and instant image display.
 */
object ImagePrefetchHelper {
    
    /**
     * Prefetch a list of image URLs in background.
     * Images will be loaded into memory and disk cache.
     * 
     * @param context Android context
     * @param imageUrls List of image URLs to prefetch
     * @param targetSize Optional target size for images (default: full size)
     */
    fun prefetchImages(
        context: Context,
        imageUrls: List<String>,
        targetSize: Pair<Int, Int>? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val imageLoader = context.imageLoader
            
            imageUrls.forEach { url ->
                try {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .apply {
                            targetSize?.let { (width, height) ->
                                size(width, height)
                            }
                        }
                        .build()
                    
                    imageLoader.enqueue(request)
                } catch (e: Exception) {
                    // Silently fail - prefetch is not critical
                }
            }
        }
    }
    
    /**
     * Prefetch next page images for pagination.
     * Call this when user scrolls to 80% of current page.
     * 
     * @param context Android context
     * @param nextPageImages List of image URLs from next page
     */
    fun prefetchNextPage(
        context: Context,
        nextPageImages: List<String>
    ) {
        // Prefetch at thumbnail size for faster loading
        prefetchImages(context, nextPageImages, targetSize = 400 to 400)
    }
    
    /**
     * Clear memory cache when needed (e.g., on low memory warning).
     */
    fun clearMemoryCache(context: Context) {
        context.imageLoader.memoryCache?.clear()
    }
    
    /**
     * Clear disk cache (use sparingly, only when needed).
     */
    suspend fun clearDiskCache(context: Context) {
        context.imageLoader.diskCache?.clear()
    }
}

