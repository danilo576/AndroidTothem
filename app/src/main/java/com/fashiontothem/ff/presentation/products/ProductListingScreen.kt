package com.fashiontothem.ff.presentation.products

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
    viewModel: ProductListingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(categoryId, categoryLevel) {
        viewModel.loadProducts(categoryId, categoryLevel)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            ProductTopBar(
                onBack = onBack,
                onFilter = { /* TODO: Implement filter */ }
            )
            
            // Content
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
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Greška pri učitavanju proizvoda",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = Fonts.Poppins,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error ?: "Unknown error",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = Fonts.Poppins,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                else -> {
                    // Product list
                    ProductList(
                        products = uiState.products,
                        isLoadingMore = uiState.isLoading,
                        onLoadMore = { viewModel.loadMoreProducts() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductTopBar(
    onBack: () -> Unit,
    onFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(onClick = onBack) {
            Image(
                painter = painterResource(id = R.drawable.close_button),
                contentDescription = "Back",
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Title
        Text(
            text = "Proizvodi",
            color = Color.White,
            fontSize = 20.sp,
            fontFamily = Fonts.Poppins,
            fontWeight = FontWeight.SemiBold
        )
        
        // Filter button
        IconButton(onClick = onFilter) {
            Image(
                painter = painterResource(id = R.drawable.filter_button),
                contentDescription = "Filter",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ProductList(
    products: List<Product>,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductCard(product = product)
        }
        
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
        
        // Load more when reaching the end
        item {
            LaunchedEffect(listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
                if (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == products.size - 1) {
                    onLoadMore()
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to product details */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Discount badge
                product.discountPercentage?.let { discount ->
                    if (discount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    Color.Red,
                                    RoundedCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "-$discount%",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Fonts.Poppins
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Product Name
            Text(
                text = product.name,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Fonts.Poppins,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Brand
            Text(
                text = product.brand.label,
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = Fonts.Poppins,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Price section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Regular price
                Text(
                    text = product.price.regularPriceWithCurrency,
                    color = if (product.price.specialPrice != null) Color.Gray else Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Fonts.Poppins,
                    textDecoration = if (product.price.specialPrice != null) TextDecoration.LineThrough else null
                )
                
                // Special price
                product.price.specialPriceWithCurrency?.let { specialPrice ->
                    Text(
                        text = specialPrice,
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Fonts.Poppins
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Availability
            if (product.salableQty > 0) {
                Text(
                    text = "Dostupno: ${product.salableQty} kom",
                    color = Color.Green,
                    fontSize = 12.sp,
                    fontFamily = Fonts.Poppins,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Nije dostupno",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontFamily = Fonts.Poppins,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ProductPrice(
    regularPrice: Int,
    specialPrice: Int?,
    discountPercentage: Int?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (specialPrice != null && specialPrice < regularPrice) {
            // Show special price
            Text(
                text = formatPrice(specialPrice),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Show crossed out regular price
            Text(
                text = formatPrice(regularPrice),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Medium
            )
            
            if (discountPercentage != null) {
                Spacer(modifier = Modifier.width(8.dp))
                
                // Discount badge
                Text(
                    text = "-$discountPercentage%",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontFamily = Fonts.Poppins,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            Color.Red.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        } else {
            // Show regular price only
            Text(
                text = formatPrice(regularPrice),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatPrice(price: Int): String {
    return "${price / 100}.${(price % 100).toString().padStart(2, '0')} RSD"
}
