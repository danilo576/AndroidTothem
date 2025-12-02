package com.fashiontothem.ff.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R

/**
 * F&F Tothem - No Internet Connection Screen
 *
 * Displayed when network connectivity is lost.
 * Automatically closes and returns to previous screen when connection is restored.
 */
@Composable
fun NoInternetScreen() {
    // Pulse animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        // Background - splash_background
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.splash_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Semi-transparent overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )

            // Responsive horizontal padding
            val horizontalPadding = when {
                screenWidth < 400.dp -> 24.dp
                screenWidth < 600.dp -> 36.dp
                else -> 48.dp
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Responsive icon box size
                val iconBoxSize = when {
                    screenWidth < 400.dp -> 100.dp
                    screenWidth < 600.dp -> 130.dp
                    else -> 160.dp
                }
                
                // Responsive warning emoji font size
                val warningEmojiFontSize = when {
                    screenWidth < 400.dp -> 50.sp
                    screenWidth < 600.dp -> 65.sp
                    else -> 80.sp
                }
                
                // No Internet visual with pulsing animation
                Box(
                    modifier = Modifier
                        .size(iconBoxSize)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .alpha(alpha),
                    contentAlignment = Alignment.Center
                ) {
                    // Large WiFi slash symbol
                    Text(
                        text = "⚠",
                        fontSize = warningEmojiFontSize,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Responsive spacing after icon
                val iconBottomSpacing = when {
                    screenHeight < 700.dp -> 32.dp
                    screenHeight < 1200.dp -> 40.dp
                    else -> 48.dp
                }
                
                Spacer(modifier = Modifier.height(iconBottomSpacing))

                // Responsive title font size
                val titleFontSize = when {
                    screenWidth < 400.dp -> 22.sp
                    screenWidth < 600.dp -> 27.sp
                    else -> 32.sp
                }
                
                // Title
                Text(
                    text = stringResource(id = R.string.no_internet_title),
                    fontFamily = Fonts.Poppins,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Responsive spacing after title
                val titleBottomSpacing = when {
                    screenHeight < 700.dp -> 12.dp
                    screenHeight < 1200.dp -> 14.dp
                    else -> 16.dp
                }
                
                Spacer(modifier = Modifier.height(titleBottomSpacing))

                // Responsive description font size
                val descriptionFontSize = when {
                    screenWidth < 400.dp -> 14.sp
                    screenWidth < 600.dp -> 16.sp
                    else -> 18.sp
                }
                
                // Responsive description line height
                val descriptionLineHeight = when {
                    screenWidth < 400.dp -> 20.sp
                    screenWidth < 600.dp -> 24.sp
                    else -> 28.sp
                }
                
                // Description
                Text(
                    text = stringResource(id = R.string.no_internet_description),
                    fontFamily = Fonts.Poppins,
                    fontSize = descriptionFontSize,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = descriptionLineHeight
                )

                // Responsive spacing before auto-restart message
                val descriptionBottomSpacing = when {
                    screenHeight < 700.dp -> 32.dp
                    screenHeight < 1200.dp -> 40.dp
                    else -> 48.dp
                }
                
                Spacer(modifier = Modifier.height(descriptionBottomSpacing))

                // Responsive auto-restart message padding
                val autoRestartHorizontalPadding = when {
                    screenWidth < 400.dp -> 20.dp
                    screenWidth < 600.dp -> 26.dp
                    else -> 32.dp
                }
                
                val autoRestartVerticalPadding = when {
                    screenHeight < 700.dp -> 12.dp
                    screenHeight < 1200.dp -> 14.dp
                    else -> 16.dp
                }
                
                // Responsive auto-restart font size
                val autoRestartFontSize = when {
                    screenWidth < 400.dp -> 11.sp
                    screenWidth < 600.dp -> 12.sp
                    else -> 14.sp
                }
                
                // Responsive auto-restart line height
                val autoRestartLineHeight = when {
                    screenWidth < 400.dp -> 16.sp
                    screenWidth < 600.dp -> 18.sp
                    else -> 20.sp
                }
                
                // Auto-restart message
                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .padding(horizontal = autoRestartHorizontalPadding, vertical = autoRestartVerticalPadding)
                ) {
                    Text(
                        text = stringResource(id = R.string.no_internet_auto_restart),
                        fontFamily = Fonts.Poppins,
                        fontSize = autoRestartFontSize,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = autoRestartLineHeight
                    )
                }
            }
        }
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun NoInternetScreenPreviewSmall() {
    NoInternetScreen()
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
private fun NoInternetScreenPreviewMedium() {
    NoInternetScreen()
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
private fun NoInternetScreenPreviewLarge() {
    NoInternetScreen()
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun NoInternetScreenPreview() {
    NoInternetScreen()
}

