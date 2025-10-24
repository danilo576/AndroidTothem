package com.fashiontothem.ff.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fashiontothem.ff.presentation.common.LoadingScreen
import com.fashiontothem.ff.presentation.home.HomeScreen
import com.fashiontothem.ff.presentation.locations.StoreLocationsScreen
import com.fashiontothem.ff.presentation.products.ProductListingScreen
import com.fashiontothem.ff.presentation.store.StoreSelectionScreen

/**
 * F&F Tothem - Navigation Graph
 * 
 * Centralized navigation for the Fashion & Friends app
 */
@Composable
fun FFNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Loading.route,
    onStartCamera: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Loading.route) {
            LoadingScreen()
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
                    // Navigate to home after location is selected
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
