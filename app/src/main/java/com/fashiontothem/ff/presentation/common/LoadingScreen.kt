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
    onNavigateToHome: () -> Unit,
) {
    // Initialize app ONCE on first composition
    LaunchedEffect(Unit) {
        Log.d("FFTothem_Loading", "🚀 App starting - reading preferences...")

        // Read preferences synchronously to avoid Flow initial value issues
        val storeCode = storePreferences.selectedStoreCode.firstOrNull()
        val locationId = locationPreferences.selectedStoreId.firstOrNull()
        
        Log.d("FFTothem_Loading", "Store: '$storeCode', Location: '$locationId'")

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

            else -> {
                // Both store and location selected - minimal delay, quick navigation
                Log.d("FFTothem_Loading", "Store and location selected, refreshing config...")
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