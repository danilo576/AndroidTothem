@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoyaltyDialog(
    onDismiss: () -> Unit,
) {
    val poppins = Fonts.Poppins
    
    // Debounced dismiss to prevent rapid clicks
    val debouncedDismiss = rememberDebouncedClick(onClick = onDismiss)

    // Remember image IDs to avoid reloading
    val closeButtonId = remember { R.drawable.close_button }
    val loyaltyRedLogoId = remember { R.drawable.loyalty_red_logo }
    val heartIconId = remember { R.drawable.heart_icon }
    val roundSilverIconId = remember { R.drawable.round_silver_icon }
    val loyaltyQrCodeId = remember { R.drawable.loyalty_qr_code }

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
                screenHeight < 1200.dp -> (screenHeight * 0.93f).coerceAtMost(850.dp)
                else -> (screenHeight * 0.95f).coerceAtMost(950.dp)
            }
            
            // Responsive corner radius
            val cornerRadius = when {
                screenWidth < 400.dp -> 24.dp
                screenWidth < 600.dp -> 32.dp
                else -> 40.dp
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
                    shape = RoundedCornerShape(cornerRadius),
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
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Responsive logo size
                                val logoSize = when {
                                    screenWidth < 400.dp -> 40.dp
                                    screenWidth < 600.dp -> 60.dp
                                    else -> 150.dp
                                }
                                
                                // Responsive spacing
                                val logoTitleSpacing = when {
                                    screenHeight < 700.dp -> 16.dp
                                    screenHeight < 1200.dp -> 20.dp
                                    else -> 24.dp
                                }
                                
                                // Top section with logo and title
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Logo at the top
                                    Image(
                                        painter = painterResource(id = loyaltyRedLogoId),
                                        contentDescription = "Logo",
                                        modifier = Modifier.size(logoSize),
                                        contentScale = ContentScale.Fit
                                    )

                                    Spacer(modifier = Modifier.height(logoTitleSpacing))

                                    // Responsive title font size
                                    val titleFontSize = when {
                                        screenWidth < 400.dp -> 18.sp
                                        screenWidth < 600.dp -> 22.sp
                                        else -> 34.sp
                                    }
                                    
                                    // Main title
                                    Text(
                                        text = stringResource(id = R.string.loyalty_welcome),
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = titleFontSize,
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
                                        screenWidth < 400.dp -> 12.sp
                                        screenWidth < 600.dp -> 18.sp
                                        else -> 26.sp
                                    }
                                    
                                    val subtitleLineHeight = when {
                                        screenWidth < 400.dp -> 18.sp
                                        screenWidth < 600.dp -> 20.sp
                                        else -> 40.sp
                                    }
                                    
                                    // Subtitle
                                    Text(
                                        text = stringResource(id = R.string.loyalty_intro),
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = subtitleFontSize,
                                        lineHeight = subtitleLineHeight,
                                        letterSpacing = 0.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFFDADADA)
                                    )
                                }

                                // Responsive spacing between sections
                                val sectionSpacing = when {
                                    screenHeight < 700.dp -> 12.dp
                                    screenHeight < 1200.dp -> 18.dp
                                    else -> 32.dp
                                }
                                
                                Spacer(modifier = Modifier.height(sectionSpacing))

                                // Responsive icon size
                                val iconSize = when {
                                    screenWidth < 400.dp -> 12.dp
                                    screenWidth < 600.dp -> 16.dp
                                    else -> 20.dp
                                }
                                
                                val iconTextSpacing = when {
                                    screenWidth < 400.dp -> 6.dp
                                    screenWidth < 600.dp -> 8.dp
                                    else -> 12.dp
                                }
                                
                                val benefitItemSpacing = when {
                                    screenHeight < 700.dp -> 8.dp
                                    screenHeight < 1200.dp -> 10.dp
                                    else -> 12.dp
                                }
                                
                                // Responsive benefit text font size
                                val benefitFontSize = when {
                                    screenWidth < 400.dp -> 12.sp
                                    screenWidth < 600.dp -> 14.sp
                                    else -> 24.sp
                                }
                                
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
                                            modifier = Modifier.size(iconSize)
                                        )
                                        Spacer(modifier = Modifier.width(iconTextSpacing))
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
                                                    append(" " + stringResource(id = R.string.discount_on_full_price))
                                                }
                                            },
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = benefitFontSize,
                                            letterSpacing = 0.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(benefitItemSpacing))

                                    // Second benefit with heart icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = heartIconId),
                                            contentDescription = "Heart Icon",
                                            modifier = Modifier.size(iconSize)
                                        )
                                        Spacer(modifier = Modifier.width(iconTextSpacing))
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
                                                    append(" " + stringResource(id = R.string.discount_on_sale))
                                                }
                                            },
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = benefitFontSize,
                                            letterSpacing = 0.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(benefitItemSpacing))

                                    // Third benefit with silver icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.round_silver_icon),
                                            contentDescription = "Silver Icon",
                                            modifier = Modifier.size(iconSize)
                                        )
                                        Spacer(modifier = Modifier.width(iconTextSpacing))
                                        Text(
                                            text = stringResource(id = R.string.early_shopping),
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = benefitFontSize,
                                            letterSpacing = 0.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(benefitItemSpacing))

                                    // Fourth benefit with silver icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.round_silver_icon),
                                            contentDescription = "Silver Icon",
                                            modifier = Modifier.size(iconSize)
                                        )
                                        Spacer(modifier = Modifier.width(iconTextSpacing))
                                        Text(
                                            text = stringResource(id = R.string.personal_shopper),
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = benefitFontSize,
                                            letterSpacing = 0.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(benefitItemSpacing))

                                    // Fifth benefit with silver icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = roundSilverIconId),
                                            contentDescription = "Silver Icon",
                                            modifier = Modifier.size(iconSize),
                                            colorFilter = ColorFilter.tint(color = Color(0xFF949494))
                                        )
                                        Spacer(modifier = Modifier.width(iconTextSpacing))
                                        Text(
                                            text = stringResource(id = R.string.more_benefits),
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = benefitFontSize,
                                            letterSpacing = 0.sp,
                                            color = Color(0xFF949494)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(sectionSpacing))

                                // Responsive QR code section
                                val qrCodeSize = when {
                                    screenWidth < 400.dp -> 60.dp
                                    screenWidth < 600.dp -> 100.dp
                                    else -> 200.dp
                                }
                                
                                // Responsive QR section text font sizes
                                val qrTitleFontSize = when {
                                    screenWidth < 400.dp -> 16.sp
                                    screenWidth < 600.dp -> 18.sp
                                    else -> 24.sp
                                }
                                
                                val qrSubtitleFontSize = when {
                                    screenWidth < 400.dp -> 14.sp
                                    screenWidth < 600.dp -> 14.sp
                                    else -> 22.sp
                                }
                                
                                val qrSubtitleLineHeight = when {
                                    screenWidth < 400.dp -> 20.sp
                                    screenWidth < 600.dp -> 26.sp
                                    else -> 35.sp
                                }
                                
                                val qrTextQrSpacing = when {
                                    screenWidth < 400.dp -> 10.dp
                                    screenWidth < 600.dp -> 12.dp
                                    else -> 16.dp
                                }
                                
                                val qrTextSpacing = when {
                                    screenHeight < 700.dp -> 6.dp
                                    else -> 8.dp
                                }
                                
                                // Bottom section with QR code
                                // For small screens, stack vertically; for larger screens, use horizontal layout
                                if (screenWidth < 400.dp) {
                                    // Vertical layout for small screens
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // QR code on top for small screens
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                            modifier = Modifier.size(qrCodeSize)
                                        ) {
                                            Image(
                                                painter = painterResource(id = loyaltyQrCodeId),
                                                contentDescription = "Loyalty QR Code",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(qrTextQrSpacing))
                                        
                                        // Text below
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.scan_qr_code),
                                                fontFamily = poppins,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = qrTitleFontSize,
                                                letterSpacing = 0.sp,
                                                textAlign = TextAlign.Center,
                                                color = Color.White
                                            )

                                            Spacer(modifier = Modifier.height(qrTextSpacing))

                                            Text(
                                                text = stringResource(id = R.string.loyalty_learn_more),
                                                fontFamily = poppins,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = qrSubtitleFontSize,
                                                lineHeight = qrSubtitleLineHeight,
                                                letterSpacing = 0.sp,
                                                textAlign = TextAlign.Center,
                                                color = Color(0xFF949494)
                                            )
                                        }
                                    }
                                } else {
                                    // Horizontal layout for larger screens
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left side - text (50% width)
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = qrTextQrSpacing),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.scan_qr_code),
                                                fontFamily = poppins,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = qrTitleFontSize,
                                                letterSpacing = 0.sp,
                                                textAlign = TextAlign.Start,
                                                color = Color.White
                                            )

                                            Spacer(modifier = Modifier.height(qrTextSpacing))

                                            Text(
                                                text = stringResource(id = R.string.loyalty_learn_more),
                                                fontFamily = poppins,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = qrSubtitleFontSize,
                                                lineHeight = qrSubtitleLineHeight,
                                                letterSpacing = 0.sp,
                                                textAlign = TextAlign.Start,
                                                color = Color(0xFF949494)
                                            )
                                        }

                                        // Right side - QR code (50% width)
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                            modifier = Modifier.size(qrCodeSize)
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
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun LoyaltyDialogPreviewSmall() {
    LoyaltyDialog(
        onDismiss = { }
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun LoyaltyDialogPreviewMedium() {
    LoyaltyDialog(
        onDismiss = { }
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun LoyaltyDialogPreviewLarge() {
    LoyaltyDialog(
        onDismiss = { }
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun LoyaltyDialogPreviewPhilips() {
    LoyaltyDialog(
        onDismiss = { }
    )
}
