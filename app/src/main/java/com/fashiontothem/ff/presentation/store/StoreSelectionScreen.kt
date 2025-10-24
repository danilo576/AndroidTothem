package com.fashiontothem.ff.presentation.store

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import com.fashiontothem.ff.domain.model.CountryStore
import com.fashiontothem.ff.presentation.common.FashionLoader
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R

/**
 * F&F Tothem - Store Selection Screen (Dialog Version)
 *
 * Dialog for selecting country and store.
 * Shows over splash_background.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StoreSelectionScreen(
    viewModel: StoreSelectionViewModel = hiltViewModel(),
    onStoreSelected: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate when store is selected
    LaunchedEffect(uiState.storeSelected) {
        if (uiState.storeSelected) {
            onStoreSelected()
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            fontFamily = Fonts.Poppins,
                            text = "Izaberi zemlju",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Content
                        when {
                            uiState.isLoading -> LoadingContent()
                            uiState.error != null -> ErrorContent(
                                error = uiState.error!!,
                                onRetry = { viewModel.loadStores() },
                                onDismiss = { viewModel.dismissError() }
                            )

                            else -> StoreList(
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
            text = "Učitavanje prodavnica...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
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
            Text(
                text = "Pokušaj ponovo",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StoreList(
    stores: List<CountryStore>,
    isSaving: Boolean,
    onStoreClick: (String, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
    }
}

@Composable
private fun CountryStoreCard(
    countryStore: CountryStore,
    enabled: Boolean,
    onClick: (String) -> Unit,
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
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Authentic flag image (circular)
            AsyncImage(
                model = flagUrl,
                contentDescription = countryStore.countryName,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Country name
            Text(
                text = countryStore.countryName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                fontFamily = Fonts.Poppins,
                modifier = Modifier.weight(1f)
            )

            // Arrow or loading indicator
            if (!enabled) {
                FashionLoader()
            } else {
                Text(
                    text = "→",
                    fontSize = 35.sp,
                    color = Color(0xFFB50938)
                )
            }
        }
    }
}

