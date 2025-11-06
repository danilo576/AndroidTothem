package com.fashiontothem.ff.data.cache

import com.fashiontothem.ff.domain.model.BrandImage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Brand Image Cache
 * 
 * Singleton cache for brand images that persists for the entire app session.
 * Brand images are loaded once and cached until the app is killed.
 */
@Singleton
class BrandImageCache @Inject constructor() {
    
    private var cachedBrandImages: List<BrandImage>? = null
    private var isLoading = false
    
    /**
     * Get cached brand images
     * @return Cached brand images or null if not loaded yet
     */
    fun getCachedBrandImages(): List<BrandImage>? = cachedBrandImages
    
    /**
     * Check if brand images are currently being loaded
     */
    fun isLoading(): Boolean = isLoading
    
    /**
     * Set brand images cache
     * @param brandImages List of brand images to cache
     */
    fun setBrandImages(brandImages: List<BrandImage>) {
        cachedBrandImages = brandImages
        isLoading = false
    }
    
    /**
     * Mark that loading has started
     */
    fun setLoading(loading: Boolean) {
        isLoading = loading
    }
    
    /**
     * Clear cache (useful for testing or forced refresh)
     */
    fun clear() {
        cachedBrandImages = null
        isLoading = false
    }
}

