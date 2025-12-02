@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.fashiontothem.ff.presentation.products

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.fashiontothem.ff.domain.model.OptionAttribute
import com.fashiontothem.ff.domain.model.OptionValue
import com.fashiontothem.ff.domain.model.ProductDetails
import com.fashiontothem.ff.domain.model.ProductDetailsImages
import com.fashiontothem.ff.domain.model.ProductDetailsOptions
import com.fashiontothem.ff.domain.model.ProductDetailsPrices
import com.fashiontothem.ff.presentation.common.BarcodeScannedDialog
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.Constants
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R
import kotlinx.coroutines.delay

/**
 * F&F Tothem - Product Details Screen
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProductDetailsScreen(
    sku: String?,
    shortDescription: String? = null,
    brandLabel: String? = null,
    isBarcodeScan: Boolean = false,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onCheckAvailability: () -> Unit,
    viewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Check if product details are already loaded
    // For barcode scans, we check if productDetails exists (regardless of SKU match)
    // For regular SKU, we check if the SKU matches
    val currentProductSku = uiState.productDetails?.sku
    val hasProductDetails = uiState.productDetails != null
    val isProductAlreadyLoaded = if (isBarcodeScan) {
        // For barcode scan, if we have product details, it's already loaded
        hasProductDetails
    } else {
        // For regular SKU, check if SKU matches
        currentProductSku != null && sku != null && 
        currentProductSku.equals(sku, ignoreCase = true)
    }
    
    var showBarcodeDialog by remember(
        sku,
        isBarcodeScan,
        isProductAlreadyLoaded
    ) { 
        val initialValue = isBarcodeScan && sku != null && !isProductAlreadyLoaded
        mutableStateOf(initialValue)
    }
    var showStandardLoader by remember(
        sku,
        isBarcodeScan,
        isProductAlreadyLoaded
    ) { 
        val initialValue = !isBarcodeScan && sku != null && !isProductAlreadyLoaded
        mutableStateOf(initialValue)
    }

    // Load product details when SKU is available
    // Only load if product details are not already loaded
    LaunchedEffect(sku, shortDescription, brandLabel, isBarcodeScan, hasProductDetails, currentProductSku) {
        if (sku != null) {
            // For barcode scan, check if we have any product details
            // For regular SKU, check if SKU matches
            val shouldLoad = if (isBarcodeScan) {
                !hasProductDetails
            } else {
                currentProductSku == null || !currentProductSku.equals(sku, ignoreCase = true)
            }
            
            if (shouldLoad) {
                if (isBarcodeScan) {
                    viewModel.loadProductDetailsByBarcode(sku)
                } else {
                    viewModel.loadProductDetails(sku, shortDescription, brandLabel)
                }
            }
        }
    }

    // Only show barcode dialog if product is not already loaded
    LaunchedEffect(sku, isBarcodeScan, isProductAlreadyLoaded) {
        if (isBarcodeScan && sku != null && !isProductAlreadyLoaded) {
            showBarcodeDialog = true
            delay(2500)
            showBarcodeDialog = false
        } else {
            showBarcodeDialog = false
        }
    }

    // Only show standard loader if product is not already loaded
    LaunchedEffect(sku, isBarcodeScan, isProductAlreadyLoaded) {
        if (!isBarcodeScan && sku != null && !isProductAlreadyLoaded) {
            showStandardLoader = true
            delay(1000)
            showStandardLoader = false
        } else {
            showStandardLoader = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dim overlay - clickable to close dialog
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onClose, enabled = !uiState.isLoading)
        )

        if (isBarcodeScan && showBarcodeDialog && sku != null) {
            BarcodeScannedDialog(barcode = sku)
        } else if (!isBarcodeScan && (uiState.isLoading || showStandardLoader)) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ProductDetailsLoader(
                    screenWidth = maxWidth,
                    screenHeight = maxHeight
                )
            }
        } else if (uiState.isLoading) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ProductDetailsLoader(
                    screenWidth = maxWidth,
                    screenHeight = maxHeight
                )
            }
    } else if (uiState.isProductUnavailable) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = maxWidth
            val horizontalPadding = when {
                screenWidth < 400.dp -> 10.dp
                screenWidth < 600.dp -> 20.dp
                else -> 40.dp
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                ProductUnavailableCard(
                    onBack = onBack,
                    screenWidth = screenWidth
                )
            }
        }
    } else if (uiState.error != null) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = maxWidth
            val horizontalPadding = when {
                screenWidth < 400.dp -> 16.dp
                screenWidth < 600.dp -> 28.dp
                else -> 40.dp
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                ProductErrorCard(
                    errorMessage = when (uiState.error) {
                        "SERVER_ERROR" -> stringResource(R.string.product_details_error_server)
                        "NETWORK_ERROR" -> stringResource(R.string.product_details_error_network)
                        "TIMEOUT_ERROR" -> stringResource(R.string.product_details_error_network)
                        "NOT_FOUND" -> stringResource(R.string.product_details_error_generic)
                        "GENERIC_ERROR" -> stringResource(R.string.product_details_error_generic)
                        else -> stringResource(R.string.product_details_error_loading)
                    },
                    onBack = onBack,
                    screenWidth = screenWidth
                )
            }
        }
    } else if (uiState.productDetails != null) {
        // Only show dialog when everything is loaded (not loading and has product details)
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
            // Responsive dialog dimensions
            val targetWidth = when {
                screenWidth < 400.dp -> (screenWidth * 0.95f).coerceAtMost(360.dp)
                screenWidth < 600.dp -> (screenWidth * 0.90f).coerceAtMost(550.dp)
                else -> (screenWidth * 0.85f).coerceAtMost(880.dp)
            }
            
            val targetHeight = when {
                screenHeight < 700.dp -> (screenHeight * 0.93f)
                screenHeight < 1200.dp -> (screenHeight * 0.95f)
                else -> (screenHeight * 0.83f)
            }
            
            // Responsive corner radius
            val cornerRadius = when {
                screenWidth < 400.dp -> 24.dp
                screenWidth < 600.dp -> 32.dp
                else -> 40.dp
            }

                // Animated dialog appearance - fade in + scale with initial values
                var startAnimation by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    startAnimation = true
                }

                val scale by animateFloatAsState(
                    targetValue = if (startAnimation) 1f else 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "dialog_scale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (startAnimation) 1f else 0f,
                    animationSpec = tween(durationMillis = 400),
                    label = "dialog_alpha"
                )

                Card(
                    shape = RoundedCornerShape(cornerRadius),
                    modifier = Modifier
                        .width(targetWidth)
                        .heightIn(max = targetHeight)
                        .padding(0.dp)
                        .scale(scale)
                        .alpha(alpha)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { /* Prevent click propagation to overlay */ }
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    ProductDetailsContent(
                        productDetails = uiState.productDetails!!,
                        passedShortDescription = shortDescription,
                        apiShortDescription = uiState.apiShortDescription,
                        brandImageUrl = uiState.brandImageUrl,
                        brandName = brandLabel,
                        apiBrandName = uiState.apiBrandName,
                        stores = uiState.stores,
                        selectedSize = uiState.selectedSize,
                        selectedColor = uiState.selectedColor,
                        secureBaseMediaUrl = uiState.secureBaseMediaUrl,
                        onSizeSelected = { viewModel.selectSize(it) },
                        onColorSelected = { viewModel.selectColor(it) },
                        onClose = onClose,
                        onCheckAvailability = {
                            // For simple products, allow navigation without selection
                            // For configurable products, require size or shade selection
                            val requiresSelection = uiState.productDetails?.requiresVariantSelection() ?: false
                            if (!requiresSelection || uiState.selectedSize != null || uiState.selectedColor != null) {
                                // onCheckAvailability callback will handle navigation based on isRetailOnly
                                onCheckAvailability()
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductErrorCard(
    errorMessage: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    // Responsive padding
    val horizontalPadding = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 36.dp
        else -> 48.dp
    }
    
    val verticalPadding = when {
        screenWidth < 400.dp -> 32.dp
        screenWidth < 600.dp -> 44.dp
        else -> 56.dp
    }
    
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 32.dp
        else -> 40.dp
    }
    
    // Responsive font sizes
    val errorFontSize = when {
        screenWidth < 400.dp -> 16.sp
        screenWidth < 600.dp -> 18.sp
        else -> 20.sp
    }
    
    val buttonFontSize = when {
        screenWidth < 400.dp -> 16.sp
        screenWidth < 600.dp -> 19.sp
        else -> 22.sp
    }
    
    // Responsive button height
    val buttonHeight = when {
        screenWidth < 400.dp -> 48.dp
        screenWidth < 600.dp -> 56.dp
        else -> 70.dp
    }
    
    val buttonCornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 35.dp
        else -> 50.dp
    }
    
    // Responsive spacing
    val contentSpacing = when {
        screenWidth < 400.dp -> 20.dp
        screenWidth < 600.dp -> 24.dp
        else -> 28.dp
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color(0xFF1A0033),
                            Color(0xFF00004D)
                        )
                    )
                )
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(contentSpacing)
            ) {
                Text(
                    text = errorMessage,
                    fontFamily = Fonts.Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = errorFontSize,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight)
                        .clip(RoundedCornerShape(buttonCornerRadius))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4F0418),
                                    Color(0xFFB50938)
                                )
                            )
                        )
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.product_details_back_button),
                        fontFamily = Fonts.Poppins,
                        fontSize = buttonFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductUnavailableCard(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    // Responsive padding
    val horizontalPadding = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 36.dp
        else -> 48.dp
    }
    
    val verticalPadding = when {
        screenWidth < 400.dp -> 32.dp
        screenWidth < 600.dp -> 44.dp
        else -> 56.dp
    }
    
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 32.dp
        else -> 40.dp
    }
    
    // Responsive font sizes
    val titleFontSize = when {
        screenWidth < 400.dp -> 24.sp
        screenWidth < 600.dp -> 28.sp
        else -> 34.sp
    }
    
    val messageFontSize = when {
        screenWidth < 400.dp -> 16.sp
        screenWidth < 600.dp -> 18.sp
        else -> 20.sp
    }
    
    val buttonFontSize = when {
        screenWidth < 400.dp -> 16.sp
        screenWidth < 600.dp -> 19.sp
        else -> 22.sp
    }
    
    // Responsive button height
    val buttonHeight = when {
        screenWidth < 400.dp -> 48.dp
        screenWidth < 600.dp -> 56.dp
        else -> 70.dp
    }
    
    val buttonCornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 35.dp
        else -> 50.dp
    }
    
    // Responsive spacing
    val contentSpacing = when {
        screenWidth < 400.dp -> 20.dp
        screenWidth < 600.dp -> 24.dp
        else -> 28.dp
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color(0xFF1A0033),
                            Color(0xFF00004D)
                        )
                    )
                )
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(contentSpacing)
            ) {
                Text(
                    text = stringResource(R.string.product_details_unavailable_title),
                    fontFamily = Fonts.Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = titleFontSize,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.product_details_unavailable_message),
                    fontFamily = Fonts.Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = messageFontSize,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight)
                        .clip(RoundedCornerShape(buttonCornerRadius))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4F0418),
                                    Color(0xFFB50938)
                                )
                            )
                        )
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.product_details_back_button),
                        fontFamily = Fonts.Poppins,
                        fontSize = buttonFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailsLoader(
    screenWidth: Dp? = null,
    screenHeight: Dp? = null,
) {
    // Create infinite transition for bouncing pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "bouncing_pulse")
    
    // Scale animation: 1.0f -> 1.2f -> 1.0f (bounce effect)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                1.0f at 0
                1.2f at 300
                1.0f at 600
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Alpha animation: 0.3f -> 1.0f -> 0.3f (fade in/out)
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                0.6f at 0
                1.0f at 300
                0.6f at 800
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    // Rotation animation: -15f -> 15f -> -15f (slight tilt)
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                -15f at 0
                15f at 400
                -15f at 800
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    // Responsive loader size
    val containerSize = if (screenWidth != null && screenHeight != null) {
        when {
            screenWidth < 400.dp -> 80.dp
            screenWidth < 600.dp -> 120.dp
            else -> 200.dp
        }
    } else {
        200.dp
    }
    
    val imageSize = if (screenWidth != null && screenHeight != null) {
        when {
            screenWidth < 400.dp -> 40.dp
            screenWidth < 600.dp -> 60.dp
            else -> 100.dp
        }
    } else {
        100.dp
    }
    
    Box(
        modifier = Modifier
            .size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.download_app),
            contentDescription = null,
            modifier = Modifier
                .size(imageSize)
                .scale(scale)
                .alpha(alpha)
                .rotate(rotation),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ProductDetailsContent(
    productDetails: ProductDetails,
    passedShortDescription: String?,
    apiShortDescription: String?,
    brandImageUrl: String?,
    brandName: String?,
    apiBrandName: String?,
    stores: List<com.fashiontothem.ff.domain.model.Store>,
    selectedSize: String?,
    selectedColor: String?,
    secureBaseMediaUrl: String?,
    onSizeSelected: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onClose: () -> Unit,
    onCheckAvailability: () -> Unit,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
    screenHeight: Dp,
) {
    // Use secureBaseMediaUrl from selected store, with fallback to default
    // secureBaseMediaUrl is like "https://fashion-assets.fashionandfriends.com/media/"
    // We need to append "catalog/product" to match the expected format
    val imageBaseUrl = secureBaseMediaUrl?.let { 
        if (it.endsWith("/")) {
            "${it}catalog/product"
        } else {
            "$it/catalog/product"
        }
    } ?: (Constants.FASHION_AND_FRIENDS_MEDIA_BASE_URL + "media/catalog/product")
    // Selected image path state - defaults to first available
    val imageListAll = (productDetails.images?.imageList ?: emptyList()).ifEmpty {
        listOfNotNull(
            productDetails.images?.baseImg
        )
    }
    var selectedImagePath by remember(productDetails.sku) { mutableStateOf(imageListAll.firstOrNull()) }
    
    // Responsive content padding
    val contentPadding = when {
        screenWidth < 400.dp -> 10.dp
        screenWidth < 600.dp -> 20.dp
        else -> 50.dp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(Color(0xFFFAFAFA))
    ) {
        val requiresSelection = productDetails.requiresVariantSelection()
        // Header - animated
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(300, delayMillis = 50)
            ) + slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProductDetailsHeader(
                productName = if (productDetails.isRetailOnly) "" else productDetails.name,
                brandImageUrl = brandImageUrl,
                brandName = brandName ?: apiBrandName,
                onClose = onClose,
                modifier = Modifier.fillMaxWidth(),
                screenWidth = screenWidth
            )
        }

        // Responsive LazyColumn padding
        val columnHorizontalPadding = when {
            screenWidth < 400.dp -> 6.dp
            screenWidth < 600.dp -> 10.dp
            else -> 20.dp
        }
        
        val columnVerticalPadding = when {
            screenHeight < 700.dp -> 8.dp
            screenHeight < 1200.dp -> 10.dp
            else -> 16.dp
        }
        
        val columnSpacing = when {
            screenHeight < 700.dp -> 10.dp
            screenHeight < 1200.dp -> 20.dp
            else -> 30.dp
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = columnHorizontalPadding, vertical = columnVerticalPadding),
            verticalArrangement = Arrangement.spacedBy(columnSpacing)
        ) {
            // Product Title - animated with stagger delay
            item(key = "title") {
                AnimatedVisibility(
                    visible = requiresSelection,
                    enter = fadeIn(
                        animationSpec = tween(300, delayMillis = 100)
                    ) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!productDetails.isRetailOnly) {
                        // Responsive title font size
                        val titleFontSize = when {
                            screenWidth < 400.dp -> 10.sp
                            screenWidth < 600.dp -> 14.sp
                            else -> 30.sp
                        }
                        
                        val titleLineHeight = when {
                            screenWidth < 400.dp -> 12.sp
                            screenWidth < 600.dp -> 20.sp
                            else -> 40.sp
                        }
                        
                        val titleBottomPadding = when {
                            screenWidth < 400.dp -> 4.dp
                            screenWidth < 600.dp -> 6.dp
                            else -> 8.dp
                        }
                        
                        Text(
                            text = productDetails.name,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Fonts.Poppins,
                            color = Color.Black,
                            lineHeight = titleLineHeight,
                            modifier = Modifier
                                .padding(bottom = titleBottomPadding)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Short description - use passed one if available, otherwise API one (from API response)
                val displayShortDescription = passedShortDescription ?: apiShortDescription
                displayShortDescription?.let { shortDesc ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = tween(300, delayMillis = 200)
                        ) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Responsive description font size
                        val descFontSize = when {
                            screenWidth < 400.dp -> 8.sp
                            screenWidth < 600.dp -> 12.sp
                            else -> 22.sp
                        }
                        
                        val descBottomPadding = when {
                            screenWidth < 400.dp -> 4.dp
                            screenWidth < 600.dp -> 6.dp
                            else -> 8.dp
                        }
                        
                        Text(
                            text = shortDesc,
                            fontSize = descFontSize,
                            fontWeight = FontWeight.Normal,
                            fontFamily = Fonts.Poppins,
                            color = Color(0xFF707070),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = descBottomPadding),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Product Images or Retail-Only UI - animated
            item(key = "images") {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(400, delayMillis = 300)
                    ) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (productDetails.isRetailOnly) {
                        RetailOnlyProductSection(
                            modifier = Modifier.fillMaxWidth(),
                            screenWidth = screenWidth
                        )
                    } else {
                        ProductImagesSection(
                            images = imageListAll,
                            selected = selectedImagePath,
                            onSelect = { selectedImagePath = it },
                            imageBaseUrl = imageBaseUrl,
                            modifier = Modifier.fillMaxWidth(),
                            screenWidth = screenWidth
                        )
                    }
                }
            }

            // Prices - animated (hide for retail-only products or when price is 0)
            item(key = "prices") {
                val shouldShowPrice = !productDetails.isRetailOnly && 
                    productDetails.prices.base != "0" && 
                    productDetails.prices.base.isNotBlank()
                AnimatedVisibility(
                    visible = shouldShowPrice,
                    enter = fadeIn(
                        animationSpec = tween(400, delayMillis = 500)
                    ) + slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProductPricesSection(
                        fictional = productDetails.prices.fictional,
                        base = productDetails.prices.base,
                        special = productDetails.prices.special,
                        modifier = Modifier.fillMaxWidth(),
                        screenWidth = screenWidth
                    )
                }
            }

            // Size or Shade Selection (only one - size takes priority) - animated
            item(key = "options") {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(400, delayMillis = 600)
                    ) + slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when {
                        productDetails.options?.size != null -> {
                            // Extend size options with sizes from stores variants
                            val extendedSizeOptions = remember(stores, productDetails.options!!.size!!) {
                                val baseOptions = productDetails.options!!.size!!.options.toMutableList()
                                
                                // Add sizes from stores variants that are not already in options
                                stores.flatMap { store ->
                                    store.variants.orEmpty().mapNotNull { variant ->
                                        val variantSize = variant.superAttribute?.size ?: variant.size
                                        variantSize?.trim()?.takeIf { it.isNotBlank() }
                                    }
                                }
                                    .distinct()
                                    .filter { sizeLabel ->
                                        // Check if this size label matches any existing option value
                                        !baseOptions.any { opt ->
                                            opt.label.equals(sizeLabel, ignoreCase = true) ||
                                            opt.value.equals(sizeLabel, ignoreCase = true)
                                        }
                                    }
                                    .map { sizeLabel ->
                                        // Create new option for this size
                                        com.fashiontothem.ff.domain.model.OptionValue(
                                            label = sizeLabel,
                                            value = sizeLabel // Use label as value for retail-only sizes
                                        )
                                    }
                                    .forEach { newOption ->
                                        if (!baseOptions.any { it.label.equals(newOption.label, ignoreCase = true) }) {
                                            baseOptions.add(newOption)
                                        }
                                    }
                                
                                // Sort sizes: XS, S, M, L, XL, XXL, XXXL, etc.
                                val sortedOptions = baseOptions.sortedBy { option ->
                                    getSizeSortOrder(option.label)
                                }
                                
                                productDetails.options!!.size!!.copy(options = sortedOptions)
                            }
                            
                            SizeSelectionSection(
                                sizeOptions = extendedSizeOptions,
                                selectedSize = selectedSize,
                                onSizeSelected = onSizeSelected,
                                modifier = Modifier.fillMaxWidth(),
                                screenWidth = screenWidth
                            )
                        }

                        productDetails.options?.colorShade != null -> {
                            // Extend shade options with shades from stores variants
                            val extendedShadeOptions = remember(stores, productDetails.options!!.colorShade!!) {
                                val baseOptions = productDetails.options!!.colorShade!!.options.toMutableList()
                                
                                // Add shades from stores variants that are not already in options
                                stores.flatMap { store ->
                                    store.variants.orEmpty().mapNotNull { variant ->
                                        val variantShade = variant.superAttribute?.colorShade ?: variant.shade
                                        variantShade?.trim()?.takeIf { it.isNotBlank() }
                                    }
                                }
                                    .distinct()
                                    .filter { shadeLabel ->
                                        // Check if this shade label matches any existing option value
                                        !baseOptions.any { opt ->
                                            opt.label.equals(shadeLabel, ignoreCase = true) ||
                                            opt.value.equals(shadeLabel, ignoreCase = true)
                                        }
                                    }
                                    .map { shadeLabel ->
                                        // Create new option for this shade
                                        com.fashiontothem.ff.domain.model.OptionValue(
                                            label = shadeLabel,
                                            value = shadeLabel // Use label as value for retail-only shades
                                        )
                                    }
                                    .forEach { newOption ->
                                        if (!baseOptions.any { it.label.equals(newOption.label, ignoreCase = true) }) {
                                            baseOptions.add(newOption)
                                        }
                                    }
                                
                                // Sort shades alphabetically
                                val sortedOptions = baseOptions.sortedBy { option ->
                                    option.label.lowercase()
                                }
                                
                                productDetails.options!!.colorShade!!.copy(options = sortedOptions)
                            }
                            
                            ShadeSelectionSection(
                                shadeOptions = extendedShadeOptions,
                                selectedShade = selectedColor,
                                onShadeSelected = onColorSelected,
                                modifier = Modifier.fillMaxWidth(),
                                screenWidth = screenWidth
                            )
                        }

                        productDetails.options?.color != null -> {
                            // Extend color options with colors from stores variants
                            val extendedColorOptions = remember(stores, productDetails.options!!.color!!) {
                                val baseOptions = productDetails.options!!.color!!.options.toMutableList()
                                
                                // Add colors from stores variants that are not already in options
                                stores.flatMap { store ->
                                    store.variants.orEmpty().mapNotNull { variant ->
                                        val variantColor = variant.superAttribute?.color ?: variant.shade
                                        variantColor?.trim()?.takeIf { it.isNotBlank() }
                                    }
                                }
                                    .distinct()
                                    .filter { colorLabel ->
                                        // Check if this color label matches any existing option value
                                        !baseOptions.any { opt ->
                                            opt.label.equals(colorLabel, ignoreCase = true) ||
                                            opt.value.equals(colorLabel, ignoreCase = true)
                                        }
                                    }
                                    .map { colorLabel ->
                                        // Create new option for this color
                                        com.fashiontothem.ff.domain.model.OptionValue(
                                            label = colorLabel,
                                            value = colorLabel // Use label as value for retail-only colors
                                        )
                                    }
                                    .forEach { newOption ->
                                        if (!baseOptions.any { it.label.equals(newOption.label, ignoreCase = true) }) {
                                            baseOptions.add(newOption)
                                        }
                                    }
                                
                                // Sort colors alphabetically
                                val sortedOptions = baseOptions.sortedBy { option ->
                                    option.label.lowercase()
                                }
                                
                                productDetails.options!!.color!!.copy(options = sortedOptions)
                            }
                            
                            ShadeSelectionSection(
                                shadeOptions = extendedColorOptions,
                                selectedShade = selectedColor,
                                onShadeSelected = onColorSelected,
                                modifier = Modifier.fillMaxWidth(),
                                screenWidth = screenWidth
                            )
                        }
                    }
                }
            }

            // Check Availability Button - animated
            item(key = "button") {
                // For simple products (no selection required), button is always enabled
                // For configurable products, check if size or shade is selected
                val isSimpleProduct = !productDetails.type.equals("configurable", ignoreCase = true)
                val hasSelection = if (isSimpleProduct || !requiresSelection) {
                    // Simple product or product without selectable options - button always enabled
                    true
                } else {
                    // Configurable product - requires selection of size or shade
                    val hasShadeOption = productDetails.options?.colorShade != null
                    val sizeSelected = selectedSize != null
                    val shadeSelected = hasShadeOption && selectedColor != null
                    sizeSelected || shadeSelected
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(400, delayMillis = 700)
                    ) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CheckAvailabilityButton(
                        onClick = onCheckAvailability,
                        enabled = hasSelection,
                        modifier = Modifier.fillMaxWidth(),
                        screenWidth = screenWidth
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetailsHeader(
    productName: String,
    brandImageUrl: String?,
    brandName: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    val context = LocalContext.current
    
    // Responsive padding
    val horizontalPadding = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 10.dp
        else -> 20.dp
    }
    
    val verticalPadding = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 10.dp
        else -> 16.dp
    }
    
    // Responsive close button size
    val closeButtonSize = when {
        screenWidth < 400.dp -> 20.dp
        screenWidth < 600.dp -> 24.dp
        else -> 50.dp
    }
    
    // Responsive spacer size
    val spacerWidth = when {
        screenWidth < 400.dp -> 30.dp
        screenWidth < 600.dp -> 40.dp
        else -> 50.dp
    }
    
    // Responsive brand logo height
    val brandLogoHeight = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 50.dp
        else -> 100.dp
    }
    
    // Responsive brand text font size
    val brandTextSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 12.sp
        else -> 20.sp
    }

    Row(
        modifier = modifier
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Spacer to balance the close button on the right
        Spacer(modifier = Modifier.width(spacerWidth))

        // Brand image or name (centered independently)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (brandImageUrl != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(brandImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = brandName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = brandLogoHeight),
                    loading = {
                        // Show text while loading brand image
                        val displayBrandName =
                            brandName ?: (if (productName.isNotBlank()) productName.split(" - ").firstOrNull() ?: "" else "")
                        if (displayBrandName.isNotBlank()) {
                            Text(
                                text = displayBrandName,
                                fontSize = brandTextSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Fonts.Poppins,
                                color = Color.Black,
                                style = TextStyle(textAlign = TextAlign.Center)
                            )
                        } else {
                            Box {} // Empty box when no brand name
                        }
                    },
                    success = {
                        SubcomposeAsyncImageContent(
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = brandLogoHeight)
                        )
                    },
                    error = {
                        // Fallback to text on error
                        val displayBrandName =
                            brandName ?: (if (productName.isNotBlank()) productName.split(" - ").firstOrNull() ?: "" else "")
                        if (displayBrandName.isNotBlank()) {
                            Text(
                                text = displayBrandName,
                                fontSize = brandTextSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Fonts.Poppins,
                                color = Color.Black,
                                style = TextStyle(textAlign = TextAlign.Center)
                            )
                        } else {
                            Box {} // Empty box when no brand name
                        }
                    }
                )
            } else {
                // Fallback to text
                val displayBrandName = brandName ?: (if (productName.isNotBlank()) productName.split(" - ").firstOrNull() ?: "" else "")
                if (displayBrandName.isNotBlank()) {
                    Text(
                        text = displayBrandName,
                        fontSize = brandTextSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Fonts.Poppins,
                        color = Color.Black,
                        style = TextStyle(textAlign = TextAlign.Center)
                    )
                }
                // If no brand name, just show empty space (nothing to display)
            }
        }

        // Close button
        IconButton(onClick = onClose) {
            Box(
                modifier = Modifier
                    .size(closeButtonSize)
                    .background(Color(0xFFB50938), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.x_white_icon),
                    contentDescription = stringResource(R.string.product_details_close),
                    modifier = Modifier.size(closeButtonSize * 0.6f),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun ProductImagesSection(
    images: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    imageBaseUrl: String,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    val context = LocalContext.current
    val mainImage = selected ?: images.firstOrNull()

    // Remember the aspect ratio of the first image to maintain consistent height
    var imageAspectRatio by remember(images.firstOrNull()) { mutableStateOf<Float?>(null) }
    
    // Responsive thumbnail size
    val thumbnailWidth = when {
        screenWidth < 400.dp -> 30.dp
        screenWidth < 600.dp -> 50.dp
        else -> 80.dp
    }
    
    val thumbnailSpacing = when {
        screenWidth < 400.dp -> 4.dp
        screenWidth < 600.dp -> 6.dp
        else -> 8.dp
    }
    
    val rowSpacing = when {
        screenWidth < 400.dp -> 6.dp
        screenWidth < 600.dp -> 8.dp
        else -> 12.dp
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(rowSpacing),
        verticalAlignment = Alignment.Top
    ) {
        // Thumbnails column
        if (images.size > 1) {
            Column(
                modifier = Modifier
                    .width(thumbnailWidth),
                verticalArrangement = Arrangement.spacedBy(thumbnailSpacing),
                horizontalAlignment = Alignment.Start
            ) {
                images.take(5).forEach { imagePath ->
                    // Image paths from API already include leading slash, e.g., "/S/B/SBSAG0004-01430H-1.jpg"
                    val imageUrl = if (imagePath.startsWith("/")) {
                        "$imageBaseUrl$imagePath"
                    } else {
                        "$imageBaseUrl/$imagePath"
                    }
                    Box(
                        modifier = Modifier
                            .width(thumbnailWidth)
                            .aspectRatio(80f / 105f) // 80x105 according to design
                            .clickable { onSelect(imagePath) }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (imagePath != selected) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.White.copy(alpha = 0.55f))
                            )
                        }
                    }
                }
            }
        }

        // Main image - align top with first thumbnail
        mainImage?.let { imagePath ->
            // Image paths from API already include leading slash, e.g., "/S/B/SBSAG0004-01430H-1.jpg"
            val imageUrl = if (imagePath.startsWith("/")) {
                "$imageBaseUrl$imagePath"
            } else {
                "$imageBaseUrl/$imagePath"
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Top),
                contentAlignment = if (images.size > 1) Alignment.TopStart else Alignment.Center
            ) {
                // Responsive dimensions according to design: 335x448
                val designAspectRatio = 335f / 448f
                val maxHeight = when {
                    screenWidth < 400.dp -> 100.dp
                    screenWidth < 600.dp -> 200.dp
                    else -> 448.dp
                }
                
                val maxWidthNoThumbnails = when {
                    screenWidth < 400.dp -> 200.dp
                    screenWidth < 600.dp -> 350.dp
                    else -> 650.dp
                }

                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Product image",
                    modifier = Modifier
                        .then(
                            if (images.size > 1) {
                                // When thumbnails exist, use full width
                                Modifier.fillMaxWidth()
                            } else {
                                // When no thumbnails, allow larger size but limit to reasonable max
                                Modifier.widthIn(max = maxWidthNoThumbnails)
                            }
                        )
                        .aspectRatio(designAspectRatio) // 335x448 according to design
                        .then(
                            if (images.size > 1) {
                                Modifier.heightIn(max = maxHeight) // Max height when thumbnails exist
                            } else {
                                val maxHeightNoThumbnails = when {
                                    screenWidth < 400.dp -> 400.dp
                                    screenWidth < 600.dp -> 550.dp
                                    else -> 880.dp
                                }
                                Modifier.heightIn(max = maxHeightNoThumbnails) // Larger max height when no thumbnails
                            }
                        ),
                    contentScale = ContentScale.Fit,
                    success = { state ->
                        // Remember aspect ratio from first loaded image
                        if (imageAspectRatio == null) {
                            val painter = state.painter
                            val intrinsicSize = painter.intrinsicSize
                            if (intrinsicSize.width > 0 && intrinsicSize.height > 0) {
                                imageAspectRatio = intrinsicSize.width / intrinsicSize.height
                            }
                        }
                        SubcomposeAsyncImageContent()
                    },
                    loading = {
                        // Show placeholder box with responsive dimensions
                        val designAspectRatio = 335f / 448f
                        val placeholderMaxHeight = when {
                            screenWidth < 400.dp -> 250.dp
                            screenWidth < 600.dp -> 300.dp
                            else -> 448.dp
                        }
                        
                        val placeholderMaxWidthNoThumbnails = when {
                            screenWidth < 400.dp -> 300.dp
                            screenWidth < 600.dp -> 420.dp
                            else -> 650.dp
                        }
                        
                        Box(
                            modifier = Modifier
                                .then(
                                    if (images.size > 1) {
                                        // When thumbnails exist, use full width
                                        Modifier.fillMaxWidth()
                                    } else {
                                        // When no thumbnails, allow larger size
                                        Modifier.widthIn(max = placeholderMaxWidthNoThumbnails)
                                    }
                                )
                                .aspectRatio(designAspectRatio) // 335x448 according to design
                                .then(
                                    if (images.size > 1) {
                                        Modifier.heightIn(max = placeholderMaxHeight)
                                    } else {
                                        val placeholderMaxHeightNoThumbnails = when {
                                            screenWidth < 400.dp -> 500.dp
                                            screenWidth < 600.dp -> 600.dp
                                            else -> 880.dp
                                        }
                                        Modifier.heightIn(max = placeholderMaxHeightNoThumbnails) // Larger when no thumbnails
                                    }
                                )
                                .background(Color(0xFFF5F5F5))
                        )
                    },
                    error = {
                        // Show placeholder box with responsive dimensions
                        val designAspectRatio = 335f / 448f
                        val placeholderMaxHeight = when {
                            screenWidth < 400.dp -> 280.dp
                            screenWidth < 600.dp -> 350.dp
                            else -> 448.dp
                        }
                        
                        val placeholderMaxWidthNoThumbnails = when {
                            screenWidth < 400.dp -> 300.dp
                            screenWidth < 600.dp -> 450.dp
                            else -> 650.dp
                        }
                        
                        Box(
                            modifier = Modifier
                                .then(
                                    if (images.size > 1) {
                                        // When thumbnails exist, use full width
                                        Modifier.fillMaxWidth()
                                    } else {
                                        // When no thumbnails, allow larger size
                                        Modifier.widthIn(max = placeholderMaxWidthNoThumbnails)
                                    }
                                )
                                .aspectRatio(designAspectRatio) // 335x448 according to design
                                .then(
                                    if (images.size > 1) {
                                        Modifier.heightIn(max = placeholderMaxHeight)
                                    } else {
                                        val placeholderMaxHeightNoThumbnails = when {
                                            screenWidth < 400.dp -> 500.dp
                                            screenWidth < 600.dp -> 650.dp
                                            else -> 880.dp
                                        }
                                        Modifier.heightIn(max = placeholderMaxHeightNoThumbnails) // Larger when no thumbnails
                                    }
                                )
                                .background(Color(0xFFF5F5F5))
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun RetailOnlyProductSection(
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    // Responsive height
    val sectionHeight = when {
        screenWidth < 400.dp -> 280.dp
        screenWidth < 600.dp -> 340.dp
        else -> 400.dp
    }
    
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 18.dp
        else -> 20.dp
    }
    
    // Responsive padding
    val sectionPadding = when {
        screenWidth < 400.dp -> 20.dp
        screenWidth < 600.dp -> 26.dp
        else -> 32.dp
    }
    
    // Responsive spacing
    val contentSpacing = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 20.dp
        else -> 24.dp
    }
    
    // Responsive icon size
    val iconBoxSize = when {
        screenWidth < 400.dp -> 80.dp
        screenWidth < 600.dp -> 100.dp
        else -> 120.dp
    }
    
    val iconSize = when {
        screenWidth < 400.dp -> 54.dp
        screenWidth < 600.dp -> 67.dp
        else -> 80.dp
    }
    
    // Responsive font sizes
    val titleFontSize = when {
        screenWidth < 400.dp -> 18.sp
        screenWidth < 600.dp -> 21.sp
        else -> 24.sp
    }
    
    val messageFontSize = when {
        screenWidth < 400.dp -> 14.sp
        screenWidth < 600.dp -> 16.sp
        else -> 18.sp
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(sectionHeight)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF5F5F5),
                        Color(0xFFE8E8E8)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(sectionPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            // Store icon or placeholder
            Box(
                modifier = Modifier
                    .size(iconBoxSize)
                    .background(
                        Color(0xFFD0D0D0),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.item_available_icon),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    contentScale = ContentScale.Fit
                )
            }
            
            Text(
                text = stringResource(R.string.product_retail_only_title),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = titleFontSize,
                color = Color(0xFF5E5E5E),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = stringResource(R.string.product_retail_only_message),
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = messageFontSize,
                color = Color(0xFF8C8C8C),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProductPricesSection(
    fictional: String?,
    base: String,
    special: String?,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    // Responsive font size
    val priceFontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 16.sp
        else -> 28.sp
    }
    
    // If fictional price exists, show both prices (strikethrough + discounted)
    // Otherwise, show only the price centered
    if (fictional != null) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Original price (fictional)
            Text(
                text = "$fictional ${stringResource(R.string.product_details_rsd)}",
                fontSize = priceFontSize,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF000000),
                textDecoration = TextDecoration.LineThrough,
                modifier = Modifier.weight(1f)
            )

            // Discounted price (special or base)
            val discountedPrice = special ?: base
            Text(
                text = "$discountedPrice ${stringResource(R.string.product_details_rsd)}",
                fontSize = priceFontSize,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Fonts.Poppins,
                color = Color(0xFFB50938),
                modifier = Modifier.weight(1f),
                style = TextStyle(textAlign = TextAlign.End)
            )
        }
    } else {
        // Only one price - show it centered
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val price = special ?: base
            Text(
                text = "$price ${stringResource(R.string.product_details_rsd)}",
                fontSize = priceFontSize,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Fonts.Poppins,
                color = Color(0xFFB50938),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Get sort order for size labels (XS, S, M, L, XL, XXL, XXXL, etc.)
 */
private fun getSizeSortOrder(sizeLabel: String): Int {
    val normalized = sizeLabel.trim().uppercase()
    return when {
        normalized == "XS" || normalized == "XXS" -> 0
        normalized == "S" -> 1
        normalized == "M" -> 2
        normalized == "L" -> 3
        normalized == "XL" -> 4
        normalized == "XXL" -> 5
        normalized == "XXXL" -> 6
        normalized == "XXXXL" -> 7
        normalized.startsWith("XXS") -> -1
        normalized.startsWith("XS") -> 0
        normalized.startsWith("S") && !normalized.startsWith("XL") -> 1
        normalized.startsWith("M") -> 2
        normalized.startsWith("L") && !normalized.startsWith("XL") -> 3
        normalized.startsWith("XL") -> {
            val xCount = normalized.count { it == 'X' }
            when {
                xCount >= 4 -> 7
                xCount == 3 -> 6
                xCount == 2 -> 5
                else -> 4
            }
        }
        else -> {
            // Try to parse as number (e.g., "36", "38", "40")
            normalized.toIntOrNull() ?: 1000
        }
    }
}

@Composable
private fun SizeSelectionSection(
    sizeOptions: OptionAttribute,
    selectedSize: String?,
    onSizeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4F0418),
            Color(0xFFB50938)
        )
    )
    
    // Responsive title font size
    val titleFontSize = when {
        screenWidth < 400.dp -> 8.sp
        screenWidth < 600.dp -> 14.sp
        else -> 24.sp
    }
    
    // Responsive title bottom padding
    val titleBottomPadding = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 12.dp
        else -> 20.dp
    }
    
    // Responsive button padding
    val buttonPaddingHorizontal = when {
        screenWidth < 400.dp -> 10.dp
        screenWidth < 600.dp -> 20.dp
        else -> 40.dp
    }
    
    val buttonPaddingVertical = when {
        screenWidth < 400.dp -> 10.dp
        screenWidth < 600.dp -> 12.dp
        else -> 20.dp
    }
    
    // Responsive button font size
    val buttonFontSize = when {
        screenWidth < 400.dp -> 8.sp
        screenWidth < 600.dp -> 10.sp
        else -> 18.sp
    }
    
    // Responsive spacing
    val buttonSpacing = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 10.dp
        else -> 20.dp
    }
    
    // Responsive content padding
    val contentPadding = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 18.dp
        else -> 50.dp
    }
    
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 30.dp
        screenWidth < 600.dp -> 40.dp
        else -> 50.dp
    }
    
    // Responsive border width
    val borderWidth = when {
        screenWidth < 400.dp -> 1.5.dp
        screenWidth < 600.dp -> 1.75.dp
        else -> 2.dp
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.product_details_select_size),
            fontSize = titleFontSize,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Fonts.Poppins,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = titleBottomPadding),
            textAlign = TextAlign.Center
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing, Alignment.CenterHorizontally),
            contentPadding = PaddingValues(horizontal = contentPadding)
        ) {
            items(sizeOptions.options) { option ->
                val isSelected = selectedSize == option.value
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(cornerRadius))
                        .then(
                            if (isSelected) {
                                Modifier.background(gradient, shape = RoundedCornerShape(cornerRadius))
                            } else {
                                Modifier
                                    .border(
                                        width = borderWidth,
                                        color = Color(0xFFEBEBEB),
                                        shape = RoundedCornerShape(cornerRadius)
                                    )
                                    .background(Color.White, shape = RoundedCornerShape(cornerRadius))
                            }
                        )
                        .clickable { onSizeSelected(option.value) }
                        .padding(horizontal = buttonPaddingHorizontal, vertical = buttonPaddingVertical)
                ) {
                    Text(
                        text = option.label,
                        color = if (isSelected) Color.White else Color.Black,
                        fontFamily = Fonts.Poppins,
                        fontSize = buttonFontSize,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ShadeSelectionSection(
    shadeOptions: OptionAttribute,
    selectedShade: String?,
    onShadeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4F0418),
            Color(0xFFB50938)
        )
    )
    
    // Responsive title font size
    val titleFontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 14.sp
        else -> 24.sp
    }
    
    // Responsive title bottom padding
    val titleBottomPadding = when {
        screenWidth < 400.dp -> 10.dp
        screenWidth < 600.dp -> 12.dp
        else -> 20.dp
    }
    
    // Responsive button padding
    val buttonPaddingHorizontal = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 20.dp
        else -> 40.dp
    }
    
    val buttonPaddingVertical = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 10.dp
        else -> 20.dp
    }
    
    // Responsive button font size
    val buttonFontSize = when {
        screenWidth < 400.dp -> 8.sp
        screenWidth < 600.dp -> 12.sp
        else -> 18.sp
    }
    
    // Responsive spacing
    val buttonSpacing = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 12.dp
        else -> 20.dp
    }
    
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 30.dp
        screenWidth < 600.dp -> 40.dp
        else -> 50.dp
    }
    
    // Responsive border width
    val borderWidth = when {
        screenWidth < 400.dp -> 1.5.dp
        screenWidth < 600.dp -> 1.75.dp
        else -> 2.dp
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.product_details_select_shade),
            fontSize = titleFontSize,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Fonts.Poppins,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = titleBottomPadding),
            textAlign = TextAlign.Center
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing, Alignment.Start),
            contentPadding = PaddingValues(horizontal = 0.dp) // No padding - section already offset
        ) {
            items(shadeOptions.options) { option ->
                val isSelected = selectedShade == option.value
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(cornerRadius))
                        .then(
                            if (isSelected) {
                                Modifier.background(gradient, shape = RoundedCornerShape(cornerRadius))
                            } else {
                                Modifier
                                    .border(
                                        width = borderWidth,
                                        color = Color(0xFFEBEBEB),
                                        shape = RoundedCornerShape(cornerRadius)
                                    )
                                    .background(Color.White, shape = RoundedCornerShape(cornerRadius))
                            }
                        )
                        .clickable { onShadeSelected(option.value) }
                        .padding(horizontal = buttonPaddingHorizontal, vertical = buttonPaddingVertical)
                ) {
                    Text(
                        text = option.label,
                        color = if (isSelected) Color.White else Color.Black,
                        fontFamily = Fonts.Poppins,
                        fontSize = buttonFontSize,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
private fun CheckAvailabilityButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    val debouncedClick = rememberDebouncedClick(onClick = onClick)
    
    // Responsive button height
    val buttonHeight = when {
        screenWidth < 400.dp -> 30.dp
        screenWidth < 600.dp -> 40.dp
        else -> 70.dp
    }
    
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 35.dp
        else -> 50.dp
    }
    
    // Responsive font size
    val buttonFontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 14.sp
        else -> 22.sp
    }
    
    // Responsive top padding
    val topPadding = when {
        screenWidth < 400.dp -> 6.dp
        screenWidth < 600.dp -> 4.dp
        else -> 20.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .height(buttonHeight)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                if (enabled) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4F0418),
                            Color(0xFFB50938)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFCCCCCC),
                            Color(0xFF999999)
                        )
                    )
                }
            )
            .clickable(enabled = enabled, onClick = debouncedClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.product_details_check_availability),
            fontFamily = Fonts.Poppins,
            fontSize = buttonFontSize,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) Color.White else Color(0xFF666666)
        )
    }
}

// Helper function to create mock product details for previews
private fun createMockProductDetails(): ProductDetails {
    return ProductDetails(
        id = "1121978",
        sku = "SBSAG0004-01430H",
        type = "configurable",
        name = "Miss Sixty - Ženski polo džemper",
        shortDescription = "Rebrasti džemper od vune i kašmira",
        brandName = "Miss Sixty",
        options = ProductDetailsOptions(
            size = OptionAttribute(
                label = "Veličina",
                attributeId = "242",
                options = listOf(
                    OptionValue("XXS", "5501"),
                    OptionValue("XS", "5502"),
                    OptionValue("S", "5503"),
                    OptionValue("M", "5504"),
                    OptionValue("L", "5505")
                )
            ),
            color = OptionAttribute(
                label = "Boja",
                attributeId = "93",
                options = listOf(
                    OptionValue("Pink", "5487"),
                    OptionValue("Crna", "5476")
                )
            ),
            colorShade = null
        ),
        images = ProductDetailsImages(
            baseImg = "/S/B/SBSAG0004-01430H-1.jpg",
            imageList = listOf(
                "/S/B/SBSAG0004-01430H-1.jpg",
                "/S/B/SBSAG0004-01430H-2.jpg",
                "/S/B/SBSAG0004-01430H-3.jpg"
            )
        ),
        prices = ProductDetailsPrices(
            isAdditionalLoyaltyDiscountAllowed = true,
            parentId = null,
            fictional = "23.990,00",
            base = "16.793,00",
            special = "16.793,00",
            loyalty = null,
            id = "1121871"
        )
    )
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun ProductDetailsScreenPreviewSmall() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Image(
                painter = painterResource(id = R.drawable.splash_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Centered modal card
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    ProductDetailsContent(
                        productDetails = createMockProductDetails(),
                        passedShortDescription = null,
                        apiShortDescription = createMockProductDetails().shortDescription,
                        brandImageUrl = null,
                        brandName = "Miss Sixty",
                        apiBrandName = null,
                        stores = emptyList(),
                        selectedSize = null,
                        selectedColor = null,
                        secureBaseMediaUrl = null,
                        onSizeSelected = {},
                        onColorSelected = {},
                        onClose = {},
                        onCheckAvailability = {},
                        modifier = Modifier.fillMaxSize(),
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
            }
        }
    }
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
private fun ProductDetailsScreenPreviewMedium() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Image(
                painter = painterResource(id = R.drawable.splash_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Centered modal card
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    ProductDetailsContent(
                        productDetails = createMockProductDetails(),
                        passedShortDescription = null,
                        apiShortDescription = createMockProductDetails().shortDescription,
                        brandImageUrl = null,
                        brandName = "Miss Sixty",
                        apiBrandName = null,
                        stores = emptyList(),
                        selectedSize = null,
                        selectedColor = null,
                        secureBaseMediaUrl = null,
                        onSizeSelected = {},
                        onColorSelected = {},
                        onClose = {},
                        onCheckAvailability = {},
                        modifier = Modifier.fillMaxSize(),
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
            }
        }
    }
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
private fun ProductDetailsScreenPreviewLarge() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Image(
                painter = painterResource(id = R.drawable.splash_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Centered modal card
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    ProductDetailsContent(
                        productDetails = createMockProductDetails(),
                        passedShortDescription = null,
                        apiShortDescription = createMockProductDetails().shortDescription,
                        brandImageUrl = null,
                        brandName = "Miss Sixty",
                        apiBrandName = null,
                        stores = emptyList(),
                        selectedSize = null,
                        selectedColor = null,
                        secureBaseMediaUrl = null,
                        onSizeSelected = {},
                        onColorSelected = {},
                        onClose = {},
                        onCheckAvailability = {},
                        modifier = Modifier.fillMaxSize(),
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
            }
        }
    }
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun ProductDetailsScreenPreviewPhilips() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Image(
                painter = painterResource(id = R.drawable.splash_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Centered modal card
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    ProductDetailsContent(
                        productDetails = createMockProductDetails(),
                        passedShortDescription = null,
                        apiShortDescription = createMockProductDetails().shortDescription,
                        brandImageUrl = null,
                        brandName = "Miss Sixty",
                        apiBrandName = null,
                        stores = emptyList(),
                        selectedSize = null,
                        selectedColor = null,
                        secureBaseMediaUrl = null,
                        onSizeSelected = {},
                        onColorSelected = {},
                        onClose = {},
                        onCheckAvailability = {},
                        modifier = Modifier.fillMaxSize(),
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
            }
        }
    }
}

