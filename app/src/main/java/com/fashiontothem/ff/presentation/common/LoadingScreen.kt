package com.fashiontothem.ff.presentation.common

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.domain.repository.StoreRepository
import kotlinx.coroutines.delay

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
    val selectedStoreCode by storePreferences.selectedStoreCode.collectAsStateWithLifecycle(
        initialValue = null
    )
    val selectedLocationId by locationPreferences.selectedStoreId.collectAsStateWithLifecycle(
        initialValue = null
    )

    // Initialize app on first composition
    LaunchedEffect(Unit) {
        Log.d("FFTothem_Loading", "🚀 App starting - checking state...")

        // Show splash for minimum duration
        delay(1500)

        when {
            selectedStoreCode.isNullOrEmpty() -> {
                Log.d("FFTothem_Loading", "No store selected - navigate to StoreSelection")
                onNavigateToStoreSelection()
            }

            selectedLocationId.isNullOrEmpty() -> {
                // Store selected but no location - refresh config and navigate to locations
                Log.d("FFTothem_Loading", "Store selected, refreshing config...")
                storeRepository.refreshStoreConfigAndInitAthena()
                onNavigateToStoreLocations()
            }

            else -> {
                // Both store and location selected - refresh config and go to home
                Log.d("FFTothem_Loading", "Store and location selected, refreshing config...")
                storeRepository.refreshStoreConfigAndInitAthena()
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

        FashionLoader()
    }
}