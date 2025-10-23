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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ScanAndFindDialog(
    onDismiss: () -> Unit,
) {
    val poppins = Fonts.Poppins

    // Remember image IDs to avoid reloading
    val closeButtonId = remember { R.drawable.close_button }
    val findIconId = remember { R.drawable.find_item }
    val barcodeImageId = remember { R.drawable.barcode_find_item_image }

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
            val dialogWidth = (maxWidth * 0.98f).coerceAtMost(750.dp)
            val dialogHeight = (maxHeight * 0.95f).coerceAtMost(1200.dp)

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
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Top section with icon and title
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Find icon logo
                                    Image(
                                        painter = painterResource(id = findIconId),
                                        contentDescription = "Find Icon",
                                    )

                                    Spacer(modifier = Modifier.height(70.dp))

                                    // Title
                                    Text(
                                        text = "Skeniraj i pronađi!",
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 34.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Description
                                    Text(
                                        text = "Ne uspevaš da pronađeš ono što tražiš?\n\nSkeniraj barkod artikla, odaberi svoju veličinu i proveri dostupnost na klik.",
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 26.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 40.sp
                                    )
                                }

                                // Middle section with barcode illustration
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Barcode illustration
                                    Image(
                                        modifier = Modifier
                                            .size(300.dp)
                                            .scale(2f)
                                            .padding(bottom = 50.dp),
                                        painter = painterResource(id = barcodeImageId),
                                        contentDescription = "Barcode",
                                        contentScale = ContentScale.Crop
                                    )


                                    // Instructions
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        InstructionItem(
                                            text = "Prisloni barkod sa poleđine etikete na skener koji se nalazi u donjem delu monitora."
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        InstructionItem(
                                            text = "Kada se barkod uspešno očita, moći ćeš da odabereš varijantu koju želiš.",
                                            color = Color(0xFF949494)
                                        )
                                    }
                                }

                                // Bottom section with animated arrow
                                AnimatedScannerArrow()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionItem(text: String, color: Color = Color.White) {
    val poppins = Fonts.Poppins

    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(id = R.drawable.round_silver_icon),
            contentDescription = "Silver Icon",
            modifier = Modifier
                .padding(top = 12.dp)
                .size(20.dp),
            tint = color
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontFamily = poppins,
            fontSize = 24.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            lineHeight = 40.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AnimatedScannerArrow() {
    val infinite = rememberInfiniteTransition(label = "scanner_arrow")

    // Glowing effect animation
    val alpha by infinite.animateFloat(
        initialValue = 0.3f,
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

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
    ) {
        // Arrow pointing down
        Image(painter = painterResource(R.drawable.arrow_down), contentDescription = null)
    }
}

@Preview(showBackground = true)
@Composable
fun ScanAndFindDialogPreview() {
    ScanAndFindDialog(
        onDismiss = { }
    )
}
