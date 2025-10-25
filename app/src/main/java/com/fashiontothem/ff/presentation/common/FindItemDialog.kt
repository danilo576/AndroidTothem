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
                        // Dialog content
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
                                // Top section with logo
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    // Find icon logo
                                    Image(
                                        painter = painterResource(id = findIconId),
                                        contentDescription = "Find Icon",
                                    )

                                    Spacer(modifier = Modifier.height(50.dp))

                                }

                                // Middle section with three buttons
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(30.dp)
                                ) {
                                    // Scan and Find button
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                            .clickableDebounced { debouncedScanAndFind() },
                                        shape = RoundedCornerShape(30.dp),
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
                                                    RoundedCornerShape(30.dp)
                                                )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.scan_and_find),
                                                    fontFamily = poppins,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 28.sp,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                )

                                                Row {
                                                    Spacer(modifier = Modifier.width(60.dp))
                                                    Image(
                                                        painter = painterResource(id = barcodeIconId),
                                                        contentDescription = "Barcode Icon",
                                                        modifier = Modifier.scale(3f)
                                                    )
                                                }

                                            }
                                        }
                                    }

                                    // Filter and Find button
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                            .clickableDebounced { debouncedFilterAndFind() },
                                        shape = RoundedCornerShape(30.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.filter_and_find),
                                                fontFamily = poppins,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 28.sp,
                                                color = Color.Black,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Image(
                                                modifier = Modifier.size(80.dp),
                                                painter = painterResource(id = filterAndFindIconId),
                                                contentDescription = "Filter and Find Icon",
                                            )
                                        }
                                    }

                                    // Visual Search button
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                            .clickableDebounced { debouncedVisualSearch() },
                                        shape = RoundedCornerShape(30.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.visual_search),
                                                fontFamily = poppins,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 28.sp,
                                                color = Color.Black,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Image(
                                                modifier = Modifier.size(80.dp),
                                                painter = painterResource(id = visualSearchIconId),
                                                contentDescription = "Visual Search Icon",
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
