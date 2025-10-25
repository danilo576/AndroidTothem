package com.fashiontothem.ff.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * F&F Tothem - Click Debouncer
 * 
 * Prevents multiple rapid clicks that can cause navigation issues
 */

/**
 * Debounced clickable modifier - prevents rapid successive clicks
 * 
 * @param debounceTime Time in milliseconds to wait before allowing another click
 * @param onClick Action to perform on click
 */
fun Modifier.clickableDebounced(
    debounceTime: Long = 500L,
    onClick: () -> Unit
): Modifier = composed {
    var lastClickTime by remember { mutableStateOf(0L) }
    
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            onClick()
        }
    }
}

/**
 * Create a debounced click handler
 * 
 * @param debounceTime Time in milliseconds to wait before allowing another click
 * @param onClick Action to perform on click
 * @return Debounced onClick handler
 */
@Composable
fun rememberDebouncedClick(
    debounceTime: Long = 500L,
    onClick: () -> Unit
): () -> Unit {
    val scope = rememberCoroutineScope()
    var isClickInProgress by remember { mutableStateOf(false) }
    
    return remember(onClick) {
        {
            if (!isClickInProgress) {
                isClickInProgress = true
                onClick()
                scope.launch {
                    delay(debounceTime)
                    isClickInProgress = false
                }
            }
        }
    }
}

