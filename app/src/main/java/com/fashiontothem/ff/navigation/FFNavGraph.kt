package com.fashiontothem.ff.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.domain.repository.StoreRepository
import com.fashiontothem.ff.presentation.common.LoadingScreen
import com.fashiontothem.ff.presentation.common.NoInternetScreen
import com.fashiontothem.ff.presentation.home.HomeScreen
import com.fashiontothem.ff.presentation.locations.StoreLocationsScreen
import com.fashiontothem.ff.presentation.pickup.PickupPointScreen
import com.fashiontothem.ff.presentation.products.ProductListingScreen
import com.fashiontothem.ff.presentation.store.StoreSelectionScreen

/**
 * F&F Tothem - Navigation Graph
 * 
 * Centralized navigation for the Fashion & Friends app
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FFNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Loading.route,
    onStartCamera: () -> Unit,
    storePreferences: StorePreferences,
    locationPreferences: LocationPreferences,
    storeRepository: StoreRepository
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Loading.route) {
            LoadingScreen(
                storePreferences = storePreferences,
                locationPreferences = locationPreferences,
                storeRepository = storeRepository,
                onNavigateToStoreSelection = {
                    navController.navigate(Screen.StoreSelection.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                },
                onNavigateToStoreLocations = {
                    navController.navigate(Screen.StoreLocations.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                },
                onNavigateToPickupPoint = {
                    navController.navigate(Screen.PickupPoint.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.NoInternet.route) {
            NoInternetScreen()
        }
        
        composable(Screen.StoreSelection.route) {
            StoreSelectionScreen(
                onStoreSelected = {
                    // Navigate to location selection after store is selected
                    navController.navigate(Screen.StoreLocations.route)
                }
            )
        }
        
        composable(Screen.StoreLocations.route) {
            StoreLocationsScreen(
                onLocationSelected = {
                    // Navigate to pickup point configuration after location is selected
                    navController.navigate(Screen.PickupPoint.route)
                }
            )
        }
        
        composable(Screen.PickupPoint.route) {
            PickupPointScreen(
                onContinue = {
                    // Navigate to home after pickup point is configured
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.StoreSelection.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onStartCamera = onStartCamera,
                onNavigateToProducts = { categoryId, categoryLevel ->
                    navController.navigate(
                        Screen.ProductListing.createRoute(categoryId, categoryLevel)
                    )
                }
            )
        }
        
        composable(
            route = Screen.ProductListing.route,
            arguments = Screen.ProductListing.arguments
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            val categoryLevel = backStackEntry.arguments?.getString("categoryLevel") ?: ""
            
            ProductListingScreen(
                categoryId = categoryId,
                categoryLevel = categoryLevel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
