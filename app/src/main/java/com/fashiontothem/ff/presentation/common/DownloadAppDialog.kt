package com.fashiontothem.ff.presentation.common

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DownloadAppDialog(
    onDismiss: () -> Unit,
) {
    val poppins = Fonts.Poppins
    
    // Debounced dismiss to prevent rapid clicks
    val debouncedDismiss = rememberDebouncedClick(onClick = onDismiss)
    
    // Remember image IDs to avoid reloading
    val closeButtonId = remember { R.drawable.close_button }
    val downloadAppId = remember { R.drawable.download_app }
    val googlePlayQrId = remember { R.drawable.google_play_download_qr }
    val appStoreQrId = remember { R.drawable.app_store_download_qr }
    
    // Memoized values to reduce allocations on recomposition
    val dialogShape = remember { RoundedCornerShape(40.dp) }
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Black,
                Color(0xFF1A0033),
                Color(0xFF00004D)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val dialogWidth = (maxWidth * 0.98f).coerceAtMost(700.dp)
            val dialogHeight = (maxHeight * 0.95f).coerceAtMost(900.dp)

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
                    .clickable { /* Prevent dialog close when clicking on card */ },
                shape = dialogShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush)
                ) {

                    // Layer 4: Dialog content (on top of gradients)
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Close button
                        IconButton(
                            onClick = debouncedDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = closeButtonId),
                                contentDescription = "Close",
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Top section with logo and title
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))

                                // F&F Logo
                                Image(
                                    painter = painterResource(id = downloadAppId),
                                    contentDescription = "F&F Logo",
                                    modifier = Modifier.size(120.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Main title
                                Text(
                                    text = stringResource(id = R.string.download_app_title),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 34.sp,
                                    lineHeight = 34.sp,
                                    letterSpacing = 0.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Subtitle
                                Text(
                                    text = stringResource(id = R.string.download_app_subtitle),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 26.sp,
                                    lineHeight = 40.sp,
                                    letterSpacing = 0.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFDADADA)
                                )
                            }

                            // Middle section with call to action
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Download text
                                Text(
                                    text = stringResource(id = R.string.download_ff_app),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 26.sp,
                                    lineHeight = 26.sp,
                                    letterSpacing = 0.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFDADADA)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Enjoy text
                                Text(
                                    text = stringResource(id = R.string.enjoy_shopping),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 24.sp,
                                    lineHeight = 35.sp,
                                    letterSpacing = 0.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF949494)
                                )
                            }

                            // Bottom section with QR codes
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // QR Codes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Google Play QR
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                            modifier = Modifier.size(200.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = googlePlayQrId),
                                                contentDescription = "Google Play QR Code",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                            )
                                        }
                                    }

                                    // App Store QR
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                            modifier = Modifier.size(200.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = appStoreQrId),
                                                contentDescription = "App Store QR Code",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun DownloadAppDialogPreviewPhilips() {
    DownloadAppDialog(
        onDismiss = { }
    )
}
