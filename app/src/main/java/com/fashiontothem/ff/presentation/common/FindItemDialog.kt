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
import androidx.compose.ui.draw.scale
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
import com.fashiontothem.ff.util.clickableDebounced
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FindItemDialog(
    onDismiss: () -> Unit,
    onScanAndFind: () -> Unit,
    onFilterAndFind: () -> Unit,
    onVisualSearch: () -> Unit,
) {
    val poppins = Fonts.Poppins
    
    // Debounced callbacks to prevent rapid clicks
    val debouncedDismiss = rememberDebouncedClick(onClick = onDismiss)
    val debouncedScanAndFind = rememberDebouncedClick(onClick = onScanAndFind)
    val debouncedFilterAndFind = rememberDebouncedClick(onClick = onFilterAndFind)
    val debouncedVisualSearch = rememberDebouncedClick(onClick = onVisualSearch)

    // Remember image IDs to avoid reloading
    val closeButtonId = remember { R.drawable.close_button }
    val findIconId = remember { R.drawable.find_item }
    val barcodeIconId = remember { R.drawable.barcode }
    val filterAndFindIconId = remember { R.drawable.filter_and_find_icon }
    val visualSearchIconId = remember { R.drawable.visual_search_icon }

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
                .clickableDebounced { debouncedDismiss() },
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
                else -> (screenHeight * 0.95f).coerceAtMost(900.dp)
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
                        // Dialog content
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
                                // Responsive find icon size
                                val findIconSize = when {
                                    screenWidth < 400.dp -> 60.dp
                                    screenWidth < 600.dp -> 80.dp
                                    else -> 120.dp
                                }
                                
                                val iconBottomSpacing = when {
                                    screenHeight < 700.dp -> 24.dp
                                    screenHeight < 1200.dp -> 32.dp
                                    else -> 50.dp
                                }
                                
                                // Top section with logo
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

                                    Spacer(modifier = Modifier.height(iconBottomSpacing))
                                }

                                // Responsive button dimensions
                                val buttonHeight = when {
                                    screenHeight < 700.dp -> 100.dp
                                    screenHeight < 1200.dp -> 130.dp
                                    else -> 170.dp
                                }
                                
                                val buttonSpacing = when {
                                    screenHeight < 700.dp -> 16.dp
                                    screenHeight < 1200.dp -> 20.dp
                                    else -> 30.dp
                                }
                                
                                val buttonCornerRadius = when {
                                    screenWidth < 400.dp -> 20.dp
                                    screenWidth < 600.dp -> 24.dp
                                    else -> 30.dp
                                }
                                
                                val buttonPadding = when {
                                    screenWidth < 400.dp -> 12.dp
                                    screenWidth < 600.dp -> 16.dp
                                    else -> 20.dp
                                }
                                
                                // Responsive font sizes
                                val buttonTextSize = when {
                                    screenWidth < 400.dp -> 18.sp
                                    screenWidth < 600.dp -> 22.sp
                                    else -> 28.sp
                                }
                                
                                // Responsive icon sizes
                                val buttonIconSize = when {
                                    screenWidth < 400.dp -> 40.dp
                                    screenWidth < 600.dp -> 60.dp
                                    else -> 80.dp
                                }
                                
                                val iconTextSpacing = when {
                                    screenHeight < 700.dp -> 8.dp
                                    screenHeight < 1200.dp -> 10.dp
                                    else -> 12.dp
                                }
                                
                                // Responsive barcode scale
                                val barcodeScale = when {
                                    screenWidth < 400.dp -> 1.5f
                                    screenWidth < 600.dp -> 2f
                                    else -> 3f
                                }
                                
                                val barcodeSpacer = when {
                                    screenWidth < 400.dp -> 30.dp
                                    screenWidth < 600.dp -> 45.dp
                                    else -> 60.dp
                                }
                                
                                // Middle section with three buttons
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(buttonSpacing)
                                ) {
                                    // Scan and Find button
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(buttonHeight)
                                            .clickableDebounced { debouncedScanAndFind() },
                                        shape = RoundedCornerShape(buttonCornerRadius),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color(0xFF4F0418),
                                                            Color(0xFFB50938),
                                                        )
                                                    ),
                                                    RoundedCornerShape(buttonCornerRadius)
                                                )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(buttonPadding),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.scan_and_find),
                                                    fontFamily = poppins,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = buttonTextSize,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                )

                                                Row {
                                                    Spacer(modifier = Modifier.width(barcodeSpacer))
                                                    Image(
                                                        painter = painterResource(id = barcodeIconId),
                                                        contentDescription = "Barcode Icon",
                                                        modifier = Modifier.scale(barcodeScale)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Filter and Find button
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(buttonHeight)
                                            .clickableDebounced { debouncedFilterAndFind() },
                                        shape = RoundedCornerShape(buttonCornerRadius),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(buttonPadding),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.filter_and_find),
                                                fontFamily = poppins,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = buttonTextSize,
                                                color = Color.Black,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(iconTextSpacing))

                                            Image(
                                                modifier = Modifier.size(buttonIconSize),
                                                painter = painterResource(id = filterAndFindIconId),
                                                contentDescription = "Filter and Find Icon",
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }

                                    // Visual Search button
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(buttonHeight)
                                            .clickableDebounced { debouncedVisualSearch() },
                                        shape = RoundedCornerShape(buttonCornerRadius),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(buttonPadding),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.visual_search),
                                                fontFamily = poppins,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = buttonTextSize,
                                                color = Color.Black,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(iconTextSpacing))

                                            Image(
                                                modifier = Modifier.size(buttonIconSize),
                                                painter = painterResource(id = visualSearchIconId),
                                                contentDescription = "Visual Search Icon",
                                                contentScale = ContentScale.Fit
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

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun FindItemDialogPreviewSmall() {
    FindItemDialog(
        onDismiss = { },
        onScanAndFind = { },
        onFilterAndFind = { },
        onVisualSearch = { }
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun FindItemDialogPreviewMedium() {
    FindItemDialog(
        onDismiss = { },
        onScanAndFind = { },
        onFilterAndFind = { },
        onVisualSearch = { }
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun FindItemDialogPreviewLarge() {
    FindItemDialog(
        onDismiss = { },
        onScanAndFind = { },
        onFilterAndFind = { },
        onVisualSearch = { }
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun FindItemDialogPreviewPhilips() {
    FindItemDialog(
        onDismiss = { },
        onScanAndFind = { },
        onFilterAndFind = { },
        onVisualSearch = { }
    )
}
