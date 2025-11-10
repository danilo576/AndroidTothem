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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            val dialogWidth = (maxWidth * 0.98f).coerceAtMost(700.dp)
            val dialogHeight = (maxHeight * 0.95f).coerceAtMost(840.dp)

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
                        .padding(16.dp)
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
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
                                .padding(horizontal = 32.dp)
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = headerIcon,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.height(36.dp))

                                Text(
                                    text = stringResource(R.string.barcode_scanned_success_title),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 32.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = stringResource(R.string.barcode_scanned_searching),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = stringResource(R.string.barcode_scanned_wait_message),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            AnimatedBarcodeImage(modifier = Modifier.size(280.dp))

                            Text(
                                text = barcode,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp,
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
private fun AnimatedBarcodeImage(modifier: Modifier = Modifier) {
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

