package com.fashiontothem.ff.util

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import coil.imageLoader

/**
 * Memory monitoring utility for kiosk app.
 * 
 * Helps track memory usage and cache performance.
 */
object MemoryMonitor {
    
    private const val TAG = "MemoryMonitor"
    
    /**
     * Get current memory usage information.
     */
    fun getMemoryInfo(context: Context): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemoryMB = runtime.maxMemory() / 1024 / 1024
        val totalMemoryMB = runtime.totalMemory() / 1024 / 1024
        val freeMemoryMB = runtime.freeMemory() / 1024 / 1024
        
        val deviceTotalMemoryMB = memoryInfo.totalMem / 1024 / 1024
        val deviceAvailableMemoryMB = memoryInfo.availMem / 1024 / 1024
        val isLowMemory = memoryInfo.lowMemory
        
        return MemoryInfo(
            usedMemoryMB = usedMemoryMB,
            maxMemoryMB = maxMemoryMB,
            totalMemoryMB = totalMemoryMB,
            freeMemoryMB = freeMemoryMB,
            deviceTotalMemoryMB = deviceTotalMemoryMB,
            deviceAvailableMemoryMB = deviceAvailableMemoryMB,
            isLowMemory = isLowMemory,
            usagePercentage = (usedMemoryMB.toFloat() / maxMemoryMB * 100).toInt()
        )
    }
    
    /**
     * Get Coil image cache information.
     */
    fun getCacheInfo(context: Context): CacheInfo {
        val imageLoader = context.imageLoader
        val memoryCache = imageLoader.memoryCache
        val diskCache = imageLoader.diskCache
        
        val memoryCacheSizeMB = (memoryCache?.size?.toLong() ?: 0L) / 1024L / 1024L
        val memoryCacheMaxSizeMB = (memoryCache?.maxSize?.toLong() ?: 0L) / 1024L / 1024L
        
        val diskCacheSizeMB = (diskCache?.size?.toLong() ?: 0L) / 1024L / 1024L
        val diskCacheMaxSizeMB = (diskCache?.maxSize?.toLong() ?: 0L) / 1024L / 1024L
        
        return CacheInfo(
            memoryCacheSizeMB = memoryCacheSizeMB,
            memoryCacheMaxSizeMB = memoryCacheMaxSizeMB,
            memoryCacheUsagePercentage = if (memoryCacheMaxSizeMB > 0) {
                (memoryCacheSizeMB.toFloat() / memoryCacheMaxSizeMB * 100).toInt()
            } else 0,
            diskCacheSizeMB = diskCacheSizeMB,
            diskCacheMaxSizeMB = diskCacheMaxSizeMB,
            diskCacheUsagePercentage = if (diskCacheMaxSizeMB > 0) {
                (diskCacheSizeMB.toFloat() / diskCacheMaxSizeMB * 100).toInt()
            } else 0
        )
    }
    
    /**
     * Log current memory and cache status.
     */
    fun logStatus(context: Context) {
        val memoryInfo = getMemoryInfo(context)
        val cacheInfo = getCacheInfo(context)
        
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "MEMORY STATUS")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "App Memory Usage: ${memoryInfo.usedMemoryMB}MB / ${memoryInfo.maxMemoryMB}MB (${memoryInfo.usagePercentage}%)")
        Log.d(TAG, "Device Memory: ${memoryInfo.deviceAvailableMemoryMB}MB / ${memoryInfo.deviceTotalMemoryMB}MB available")
        Log.d(TAG, "Low Memory Warning: ${if (memoryInfo.isLowMemory) "YES ⚠️" else "NO ✓"}")
        Log.d(TAG, "───────────────────────────────────────")
        Log.d(TAG, "CACHE STATUS")
        Log.d(TAG, "───────────────────────────────────────")
        Log.d(TAG, "Memory Cache: ${cacheInfo.memoryCacheSizeMB}MB / ${cacheInfo.memoryCacheMaxSizeMB}MB (${cacheInfo.memoryCacheUsagePercentage}%)")
        Log.d(TAG, "Disk Cache: ${cacheInfo.diskCacheSizeMB}MB / ${cacheInfo.diskCacheMaxSizeMB}MB (${cacheInfo.diskCacheUsagePercentage}%)")
        Log.d(TAG, "═══════════════════════════════════════")
    }
    
    /**
     * Clear caches if memory is low.
     */
    fun clearCachesIfNeeded(context: Context) {
        val memoryInfo = getMemoryInfo(context)
        
        if (memoryInfo.isLowMemory || memoryInfo.usagePercentage > 90) {
            Log.w(TAG, "⚠️ Low memory detected! Clearing image memory cache...")
            ImagePrefetchHelper.clearMemoryCache(context)
        }
    }
}

data class MemoryInfo(
    val usedMemoryMB: Long,
    val maxMemoryMB: Long,
    val totalMemoryMB: Long,
    val freeMemoryMB: Long,
    val deviceTotalMemoryMB: Long,
    val deviceAvailableMemoryMB: Long,
    val isLowMemory: Boolean,
    val usagePercentage: Int
)

data class CacheInfo(
    val memoryCacheSizeMB: Long,
    val memoryCacheMaxSizeMB: Long,
    val memoryCacheUsagePercentage: Int,
    val diskCacheSizeMB: Long,
    val diskCacheMaxSizeMB: Long,
    val diskCacheUsagePercentage: Int
)

