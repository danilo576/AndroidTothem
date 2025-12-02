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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.unit.Dp
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

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

            // Responsive logo top padding
            val logoTopPadding = when {
                screenHeight < 700.dp -> 10.dp
                screenHeight < 1200.dp -> 10.dp
                else -> 52.dp
            }

            // Responsive logo height
            val logoHeight = when {
                screenWidth < 400.dp -> 15.dp
                screenWidth < 600.dp -> 30.dp
                else -> 120.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = logoTopPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fashion_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(logoHeight),
                    contentScale = ContentScale.Fit
                )
            }

            // Responsive dialog horizontal padding
            val dialogHorizontalPadding = when {
                screenWidth < 400.dp -> 12.dp
                screenWidth < 600.dp -> 20.dp
                else -> 32.dp
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dialogHorizontalPadding),
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
                        onClose = onClose,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
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
    screenWidth: Dp,
    screenHeight: Dp,
) {
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 32.dp
        else -> 40.dp
    }

    // Responsive shadow elevation
    val shadowElevation = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 18.dp
        else -> 24.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        // Responsive padding
        val horizontalPadding = when {
            screenWidth < 400.dp -> 20.dp
            screenWidth < 600.dp -> 28.dp
            else -> 36.dp
        }

        val topPadding = when {
            screenHeight < 700.dp -> 20.dp
            screenHeight < 1200.dp -> 26.dp
            else -> 32.dp
        }

        val bottomPadding = when {
            screenHeight < 700.dp -> 24.dp
            screenHeight < 1200.dp -> 32.dp
            else -> 40.dp
        }

        // Responsive spacing
        val contentSpacing = when {
            screenHeight < 700.dp -> 10.dp
            screenHeight < 1200.dp -> 20.dp
            else -> 24.dp
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = topPadding, bottom = bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Responsive spacer size
                val spacerSize = when {
                    screenWidth < 400.dp -> 30.dp
                    screenWidth < 600.dp -> 40.dp
                    else -> 50.dp
                }

                Spacer(modifier = Modifier.size(spacerSize)) // Spacer to balance the close button
                RoundIconButton(
                    iconRes = R.drawable.x_white_icon,
                    contentDescription = stringResource(id = R.string.product_details_close),
                    gradient = gradient,
                    onClick = onClose,
                    screenWidth = screenWidth
                )
            }

            // Loyalty red logo
            // Responsive logo size
            val logoSize = when {
                screenWidth < 400.dp -> 50.dp
                screenWidth < 600.dp -> 60.dp
                else -> 150.dp
            }

            Image(
                painter = painterResource(id = R.drawable.loyalty_red_logo),
                contentDescription = null,
                modifier = Modifier.size(logoSize),
                contentScale = ContentScale.Fit
            )

            // Main heading
            // Responsive title font size
            val titleFontSize = when {
                screenWidth < 400.dp -> 20.sp
                screenWidth < 600.dp -> 24.sp
                else -> 34.sp
            }

            Text(
                text = stringResource(id = R.string.loyalty_card_success_title),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = titleFontSize,
                color = Color(0xFFB50938),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Instruction text
            // Responsive message font size
            val messageFontSize = when {
                screenWidth < 400.dp -> 12.sp
                screenWidth < 600.dp -> 18.sp
                else -> 26.sp
            }

            val messageLineHeight = when {
                screenWidth < 400.dp -> 18.sp
                screenWidth < 600.dp -> 24.sp
                else -> 40.sp
            }

            Text(
                text = stringResource(id = R.string.loyalty_card_success_message),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = messageFontSize,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = messageLineHeight
            )

            // Display scanned card number
            // Responsive card corner radius
            val cardCornerRadius = when {
                screenWidth < 400.dp -> 12.dp
                screenWidth < 600.dp -> 14.dp
                else -> 16.dp
            }

            // Responsive card padding
            val cardPadding = when {
                screenWidth < 400.dp -> 12.dp
                screenWidth < 600.dp -> 16.dp
                else -> 20.dp
            }

            // Responsive card number font size
            val cardNumberFontSize = when {
                screenWidth < 400.dp -> 12.sp
                screenWidth < 600.dp -> 16.sp
                else -> 24.sp
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(cardPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scannedCardNumber,
                        fontFamily = Fonts.Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = cardNumberFontSize,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Closing message
            // Responsive thanks font size
            val thanksFontSize = when {
                screenWidth < 400.dp -> 14.sp
                screenWidth < 600.dp -> 18.sp
                else -> 26.sp
            }

            Text(
                text = stringResource(id = R.string.loyalty_card_success_thanks),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = thanksFontSize,
                color = Color(0xFF8C8C8C),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Cart animation
            // Responsive animation size
            val animationSize = when {
                screenWidth < 400.dp -> 60.dp
                screenWidth < 600.dp -> 90.dp
                else -> 120.dp
            }

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
                modifier = Modifier.size(animationSize)
            )

            // Finish button with timer
            FinishButtonWithTimer(
                timeRemaining = timeRemaining,
                onClick = onClose,
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
private fun FinishButtonWithTimer(
    timeRemaining: Int,
    onClick: () -> Unit,
    screenWidth: Dp,
) {
    // Format: 00:30:00 (hours:minutes:seconds) - always shows 00:XX:00
    val minutes = timeRemaining
    val timeString = String.format("00:%02d:00", minutes)

    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 35.dp
        else -> 50.dp
    }

    val shape = RoundedCornerShape(cornerRadius)

    // Responsive button height
    val buttonHeight = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 50.dp
        else -> 66.dp
    }

    // Responsive border width
    val borderWidth = when {
        screenWidth < 400.dp -> 1.5.dp
        screenWidth < 600.dp -> 1.75.dp
        else -> 2.dp
    }

    // Responsive font size
    val buttonFontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 16.sp
        else -> 22.sp
    }

    // Responsive icon size
    val iconSize = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 20.dp
        else -> 28.dp
    }

    // Responsive spacing
    val iconTextSpacing = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 10.dp
        else -> 12.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .clip(shape)
            .background(Color.White)
            .border(
                width = borderWidth,
                color = Color(0xFFE5E5E5),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(iconTextSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.loyalty_card_success_finish),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = buttonFontSize,
                color = Color.Black
            )

            Image(
                painter = painterResource(id = R.drawable.fashion_and_friends_loader),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Fit
            )

            Text(
                text = timeString,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = buttonFontSize,
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
    screenWidth: Dp,
) {
    // Responsive button size
    val buttonSize = when {
        screenWidth < 400.dp -> 30.dp
        screenWidth < 600.dp -> 40.dp
        else -> 50.dp
    }

    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .background(Color(0xFFB50938), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(buttonSize * 0.6f),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun LoyaltyCardSuccessPreviewSmall() {
    LoyaltyCardSuccessScreen(
        scannedCardNumber = "CMC123456",
        onClose = {}
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
private fun LoyaltyCardSuccessPreviewMedium() {
    LoyaltyCardSuccessScreen(
        scannedCardNumber = "CMC123456",
        onClose = {}
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
private fun LoyaltyCardSuccessPreviewLarge() {
    LoyaltyCardSuccessScreen(
        scannedCardNumber = "CMC123456",
        onClose = {}
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun LoyaltyCardSuccessPreviewPhilips() {
    LoyaltyCardSuccessScreen(
        scannedCardNumber = "CMC123456",
        onClose = {}
    )
}

