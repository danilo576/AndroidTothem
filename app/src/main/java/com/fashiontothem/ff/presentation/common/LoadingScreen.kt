package com.fashiontothem.ff.presentation.common

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
    onNavigateToVisualSearch: () -> Unit,
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
                // All configured - check for visual search first
                Log.d("FFTothem_Loading", "All configured, refreshing config...")
                storeRepository.refreshStoreConfigAndInitAthena()
                delay(300) // Just enough to show splash briefly
                
                // Check if we have a visual search image from camera
                val visualSearchImage = locationPreferences.visualSearchImage.firstOrNull()
                if (!visualSearchImage.isNullOrEmpty()) {
                    Log.d("FFTothem_Loading", "Visual search image found - navigate to visual search")
                    onNavigateToVisualSearch()
                } else {
                    Log.d("FFTothem_Loading", "No visual search - navigate to home")
                    onNavigateToHome()
                }
            }
        }
    }

    // Splash screen with animated loader
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background
        Image(
            painter = painterResource(id = humer.UvcCamera.R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Animated Fashion logo loader in center
        val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
        
        // Pulse animation (scale + fade)
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        
        Image(
            painter = painterResource(id = humer.UvcCamera.R.drawable.fashion_logo),
            contentDescription = "Fashion & Friends",
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.Center)
                .scale(scale)
                .alpha(alpha)
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