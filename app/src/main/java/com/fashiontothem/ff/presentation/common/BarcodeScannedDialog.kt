package com.fashiontothem.ff.presentation.common

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R
import android.graphics.Color as AndroidColor

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BarcodeScannedDialog(
    barcode: String,
) {
    val poppins = Fonts.Poppins
    val headerIcon = painterResource(id = R.drawable.find_item)

    Dialog(
        onDismissRequest = { /* Block dismissal while processing */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
            // Responsive dialog dimensions
            val dialogWidth = when {
                screenWidth < 400.dp -> (screenWidth * 0.95f).coerceAtMost(350.dp)
                screenWidth < 600.dp -> (screenWidth * 0.95f).coerceAtMost(500.dp)
                else -> (screenWidth * 0.98f).coerceAtMost(700.dp)
            }
            
            val dialogHeight = when {
                screenHeight < 700.dp -> (screenHeight * 0.90f).coerceAtMost(600.dp)
                screenHeight < 1200.dp -> (screenHeight * 0.92f).coerceAtMost(720.dp)
                else -> (screenHeight * 0.95f).coerceAtMost(840.dp)
            }
            
            // Responsive corner radius
            val cornerRadius = when {
                screenWidth < 400.dp -> 24.dp
                screenWidth < 600.dp -> 32.dp
                else -> 40.dp
            }
            
            // Responsive elevation
            val elevation = when {
                screenWidth < 400.dp -> 8.dp
                screenWidth < 600.dp -> 10.dp
                else -> 12.dp
            }
            
            // Responsive padding
            val outerPadding = when {
                screenWidth < 400.dp -> 12.dp
                screenWidth < 600.dp -> 14.dp
                else -> 16.dp
            }
            
            val horizontalPadding = when {
                screenWidth < 400.dp -> 20.dp
                screenWidth < 600.dp -> 26.dp
                else -> 32.dp
            }
            
            val verticalPadding = when {
                screenHeight < 700.dp -> 24.dp
                screenHeight < 1200.dp -> 32.dp
                else -> 40.dp
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 180)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(durationMillis = 120))
            ) {
                Card(
                    modifier = Modifier
                        .width(dialogWidth)
                        .height(dialogHeight)
                        .padding(outerPadding)
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(cornerRadius),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black,
                                        Color(0xFF1A0033),
                                        Color(0xFF00004D)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = horizontalPadding)
                                .padding(vertical = verticalPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Responsive icon size
                                val iconSize = when {
                                    screenWidth < 400.dp -> 40.dp
                                    screenWidth < 600.dp -> 60.dp
                                    else -> 80.dp
                                }
                                
                                Image(
                                    painter = headerIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(iconSize),
                                    contentScale = ContentScale.Fit
                                )

                                // Responsive spacing
                                val topSpacing = when {
                                    screenHeight < 700.dp -> 20.dp
                                    screenHeight < 1200.dp -> 28.dp
                                    else -> 36.dp
                                }
                                
                                Spacer(modifier = Modifier.height(topSpacing))

                                // Responsive title font size
                                val titleFontSize = when {
                                    screenWidth < 400.dp -> 20.sp
                                    screenWidth < 600.dp -> 26.sp
                                    else -> 32.sp
                                }
                                
                                Text(
                                    text = stringResource(R.string.barcode_scanned_success_title),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = titleFontSize,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                // Responsive spacing
                                val titleBottomSpacing = when {
                                    screenHeight < 700.dp -> 12.dp
                                    screenHeight < 1200.dp -> 15.dp
                                    else -> 18.dp
                                }
                                
                                Spacer(modifier = Modifier.height(titleBottomSpacing))

                                // Responsive searching font size
                                val searchingFontSize = when {
                                    screenWidth < 400.dp -> 16.sp
                                    screenWidth < 600.dp -> 20.sp
                                    else -> 24.sp
                                }
                                
                                Text(
                                    text = stringResource(R.string.barcode_scanned_searching),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = searchingFontSize,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                // Responsive spacing
                                val searchingBottomSpacing = when {
                                    screenHeight < 700.dp -> 6.dp
                                    screenHeight < 1200.dp -> 8.dp
                                    else -> 10.dp
                                }
                                
                                Spacer(modifier = Modifier.height(searchingBottomSpacing))

                                // Responsive wait message font size
                                val waitMessageFontSize = when {
                                    screenWidth < 400.dp -> 12.sp
                                    screenWidth < 600.dp -> 15.sp
                                    else -> 18.sp
                                }
                                
                                Text(
                                    text = stringResource(R.string.barcode_scanned_wait_message),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = waitMessageFontSize,
                                    color = Color.White.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Responsive animation size
                            val animationSize = when {
                                screenWidth < 400.dp -> 180.dp
                                screenWidth < 600.dp -> 220.dp
                                else -> 280.dp
                            }
                            
                            AnimatedBarcodeImage(
                                modifier = Modifier.size(animationSize),
                                screenWidth = screenWidth
                            )

                            // Responsive barcode font size
                            val barcodeFontSize = when {
                                screenWidth < 400.dp -> 14.sp
                                screenWidth < 600.dp -> 17.sp
                                else -> 20.sp
                            }
                            
                            Text(
                                text = barcode,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = barcodeFontSize,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedBarcodeImage(
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("scanner_animation.json"),
        cacheKey = "barcode_scanner_animation"
    )

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = AndroidColor.WHITE,
            "**",
            "Fill 1"
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = Color(0xFFB50937).toArgb(),
            "Line Outlines",
            "**",
            "Fill 1"
        )
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 2f
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
        dynamicProperties = dynamicProperties
    )
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun BarcodeScannedDialogPreviewSmall() {
    BarcodeScannedDialog(barcode = "1234567890123")
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
private fun BarcodeScannedDialogPreviewMedium() {
    BarcodeScannedDialog(barcode = "1234567890123")
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
private fun BarcodeScannedDialogPreviewLarge() {
    BarcodeScannedDialog(barcode = "1234567890123")
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun BarcodeScannedDialogPreviewPhilips() {
    BarcodeScannedDialog(barcode = "1234567890123")
}

