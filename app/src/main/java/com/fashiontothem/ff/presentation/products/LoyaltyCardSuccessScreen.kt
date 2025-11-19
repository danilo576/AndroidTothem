package com.fashiontothem.ff.presentation.products

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R
import kotlinx.coroutines.delay

/**
 * Success screen shown after successfully scanning a loyalty card
 */
@Composable
fun LoyaltyCardSuccessScreen(
    scannedCardNumber: String,
    onClose: () -> Unit,
) {
    var timeRemaining by remember { mutableStateOf(30) } // 30 seconds countdown
    var isTimerActive by remember { mutableStateOf(true) }

    // Countdown timer
    LaunchedEffect(isTimerActive) {
        while (isTimerActive && timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }
        // When timer reaches 0, close all screens and navigate to home
        if (timeRemaining == 0) {
            onClose()
        }
    }

    val gradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF4F0418),
                Color(0xFFB50938)
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                .padding(top = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.fashion_logo),
                contentDescription = null
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(initialScale = 0.95f)
            ) {
                LoyaltyCardSuccessDialog(
                    scannedCardNumber = scannedCardNumber,
                    timeRemaining = timeRemaining,
                    gradient = gradient,
                    onClose = onClose
                )
            }
        }
    }
}

@Composable
private fun LoyaltyCardSuccessDialog(
    scannedCardNumber: String,
    timeRemaining: Int,
    gradient: Brush,
    onClose: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 24.dp, shape = RoundedCornerShape(40.dp)),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .padding(top = 32.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(50.dp)) // Spacer to balance the close button
                RoundIconButton(
                    iconRes = R.drawable.x_white_icon,
                    contentDescription = stringResource(id = R.string.product_details_close),
                    gradient = gradient,
                    onClick = onClose
                )
            }

            // Loyalty red logo
            Image(
                painter = painterResource(id = R.drawable.loyalty_red_logo),
                contentDescription = null,
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit
            )

            // Main heading
            Text(
                text = stringResource(id = R.string.loyalty_card_success_title),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 34.sp,
                color = Color(0xFFB50938),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Instruction text
            Text(
                text = stringResource(id = R.string.loyalty_card_success_message),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 26.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 40.sp
            )

            // Display scanned card number
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scannedCardNumber,
                        fontFamily = Fonts.Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Closing message
            Text(
                text = stringResource(id = R.string.loyalty_card_success_thanks),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 26.sp,
                color = Color(0xFF8C8C8C),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Cart animation
            val cartComposition by rememberLottieComposition(
                LottieCompositionSpec.Asset("cart_animated_icon.json")
            )
            val cartProgress by animateLottieCompositionAsState(
                composition = cartComposition,
                iterations = LottieConstants.IterateForever
            )
            LottieAnimation(
                composition = cartComposition,
                progress = { cartProgress },
                modifier = Modifier.size(120.dp)
            )

            // Finish button with timer
            FinishButtonWithTimer(
                timeRemaining = timeRemaining,
                onClick = onClose
            )
        }
    }
}

@Composable
private fun FinishButtonWithTimer(
    timeRemaining: Int,
    onClick: () -> Unit,
) {
    // Format: 00:30:00 (hours:minutes:seconds) - always shows 00:XX:00
    val minutes = timeRemaining
    val timeString = String.format("00:%02d:00", minutes)

    val shape = RoundedCornerShape(50.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(shape)
            .background(Color.White)
            .border(
                width = 2.dp,
                color = Color(0xFFE5E5E5),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.loyalty_card_success_finish),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Color.Black
            )

            Image(
                painter = painterResource(id = R.drawable.fashion_and_friends_loader),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = timeString,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Color(0xFFB50938)
            )
        }
    }
}

@Composable
private fun RoundIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    gradient: Brush,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color(0xFFB50938), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
            )
        }
    }
}

@Preview(name = "Loyalty Card Success", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun LoyaltyCardSuccessPreview() {
    LoyaltyCardSuccessScreen(
        scannedCardNumber = "CMC123456",
        onClose = {}
    )
}

