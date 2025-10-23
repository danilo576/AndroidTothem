package com.fashiontothem.ff.presentation.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fashiontothem.ff.domain.model.CountryStore

/**
 * F&F Tothem - Store Selection Screen
 * 
 * Initial screen for selecting country and store.
 * Shows when no store has been selected previously.
 */
@Composable
fun StoreSelectionScreen(
    viewModel: StoreSelectionViewModel = hiltViewModel(),
    onStoreSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate to main screen when store is selected
    LaunchedEffect(uiState.storeSelected) {
        if (uiState.storeSelected) {
            onStoreSelected()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "F&F Tothem",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Fashion & Friends Kiosk",
                fontSize = 18.sp,
                color = Color(0xFF03DAC5),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Izaberi svoju zemlju",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Content
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadStores() },
                        onDismiss = { viewModel.dismissError() }
                    )
                }
                
                else -> {
                    StoreList(
                        stores = uiState.stores,
                        isSaving = uiState.isSaving,
                        onStoreClick = { countryCode, storeCode ->
                            viewModel.selectStore(countryCode, storeCode)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF03DAC5),
                strokeWidth = 4.dp,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Učitavanje prodavnica...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            fontSize = 72.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Greška",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCF6679)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            ),
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(56.dp)
        ) {
            Text(
                text = "Pokušaj ponovo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StoreList(
    stores: List<CountryStore>,
    isSaving: Boolean,
    onStoreClick: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stores) { countryStore ->
            CountryStoreCard(
                countryStore = countryStore,
                enabled = !isSaving,
                onClick = { storeCode ->
                    onStoreClick(countryStore.countryCode, storeCode)
                }
            )
        }
        
        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CountryStoreCard(
    countryStore: CountryStore,
    enabled: Boolean,
    onClick: (String) -> Unit
) {
    // Use flagcdn.com for high-quality, authentic flag images
    val flagUrl = "https://flagcdn.com/w160/${countryStore.countryCode.lowercase()}.png"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                // Click on first store (usually only one per country)
                countryStore.stores.firstOrNull()?.let { store ->
                    onClick(store.code)
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a3e)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Authentic flag image (circular)
            AsyncImage(
                model = flagUrl,
                contentDescription = countryStore.countryName,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 24.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
            
            // Country name only
            Text(
                text = countryStore.countryName,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            // Arrow or loading indicator
            if (!enabled) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color(0xFF03DAC5)
                )
            } else {
                Text(
                    text = "→",
                    fontSize = 36.sp,
                    color = Color(0xFF03DAC5)
                )
            }
        }
    }
}

