package com.fashiontothem.ff.presentation.products

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fashiontothem.ff.domain.model.Product
import com.fashiontothem.ff.presentation.common.FashionLoader
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R

@Composable
fun ProductListingScreen(
    categoryId: String,
    categoryLevel: String,
    onBack: () -> Unit,
    viewModel: ProductListingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var gridColumns by remember { mutableIntStateOf(2) } // Default 2 kolone

    LaunchedEffect(categoryId, categoryLevel) {
        viewModel.loadProducts(categoryId, categoryLevel)
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
                onHomeClick = onBack
            )

            // Search/Filter sekcija
            SearchFilterSection(
                gridColumns = gridColumns,
                onToggleColumns = {
                    gridColumns = if (gridColumns == 2) 3 else 2
                }
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
                                    text = "Greška pri učitavanju proizvoda",
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
                            onLoadMore = { viewModel.loadMoreProducts() }
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
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_topbar),
                contentDescription = "Home",
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
                text = "Rezultati pretrage",
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
                        color = Color(0xFFB50938),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            // Druga kockica - uvek crvena
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = Color(0xFFB50938),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            // Treća kockica - crvena samo ako je 3 kolone, inače siva
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = if (gridColumns == 3) Color(0xFFB50938) else Color(0xFFD9D9D9),
                        shape = RoundedCornerShape(2.dp)
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

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductCard(product = product)
        }
        
        // Loading indicator as grid item (if loading more)
        if (isLoadingMore) {
            item {
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
    
    // Trigger load more when scrolling near bottom
    LaunchedEffect(gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index, products.size) {
        if (products.isEmpty() || isLoadingMore) {
            return@LaunchedEffect // Don't trigger if empty or already loading
        }
        
        val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (lastVisibleIndex != null && lastVisibleIndex >= products.size - (columns * 2)) {
            onLoadMore()
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to product details */ },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f) // Portrait aspect ratio za cipele
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Product Name (brand + naziv)
            Text(
                text = "${product.brand.label} - ${product.name}",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = Fonts.Poppins,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price
            Text(
                text = product.price.specialPriceWithCurrency
                    ?: product.price.regularPriceWithCurrency,
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Fonts.Poppins,
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

