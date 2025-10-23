package com.fashiontothem.ff.presentation.locations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fashiontothem.ff.domain.model.StoreLocation

@Composable
fun StoreLocationsScreen(
    viewModel: StoreLocationsViewModel = hiltViewModel(),
    onLocationSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.locationSelected) {
        if (uiState.locationSelected) {
            onLocationSelected()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0f0c29),
                        Color(0xFF302b63),
                        Color(0xFF24243e)
                    )
                )
            )
    ) {
        when {
            uiState.isLoading -> LoadingContent()
            uiState.error != null -> ErrorContent(uiState.error!!) { viewModel.loadLocations() }
            else -> LocationsContent(
                cities = uiState.cities,
                selectedCity = uiState.selectedCity,
                locationsByCity = uiState.locationsByCity,
                isSaving = uiState.isSaving,
                onCityClick = { viewModel.selectCity(it) },
                onStoreClick = { viewModel.selectStore(it) }
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Color(0xFF03DAC5),
                strokeWidth = 4.dp,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Učitavanje lokacija...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun ErrorContent(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "❌", fontSize = 72.sp)
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) {
            Text("Pokušaj ponovo")
        }
    }
}

@Composable
private fun LocationsContent(
    cities: List<String>,
    selectedCity: String,
    locationsByCity: Map<String, List<StoreLocation>>,
    isSaving: Boolean,
    onCityClick: (String) -> Unit,
    onStoreClick: (StoreLocation) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF6200EE), Color(0xFFBB86FC))
                    )
                )
                .padding(horizontal = 28.dp, vertical = 36.dp)
        ) {
            Column {
                Text(
                    text = "Naše prodavnice",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Izaberite vašu lokaciju",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(bottom = 28.dp)
        ) {
            items(cities) { city ->
                ModernCityTab(
                    city = city,
                    isSelected = city == selectedCity,
                    storeCount = locationsByCity[city]?.size ?: 0,
                    onClick = { onCityClick(city) }
                )
            }
        }
        
        val selectedStores = locationsByCity[selectedCity] ?: emptyList()
        
        if (selectedStores.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nema aktivnih prodavnica",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(selectedStores) { store ->
                    PremiumStoreCard(
                        store = store,
                        enabled = !isSaving,
                        onClick = { onStoreClick(store) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun ModernCityTab(
    city: String,
    isSelected: Boolean,
    storeCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .shadow(
                elevation = if (isSelected) 14.dp else 4.dp,
                shape = RoundedCornerShape(32.dp)
            )
            .background(
                if (isSelected) {
                    Brush.linearGradient(colors = listOf(Color(0xFF6200EE), Color(0xFFBB86FC)))
                } else {
                    Brush.linearGradient(colors = listOf(Color(0xFF2a2a3e), Color(0xFF2a2a3e)))
                },
                RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 32.dp, vertical = 18.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = city,
                fontSize = if (isSelected) 22.sp else 18.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = 0.8.sp
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF03DAC5), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "$storeCount",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0f0c29)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumStoreCard(
    store: StoreLocation,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1e1e2e)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp,
            pressedElevation = 18.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!store.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = store.imageUrl,
                    contentDescription = store.name,
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF434343), Color(0xFF000000))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🏬", fontSize = 72.sp)
                        Text(
                            text = "Fashion & Friends",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    text = store.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 32.sp
                )
                
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 16.dp)
                        .width(70.dp)
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF03DAC5), Color(0xFF6200EE))
                            ),
                            RoundedCornerShape(2.dp)
                        )
                )
                
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF6200EE).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📍", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = store.streetAddress,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE8E8E8),
                            lineHeight = 26.sp
                        )
                        if (!store.zipcode.isNullOrEmpty()) {
                            Text(
                                text = "${store.zipcode}, ${store.city}",
                                fontSize = 16.sp,
                                color = Color(0xFFB0B0B0),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                
                if (!store.tradingHours.isNullOrEmpty()) {
                    Divider(
                        color = Color(0xFF2a2a3e),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF03DAC5).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🕐", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Radno vreme: 10:00 - 22:00",
                            fontSize = 15.sp,
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
                
                Button(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color(0xFF1a1a2e)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (enabled) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF03DAC5), Color(0xFF00B4A6))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF2a2a3e), Color(0xFF2a2a3e))
                                    )
                                },
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (enabled) "IZABERI OVU PRODAVNICU" else "Čuvanje...",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (enabled) Color(0xFF0f0c29) else Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
