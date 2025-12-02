package com.fashiontothem.ff.presentation.locations

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.Dp
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

@SuppressLint("UnusedBoxWithConstraintsScope")
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                // Responsive dimensions based on screen size
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                
                // Responsive dialog width - larger for tablets
                val dialogWidth = when {
                    screenWidth < 400.dp -> (screenWidth * 0.95f).coerceAtMost(380.dp)
                    screenWidth < 600.dp -> (screenWidth * 0.90f).coerceAtMost(550.dp)
                    screenWidth < 800.dp -> (screenWidth * 0.85f).coerceAtMost(700.dp)
                    else -> (screenWidth * 0.80f).coerceAtMost(900.dp)
                }
                
                // Responsive dialog height - limit max height for tablets
                val maxDialogHeight = when {
                    screenHeight < 700.dp -> (screenHeight * 0.90f).coerceAtMost(650.dp)
                    screenHeight < 1200.dp -> (screenHeight * 0.85f).coerceAtMost(1000.dp)
                    else -> (screenHeight * 0.80f).coerceAtMost(1200.dp)
                }
                
                // Responsive corner radius
                val cornerRadius = when {
                    screenWidth < 400.dp -> 20.dp
                    screenWidth < 600.dp -> 24.dp
                    screenWidth < 800.dp -> 28.dp
                    else -> 32.dp
                }
                
                // Responsive elevation
                val elevation = when {
                    screenWidth < 400.dp -> 12.dp
                    screenWidth < 600.dp -> 14.dp
                    else -> 16.dp
                }
                
                // Outer padding to ensure dialog fits within screen
                val outerPadding = when {
                    screenWidth < 400.dp -> 12.dp
                    screenWidth < 600.dp -> 16.dp
                    else -> 20.dp
                }
                
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
                            .width(dialogWidth)
                            .heightIn(max = maxDialogHeight)
                            .padding(outerPadding),
                        shape = RoundedCornerShape(cornerRadius),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            when {
                                uiState.isLoading -> LoadingContent(
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight
                                )
                                uiState.error != null -> ErrorContent(
                                    error = uiState.error!!,
                                    onRetry = { viewModel.loadLocations() },
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight
                                )
                                else -> LocationsDialogContent(
                                    cities = uiState.cities,
                                    selectedCity = uiState.selectedCity,
                                    locationsByCity = uiState.locationsByCity,
                                    preselectedStoreId = uiState.preselectedStoreId,
                                    isSaving = uiState.isSaving,
                                    isUpdateMode = isUpdateMode,
                                    onCityClick = { viewModel.selectCity(it) },
                                    onStoreClick = { viewModel.selectStore(it) },
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(
    screenWidth: Dp,
    screenHeight: Dp
) {
    val padding = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 32.dp
        screenWidth < 800.dp -> 36.dp
        else -> 40.dp
    }
    
    val fontSize = when {
        screenWidth < 400.dp -> 16.sp
        screenWidth < 600.dp -> 18.sp
        screenWidth < 800.dp -> 20.sp
        else -> 22.sp
    }
    
    val spacing = when {
        screenHeight < 700.dp -> 20.dp
        screenHeight < 1200.dp -> 24.dp
        else -> 28.dp
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FashionLoader(screenWidth = screenWidth, screenHeight = screenHeight)
        Spacer(modifier = Modifier.height(spacing))
        Text(
            text = stringResource(id = R.string.loading_locations),
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    screenWidth: Dp,
    screenHeight: Dp
) {
    val padding = when {
        screenWidth < 400.dp -> 20.dp
        screenWidth < 600.dp -> 24.dp
        screenWidth < 800.dp -> 28.dp
        else -> 32.dp
    }
    
    val emojiSize = when {
        screenWidth < 400.dp -> 48.sp
        screenWidth < 600.dp -> 56.sp
        screenWidth < 800.dp -> 64.sp
        else -> 72.sp
    }
    
    val titleFontSize = when {
        screenWidth < 400.dp -> 20.sp
        screenWidth < 600.dp -> 22.sp
        screenWidth < 800.dp -> 24.sp
        else -> 26.sp
    }
    
    val errorFontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 14.sp
        screenWidth < 800.dp -> 16.sp
        else -> 18.sp
    }
    
    val buttonHeight = when {
        screenWidth < 400.dp -> 48.dp
        screenWidth < 600.dp -> 50.dp
        screenWidth < 800.dp -> 52.dp
        else -> 54.dp
    }
    
    val spacing = when {
        screenHeight < 700.dp -> 12.dp
        screenHeight < 1200.dp -> 16.dp
        else -> 20.dp
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "❌", fontSize = emojiSize)
        Spacer(modifier = Modifier.height(spacing))
        Text(
            text = stringResource(id = R.string.error_title),
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB50938)
        )
        Spacer(modifier = Modifier.height(spacing * 0.5f))
        Text(
            text = error,
            fontSize = errorFontSize,
            color = Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacing * 1.5f))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB50938)),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.retry_button),
                fontSize = if (screenWidth < 400.dp) 14.sp else 16.sp,
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
    screenWidth: Dp,
    screenHeight: Dp
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

    // Responsive dimensions
    val headerPaddingHorizontal = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 14.dp
        screenWidth < 800.dp -> 16.dp
        else -> 20.dp
    }
    
    val headerPaddingVertical = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 18.dp
        screenWidth < 800.dp -> 20.dp
        else -> 24.dp
    }
    
    val iconButtonSize = when {
        screenWidth < 400.dp -> 36.dp
        screenWidth < 600.dp -> 40.dp
        screenWidth < 800.dp -> 44.dp
        else -> 48.dp
    }
    
    val iconSize = when {
        screenWidth < 400.dp -> 28.dp
        screenWidth < 600.dp -> 32.dp
        screenWidth < 800.dp -> 36.dp
        else -> 40.dp
    }
    
    val cityNameFontSize = when {
        screenWidth < 400.dp -> 18.sp
        screenWidth < 600.dp -> 20.sp
        screenWidth < 800.dp -> 22.sp
        else -> 24.sp
    }
    
    val updateModeFontSize = when {
        screenWidth < 400.dp -> 11.sp
        screenWidth < 600.dp -> 12.sp
        screenWidth < 800.dp -> 13.sp
        else -> 14.sp
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header sa chevronima (kao na dizajnu)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = headerPaddingHorizontal, vertical = headerPaddingVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left chevron
            IconButton(
                onClick = debouncedPrevious,
                enabled = hasPrevious,
                modifier = Modifier.size(iconButtonSize)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous city",
                    tint = if (hasPrevious) Color(0xFFB0B0B0) else Color(0xFFE0E0E0),
                    modifier = Modifier.size(iconSize)
                )
            }

            // City name with update indicator
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedCity,
                    fontSize = cityNameFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                if (isUpdateMode) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.updating_location),
                        fontSize = updateModeFontSize,
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
                modifier = Modifier.size(iconButtonSize)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next city",
                    tint = if (hasNext) Color(0xFFB0B0B0) else Color(0xFFE0E0E0),
                    modifier = Modifier.size(iconSize)
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

        // Responsive padding and spacing for list
        val emptyStatePadding = when {
            screenWidth < 400.dp -> 40.dp
            screenWidth < 600.dp -> 50.dp
            screenWidth < 800.dp -> 60.dp
            else -> 80.dp
        }
        
        val listPaddingHorizontal = when {
            screenWidth < 400.dp -> 12.dp
            screenWidth < 600.dp -> 16.dp
            screenWidth < 800.dp -> 20.dp
            else -> 24.dp
        }
        
        val listVerticalPadding = when {
            screenHeight < 700.dp -> 12.dp
            screenHeight < 1200.dp -> 16.dp
            else -> 20.dp
        }
        
        val listSpacing = when {
            screenWidth < 400.dp -> 8.dp
            screenWidth < 600.dp -> 10.dp
            screenWidth < 800.dp -> 12.dp
            else -> 14.dp
        }
        
        if (sortedStores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(emptyStatePadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_active_stores),
                    fontSize = when {
                        screenWidth < 400.dp -> 14.sp
                        screenWidth < 600.dp -> 15.sp
                        screenWidth < 800.dp -> 16.sp
                        else -> 18.sp
                    },
                    color = Color.Black.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = listPaddingHorizontal,
                    vertical = listVerticalPadding
                ),
                verticalArrangement = Arrangement.spacedBy(listSpacing)
            ) {
                items(sortedStores) { store ->
                    LocationCard(
                        store = store,
                        enabled = !isSaving,
                        isSelected = preselectedStoreId == store.id,
                        onClick = { onStoreClick(store) },
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
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
    screenWidth: Dp,
    screenHeight: Dp
) {
    // Responsive dimensions
    val cardPadding = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 18.dp
        screenWidth < 800.dp -> 20.dp
        else -> 24.dp
    }
    
    val storeNameFontSize = when {
        screenWidth < 400.dp -> 16.sp
        screenWidth < 600.dp -> 17.sp
        screenWidth < 800.dp -> 18.sp
        else -> 20.sp
    }
    
    val addressFontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 13.sp
        screenWidth < 800.dp -> 14.sp
        else -> 16.sp
    }
    
    val cardSpacing = when {
        screenHeight < 700.dp -> 3.dp
        screenHeight < 1200.dp -> 4.dp
        else -> 6.dp
    }
    
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
                .padding(cardPadding)
        ) {
            // Store Name (bold, white if selected, black otherwise)
            Text(
                text = store.name,
                fontSize = storeNameFontSize,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Black,
                lineHeight = storeNameFontSize * 1.33f
            )

            Spacer(modifier = Modifier.height(cardSpacing))

            // Address (regular, light gray if selected, gray otherwise)
            Text(
                text = store.streetAddress,
                fontSize = addressFontSize,
                fontWeight = FontWeight.Normal,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF808080),
                lineHeight = addressFontSize * 1.43f
            )
        }
    }
}

// Helper funkcija za mock data
@Composable
private fun getMockLocations(): List<StoreLocation> {
    return listOf(
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
        ),
        StoreLocation(
            id = "3",
            name = "Fashion & Friends Promenada",
            imageUrl = null,
            email = "promenada@fashionandfriends.rs",
            phoneNumber1 = "+381 21 4444 555",
            phoneNumber2 = null,
            latitude = 45.2671,
            longitude = 19.8335,
            streetAddress = "Bulevar Oslobođenja 108",
            country = "Srbija",
            zipcode = "21000",
            city = "Novi Sad",
            tradingHours = "10:00 - 22:00",
            isActive = true
        )
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun StoreLocationsScreenPreviewSmall() {
    val mockLocations = getMockLocations()
    BoxWithConstraints {
        LocationsDialogContent(
            cities = listOf("Beograd", "Novi Sad"),
            selectedCity = "Beograd",
            locationsByCity = mapOf(
                "Beograd" to mockLocations.filter { it.city == "Beograd" },
                "Novi Sad" to mockLocations.filter { it.city == "Novi Sad" }
            ),
            isSaving = false,
            onCityClick = {},
            onStoreClick = {},
            screenWidth = maxWidth,
            screenHeight = maxHeight
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun StoreLocationsScreenPreviewMedium() {
    val mockLocations = getMockLocations()
    BoxWithConstraints {
        LocationsDialogContent(
            cities = listOf("Beograd", "Novi Sad"),
            selectedCity = "Beograd",
            locationsByCity = mapOf(
                "Beograd" to mockLocations.filter { it.city == "Beograd" },
                "Novi Sad" to mockLocations.filter { it.city == "Novi Sad" }
            ),
            isSaving = false,
            onCityClick = {},
            onStoreClick = {},
            screenWidth = maxWidth,
            screenHeight = maxHeight
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun StoreLocationsScreenPreviewLarge() {
    val mockLocations = getMockLocations()
    BoxWithConstraints {
        LocationsDialogContent(
            cities = listOf("Beograd", "Novi Sad"),
            selectedCity = "Beograd",
            locationsByCity = mapOf(
                "Beograd" to mockLocations.filter { it.city == "Beograd" },
                "Novi Sad" to mockLocations.filter { it.city == "Novi Sad" }
            ),
            isSaving = false,
            onCityClick = {},
            onStoreClick = {},
            screenWidth = maxWidth,
            screenHeight = maxHeight
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun StoreLocationsScreenPreviewPhilips() {
    val mockLocations = getMockLocations()
    BoxWithConstraints {
        LocationsDialogContent(
            cities = listOf("Beograd", "Novi Sad"),
            selectedCity = "Beograd",
            locationsByCity = mapOf(
                "Beograd" to mockLocations.filter { it.city == "Beograd" },
                "Novi Sad" to mockLocations.filter { it.city == "Novi Sad" }
            ),
            isSaving = false,
            onCityClick = {},
            onStoreClick = {},
            screenWidth = maxWidth,
            screenHeight = maxHeight
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(name = "Error State", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun StoreLocationsScreenPreviewError() {
    BoxWithConstraints {
        ErrorContent(
            error = "Failed to load store locations. Please check your internet connection.",
            onRetry = {},
            screenWidth = maxWidth,
            screenHeight = maxHeight
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(name = "Update Mode", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun StoreLocationsScreenPreviewUpdateMode() {
    val mockLocations = getMockLocations()
    BoxWithConstraints {
        LocationsDialogContent(
            cities = listOf("Beograd", "Novi Sad"),
            selectedCity = "Beograd",
            locationsByCity = mapOf(
                "Beograd" to mockLocations.filter { it.city == "Beograd" },
                "Novi Sad" to mockLocations.filter { it.city == "Novi Sad" }
            ),
            preselectedStoreId = "1",
            isSaving = false,
            isUpdateMode = true,
            onCityClick = {},
            onStoreClick = {},
            screenWidth = maxWidth,
            screenHeight = maxHeight
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(name = "Empty State", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun StoreLocationsScreenPreviewEmpty() {
    BoxWithConstraints {
        LocationsDialogContent(
            cities = listOf("Beograd"),
            selectedCity = "Beograd",
            locationsByCity = mapOf("Beograd" to emptyList()),
            isSaving = false,
            onCityClick = {},
            onStoreClick = {},
            screenWidth = maxWidth,
            screenHeight = maxHeight
        )
    }
}

