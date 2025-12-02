package com.fashiontothem.ff.presentation.products

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.fashiontothem.ff.core.scanner.BarcodeScannerEvents
import com.fashiontothem.ff.domain.repository.QuantityNotAvailableException
import com.fashiontothem.ff.presentation.common.LoyaltyDialog
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R
import kotlinx.coroutines.delay
import android.graphics.Color as AndroidColor

/**
 * Screen for scanning loyalty card when ordering online
 */
@Composable
fun ScanLoyaltyCardScreen(
    uiState: ProductDetailsUiState,
    onClose: () -> Unit,
    onCardScanned: (String) -> Unit,
    viewModel: ProductDetailsViewModel,
) {
    // Debounced click handler to prevent multiple rapid navigations
    val debouncedOnClose = rememberDebouncedClick(onClick = onClose)

    var showLoyaltyDialog by remember { mutableStateOf(false) }
    var showInvalidCardDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scanHandlingInProgress by remember { mutableStateOf(false) }
    var showScanAnimation by remember { mutableStateOf(false) }
    var scannedCardNumber by remember { mutableStateOf<String?>(null) }

    // Listen for barcode scans
    LaunchedEffect(Unit) {
        BarcodeScannerEvents.scans.collect { barcode ->
            if (scanHandlingInProgress) {
                return@collect
            }
            scanHandlingInProgress = true

            try {
                // Validate loyalty card format: CMCxxxxxx or PVCxxxxxx (6 or 7 digits)
                val isValid = isValidLoyaltyCard(barcode)
                if (isValid) {
                    // Show animation overlay
                    scannedCardNumber = barcode
                    showScanAnimation = true

                    // Extract option IDs and values from productDetails
                    val productDetails = uiState.productDetails
                    val sku = productDetails?.sku ?: ""

                    // Auto-select size if only one option exists and not already selected
                    val selectedSize = uiState.selectedSize
                        ?: productDetails?.options?.size?.options?.takeIf { it.size == 1 }
                            ?.firstOrNull()?.value

                    // Auto-select color if only one option exists and not already selected
                    // Prioritize colorShade over color
                    val selectedColor = uiState.selectedColor
                        ?: productDetails?.options?.colorShade?.options?.takeIf { it.size == 1 }
                            ?.firstOrNull()?.value
                        ?: productDetails?.options?.color?.options?.takeIf { it.size == 1 }
                            ?.firstOrNull()?.value

                    // Extract size option
                    // Note: selectedSize is already the value (from option.value), not the label
                    val sizeAttributeId = productDetails?.options?.size?.attributeId ?: ""
                    val sizeOptionValue = selectedSize ?: ""

                    // Extract color option (use colorShade if available, otherwise color)
                    // Note: selectedColor is already the value (from option.value), not the label
                    val colorAttributeId = productDetails?.options?.colorShade?.attributeId
                        ?: productDetails?.options?.color?.attributeId ?: ""
                    val colorOptionValue = selectedColor ?: ""

                    // Call API and measure time
                    val apiStartTime = System.currentTimeMillis()
                    val apiResult = viewModel.addToCart(
                        loyaltyScannedBarcode = barcode,
                        sku = sku,
                        sizeAttributeId = sizeAttributeId,
                        sizeOptionValue = sizeOptionValue,
                        colorAttributeId = colorAttributeId,
                        colorOptionValue = colorOptionValue,
                    )
                    val apiDuration = System.currentTimeMillis() - apiStartTime

                    // Minimum animation duration is 2500ms
                    val minAnimationDuration = 2500L
                    val remainingTime = (minAnimationDuration - apiDuration).coerceAtLeast(0)

                    // Wait for remaining time if API was faster
                    if (remainingTime > 0) {
                        delay(remainingTime)
                    }

                    // Hide animation
                    showScanAnimation = false

                    // Check API result
                    apiResult.fold(
                        onSuccess = { success ->
                            if (success) {
                                // Navigate to success screen
                                onCardScanned(barcode)
                            } else {
                                // Show error dialog
                                errorMessage = "GENERIC_ERROR"
                                showErrorDialog = true
                            }
                        },
                        onFailure = { exception ->
                            // Determine error type based on exception type and message
                            errorMessage = when {
                                exception is QuantityNotAvailableException -> "QUANTITY_NOT_AVAILABLE"
                                exception.message?.contains("404") == true -> "NOT_FOUND"
                                exception.message?.contains("500") == true -> "SERVER_ERROR"
                                exception.message?.contains(
                                    "network",
                                    ignoreCase = true
                                ) == true -> "NETWORK_ERROR"

                                exception.message?.contains(
                                    "timeout",
                                    ignoreCase = true
                                ) == true -> "TIMEOUT_ERROR"

                                else -> "GENERIC_ERROR"
                            }
                            showErrorDialog = true
                        }
                    )
                } else {
                    showInvalidCardDialog = true
                }
            } catch (e: Exception) {
                // Show error dialog
                errorMessage = when {
                    e is QuantityNotAvailableException -> "QUANTITY_NOT_AVAILABLE"
                    e.message?.contains("404") == true -> "NOT_FOUND"
                    e.message?.contains("500") == true -> "SERVER_ERROR"
                    e.message?.contains("network", ignoreCase = true) == true -> "NETWORK_ERROR"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "TIMEOUT_ERROR"
                    else -> "GENERIC_ERROR"
                }
                showErrorDialog = true
                showScanAnimation = false
            } finally {
                delay(600)
                scanHandlingInProgress = false
            }
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
                    ScanLoyaltyCardDialog(
                        gradient = gradient,
                        onClose = debouncedOnClose,
                        onBecomeMember = { showLoyaltyDialog = true },
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
            }
        }
    }

    // Show LoyaltyDialog when "Postani član" is clicked
    if (showLoyaltyDialog) {
        LoyaltyDialog(
            onDismiss = { showLoyaltyDialog = false }
        )
    }

    // Show error dialog when invalid loyalty card is scanned
    if (showInvalidCardDialog) {
        InvalidLoyaltyCardDialog(
            gradient = gradient,
            onDismiss = { showInvalidCardDialog = false }
        )
    }

    // Show error dialog when API call fails
    if (showErrorDialog) {
        AddToCartErrorDialog(
            gradient = gradient,
            errorMessage = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }

    // Show scan animation overlay when valid loyalty card is scanned
    if (showScanAnimation && scannedCardNumber != null) {
        LoyaltyCardScannedAnimationDialog()
    }
}

@Composable
private fun ScanLoyaltyCardDialog(
    gradient: Brush,
    onClose: () -> Unit,
    onBecomeMember: () -> Unit,
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

    // Responsive padding
    val horizontalPadding = when {
        screenWidth < 400.dp -> 20.dp
        screenWidth < 600.dp -> 20.dp
        else -> 36.dp
    }

    val topPadding = when {
        screenHeight < 700.dp -> 15.dp
        screenHeight < 1200.dp -> 20.dp
        else -> 32.dp
    }

    val bottomPadding = when {
        screenHeight < 700.dp -> 20.dp
        screenHeight < 1200.dp -> 26.dp
        else -> 40.dp
    }

    // Responsive spacing
    val contentSpacing = when {
        screenHeight < 700.dp -> 8.dp
        screenHeight < 1200.dp -> 12.dp
        else -> 24.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
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
                    screenWidth < 400.dp -> 36.dp
                    screenWidth < 600.dp -> 42.dp
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

            // Title
            // Responsive title font size
            val titleFontSize = when {
                screenWidth < 400.dp -> 18.sp
                screenWidth < 600.dp -> 22.sp
                else -> 34.sp
            }

            Text(
                text = stringResource(id = R.string.scan_loyalty_card_title),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = titleFontSize,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Loyalty card image
            // Responsive card image size
            val cardImageSize = when {
                screenWidth < 400.dp -> 60.dp
                screenWidth < 600.dp -> 80.dp
                else -> 250.dp
            }

            Image(
                painter = painterResource(id = R.drawable.isometric_card),
                contentDescription = null,
                modifier = Modifier.size(cardImageSize),
                contentScale = ContentScale.Fit
            )

            // Instructions list
            // Responsive instructions padding
            val instructionsPadding = when {
                screenWidth < 400.dp -> 10.dp
                screenWidth < 600.dp -> 20.dp
                else -> 115.dp
            }

            // Responsive instruction spacing
            val instructionSpacing = when {
                screenHeight < 700.dp -> 4.dp
                screenHeight < 1200.dp -> 6.dp
                else -> 12.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = instructionsPadding),
                verticalArrangement = Arrangement.spacedBy(instructionSpacing)
            ) {
                InstructionItem(
                    number = 1,
                    text = stringResource(id = R.string.scan_loyalty_instruction_1),
                    screenWidth = screenWidth
                )
                InstructionItem(
                    number = 2,
                    text = stringResource(id = R.string.scan_loyalty_instruction_2),
                    screenWidth = screenWidth
                )
                InstructionItem(
                    number = 3,
                    text = stringResource(id = R.string.scan_loyalty_instruction_3),
                    screenWidth = screenWidth
                )
                InstructionItem(
                    number = 4,
                    text = stringResource(id = R.string.scan_loyalty_instruction_4),
                    screenWidth = screenWidth
                )
            }

            // Explanation text
            // Responsive explanation font size
            val explanationFontSize = when {
                screenWidth < 400.dp -> 12.sp
                screenWidth < 600.dp -> 16.sp
                else -> 24.sp
            }

            Text(
                text = stringResource(id = R.string.scan_loyalty_explanation),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = explanationFontSize,
                color = Color(0xFF8C8C8C),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Responsive spacer height
            val spacerHeight = when {
                screenHeight < 700.dp -> 4.dp
                screenHeight < 1200.dp -> 6.dp
                else -> 10.dp
            }

            Spacer(modifier = Modifier.height(spacerHeight))

            // "Not yet a member?" question
            // Responsive question font size
            val questionFontSize = when {
                screenWidth < 400.dp -> 10.sp
                screenWidth < 600.dp -> 18.sp
                else -> 27.sp
            }

            Text(
                text = stringResource(id = R.string.scan_loyalty_not_member),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = questionFontSize,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacerHeight))

            // "Postani član" button
            OutlineActionButton(
                text = stringResource(id = R.string.scan_loyalty_become_member),
                iconRes = R.drawable.fashion_and_friends_loader,
                enabled = true,
                onClick = onBecomeMember,
                screenWidth = screenWidth
            )

            Spacer(modifier = Modifier.height(spacerHeight))

            // Animated arrow icon at the bottom
            AnimatedScannerArrow(screenWidth = screenWidth)

        }
    }
}

@Composable
private fun InstructionItem(
    number: Int,
    text: String,
    screenWidth: Dp,
) {
    // Responsive font size
    val instructionFontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 14.sp
        else -> 24.sp
    }

    // Responsive spacing
    val numberTextSpacing = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 12.dp
        else -> 12.dp
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number.",
            fontFamily = Fonts.Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = instructionFontSize,
            color = Color.Black,
            modifier = Modifier.padding(end = numberTextSpacing)
        )
        Text(
            text = text,
            fontFamily = Fonts.Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = instructionFontSize,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
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
        screenWidth < 400.dp -> 20.dp
        screenWidth < 600.dp -> 30.dp
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

@Composable
private fun OutlineActionButton(
    text: String,
    @DrawableRes iconRes: Int? = null,
    enabled: Boolean,
    onClick: () -> Unit,
    screenWidth: Dp,
) {
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 35.dp
        else -> 50.dp
    }

    val shape = RoundedCornerShape(cornerRadius)

    // Responsive button height
    val buttonHeight = when {
        screenWidth < 400.dp -> 44.dp
        screenWidth < 600.dp -> 52.dp
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
        screenWidth < 400.dp -> 16.dp
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
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(iconTextSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = text,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = buttonFontSize,
                color = Color(0xFF2C2C2C)
            )
        }
    }
}

@Composable
private fun AnimatedScannerArrow(
    screenWidth: Dp,
) {
    val infinite = rememberInfiniteTransition(label = "scanner_arrow")

    // Glowing effect animation
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
        screenWidth < 600.dp -> 60.dp
        else -> 80.dp
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
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

/**
 * Validates if the scanned barcode is a valid loyalty card format
 * Format: CMCxxxxxx or PVCxxxxxx where x is 6 or 7 digits
 */
private fun isValidLoyaltyCard(barcode: String): Boolean {
    val trimmed = barcode.trim()

    if (trimmed.length !in 9..10) {
        return false
    }

    val prefix = trimmed.take(3).uppercase()
    if (prefix != "CMC" && prefix != "PVC") {
        return false
    }

    val digits = trimmed.drop(3)
    if (digits.length != 6 && digits.length != 7) {
        return false
    }

    return digits.all { it.isDigit() }
}

@Composable
private fun InvalidLoyaltyCardDialog(
    gradient: Brush,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
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

                // Responsive horizontal padding
                val cardHorizontalPadding = when {
                    screenWidth < 400.dp -> 16.dp
                    screenWidth < 600.dp -> 24.dp
                    else -> 32.dp
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = cardHorizontalPadding)
                        .shadow(
                            elevation = shadowElevation,
                            shape = RoundedCornerShape(cornerRadius)
                        )
                        .clickable { /* Prevent dialog close when clicking on card */ },
                    shape = RoundedCornerShape(cornerRadius),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    // Responsive content padding
                    val contentHorizontalPadding = when {
                        screenWidth < 400.dp -> 20.dp
                        screenWidth < 600.dp -> 28.dp
                        else -> 36.dp
                    }

                    val contentTopPadding = when {
                        screenHeight < 700.dp -> 20.dp
                        screenHeight < 1200.dp -> 26.dp
                        else -> 32.dp
                    }

                    val contentBottomPadding = when {
                        screenHeight < 700.dp -> 24.dp
                        screenHeight < 1200.dp -> 32.dp
                        else -> 40.dp
                    }

                    // Responsive spacing
                    val contentSpacing = when {
                        screenHeight < 700.dp -> 16.dp
                        screenHeight < 1200.dp -> 20.dp
                        else -> 24.dp
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentHorizontalPadding)
                            .padding(top = contentTopPadding, bottom = contentBottomPadding),
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
                                screenWidth < 400.dp -> 36.dp
                                screenWidth < 600.dp -> 42.dp
                                else -> 50.dp
                            }

                            Spacer(modifier = Modifier.size(spacerSize)) // Spacer to balance the close button
                            RoundIconButton(
                                iconRes = R.drawable.x_white_icon,
                                contentDescription = stringResource(id = R.string.product_details_close),
                                gradient = gradient,
                                onClick = onDismiss,
                                screenWidth = screenWidth
                            )
                        }

                        // Error icon or title
                        // Responsive title font size
                        val titleFontSize = when {
                            screenWidth < 400.dp -> 12.sp
                            screenWidth < 600.dp -> 18.sp
                            else -> 28.sp
                        }

                        Text(
                            text = stringResource(id = R.string.invalid_loyalty_card_title),
                            fontFamily = Fonts.Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = titleFontSize,
                            color = Color(0xFFB50938),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Error message
                        // Responsive message font size
                        val messageFontSize = when {
                            screenWidth < 400.dp -> 12.sp
                            screenWidth < 600.dp -> 18.sp
                            else -> 20.sp
                        }

                        val messageLineHeight = when {
                            screenWidth < 400.dp -> 18.sp
                            screenWidth < 600.dp -> 22.sp
                            else -> 28.sp
                        }

                        Text(
                            text = stringResource(id = R.string.invalid_loyalty_card_message),
                            fontFamily = Fonts.Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = messageFontSize,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = messageLineHeight
                        )

                        // OK button
                        // Responsive button height
                        val buttonHeight = when {
                            screenWidth < 400.dp -> 44.dp
                            screenWidth < 600.dp -> 50.dp
                            else -> 66.dp
                        }

                        // Responsive button corner radius
                        val buttonCornerRadius = when {
                            screenWidth < 400.dp -> 24.dp
                            screenWidth < 600.dp -> 35.dp
                            else -> 50.dp
                        }

                        // Responsive button font size
                        val buttonFontSize = when {
                            screenWidth < 400.dp -> 12.sp
                            screenWidth < 600.dp -> 16.sp
                            else -> 22.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight)
                                .clip(RoundedCornerShape(buttonCornerRadius))
                                .background(gradient)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.invalid_loyalty_card_ok),
                                fontFamily = Fonts.Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = buttonFontSize,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoyaltyCardScannedAnimationDialog() {
    Dialog(
        onDismissRequest = { /* Block dismissal while processing */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            // Responsive horizontal padding
            val horizontalPadding = when {
                screenWidth < 400.dp -> 16.dp
                screenWidth < 600.dp -> 24.dp
                else -> 32.dp
            }

            // Responsive spacing
            val contentSpacing = when {
                screenHeight < 700.dp -> 16.dp
                screenHeight < 1200.dp -> 20.dp
                else -> 24.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(contentSpacing)
            ) {
                // Lottie animation (same as BarcodeScannedDialog)
                // Responsive animation size
                val animationSize = when {
                    screenWidth < 400.dp -> 180.dp
                    screenWidth < 600.dp -> 230.dp
                    else -> 280.dp
                }

                AnimatedLoyaltyCardScanner(
                    modifier = Modifier.size(animationSize),
                    screenWidth = screenWidth
                )
            }
        }
    }
}

@Composable
private fun AnimatedLoyaltyCardScanner(
    modifier: Modifier = Modifier,
    screenWidth: Dp? = null,
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("scanner_animation.json"),
        cacheKey = "loyalty_card_scanner_animation"
    )

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = AndroidColor.WHITE,
            "**",
            "Fill 1"
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = Color(0xFFB50937).toArgb(),
            "Line Outlines",
            "**",
            "Fill 1"
        )
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 2f
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
        dynamicProperties = dynamicProperties
    )
}

@Composable
private fun AddToCartErrorDialog(
    gradient: Brush,
    errorMessage: String?,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
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

                // Responsive horizontal padding
                val cardHorizontalPadding = when {
                    screenWidth < 400.dp -> 16.dp
                    screenWidth < 600.dp -> 24.dp
                    else -> 32.dp
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = cardHorizontalPadding)
                        .shadow(
                            elevation = shadowElevation,
                            shape = RoundedCornerShape(cornerRadius)
                        )
                        .clickable { /* Prevent dialog close when clicking on card */ },
                    shape = RoundedCornerShape(cornerRadius),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    // Responsive content padding
                    val contentHorizontalPadding = when {
                        screenWidth < 400.dp -> 20.dp
                        screenWidth < 600.dp -> 28.dp
                        else -> 36.dp
                    }

                    val contentTopPadding = when {
                        screenHeight < 700.dp -> 20.dp
                        screenHeight < 1200.dp -> 26.dp
                        else -> 32.dp
                    }

                    val contentBottomPadding = when {
                        screenHeight < 700.dp -> 24.dp
                        screenHeight < 1200.dp -> 32.dp
                        else -> 40.dp
                    }

                    // Responsive spacing
                    val contentSpacing = when {
                        screenHeight < 700.dp -> 16.dp
                        screenHeight < 1200.dp -> 20.dp
                        else -> 24.dp
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentHorizontalPadding)
                            .padding(top = contentTopPadding, bottom = contentBottomPadding),
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
                                screenWidth < 400.dp -> 36.dp
                                screenWidth < 600.dp -> 42.dp
                                else -> 50.dp
                            }

                            Spacer(modifier = Modifier.size(spacerSize)) // Spacer to balance the close button
                            RoundIconButton(
                                iconRes = R.drawable.x_white_icon,
                                contentDescription = stringResource(id = R.string.product_details_close),
                                gradient = gradient,
                                onClick = onDismiss,
                                screenWidth = screenWidth
                            )
                        }

                        // Error title
                        // Responsive title font size
                        val titleFontSize = when {
                            screenWidth < 400.dp -> 20.sp
                            screenWidth < 600.dp -> 24.sp
                            else -> 28.sp
                        }

                        Text(
                            text = stringResource(id = R.string.add_to_cart_error_title),
                            fontFamily = Fonts.Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = titleFontSize,
                            color = Color(0xFFB50938),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Error message
                        // Responsive message font size
                        val messageFontSize = when {
                            screenWidth < 400.dp -> 16.sp
                            screenWidth < 600.dp -> 18.sp
                            else -> 20.sp
                        }

                        val messageLineHeight = when {
                            screenWidth < 400.dp -> 22.sp
                            screenWidth < 600.dp -> 25.sp
                            else -> 28.sp
                        }

                        Text(
                            text = when (errorMessage) {
                                "QUANTITY_NOT_AVAILABLE" -> stringResource(R.string.add_to_cart_error_quantity_not_available)
                                "SERVER_ERROR" -> stringResource(R.string.product_details_error_server)
                                "NETWORK_ERROR" -> stringResource(R.string.product_details_error_network)
                                "TIMEOUT_ERROR" -> stringResource(R.string.product_details_error_network)
                                "NOT_FOUND" -> stringResource(R.string.add_to_cart_error_message)
                                "GENERIC_ERROR" -> stringResource(R.string.add_to_cart_error_message)
                                else -> stringResource(R.string.add_to_cart_error_message)
                            },
                            fontFamily = Fonts.Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = messageFontSize,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = messageLineHeight
                        )

                        // OK button
                        // Responsive button height
                        val buttonHeight = when {
                            screenWidth < 400.dp -> 42.dp
                            screenWidth < 600.dp -> 50.dp
                            else -> 66.dp
                        }

                        // Responsive button corner radius
                        val buttonCornerRadius = when {
                            screenWidth < 400.dp -> 24.dp
                            screenWidth < 600.dp -> 35.dp
                            else -> 50.dp
                        }

                        // Responsive button font size
                        val buttonFontSize = when {
                            screenWidth < 400.dp -> 12.sp
                            screenWidth < 600.dp -> 16.sp
                            else -> 22.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight)
                                .clip(RoundedCornerShape(buttonCornerRadius))
                                .background(gradient)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.add_to_cart_error_ok),
                                fontFamily = Fonts.Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = buttonFontSize,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Mock implementations for Preview
// Note: Since StorePreferences and LocationPreferences are final classes, we cannot inherit from them
// Instead, we'll use a different approach - create a mock ViewModel that doesn't require these dependencies
private class MockProductRepository : com.fashiontothem.ff.domain.repository.ProductRepository {
    override suspend fun getProductsByCategory(
        token: String,
        categoryId: String,
        categoryLevel: String,
        page: Int,
        filters: com.fashiontothem.ff.domain.repository.ProductFilters?,
        filterOptions: com.fashiontothem.ff.domain.model.FilterOptions?,
        activeFilters: Map<String, Set<String>>,
        preferConsolidatedCategories: Boolean,
    ): Result<com.fashiontothem.ff.domain.repository.ProductPageResult> {
        return Result.failure(Exception("Not implemented in Preview"))
    }

    override suspend fun getProductsByVisualSearch(
        token: String,
        image: String,
        page: Int,
        filters: com.fashiontothem.ff.domain.repository.ProductFilters?,
        filterOptions: com.fashiontothem.ff.domain.model.FilterOptions?,
        activeFilters: Map<String, Set<String>>,
    ): Result<com.fashiontothem.ff.domain.repository.ProductPageResult> {
        return Result.failure(Exception("Not implemented in Preview"))
    }

    override suspend fun getBrandImages(): Result<List<com.fashiontothem.ff.domain.model.BrandImage>> {
        return Result.failure(Exception("Not implemented in Preview"))
    }

    override suspend fun getProductDetails(
        barcodeOrSku: String,
        isSku: Boolean,
    ): Result<com.fashiontothem.ff.domain.repository.ProductDetailsResult> {
        return Result.failure(Exception("Not implemented in Preview"))
    }

    override suspend fun addToCart(
        loyaltyScannedBarcode: String,
        sku: String,
        sizeAttributeId: String,
        sizeOptionValue: String,
        colorAttributeId: String,
        colorOptionValue: String,
    ): Result<Boolean> {
        return Result.success(true)
    }
}

// Helper function to create mock UI state for previews
private fun createMockScanLoyaltyUiState(): ProductDetailsUiState {
    return ProductDetailsUiState(
        productDetails = com.fashiontothem.ff.domain.model.ProductDetails(
            id = "123",
            sku = "TEST-SKU-001",
            type = "configurable",
            name = "Test Product",
            shortDescription = "Test product description",
            brandName = "Test Brand",
            options = com.fashiontothem.ff.domain.model.ProductDetailsOptions(
                size = com.fashiontothem.ff.domain.model.OptionAttribute(
                    label = "Veličina",
                    attributeId = "93",
                    options = listOf(
                        com.fashiontothem.ff.domain.model.OptionValue("S", "1"),
                        com.fashiontothem.ff.domain.model.OptionValue("M", "2"),
                        com.fashiontothem.ff.domain.model.OptionValue("L", "3")
                    )
                ),
                color = null,
                colorShade = null
            ),
            images = com.fashiontothem.ff.domain.model.ProductDetailsImages(
                baseImg = null,
                imageList = emptyList()
            ),
            prices = com.fashiontothem.ff.domain.model.ProductDetailsPrices(
                isAdditionalLoyaltyDiscountAllowed = false,
                parentId = null,
                fictional = null,
                base = "1000",
                special = null,
                loyalty = null,
                id = "123"
            )
        ),
        stores = emptyList(),
        brandImageUrl = null,
        apiShortDescription = null,
        apiBrandName = null,
        isLoading = false,
        error = null,
        isProductUnavailable = false,
        selectedSize = "M",
        selectedColor = null,
        selectedStoreCode = null,
        selectedStoreId = null,
        isPickupPointEnabled = false
    )
}

// Helper function to create mock ViewModel for previews
@Composable
private fun createMockScanLoyaltyViewModel(): ProductDetailsViewModel {
    val mockContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
    return remember {
        ProductDetailsViewModel(
            productRepository = MockProductRepository(),
            storePreferences = com.fashiontothem.ff.data.local.preferences.StorePreferences(
                mockContext
            ),
            locationPreferences = com.fashiontothem.ff.data.local.preferences.LocationPreferences(
                mockContext
            )
        )
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun ScanLoyaltyCardPreviewSmall() {
    ScanLoyaltyCardScreen(
        uiState = createMockScanLoyaltyUiState(),
        viewModel = createMockScanLoyaltyViewModel(),
        onClose = {},
        onCardScanned = {}
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
private fun ScanLoyaltyCardPreviewMedium() {
    ScanLoyaltyCardScreen(
        uiState = createMockScanLoyaltyUiState(),
        viewModel = createMockScanLoyaltyViewModel(),
        onClose = {},
        onCardScanned = {}
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
private fun ScanLoyaltyCardPreviewLarge() {
    ScanLoyaltyCardScreen(
        uiState = createMockScanLoyaltyUiState(),
        viewModel = createMockScanLoyaltyViewModel(),
        onClose = {},
        onCardScanned = {}
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun ScanLoyaltyCardPreviewPhilips() {
    ScanLoyaltyCardScreen(
        uiState = createMockScanLoyaltyUiState(),
        viewModel = createMockScanLoyaltyViewModel(),
        onClose = {},
        onCardScanned = {}
    )
}

