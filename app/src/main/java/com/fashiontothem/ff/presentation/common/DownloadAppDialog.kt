@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    
    // Memoized background brush
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
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
            // Responsive dialog dimensions
            val dialogWidth = when {
                screenWidth < 400.dp -> (screenWidth * 0.95f).coerceAtMost(380.dp)
                screenWidth < 600.dp -> (screenWidth * 0.90f).coerceAtMost(550.dp)
                else -> (screenWidth * 0.98f).coerceAtMost(700.dp)
            }
            
            val dialogHeight = when {
                screenHeight < 700.dp -> (screenHeight * 0.92f).coerceAtMost(650.dp)
                screenHeight < 1200.dp -> (screenHeight * 0.93f).coerceAtMost(800.dp)
                else -> (screenHeight * 0.95f).coerceAtMost(900.dp)
            }
            
            // Responsive corner radius
            val dialogShape = remember { 
                RoundedCornerShape(
                    when {
                        screenWidth < 400.dp -> 24.dp
                        screenWidth < 600.dp -> 32.dp
                        else -> 40.dp
                    }
                )
            }
            
            // Responsive padding
            val cardPadding = when {
                screenWidth < 400.dp -> 12.dp
                screenWidth < 600.dp -> 16.dp
                else -> 16.dp
            }
            
            val contentPadding = when {
                screenWidth < 400.dp -> 20.dp
                screenWidth < 600.dp -> 24.dp
                else -> 32.dp
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
                    .padding(cardPadding)
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
                        // Responsive close button size
                        val closeButtonSize = when {
                            screenWidth < 400.dp -> 20.dp
                            screenWidth < 600.dp -> 30.dp
                            else -> 50.dp
                        }
                        
                        val closeButtonPadding = when {
                            screenWidth < 400.dp -> 8.dp
                            screenWidth < 600.dp -> 10.dp
                            else -> 12.dp
                        }
                        
                        // Close button
                        IconButton(
                            onClick = debouncedDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(closeButtonPadding)
                        ) {
                            Image(
                                painter = painterResource(id = closeButtonId),
                                contentDescription = "Close",
                                modifier = Modifier.size(closeButtonSize)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Responsive top spacing
                            val topSpacing = when {
                                screenHeight < 700.dp -> 8.dp
                                screenHeight < 1200.dp -> 12.dp
                                else -> 16.dp
                            }
                            
                            // Responsive logo size
                            val logoSize = when {
                                screenWidth < 400.dp -> 40.dp
                                screenWidth < 600.dp -> 60.dp
                                else -> 120.dp
                            }
                            
                            // Top section with logo and title
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(topSpacing))

                                // F&F Logo
                                Image(
                                    painter = painterResource(id = downloadAppId),
                                    contentDescription = "F&F Logo",
                                    modifier = Modifier.size(logoSize),
                                    contentScale = ContentScale.Fit
                                )

                                // Responsive logo-title spacing
                                val logoTitleSpacing = when {
                                    screenHeight < 700.dp -> 12.dp
                                    screenHeight < 1200.dp -> 18.dp
                                    else -> 24.dp
                                }
                                
                                Spacer(modifier = Modifier.height(logoTitleSpacing))

                                // Responsive title font size
                                val titleFontSize = when {
                                    screenWidth < 400.dp -> 20.sp
                                    screenWidth < 600.dp -> 24.sp
                                    else -> 34.sp
                                }
                                
                                // Main title
                                Text(
                                    text = stringResource(id = R.string.download_app_title),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = titleFontSize,
                                    lineHeight = titleFontSize,
                                    letterSpacing = 0.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )

                                // Responsive title-subtitle spacing
                                val titleSubtitleSpacing = when {
                                    screenHeight < 700.dp -> 10.dp
                                    screenHeight < 1200.dp -> 12.dp
                                    else -> 16.dp
                                }
                                
                                Spacer(modifier = Modifier.height(titleSubtitleSpacing))

                                // Responsive subtitle font size
                                val subtitleFontSize = when {
                                    screenWidth < 400.dp -> 18.sp
                                    screenWidth < 600.dp -> 22.sp
                                    else -> 26.sp
                                }
                                
                                val subtitleLineHeight = when {
                                    screenWidth < 400.dp -> 26.sp
                                    screenWidth < 600.dp -> 32.sp
                                    else -> 40.sp
                                }
                                
                                // Subtitle
                                Text(
                                    text = stringResource(id = R.string.download_app_subtitle),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = subtitleFontSize,
                                    lineHeight = subtitleLineHeight,
                                    letterSpacing = 0.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFDADADA)
                                )
                            }

                            // Middle section with call to action
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Responsive call to action font sizes
                                val ctaFontSize = when {
                                    screenWidth < 400.dp -> 20.sp
                                    screenWidth < 600.dp -> 22.sp
                                    else -> 26.sp
                                }
                                
                                val enjoyFontSize = when {
                                    screenWidth < 400.dp -> 18.sp
                                    screenWidth < 600.dp -> 20.sp
                                    else -> 24.sp
                                }
                                
                                val enjoyLineHeight = when {
                                    screenWidth < 400.dp -> 24.sp
                                    screenWidth < 600.dp -> 28.sp
                                    else -> 35.sp
                                }
                                
                                val ctaSpacing = when {
                                    screenHeight < 700.dp -> 8.dp
                                    else -> 12.dp
                                }
                                
                                // Download text
                                Text(
                                    text = stringResource(id = R.string.download_ff_app),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = ctaFontSize,
                                    lineHeight = ctaFontSize,
                                    letterSpacing = 0.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFDADADA)
                                )

                                Spacer(modifier = Modifier.height(ctaSpacing))

                                // Enjoy text
                                Text(
                                    text = stringResource(id = R.string.enjoy_shopping),
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = enjoyFontSize,
                                    lineHeight = enjoyLineHeight,
                                    letterSpacing = 0.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF949494)
                                )
                            }

                            // Responsive QR code sizes
                            val qrCodeSize = when {
                                screenWidth < 400.dp -> 110.dp
                                screenWidth < 600.dp -> 140.dp
                                else -> 200.dp
                            }
                            
                            val qrSpacing = when {
                                screenWidth < 400.dp -> 8.dp
                                screenWidth < 600.dp -> 12.dp
                                else -> 16.dp
                            }
                            
                            val qrCardPadding = when {
                                screenWidth < 400.dp -> 8.dp
                                screenWidth < 600.dp -> 10.dp
                                else -> 12.dp
                            }
                            
                            val qrBottomSpacing = when {
                                screenHeight < 700.dp -> 8.dp
                                screenHeight < 1200.dp -> 12.dp
                                else -> 16.dp
                            }
                            
                            // Bottom section with QR codes
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // QR Codes - for small screens, stack vertically; for larger screens, horizontal
                                if (screenWidth < 400.dp) {
                                    // Vertical layout for small screens
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(qrSpacing)
                                    ) {
                                        // Google Play QR
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                            modifier = Modifier.size(qrCodeSize)
                                        ) {
                                            Image(
                                                painter = painterResource(id = googlePlayQrId),
                                                contentDescription = "Google Play QR Code",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(qrCardPadding)
                                            )
                                        }

                                        // App Store QR
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                            modifier = Modifier.size(qrCodeSize)
                                        ) {
                                            Image(
                                                painter = painterResource(id = appStoreQrId),
                                                contentDescription = "App Store QR Code",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(qrCardPadding)
                                            )
                                        }
                                    }
                                } else {
                                    // Horizontal layout for larger screens
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(qrSpacing)
                                    ) {
                                        // Google Play QR
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                                modifier = Modifier.size(qrCodeSize)
                                            ) {
                                                Image(
                                                    painter = painterResource(id = googlePlayQrId),
                                                    contentDescription = "Google Play QR Code",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(qrCardPadding)
                                                )
                                            }
                                        }

                                        // App Store QR
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                                modifier = Modifier.size(qrCodeSize)
                                            ) {
                                                Image(
                                                    painter = painterResource(id = appStoreQrId),
                                                    contentDescription = "App Store QR Code",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(qrCardPadding)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(qrBottomSpacing))
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun DownloadAppDialogPreviewSmall() {
    DownloadAppDialog(
        onDismiss = { }
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun DownloadAppDialogPreviewMedium() {
    DownloadAppDialog(
        onDismiss = { }
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun DownloadAppDialogPreviewLarge() {
    DownloadAppDialog(
        onDismiss = { }
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun DownloadAppDialogPreviewPhilips() {
    DownloadAppDialog(
        onDismiss = { }
    )
}
