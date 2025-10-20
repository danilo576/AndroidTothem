package com.fashiontothem.ff.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R

@Composable
fun DownloadAppDialog(
    onDismiss: () -> Unit
) {
    val poppins = Fonts.Poppins
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A2A)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
            Box {
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
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // F&F Logo
                    Image(
                        painter = painterResource(id = R.drawable.download_app),
                        contentDescription = "F&F Logo",
                        modifier = Modifier.size(100.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
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
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
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
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
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
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
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
                                modifier = Modifier.size(140.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.google_play_download_qr),
                                    contentDescription = "Google Play QR Code",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Google Play icon (simplified as text)
                                Text(
                                    text = "▶",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Google Play",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium
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
                                modifier = Modifier.size(140.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_store_download_qr),
                                    contentDescription = "App Store QR Code",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // App Store icon (simplified as text)
                                Text(
                                    text = "🍎",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "App Store",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(name = "Download Dialog", showBackground = true)
@Composable
fun DownloadAppDialogPreview() {
    DownloadAppDialog(
        onDismiss = { }
    )
}
