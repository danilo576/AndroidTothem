package com.fashiontothem.ff.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import humer.UvcCamera.R

@Composable
fun FashionLoader() {
    val infinite = rememberInfiniteTransition(label = "loader")
    
    // Bouncing scale animation
    val scale by infinite.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = androidx.compose.animation.core.EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Gentle rotation animation
    val rotation by infinite.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = androidx.compose.animation.core.EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    // Alpha pulse animation
    val alpha by infinite.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = androidx.compose.animation.core.EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Image(
        painter = painterResource(R.drawable.fashion_and_friends_loader),
        contentDescription = "Loading",
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
                this.alpha = alpha
            }
    )
}