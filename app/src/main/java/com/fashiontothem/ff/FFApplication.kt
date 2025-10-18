package com.fashiontothem.ff

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.fashiontothem.ff.util.MemoryMonitor
import dagger.hilt.android.HiltAndroidApp
import humer.UvcCamera.BuildConfig

/**
 * Application class for F&F Camera app.
 * 
 * Optimized for dedicated kiosk (Philips 32BDL3751E):
 * - 45% RAM for image cache (~1.8GB) - ~450 high-res images
 * - 500MB disk cache - ~2500 thumbnails
 * - Hardware-accelerated rendering (Mali-G52 GPU)
 * - Automatic memory management on low memory warnings
 */
@HiltAndroidApp
class FFApplication : Application(), ImageLoaderFactory {
    
    private val TAG = "FFApplication"
    
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "F&F Camera Starting...")
            Log.d(TAG, "Kiosk Mode: ENABLED (dedicated device)")
            Log.d(TAG, "═══════════════════════════════════════")
            
            // Log memory status on startup
            MemoryMonitor.logStatus(this)
        }
    }
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.45)  // 45% RAM = ~1.8GB (dedicated kiosk - only app running)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(500 * 1024 * 1024)  // 500MB (1.5% of 32GB - excellent for pagination)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)  // Smooth animations (Mali-G52)
            .allowHardware(true)  // GPU-accelerated bitmaps
            .build()
    }
    
    /**
     * Handle system memory pressure.
     * Clear caches when memory is running low.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        val loader = newImageLoader()
        
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "⚠️ Memory pressure detected (level: $level). Clearing memory cache...")
                loader.memoryCache?.clear()
                
                if (BuildConfig.DEBUG) {
                    MemoryMonitor.logStatus(this)
                }
            }
            
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                Log.d(TAG, "UI hidden. Keeping cache intact.")
                // Memory cache will auto-evict oldest entries when needed
            }
            
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                Log.w(TAG, "⚠️ App in background. Clearing all caches...")
                loader.memoryCache?.clear()
            }
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Log.e(TAG, "🚨 CRITICAL: Low memory warning! Clearing all image caches...")
        val loader = newImageLoader()
        loader.memoryCache?.clear()
        System.gc()  // Suggest garbage collection
    }
}

