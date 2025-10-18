package com.fashiontothem.ff.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * F&F Tothem - Pagination Helper
 * 
 * Helper for pagination with image prefetching in fashion gallery.
 * Detects when user is close to the end of the list and triggers
 * loading of the next page + prefetching of images.
 */
object PaginationHelper {
    
    /**
     * Calculate if user has scrolled past the given threshold percentage.
     * 
     * @param threshold Percentage (0.0 to 1.0) - default 0.8 (80%)
     */
    @Composable
    fun LazyListState.shouldLoadMore(
        totalItems: Int,
        threshold: Float = 0.8f
    ): Boolean {
        return remember(this) {
            derivedStateOf {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItemsCount = layoutInfo.totalItemsCount
                
                // Load more when user scrolls past threshold
                totalItemsCount > 0 && lastVisibleItem >= (totalItemsCount * threshold).toInt()
            }
        }.value
    }
    
    /**
     * Observe scroll state and trigger action when threshold is reached.
     * 
     * Example:
     * ```
     * val listState = rememberLazyListState()
     * 
     * PaginationHelper.OnScrollThreshold(
     *     listState = listState,
     *     totalItems = items.size,
     *     threshold = 0.8f
     * ) {
     *     viewModel.loadNextPage()
     *     ImagePrefetchHelper.prefetchNextPage(context, nextPageImageUrls)
     * }
     * ```
     */
    @Composable
    fun OnScrollThreshold(
        listState: LazyListState,
        totalItems: Int,
        threshold: Float = 0.8f,
        onThresholdReached: () -> Unit
    ) {
        LaunchedEffect(listState) {
            snapshotFlow {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItemsCount = listState.layoutInfo.totalItemsCount
                
                totalItemsCount > 0 && lastVisibleItem >= (totalItemsCount * threshold).toInt()
            }
                .distinctUntilChanged()
                .collect { shouldLoad ->
                    if (shouldLoad) {
                        onThresholdReached()
                    }
                }
        }
    }
}

/**
 * Extension function to easily check if we should load more.
 */
@Composable
fun LazyListState.reachedBottom(threshold: Float = 0.8f): Boolean {
    return remember(this) {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = layoutInfo.totalItemsCount
            
            totalItemsCount > 0 && lastVisibleItem >= (totalItemsCount * threshold).toInt()
        }
    }.value
}

