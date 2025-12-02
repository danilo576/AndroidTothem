package com.fashiontothem.ff.presentation.common

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
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
fun ScanAndFindDialog(
    onDismiss: () -> Unit,
) {
    val poppins = Fonts.Poppins
    
    // Debounced dismiss to prevent rapid clicks
    val debouncedDismiss = rememberDebouncedClick(onClick = onDismiss)

    // Remember image IDs to avoid reloading
    val closeButtonId = remember { R.drawable.close_button }
    val findIconId = remember { R.drawable.find_item }
    val barcodeImageId = remember { R.drawable.barcode_find_item_image }

    Dialog(
        onDismissRequest = debouncedDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable { debouncedDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
            // Responsive dialog dimensions
            val dialogWidth = when {
                screenWidth < 400.dp -> (screenWidth * 0.95f).coerceAtMost(380.dp)
                screenWidth < 600.dp -> (screenWidth * 0.90f).coerceAtMost(550.dp)
                else -> (screenWidth * 0.98f).coerceAtMost(750.dp)
            }
            
            val dialogHeight = when {
                screenHeight < 700.dp -> (screenHeight * 0.92f).coerceAtMost(650.dp)
                screenHeight < 1200.dp -> (screenHeight * 0.93f).coerceAtMost(1000.dp)
                else -> (screenHeight * 0.95f).coerceAtMost(1200.dp)
            }
            
            // Responsive corner radius
            val cornerRadius = when {
                screenWidth < 400.dp -> 24.dp
                screenWidth < 600.dp -> 32.dp
                else -> 40.dp
            }
            
            // Responsive padding
            val cardPadding = when {
                screenWidth < 400.dp -> 10.dp
                screenWidth < 600.dp -> 14.dp
                else -> 16.dp
            }
            
            val contentPadding = when {
                screenWidth < 400.dp -> 18.dp
                screenWidth < 600.dp -> 22.dp
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
                        // Dialog content
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Responsive close button size
                            val closeButtonSize = when {
                                screenWidth < 400.dp -> 16.dp
                                screenWidth < 600.dp -> 24.dp
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
                                // Responsive find icon size
                                val findIconSize = when {
                                    screenWidth < 400.dp -> 40.dp
                                    screenWidth < 600.dp -> 50.dp
                                    else -> 120.dp
                                }
                                
                                val iconTitleSpacing = when {
                                    screenHeight < 700.dp -> 18.dp
                                    screenHeight < 1200.dp -> 20.dp
                                    else -> 70.dp
                                }
                                
                                // Top section with icon and title
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Find icon logo
                                    Image(
                                        painter = painterResource(id = findIconId),
                                        contentDescription = "Find Icon",
                                        modifier = Modifier.size(findIconSize),
                                        contentScale = ContentScale.Fit
                                    )

                                    Spacer(modifier = Modifier.height(iconTitleSpacing))

                                    // Responsive title font size
                                    val titleFontSize = when {
                                        screenWidth < 400.dp -> 18.sp
                                        screenWidth < 600.dp -> 20.sp
                                        else -> 34.sp
                                    }
                                    
                                    // Title
                                    Text(
                                        text = stringResource(id = R.string.scan_and_find_title),
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = titleFontSize,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )

                                    // Responsive title-description spacing
                                    val titleDescSpacing = when {
                                        screenHeight < 700.dp -> 10.dp
                                        screenHeight < 1200.dp -> 12.dp
                                        else -> 16.dp
                                    }
                                    
                                    Spacer(modifier = Modifier.height(titleDescSpacing))

                                    // Responsive description font size
                                    val descFontSize = when {
                                        screenWidth < 400.dp -> 10.sp
                                        screenWidth < 600.dp -> 12.sp
                                        else -> 26.sp
                                    }
                                    
                                    val descLineHeight = when {
                                        screenWidth < 400.dp -> 12.sp
                                        screenWidth < 600.dp -> 20.sp
                                        else -> 40.sp
                                    }
                                    
                                    // Description
                                    Text(
                                        text = stringResource(id = R.string.scan_and_find_description),
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = descFontSize,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center,
                                        lineHeight = descLineHeight
                                    )
                                }

                                // Responsive barcode size
                                val barcodeBaseSize = when {
                                    screenWidth < 400.dp -> 80.dp
                                    screenWidth < 600.dp -> 130.dp
                                    else -> 300.dp
                                }
                                
                                val barcodeScale = when {
                                    screenWidth < 400.dp -> 1f
                                    screenWidth < 600.dp -> 1.2f
                                    else -> 2f
                                }
                                
                                val barcodeBottomPadding = when {
                                    screenHeight < 700.dp -> 10.dp
                                    screenHeight < 1200.dp -> 20.dp
                                    else -> 50.dp
                                }
                                
                                // Middle section with barcode illustration
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Barcode illustration
                                    Image(
                                        modifier = Modifier
                                            .size(barcodeBaseSize)
                                            .scale(barcodeScale)
                                            .padding(bottom = barcodeBottomPadding),
                                        painter = painterResource(id = barcodeImageId),
                                        contentDescription = "Barcode",
                                        contentScale = ContentScale.Crop
                                    )

                                    // Responsive instruction spacing
                                    val instructionSpacing = when {
                                        screenHeight < 700.dp -> 8.dp
                                        screenHeight < 1200.dp -> 10.dp
                                        else -> 14.dp
                                    }

                                    // Instructions
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        InstructionItem(
                                            text = stringResource(id = R.string.scan_instruction_1),
                                            screenWidth = screenWidth,
                                            screenHeight = screenHeight
                                        )

                                        Spacer(modifier = Modifier.height(instructionSpacing))

                                        InstructionItem(
                                            text = stringResource(id = R.string.scan_instruction_2),
                                            color = Color(0xFF949494),
                                            screenWidth = screenWidth,
                                            screenHeight = screenHeight
                                        )
                                    }
                                }

                                // Bottom section with animated arrow
                                // Add spacing before arrow to ensure it's visible
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AnimatedScannerArrow(
                                        screenWidth = screenWidth,
                                        screenHeight = screenHeight
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

@Composable
private fun InstructionItem(
    text: String,
    color: Color = Color.White,
    screenWidth: Dp,
    screenHeight: Dp
) {
    val poppins = Fonts.Poppins
    
    // Responsive icon size
    val iconSize = when {
        screenWidth < 400.dp -> 10.dp
        screenWidth < 600.dp -> 14.dp
        else -> 20.dp
    }
    
    val iconTopPadding = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 10.dp
        else -> 12.dp
    }
    
    val iconTextSpacing = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 10.dp
        else -> 12.dp
    }
    
    // Responsive text font size
    val textFontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 16.sp
        else -> 24.sp
    }
    
    val textLineHeight = when {
        screenWidth < 400.dp -> 16.sp
        screenWidth < 600.dp -> 20.sp
        else -> 40.sp
    }

    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(id = R.drawable.round_silver_icon),
            contentDescription = "Silver Icon",
            modifier = Modifier
                .padding(top = iconTopPadding)
                .size(iconSize),
            tint = color
        )

        Spacer(modifier = Modifier.width(iconTextSpacing))

        Text(
            text = text,
            fontFamily = poppins,
            fontSize = textFontSize,
            color = color,
            fontWeight = FontWeight.Medium,
            lineHeight = textLineHeight,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AnimatedScannerArrow(
    screenWidth: Dp,
    screenHeight: Dp
) {
    val infinite = rememberInfiniteTransition(label = "scanner_arrow")

    // Glowing effect animation - start from higher alpha for better visibility
    val alpha by infinite.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Bounce animation
    val scale by infinite.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    // Responsive arrow size
    val arrowSize = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 70.dp
        else -> 80.dp
    }

    Box(
        modifier = Modifier
            .padding(top = 8.dp) // Add padding to ensure visibility
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        // Arrow pointing down
        Image(
            painter = painterResource(R.drawable.arrow_down),
            contentDescription = null,
            modifier = Modifier.size(arrowSize),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun ScanAndFindDialogPreviewSmall() {
    ScanAndFindDialog(
        onDismiss = { }
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun ScanAndFindDialogPreviewMedium() {
    ScanAndFindDialog(
        onDismiss = { }
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun ScanAndFindDialogPreviewLarge() {
    ScanAndFindDialog(
        onDismiss = { }
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun ScanAndFindDialogPreviewPhilips() {
    ScanAndFindDialog(
        onDismiss = { }
    )
}
