package com.fashiontothem.ff.presentation.products

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import com.fashiontothem.ff.domain.model.Product
import com.fashiontothem.ff.presentation.common.FashionLoader
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Helper function to get valid discount percentage for display
 * Filters out null, zero, and negative values
 * If backend returns decimal values (Double), this will round to nearest integer
 * Note: Currently discountPercentage is Int? in models, but this function is ready
 * for future changes if backend starts returning Double values
 */
private fun getValidDiscountPercentage(discount: Int?): Int? {
    return discount?.takeIf { it > 0 }
}


@Composable
fun ProductListingScreen(
    categoryId: String? = null,
    categoryLevel: String? = null,
    filterType: String = "none",
    onBack: () -> Unit,
    onHome: () -> Unit = onBack,
    onOpenFilters: () -> Unit = {},
    onNavigateToProductDetails: (sku: String, shortDescription: String?, brandLabel: String?) -> Unit = { _, _, _ -> },
    viewModel: ProductListingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridColumns by viewModel.gridColumns.collectAsStateWithLifecycle() // ✅ Grid columns from ViewModel
    val shouldResetScroll by viewModel.shouldResetScroll.collectAsStateWithLifecycle() // ✅ Scroll reset trigger

    LaunchedEffect(categoryId, categoryLevel, filterType) {
        // Check if we have a visual search image in DataStore
        viewModel.checkAndLoadVisualSearchOrCategory(categoryId, categoryLevel, filterType)
    }

    ProductListingContent(
        uiState = uiState,
        gridColumns = gridColumns,
        shouldResetScroll = shouldResetScroll, // ✅ Pass scroll reset trigger
        onGridColumnsChange = { viewModel.toggleGridColumns() }, // ✅ Use ViewModel method
        onScrollResetComplete = { viewModel.onScrollResetComplete() }, // ✅ Notify ViewModel when scroll reset is done
        onLoadMore = { viewModel.loadMoreProducts() },
        onBack = onBack,
        onHome = onHome,
        onOpenFilters = onOpenFilters,
        onNavigateToProductDetails = onNavigateToProductDetails
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ProductListingContent(
    uiState: ProductListingUiState,
    gridColumns: Int,
    shouldResetScroll: Boolean, // ✅ Scroll reset trigger
    onGridColumnsChange: () -> Unit,
    onScrollResetComplete: () -> Unit, // ✅ Callback when scroll reset is done
    onLoadMore: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onOpenFilters: () -> Unit,
    onNavigateToProductDetails: (sku: String, shortDescription: String?, brandLabel: String?) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        // Debounced callbacks to prevent rapid clicks
        val debouncedBack = rememberDebouncedClick(onClick = onBack)
        val debouncedHome = rememberDebouncedClick(onClick = onHome)
        var showInitialLoader by remember { mutableStateOf(false) }

        LaunchedEffect(uiState.isLoading, uiState.products.isEmpty()) {
            if (uiState.isLoading && uiState.products.isEmpty()) {
                showInitialLoader = true
                delay(1500)
                if (!uiState.isLoading || uiState.products.isNotEmpty()) {
                    showInitialLoader = false
                }
            } else {
                showInitialLoader = false
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Top bar (tamno sivi)
                FashionTopBar(
                    onHomeClick = debouncedHome
                )

                // Search/Filter sekcija
                SearchFilterSection(
                    gridColumns = gridColumns,
                    onToggleColumns = onGridColumnsChange,
                    screenWidth = screenWidth
                )

            // Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    showInitialLoader -> {
                        // Initial loading
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            FashionLoader(
                                assetName = "ff_black.json",
                                speed = 3.5f,
                                screenWidth = screenWidth,
                                screenHeight = screenHeight
                            )
                        }
                    }

                    uiState.categoryNotFound -> {
                        // Category doesn't exist
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Responsive padding
                            val errorPadding = when {
                                screenWidth < 400.dp -> 16.dp
                                screenWidth < 600.dp -> 20.dp
                                else -> 24.dp
                            }
                            
                            // Responsive emoji size
                            val emojiSize = when {
                                screenWidth < 400.dp -> 48.sp
                                screenWidth < 600.dp -> 56.sp
                                else -> 64.sp
                            }
                            
                            // Responsive title font size
                            val titleFontSize = when {
                                screenWidth < 400.dp -> 18.sp
                                screenWidth < 600.dp -> 21.sp
                                else -> 24.sp
                            }
                            
                            // Responsive message font size
                            val messageFontSize = when {
                                screenWidth < 400.dp -> 14.sp
                                screenWidth < 600.dp -> 15.sp
                                else -> 16.sp
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(errorPadding)
                            ) {
                                Text(
                                    text = "❌",
                                    fontSize = emojiSize,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.category_not_found_title),
                                    color = Color.Black,
                                    fontSize = titleFontSize,
                                    fontFamily = Fonts.Poppins,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = R.string.category_not_found_message),
                                    color = Color(0xFF666666),
                                    fontSize = messageFontSize,
                                    fontFamily = Fonts.Poppins,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    uiState.isEmpty -> {
                        // Category exists but has no products
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Responsive padding
                            val emptyPadding = when {
                                screenWidth < 400.dp -> 16.dp
                                screenWidth < 600.dp -> 20.dp
                                else -> 24.dp
                            }
                            
                            // Responsive emoji size
                            val emojiSize = when {
                                screenWidth < 400.dp -> 48.sp
                                screenWidth < 600.dp -> 56.sp
                                else -> 64.sp
                            }
                            
                            // Responsive title font size
                            val titleFontSize = when {
                                screenWidth < 400.dp -> 18.sp
                                screenWidth < 600.dp -> 21.sp
                                else -> 24.sp
                            }
                            
                            // Responsive message font size
                            val messageFontSize = when {
                                screenWidth < 400.dp -> 14.sp
                                screenWidth < 600.dp -> 15.sp
                                else -> 16.sp
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(emptyPadding)
                            ) {
                                Text(
                                    text = "📦",
                                    fontSize = emojiSize,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.no_products_title),
                                    color = Color.Black,
                                    fontSize = titleFontSize,
                                    fontFamily = Fonts.Poppins,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = R.string.no_products_message),
                                    color = Color(0xFF666666),
                                    fontSize = messageFontSize,
                                    fontFamily = Fonts.Poppins,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    uiState.error != null -> {
                        // Error state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Responsive padding
                            val errorPadding = when {
                                screenWidth < 400.dp -> 16.dp
                                screenWidth < 600.dp -> 20.dp
                                else -> 24.dp
                            }
                            
                            // Responsive font sizes
                            val errorTitleFontSize = when {
                                screenWidth < 400.dp -> 16.sp
                                screenWidth < 600.dp -> 17.sp
                                else -> 18.sp
                            }
                            
                            val errorMessageFontSize = when {
                                screenWidth < 400.dp -> 12.sp
                                screenWidth < 600.dp -> 13.sp
                                else -> 14.sp
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(errorPadding)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.error_loading_products),
                                    color = Color.Black,
                                    fontSize = errorTitleFontSize,
                                    fontFamily = Fonts.Poppins,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = uiState.error ?: "Unknown error",
                                    color = Color.Gray,
                                    fontSize = errorMessageFontSize,
                                    fontFamily = Fonts.Poppins,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    else -> {
                        // Product grid
                        ProductGrid(
                            products = uiState.products,
                            columns = gridColumns,
                            isLoadingMore = uiState.isLoading,
                            shouldResetScroll = shouldResetScroll, // ✅ Use scroll reset trigger
                            onScrollResetComplete = onScrollResetComplete, // ✅ Notify when done
                            onLoadMore = onLoadMore,
                            onNavigateToProductDetails = onNavigateToProductDetails,
                            screenWidth = screenWidth,
                            screenHeight = screenHeight
                        )
                    }
                }
            }
        }

        // Filter button na dnu u centru - prikazi samo ako ima dostupnih filtera
        val hasAvailableFilters = remember(uiState.availableFilters) {
            uiState.availableFilters?.let { filters ->
                filters.genders.isNotEmpty() ||
                        filters.categories.isNotEmpty() ||
                        filters.brands.isNotEmpty() ||
                        filters.sizes.isNotEmpty() ||
                        filters.colors.isNotEmpty()
            } ?: false
        }

        if (hasAvailableFilters) {
            // Responsive filter button size
            val filterButtonSize = when {
                screenWidth < 400.dp -> 30.dp
                screenWidth < 600.dp -> 60.dp
                else -> 100.dp
            }
            
            // Responsive bottom padding
            val bottomPadding = when {
                screenHeight < 700.dp -> 12.dp
                screenHeight < 1200.dp -> 18.dp
                else -> 24.dp
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomPadding)
            ) {
                IconButton(
                    onClick = onOpenFilters,
                    modifier = Modifier
                        .size(filterButtonSize)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.filter_button),
                        contentDescription = "Filter",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        }
    }
}

@Composable
fun FashionTopBar(
    onHomeClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
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
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        // Responsive padding
        val horizontalPadding = when {
            screenWidth < 400.dp -> 12.dp
            screenWidth < 600.dp -> 18.dp
            else -> 24.dp
        }
        
        val verticalPadding = when {
            screenHeight < 700.dp -> 10.dp
            screenHeight < 1200.dp -> 14.dp
            else -> 16.dp
        }
        
        // Responsive icon button size
        val iconButtonSize = when {
            screenWidth < 400.dp -> 30.dp
            screenWidth < 600.dp -> 40.dp
            else -> 64.dp
        }
        
        // Responsive icon size
        val iconSize = when {
            screenWidth < 400.dp -> 20.dp
            screenWidth < 600.dp -> 32.dp
            else -> 48.dp
        }
        
        // Responsive logo height
        val logoHeight = when {
            screenHeight < 700.dp -> 20.dp
            screenHeight < 1200.dp -> 30.dp
            else -> 50.dp
        }
        
        // Responsive spacer size
        val spacerSize = when {
            screenWidth < 400.dp -> 30.dp
            screenWidth < 600.dp -> 40.dp
            else -> 50.dp
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home icon sa srcem
            IconButton(
                onClick = onHomeClick,
                modifier = Modifier.size(iconButtonSize)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_topbar),
                    contentDescription = "Home",
                    modifier = Modifier.size(iconSize),
                    contentScale = ContentScale.Fit
                )
            }

            // Fashion&Friends logo
            Image(
                modifier = Modifier
                    .clickable { onHomeClick() }
                    .height(logoHeight),
                painter = painterResource(id = R.drawable.fashion_logo),
                contentDescription = "Fashion & Friends",
                contentScale = ContentScale.Fit
            )

            // Prazan prostor za balans
            Spacer(modifier = Modifier.size(spacerSize))
        }
    }
}

@Composable
private fun SearchFilterSection(
    gridColumns: Int,
    onToggleColumns: () -> Unit,
    screenWidth: Dp,
) {
    // Memorize static values
    val redColor = remember { Color(0xFFB50938) }
    val grayColor = remember { Color(0xFFD9D9D9) }
    val boxShape = remember { RoundedCornerShape(2.dp) }
    
    // Responsive padding
    val horizontalPadding = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 14.dp
        else -> 16.dp
    }
    
    val verticalPadding = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 14.dp
        else -> 16.dp
    }
    
    // Responsive spacer width
    val spacerWidth = when {
        screenWidth < 400.dp -> 50.dp
        screenWidth < 600.dp -> 60.dp
        else -> 70.dp
    }
    
    // Responsive search icon size
    val searchIconSize = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 50.dp
        else -> 80.dp
    }
    
    // Responsive text font size
    val textFontSize = when {
        screenWidth < 400.dp -> 14.sp
        screenWidth < 600.dp -> 18.sp
        else -> 30.sp
    }
    
    // Responsive grid box size
    val boxSize = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 15.dp
        else -> 18.dp
    }
    
    // Responsive spacing
    val boxSpacing = when {
        screenWidth < 400.dp -> 3.dp
        screenWidth < 600.dp -> 3.5.dp
        else -> 4.dp
    }
    
    val iconTextSpacing = when {
        screenWidth < 400.dp -> 2.dp
        screenWidth < 600.dp -> 3.dp
        else -> 4.dp
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = verticalPadding, horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Spacer za balans (prazno mesto sa leve strane)
        Spacer(modifier = Modifier.width(spacerWidth))

        // Centar: Ikona pretrage + tekst
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.search_filter_icon),
                contentDescription = "Search",
                modifier = Modifier.size(searchIconSize),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(iconTextSpacing))

            Text(
                text = stringResource(id = R.string.search_results),
                fontSize = textFontSize,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Fonts.Poppins,
                color = Color.Black
            )
        }

        // Grid layout switcher (3 kockice)
        Row(
            horizontalArrangement = Arrangement.spacedBy(boxSpacing),
            modifier = Modifier
                .clickable(onClick = onToggleColumns)
                .padding(4.dp)
        ) {
            // Prva kockica - uvek crvena
            Box(
                modifier = Modifier
                    .size(boxSize)
                    .background(
                        color = redColor,
                        shape = boxShape
                    )
            )

            // Druga kockica - uvek crvena
            Box(
                modifier = Modifier
                    .size(boxSize)
                    .background(
                        color = redColor,
                        shape = boxShape
                    )
            )

            // Treća kockica - crvena samo ako je 3 kolone, inače siva
            Box(
                modifier = Modifier
                    .size(boxSize)
                    .background(
                        color = if (gridColumns == 3) redColor else grayColor,
                        shape = boxShape
                    )
            )
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<Product>,
    columns: Int,
    isLoadingMore: Boolean,
    shouldResetScroll: Boolean, // ✅ Scroll reset trigger (only when filters are applied)
    onScrollResetComplete: () -> Unit, // ✅ Notify when scroll reset is done
    onLoadMore: () -> Unit,
    onNavigateToProductDetails: (sku: String, shortDescription: String?, brandLabel: String?) -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
) {
    val gridState = rememberLazyGridState()

    // ✅ Reset scroll to top ONLY when filters are applied (not when Filter Screen opens/closes)
    LaunchedEffect(shouldResetScroll) {
        if (shouldResetScroll) {
            gridState.scrollToItem(0)
            onScrollResetComplete() // Notify ViewModel that scroll reset is complete
        }
    }

    // Optimized scroll detection with snapshotFlow
    LaunchedEffect(gridState, isLoadingMore) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: -1
            val totalItems = layoutInfo.totalItemsCount

            // Return pair for distinctUntilChanged
            lastVisibleIndex to totalItems
        }
            .distinctUntilChanged()
            .collect { (lastVisibleIndex, totalItems) ->
                // Load more when we're near the end (3 rows before)
                // Don't trigger if initial loading is in progress or already loading more
                val threshold = totalItems - (columns * 3)
                if (totalItems > 0 && !isLoadingMore && lastVisibleIndex >= threshold) {
                    onLoadMore()
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer {
                // Enable hardware layer for entire grid - major performance boost
                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                alpha = 0.99f // Force hardware layer
            },
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 0.dp,
            top = 0.dp,
            bottom = 100.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(
            items = products,
            key = { product -> product.sku ?: product.id },
            contentType = { "product" },
            span = null
        ) { product ->
            ProductCard(
                product = product,
                onProductClick = {
                    product.sku?.let { sku ->
                        onNavigateToProductDetails(
                            sku,
                            product.shortDescription,
                            product.brand?.label
                        )
                    }
                },
                screenWidth = screenWidth
            )
        }

        // Loading indicator
        if (isLoadingMore) {
            item(
                key = "loading_indicator",
                contentType = "loading"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FashionLoader(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onProductClick: () -> Unit,
    screenWidth: Dp,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    // Responsive card padding
    val cardPadding = when {
        screenWidth < 400.dp -> 10.dp
        screenWidth < 600.dp -> 16.dp
        else -> 30.dp
    }
    
    // Responsive image corner radius
    val imageCornerRadius = when {
        screenWidth < 400.dp -> 10.dp
        screenWidth < 600.dp -> 18.dp
        else -> 30.dp
    }
    
    // Memorize ALL static values to prevent recreation
    val imageShape = remember(imageCornerRadius) { RoundedCornerShape(imageCornerRadius) }
    val borderColor = remember { Color(0xFFE5E5E5) }
    val greyTextColor = remember { Color(0xFF707070) }
    val redColor = remember { Color(0xFFB50938) }

    // Memorize title. Avoid duplicating brand if product.name already contains it
    val productTitle = remember(product.brand?.label, product.name) {
        val brand = product.brand?.label?.trim().orEmpty()
        val name = product.name.orEmpty().trim()
        if (brand.isEmpty()) {
            name
        } else {
            val brandLower = brand.lowercase()
            val nameLower = name.lowercase()
            if (nameLower.startsWith(brandLower) || nameLower.startsWith("$brandLower -")) {
                name
            } else {
                "$brand - $name"
            }
        }
    }

    // Memorize price values - with null safety
    val hasSpecialPrice = remember(product.price?.specialPriceWithCurrency) {
        product.price?.specialPriceWithCurrency != null
    }
    val regularPrice = remember(product.price?.regularPriceWithCurrency) {
        product.price?.regularPriceWithCurrency.orEmpty()
    }
    val finalPrice =
        remember(product.price?.specialPriceWithCurrency, product.price?.regularPriceWithCurrency) {
            product.price?.specialPriceWithCurrency
                ?: product.price?.regularPriceWithCurrency.orEmpty()
        }

    // Memorize discount percentage for badge - only show if > 0
    val discountPercentage =
        remember(product.price?.discountPercentage, product.discountPercentage) {
            val discount = product.price?.discountPercentage ?: product.discountPercentage
            getValidDiscountPercentage(discount)
        }

    // Optimized Coil image request with size constraint
    val imageRequest = remember(product.imageUrl) {
        ImageRequest.Builder(context)
            .data(product.imageUrl)
            .crossfade(100) // Faster crossfade for smoother scroll
            .scale(Scale.FIT)
            .allowHardware(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .size(Size.ORIGINAL) // Load original size, let Compose scale
            .build()
    }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onProductClick() }
            .padding(cardPadding)
            .graphicsLayer {
                // Hardware layer for each card
                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
            }
    ) {
        // Product Image Card
        Box {
            Card(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = imageShape
                    )
                    .clip(imageShape)
            ) {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(imageShape)
                        .aspectRatio(ratio = 0.67f)
                        .background(Color.White),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = redColor,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                )
            }

            // Responsive discount badge
            val badgePadding = when {
                screenWidth < 400.dp -> 4.dp
                screenWidth < 600.dp -> 6.dp
                else -> 10.dp
            }
            
            val badgeFontSize = when {
                screenWidth < 400.dp -> 6.sp
                screenWidth < 600.dp -> 8.sp
                else -> 15.sp
            }
            
            val badgeInnerPaddingHorizontal = when {
                screenWidth < 400.dp -> 3.dp
                screenWidth < 600.dp -> 6.dp
                else -> 10.dp
            }
            
            val badgeInnerPaddingVertical = when {
                screenWidth < 400.dp -> 2.dp
                screenWidth < 600.dp -> 3.dp
                else -> 5.dp
            }
            
            // Discount badge - gornja leva ivica (prikazuje se samo ako je discount > 0)
            discountPercentage?.let { discount ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = badgePadding, top = badgePadding)
                        .background(
                            color = redColor,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = badgeInnerPaddingHorizontal, vertical = badgeInnerPaddingVertical)
                ) {
                    Text(
                        text = "-$discount%",
                        color = Color.White,
                        fontSize = badgeFontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Fonts.Poppins
                    )
                }
            }
        }

        // Responsive spacing
        val detailTopSpacing = when {
            screenWidth < 400.dp -> 8.dp
            screenWidth < 600.dp -> 10.dp
            else -> 16.dp
        }
        
        val detailStartPadding = when {
            screenWidth < 400.dp -> 4.dp
            screenWidth < 600.dp -> 6.dp
            else -> 8.dp
        }
        
        // Responsive text styles
        val productNameFontSize = when {
            screenWidth < 400.dp -> 8.sp
            screenWidth < 600.dp -> 10.sp
            else -> 20.sp
        }
        
        val productNameLineHeight = when {
            screenWidth < 400.dp -> 10.sp
            screenWidth < 600.dp -> 14.sp
            else -> 26.sp
        }
        
        val regularPriceFontSize = when {
            screenWidth < 400.dp -> 8.sp
            screenWidth < 600.dp -> 10.sp
            else -> 24.sp
        }
        
        val finalPriceFontSize = when {
            screenWidth < 400.dp -> 8.sp
            screenWidth < 600.dp -> 10.sp
            else -> 24.sp
        }
        
        val finalPriceWithDiscountFontSize = when {
            screenWidth < 400.dp -> 10.sp
            screenWidth < 600.dp -> 12.sp
            else -> 28.sp
        }
        
        Spacer(modifier = Modifier.height(detailTopSpacing))

        // Product Details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = detailStartPadding)
        ) {
            // Product Name
            Text(
                text = productTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = productNameFontSize,
                lineHeight = productNameLineHeight,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price logic - ako ima special cenu, prikaži obe
            if (hasSpecialPrice) {
                // Regular price - precrtana
                Text(
                    text = regularPrice,
                    fontFamily = Fonts.Poppins,
                    fontWeight = FontWeight.Light,
                    fontSize = regularPriceFontSize,
                    letterSpacing = 0.sp,
                    lineHeight = (regularPriceFontSize * 0.875f),
                    textDecoration = TextDecoration.LineThrough,
                    color = greyTextColor
                )
            }

            // Final price (special ili regular) - crvena, veća ako ima popust
            Text(
                text = finalPrice,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = if (hasSpecialPrice) finalPriceWithDiscountFontSize else finalPriceFontSize,
                letterSpacing = 0.sp,
                lineHeight = if (hasSpecialPrice) (finalPriceWithDiscountFontSize * 0.89f) else (finalPriceFontSize * 0.875f),
                color = if (hasSpecialPrice) redColor else greyTextColor
            )
        }
    }
}

// Helper function to create mock products for previews
private fun createMockProducts(): List<Product> {
    return listOf(
        Product(
            id = 1,
            sku = "MOCK1",
            name = "Elegantna haljina",
            shortDescription = "Prelepa haljina",
            description = "Detaljan opis haljine",
            imageUrl = null,
            hoverImageUrl = null,
            link = null,
            price = com.fashiontothem.ff.domain.model.ProductPrice(
                regularPrice = 5990.0,
                regularPriceWithCurrency = "5.990,00 RSD",
                specialPrice = 4990.0,
                specialPriceWithCurrency = "4.990,00 RSD",
                loyaltyPrice = null,
                loyaltyPriceWithCurrency = null,
                discountPercentage = 20,
                discountValue = 1000.0,
                bestMonthPrice = null,
                bestMonthPriceWithCurrency = null,
                customerGroupId = null
            ),
            brand = com.fashiontothem.ff.domain.model.ProductBrand(
                id = 1,
                label = "Zara",
                optionId = "1",
                attributeCode = "brand"
            ),
            availability = 1,
            salableQty = 10,
            discountPercentage = 20,
            configurableOptions = null,
            categoryNames = null,
            attributes = null,
            childProducts = null,
            galleryImages = null,
            productCombinations = null,
            totalReviews = null,
            views = null,
            productScore = null,
            productTypeId = null,
            metaTitle = null,
            metaDescription = null,
            type = null,
            categoryIds = null
        ),
        Product(
            id = 2,
            sku = "MOCK2",
            name = "Casual patike",
            shortDescription = null,
            description = null,
            imageUrl = null,
            hoverImageUrl = null,
            link = null,
            price = com.fashiontothem.ff.domain.model.ProductPrice(
                regularPrice = 12990.0,
                regularPriceWithCurrency = "12.990,00 RSD",
                specialPrice = null,
                specialPriceWithCurrency = null,
                loyaltyPrice = null,
                loyaltyPriceWithCurrency = null,
                discountPercentage = null,
                discountValue = null,
                bestMonthPrice = null,
                bestMonthPriceWithCurrency = null,
                customerGroupId = null
            ),
            brand = com.fashiontothem.ff.domain.model.ProductBrand(
                id = 2,
                label = "Nike",
                optionId = "2",
                attributeCode = "brand"
            ),
            availability = null,
            salableQty = null,
            discountPercentage = null,
            configurableOptions = null,
            categoryNames = null,
            attributes = null,
            childProducts = null,
            galleryImages = null,
            productCombinations = null,
            totalReviews = null,
            views = null,
            productScore = null,
            productTypeId = null,
            metaTitle = null,
            metaDescription = null,
            type = null,
            categoryIds = null
        )
    )

}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun ProductListingScreenPreviewSmall() {
    ProductListingContent(
        uiState = ProductListingUiState(
            products = createMockProducts(),
            isLoading = false,
            error = null
        ),
        gridColumns = 2,
        shouldResetScroll = false,
        onGridColumnsChange = {},
        onScrollResetComplete = {},
        onLoadMore = {},
        onBack = {},
        onHome = {},
        onOpenFilters = {},
        onNavigateToProductDetails = { _, _, _ -> }
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun ProductListingScreenPreviewMedium() {
    ProductListingContent(
        uiState = ProductListingUiState(
            products = createMockProducts(),
            isLoading = false,
            error = null
        ),
        gridColumns = 2,
        shouldResetScroll = false,
        onGridColumnsChange = {},
        onScrollResetComplete = {},
        onLoadMore = {},
        onBack = {},
        onHome = {},
        onOpenFilters = {},
        onNavigateToProductDetails = { _, _, _ -> }
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun ProductListingScreenPreviewLarge() {
    ProductListingContent(
        uiState = ProductListingUiState(
            products = createMockProducts(),
            isLoading = false,
            error = null
        ),
        gridColumns = 2,
        shouldResetScroll = false,
        onGridColumnsChange = {},
        onScrollResetComplete = {},
        onLoadMore = {},
        onBack = {},
        onHome = {},
        onOpenFilters = {},
        onNavigateToProductDetails = { _, _, _ -> }
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun ProductListingScreenPreviewPhilips() {
    ProductListingContent(
        uiState = ProductListingUiState(
            products = createMockProducts(),
            isLoading = false,
            error = null
        ),
        gridColumns = 2,
        shouldResetScroll = false,
        onGridColumnsChange = {},
        onScrollResetComplete = {},
        onLoadMore = {},
        onBack = {},
        onHome = {},
        onOpenFilters = {},
        onNavigateToProductDetails = { _, _, _ -> }
    )
}

