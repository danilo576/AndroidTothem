package com.fashiontothem.ff.presentation.home

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fashiontothem.ff.presentation.common.DownloadAppDialog
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen() {
    // Poppins font family (regular, medium, semibold, bold) from res/font
    val poppins = Fonts.Poppins
    var showDownloadDialog by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenH = maxHeight
        val sidePadding = 40.dp
        val logoHeight = (screenH * 0.06f).coerceAtLeast(44.dp)
        val searchHeight = (screenH * 0.10f).coerceAtLeast(88.dp)
        val cardHeight = (screenH * 0.10f).coerceAtLeast(108.dp)
        val verticalGap = 20.dp
        val corner = 50.dp
        val borderStroke = 2.dp
        val glassAlpha = 0.28f

        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sidePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.fashion_logo),
                contentDescription = null,
                modifier = Modifier.height(logoHeight)
            )

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassCard(corner, borderStroke, glassAlpha) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(searchHeight),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painterResource(id = R.drawable.fint_item),
                                null,
                                Modifier.height((searchHeight * 0.55f).coerceAtLeast(44.dp))
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Pronađi artikal",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = poppins
                            )
                        }
                    }

                    Spacer(Modifier.height(verticalGap))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FeatureCard(
                                R.drawable.new_icon,
                                "Novo",
                                cardHeight,
                                corner,
                                borderStroke,
                                glassAlpha,
                                poppins
                            )
                            FeatureCard(
                                R.drawable.download_app,
                                "Preuzmi app",
                                cardHeight,
                                corner,
                                borderStroke,
                                glassAlpha,
                                poppins,
                                onClick = { showDownloadDialog = true }
                            )
                        }
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FeatureCard(
                                R.drawable.actions,
                                "Akcije",
                                cardHeight,
                                corner,
                                borderStroke,
                                glassAlpha,
                                poppins
                            )
                            FeatureCard(
                                R.drawable.loaylty,
                                "Loyalty",
                                cardHeight,
                                corner,
                                borderStroke,
                                glassAlpha,
                                poppins
                            )
                        }
                    }
                }
            }

            Image(
                painter = painterResource(id = R.drawable.diesel_logo),
                contentDescription = null,
                modifier = Modifier.height((screenH * 0.05f).coerceAtLeast(48.dp))
            )

            Spacer(Modifier.height(24.dp))
        }
    }
    
    // Show download dialog
    if (showDownloadDialog) {
        DownloadAppDialog(
            onDismiss = { showDownloadDialog = false }
        )
    }
}

@Composable
private fun FeatureCard(
    icon: Int,
    label: String,
    height: Dp,
    corner: Dp,
    borderStroke: Dp,
    glassAlpha: Float,
    fontFamily: FontFamily,
    onClick: (() -> Unit)? = null
) {
    GlassCard(corner, borderStroke, glassAlpha) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clickable { onClick?.invoke() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painterResource(id = icon),
                null,
                Modifier.height((height * 0.38f).coerceAtLeast(44.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = fontFamily
            )
        }
    }
}

@Composable
private fun GlassCard(
    corner: Dp,
    borderWidth: Dp,
    alphaOverlay: Float,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Color.Transparent,
        shadowElevation = 0.dp,
        border = BorderStroke(borderWidth, Color(0xFFB50938))
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(Color.Black.copy(alpha = alphaOverlay))
                .padding(16.dp)
        ) { content() }
    }
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun HomeScreenPreviewPhilips() {
    HomeScreen()
}

@Preview(name = "Normal Phone", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun HomeScreenPreviewPhone() {
    HomeScreen()
}

