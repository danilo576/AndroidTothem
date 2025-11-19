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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.fashiontothem.ff.domain.model.StoreLocation
import com.fashiontothem.ff.presentation.common.FashionLoader
import com.fashiontothem.ff.util.clickableDebounced
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R

@Composable
fun StoreLocationsScreen(
    viewModel: StoreLocationsViewModel = hiltViewModel(),
    onLocationSelected: () -> Unit,
    isUpdateMode: Boolean = false,
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
                                preselectedStoreId = uiState.preselectedStoreId,
                                isSaving = uiState.isSaving,
                                isUpdateMode = isUpdateMode,
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
            text = stringResource(id = R.string.loading_locations),
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
            text = stringResource(id = R.string.error_title),
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
            Text(
                text = stringResource(id = R.string.retry_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LocationsDialogContent(
    cities: List<String>,
    selectedCity: String,
    locationsByCity: Map<String, List<StoreLocation>>,
    preselectedStoreId: String? = null,
    isSaving: Boolean,
    isUpdateMode: Boolean = false,
    onCityClick: (String) -> Unit,
    onStoreClick: (StoreLocation) -> Unit,
) {
    val currentCityIndex = remember(selectedCity) { cities.indexOf(selectedCity).coerceAtLeast(0) }
    val hasPrevious = currentCityIndex > 0
    val hasNext = currentCityIndex < cities.size - 1
    
    // Debounced navigation for chevrons
    val debouncedPrevious = rememberDebouncedClick {
        if (hasPrevious) onCityClick(cities[currentCityIndex - 1])
    }
    
    val debouncedNext = rememberDebouncedClick {
        if (hasNext) onCityClick(cities[currentCityIndex + 1])
    }

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
                onClick = debouncedPrevious,
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

            // City name with update indicator
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedCity,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                if (isUpdateMode) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.updating_location),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF808080),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Right chevron
            IconButton(
                onClick = debouncedNext,
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

        // Store locations list - sort so preselected store is first
        val selectedStores = locationsByCity[selectedCity] ?: emptyList()
        val sortedStores = if (preselectedStoreId != null && selectedStores.isNotEmpty()) {
            val preselectedStore = selectedStores.find { it.id == preselectedStoreId }
            if (preselectedStore != null) {
                val otherStores = selectedStores.filter { it.id != preselectedStoreId }
                listOf(preselectedStore) + otherStores
            } else {
                selectedStores
            }
        } else {
            selectedStores
        }

        if (sortedStores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_active_stores),
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedStores) { store ->
                    LocationCard(
                        store = store,
                        enabled = !isSaving,
                        isSelected = preselectedStoreId == store.id,
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
    isSelected: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickableDebounced {
                if (enabled) onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Store Name (bold, white if selected, black otherwise)
            Text(
                text = store.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Black,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Address (regular, light gray if selected, gray otherwise)
            Text(
                text = store.streetAddress,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF808080),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun StoreLocationsScreenPreviewPhilips() {
    // Mock data za preview (bez ViewModel-a)
    val mockLocations = listOf(
        StoreLocation(
            id = "1",
            name = "Fashion & Friends Delta City",
            imageUrl = null,
            email = "info@fashionandfriends.rs",
            phoneNumber1 = "+381 11 2222 333",
            phoneNumber2 = null,
            latitude = 44.8154,
            longitude = 20.4280,
            streetAddress = "Jurija Gagarina 16, Novi Beograd",
            country = "Srbija",
            zipcode = "11070",
            city = "Beograd",
            tradingHours = "10:00 - 22:00",
            isActive = true
        ),
        StoreLocation(
            id = "2",
            name = "Fashion & Friends Usce",
            imageUrl = null,
            email = "usce@fashionandfriends.rs",
            phoneNumber1 = "+381 11 3333 444",
            phoneNumber2 = null,
            latitude = 44.8203,
            longitude = 20.4487,
            streetAddress = "Bulevar Mihajla Pupina 4",
            country = "Srbija",
            zipcode = "11000",
            city = "Beograd",
            tradingHours = "10:00 - 22:00",
            isActive = true
        )
    )
    
    LocationsDialogContent(
        cities = listOf("Beograd", "Novi Sad"),
        selectedCity = "Beograd",
        locationsByCity = mapOf("Beograd" to mockLocations),
        isSaving = false,
        onCityClick = {},
        onStoreClick = {}
    )
}

