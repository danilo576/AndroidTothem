package com.fashiontothem.ff.presentation.locations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.fashiontothem.ff.domain.model.StoreLocation
import com.fashiontothem.ff.presentation.common.FashionLoader
import humer.UvcCamera.R

@Composable
fun StoreLocationsScreen(
    viewModel: StoreLocationsViewModel = hiltViewModel(),
    onLocationSelected: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.locationSelected) {
        if (uiState.locationSelected) {
            onLocationSelected()
        }
    }

    // Background - splash_background
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dialog
        Dialog(
            onDismissRequest = { /* Cannot dismiss - must select */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(200)) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(200)
                ),
                exit = fadeOut(tween(150)) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(150)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 40.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when {
                            uiState.isLoading -> LoadingContent()
                            uiState.error != null -> ErrorContent(uiState.error!!) { viewModel.loadLocations() }
                            else -> LocationsDialogContent(
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
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FashionLoader()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Učitavanje lokacija...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorContent(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "❌", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Greška",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB50938)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB50938)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Pokušaj ponovo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LocationsDialogContent(
    cities: List<String>,
    selectedCity: String,
    locationsByCity: Map<String, List<StoreLocation>>,
    isSaving: Boolean,
    onCityClick: (String) -> Unit,
    onStoreClick: (StoreLocation) -> Unit,
) {
    val currentCityIndex = remember(selectedCity) { cities.indexOf(selectedCity).coerceAtLeast(0) }
    val hasPrevious = currentCityIndex > 0
    val hasNext = currentCityIndex < cities.size - 1

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header sa chevronima (kao na dizajnu)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left chevron
            IconButton(
                onClick = {
                    if (hasPrevious) onCityClick(cities[currentCityIndex - 1])
                },
                enabled = hasPrevious,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous city",
                    tint = if (hasPrevious) Color(0xFFB0B0B0) else Color(0xFFE0E0E0),
                    modifier = Modifier.size(32.dp)
                )
            }

            // City name
            Text(
                text = selectedCity,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // Right chevron
            IconButton(
                onClick = {
                    if (hasNext) onCityClick(cities[currentCityIndex + 1])
                },
                enabled = hasNext,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next city",
                    tint = if (hasNext) Color(0xFFB0B0B0) else Color(0xFFE0E0E0),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE5E5E5))
        )

        // Store locations list
        val selectedStores = locationsByCity[selectedCity] ?: emptyList()

        if (selectedStores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nema aktivnih prodavnica",
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedStores) { store ->
                    LocationCard(
                        store = store,
                        enabled = !isSaving,
                        onClick = { onStoreClick(store) }
                    )
                }
            }
        }
    }
}

/**
 * Location card po dizajnu - bez slika, samo tekst
 */
@Composable
private fun LocationCard(
    store: StoreLocation,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Store Name (bold, black)
            Text(
                text = store.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Address (regular, gray)
            Text(
                text = store.streetAddress,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF808080),
                lineHeight = 20.sp
            )
        }
    }
}

