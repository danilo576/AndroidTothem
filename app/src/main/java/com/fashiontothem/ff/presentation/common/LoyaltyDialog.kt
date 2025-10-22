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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoyaltyDialog(
    onDismiss: () -> Unit,
) {
    val poppins = Fonts.Poppins

    // Remember image IDs to avoid reloading
    val closeButtonId = remember { R.drawable.close_button }
    val loyaltyRedLogoId = remember { R.drawable.loyalty_red_logo }
    val heartIconId = remember { R.drawable.heart_icon }
    val roundSilverIconId = remember { R.drawable.round_silver_icon }
    val loyaltyQrCodeId = remember { R.drawable.loyalty_qr_code }

    // Remember benefits list
    val benefitsList = remember {
        listOf(
            Triple("20%", " popusta na punu cenu", true),
            Triple("10%", " popusta na već sniženo", true),
            Triple("", "Kupovina sa popustom pre ostalih", false),
            Triple("", "Personal shopper usluga", false),
            Triple("", "I još mnogo sjajnih benefita", false)
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
            val dialogHeight = (maxHeight * 0.95f).coerceAtMost(950.dp)

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
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                remember {
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
                            )
                    ) {

                        // Layer 5: Dialog content (on top of gradients)
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
                                    painter = painterResource(id = closeButtonId),
                                    contentDescription = "Close",
                                    modifier = Modifier.size(50.dp)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Top section with logo and title
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Logo at the top
                                    Image(
                                        painter = painterResource(id = loyaltyRedLogoId),
                                        contentDescription = "Logo",
                                        modifier = Modifier.size(150.dp)
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Main title
                                    Text(
                                        text = "Dobrodošli u Forever Friends!",
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 34.sp,
                                        letterSpacing = 0.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Subtitle
                                    Text(
                                        text = "Postani član programa lojalnosti,\nsakupljaj poene i ostvari i do:",
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 26.sp,
                                        lineHeight = 40.sp,
                                        letterSpacing = 0.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFFDADADA)
                                    )
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Middle section with benefits
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    // First benefit with heart icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = heartIconId),
                                            contentDescription = "Heart Icon",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(
                                                    style = SpanStyle(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB50938)
                                                    )
                                                ) {
                                                    append("20%")
                                                }
                                                withStyle(style = SpanStyle(color = Color.White)) {
                                                    append(" popusta na punu cenu")
                                                }
                                            },
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 24.sp,
                                            letterSpacing = 0.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Second benefit with heart icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = heartIconId),
                                            contentDescription = "Heart Icon",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(
                                                    style = SpanStyle(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB50938)
                                                    )
                                                ) {
                                                    append("10%")
                                                }
                                                withStyle(style = SpanStyle(color = Color.White)) {
                                                    append(" popusta na već sniženo")
                                                }
                                            },
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 24.sp,
                                            letterSpacing = 0.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Third benefit with silver icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.round_silver_icon),
                                            contentDescription = "Silver Icon",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Kupovina sa popustom pre ostalih",
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 24.sp,
                                            letterSpacing = 0.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Fourth benefit with silver icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.round_silver_icon),
                                            contentDescription = "Silver Icon",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Personal shopper usluga",
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 24.sp,
                                            letterSpacing = 0.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Fifth benefit with silver icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = roundSilverIconId),
                                            contentDescription = "Silver Icon",
                                            modifier = Modifier.size(20.dp),
                                            colorFilter = ColorFilter.tint(color = Color(0xFF949494))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "I još mnogo sjajnih benefita",
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 24.sp,
                                            letterSpacing = 0.sp,
                                            color = Color(0xFF949494)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Bottom section with QR code
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left side - text (50% width)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = "Skeniraj QR Code",
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 24.sp,
                                            letterSpacing = 0.sp,
                                            textAlign = TextAlign.Start,
                                            color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Saznaj više o benefitima i postani član programa lojalnosti!",
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 22.sp,
                                            lineHeight = 35.sp,
                                            letterSpacing = 0.sp,
                                            textAlign = TextAlign.Start,
                                            color = Color(0xFF949494)
                                        )
                                    }

                                    // Right side - QR code (50% width)
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .size(200.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = loyaltyQrCodeId),
                                            contentDescription = "Loyalty QR Code",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoyaltyDialogPreview() {
    LoyaltyDialog(
        onDismiss = { }
    )
}
