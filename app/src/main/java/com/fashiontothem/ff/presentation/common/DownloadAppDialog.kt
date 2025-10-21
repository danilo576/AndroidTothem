package com.fashiontothem.ff.presentation.common

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DownloadAppDialog(
    onDismiss: () -> Unit,
) {
    val poppins = Fonts.Poppins

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

            Card(
                modifier = Modifier
                    .width(dialogWidth)
                    .height(dialogHeight)
                    .padding(16.dp)
                    .clickable { /* Prevent dialog close when clicking on card */ },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Layer 1: Base vertical gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black,
                                        Color(0xFF1A0033), // Dark purple
                                        Color(0xFF00004D)  // Dark blue
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )

                    // Layer 2: Bottom-left cyan glow
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(200.dp)
                            .graphicsLayer(alpha = 0.3f)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00FFFF).copy(alpha = 0.15f),
                                        Color.Transparent
                                    ),
                                    center = Offset(0f, 1f),
                                    radius = 200f
                                )
                            )
                    )

                    // Layer 3: Bottom-right magenta glow
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(200.dp)
                            .graphicsLayer(alpha = 0.3f)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF00FF).copy(alpha = 0.15f),
                                        Color.Transparent
                                    ),
                                    center = Offset(1f, 1f),
                                    radius = 200f
                                )
                            )
                    )

                    // Layer 4: Top-right subtle glow
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(150.dp)
                            .graphicsLayer(alpha = 0.2f)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00FF88).copy(alpha = 0.1f),
                                        Color.Transparent
                                    ),
                                    center = Offset(1f, 0f),
                                    radius = 150f
                                )
                            )
                    )

                    // Layer 4: Dialog content (on top of gradients)
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.close_button),
                                contentDescription = "Close",
                                modifier = Modifier.size(34.dp)
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
                                    painter = painterResource(id = R.drawable.download_app),
                                    contentDescription = "F&F Logo",
                                    modifier = Modifier.size(120.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Main title
                                Text(
                                    text = "Preuzmi app i kupuj na klik!",
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
                                    text = "Više od 40 vodećih svetskih modnih brendova su samo na klik od tebe.",
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
                                    text = "Preuzmi F&F aplikaciju",
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
                                    text = "Uživaj u kupovini bilo kada i bilo gde!",
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
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Google Play QR
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier.size(160.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.google_play_download_qr),
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
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier.size(160.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.app_store_download_qr),
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