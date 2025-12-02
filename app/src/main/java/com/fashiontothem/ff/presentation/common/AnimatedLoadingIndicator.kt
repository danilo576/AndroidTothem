package com.fashiontothem.ff.presentation.common

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

@Composable
fun FashionLoader(
    modifier: Modifier = Modifier,
    speed: Float = 1f,
    colorOverride: Color? = null,
    assetName: String = "ff.json",
    screenWidth: Dp? = null,
    screenHeight: Dp? = null
) {
    // Responsive size calculation
    val loaderSize = if (screenWidth != null && screenHeight != null) {
        when {
            screenWidth < 400.dp -> 80.dp
            screenWidth < 600.dp -> 100.dp
            else -> 120.dp
        }
    } else {
        180.dp // Default size
    }
    
    val finalModifier = if (modifier == Modifier) {
        modifier.size(loaderSize)
    } else {
        modifier
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset(assetName)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        speed = speed
    )

    val dynamicProperties = colorOverride?.let { overrideColor ->
        val targetColor = overrideColor.toArgb()
        rememberLottieDynamicProperties(
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = targetColor,
                "FASHION & FRIENDS", "Group 1", "Fill 1"
            )
        )
    }

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = finalModifier,
        dynamicProperties = dynamicProperties
    )
}