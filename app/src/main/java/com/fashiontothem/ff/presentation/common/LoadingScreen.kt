package com.fashiontothem.ff.presentation.common

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.domain.repository.StoreRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

/**
 * F&F Tothem - Loading Screen
 *
 * Shows splash screen and initializes app:
 * - Refreshes store config and Athena token on app start
 * - Auto-navigates to appropriate screen based on state
 */
@Composable
fun LoadingScreen(
    storePreferences: StorePreferences,
    locationPreferences: LocationPreferences,
    storeRepository: StoreRepository,
    onNavigateToStoreSelection: () -> Unit,
    onNavigateToStoreLocations: () -> Unit,
    onNavigateToPickupPoint: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    // Initialize app ONCE on first composition
    LaunchedEffect(Unit) {
        Log.d("FFTothem_Loading", "🚀 App starting - reading preferences...")

        // Read preferences synchronously to avoid Flow initial value issues
        val storeCode = storePreferences.selectedStoreCode.firstOrNull()
        val locationId = locationPreferences.selectedStoreId.firstOrNull()
        val hasConfiguredPickup = locationPreferences.hasConfiguredPickup.firstOrNull() ?: false
        
        Log.d("FFTothem_Loading", "Store: '$storeCode', Location: '$locationId', PickupConfigured: $hasConfiguredPickup")

        when {
            storeCode.isNullOrEmpty() -> {
                // No store - show splash longer
                delay(1500)
                Log.d("FFTothem_Loading", "No store selected - navigate to StoreSelection")
                onNavigateToStoreSelection()
            }

            locationId.isNullOrEmpty() -> {
                // Store selected but no location - minimal delay
                delay(500)
                Log.d("FFTothem_Loading", "Store selected, refreshing config...")
                storeRepository.refreshStoreConfigAndInitAthena()
                onNavigateToStoreLocations()
            }

            !hasConfiguredPickup -> {
                // Location selected but pickup not configured yet - first time setup
                delay(300)
                Log.d("FFTothem_Loading", "Location selected, pickup not configured - navigate to PickupPoint")
                storeRepository.refreshStoreConfigAndInitAthena()
                onNavigateToPickupPoint()
            }

            else -> {
                // All configured - go to home
                Log.d("FFTothem_Loading", "All configured, refreshing config...")
                storeRepository.refreshStoreConfigAndInitAthena()
                delay(300) // Just enough to show splash briefly
                onNavigateToHome()
            }
        }
    }

    // Splash screen
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = humer.UvcCamera.R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun LoadingScreenPreviewPhilips() {
    // Simple splash preview without dependencies
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = humer.UvcCamera.R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}