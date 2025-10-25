package com.fashiontothem.ff.presentation.products

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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

// Cached text styles for better performance
private val ProductNameStyle = TextStyle(
    fontFamily = Fonts.Poppins,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    color = Color.Black
)

private val RegularPriceStyle = TextStyle(
    fontFamily = Fonts.Poppins,
    fontWeight = FontWeight.Light,
    fontSize = 18.sp,
    letterSpacing = 0.sp,
    lineHeight = 21.sp,
    textDecoration = TextDecoration.LineThrough
)

private val FinalPriceStyle = TextStyle(
    fontFamily = Fonts.Poppins,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    letterSpacing = 0.sp,
    lineHeight = 21.sp
)

@Composable
fun ProductListingScreen(
    categoryId: String? = null,
    categoryLevel: String? = null,
    filterType: String = "none",
    onBack: () -> Unit,
    onHome: () -> Unit = onBack,
    viewModel: ProductListingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var gridColumns by remember { mutableIntStateOf(2) }

    LaunchedEffect(categoryId, categoryLevel, filterType) {
        // Check if we have a visual search image in DataStore
        viewModel.checkAndLoadVisualSearchOrCategory(categoryId, categoryLevel, filterType)
    }

    ProductListingContent(
        uiState = uiState,
        gridColumns = gridColumns,
        onGridColumnsChange = { gridColumns = if (gridColumns == 2) 3 else 2 },
        onLoadMore = { viewModel.loadMoreProducts() },
        onBack = onBack,
        onHome = onHome
    )
}

@Composable
private fun ProductListingContent(
    uiState: ProductListingUiState,
    gridColumns: Int,
    onGridColumnsChange: () -> Unit,
    onLoadMore: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    // Debounced callbacks to prevent rapid clicks
    val debouncedBack = rememberDebouncedClick(onClick = onBack)
    val debouncedHome = rememberDebouncedClick(onClick = onHome)

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
                onToggleColumns = onGridColumnsChange
            )

            // Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoading && uiState.products.isEmpty() -> {
                        // Initial loading
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            FashionLoader()
                        }
                    }

                    uiState.error != null -> {
                        // Error state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.error_loading_products),
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontFamily = Fonts.Poppins,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = uiState.error ?: "Unknown error",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
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
                            onLoadMore = onLoadMore
                        )
                    }
                }
            }
        }

        // Filter button na dnu u centru
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            IconButton(
                onClick = { /* TODO: Open filter */ },
                modifier = Modifier
                    .size(100.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.filter_button),
                    contentDescription = "Filter"
                )
            }
        }
    }
}

@Composable
private fun FashionTopBar(
    onHomeClick: () -> Unit,
) {
    Row(
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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home icon sa srcem
        IconButton(
            onClick = onHomeClick,
            modifier = Modifier.size(64.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_topbar),
                contentDescription = "Home",
                modifier = Modifier.size(48.dp)
            )
        }

        // Fashion&Friends logo
        Image(
            modifier = Modifier.clickable { onHomeClick() },
            painter = painterResource(id = R.drawable.fashion_logo),
            contentDescription = "Fashion & Friends",
        )

        // Prazan prostor za balans
        Spacer(modifier = Modifier.size(50.dp))
    }
}

@Composable
private fun SearchFilterSection(
    gridColumns: Int,
    onToggleColumns: () -> Unit,
) {
    // Memorize static values
    val redColor = remember { Color(0xFFB50938) }
    val grayColor = remember { Color(0xFFD9D9D9) }
    val boxShape = remember { RoundedCornerShape(2.dp) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Spacer za balans (prazno mesto sa leve strane)
        Spacer(modifier = Modifier.width(70.dp))

        // Centar: Ikona pretrage + tekst
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.search_filter_icon),
                contentDescription = "Search",
            )

            Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(id = R.string.search_results),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Fonts.Poppins,
                    color = Color.Black
                )
        }

        // Grid layout switcher (3 kockice)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clickable(onClick = onToggleColumns)
                .padding(4.dp)
        ) {
            // Prva kockica - uvek crvena
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = redColor,
                        shape = boxShape
                    )
            )

            // Druga kockica - uvek crvena
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = redColor,
                        shape = boxShape
                    )
            )

            // Treća kockica - crvena samo ako je 3 kolone, inače siva
            Box(
                modifier = Modifier
                    .size(18.dp)
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
    onLoadMore: () -> Unit,
) {
    val gridState = rememberLazyGridState()
    
    // Optimized scroll detection with snapshotFlow
    LaunchedEffect(gridState) {
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
            ProductCard(product = product)
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
                    FashionLoader()
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    
    // Memorize ALL static values to prevent recreation
    val imageShape = remember { RoundedCornerShape(30.dp) }
    val borderColor = remember { Color(0xFFE5E5E5) }
    val greyTextColor = remember { Color(0xFFB0B0B0) }
    val redColor = remember { Color(0xFFB50938) }
    
    // Memorize computed strings - with null safety
    val productTitle = remember(product.brand?.label, product.name) {
        "${product.brand?.label.orEmpty()} - ${product.name.orEmpty()}"
    }
    
    // Memorize price values - with null safety
    val hasSpecialPrice = remember(product.price?.specialPriceWithCurrency) {
        product.price?.specialPriceWithCurrency != null
    }
    val regularPrice = remember(product.price?.regularPriceWithCurrency) {
        product.price?.regularPriceWithCurrency.orEmpty()
    }
    val finalPrice = remember(product.price?.specialPriceWithCurrency, product.price?.regularPriceWithCurrency) {
        product.price?.specialPriceWithCurrency ?: product.price?.regularPriceWithCurrency.orEmpty()
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
            ) { /* TODO: Navigate to product details */ }
            .padding(30.dp)
            .graphicsLayer {
                // Hardware layer for each card
                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
            }
    ) {
        // Product Image Card
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Product Details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        ) {
            // Product Name
            Text(
                text = productTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = ProductNameStyle
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Price logic - ako ima special cenu, prikaži obe
            if (hasSpecialPrice) {
                // Regular price - precrtana
                Text(
                    text = regularPrice,
                    style = RegularPriceStyle,
                    color = greyTextColor
                )
            }
            
            // Final price (special ili regular) - crvena
            Text(
                text = finalPrice,
                style = FinalPriceStyle,
                color = redColor
            )
        }
    }
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun ProductListingScreenPreviewPhilips() {
    // Mock data za preview (bez ViewModel-a)
    val mockProducts = listOf(
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
    
    ProductListingContent(
        uiState = ProductListingUiState(
            products = mockProducts,
            isLoading = false,
            error = null
        ),
        gridColumns = 2,
        onGridColumnsChange = {},
        onLoadMore = {},
        onBack = {},
        onHome = {}
    )
}

