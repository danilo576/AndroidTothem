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
import com.fashiontothem.ff.data.config.ProductCategories
import com.fashiontothem.ff.presentation.common.LoadingScreen
import com.fashiontothem.ff.presentation.common.NoInternetScreen
import com.fashiontothem.ff.presentation.filter.BrandOrCategorySelectionScreen
import com.fashiontothem.ff.presentation.filter.FilterType
import com.fashiontothem.ff.presentation.filter.GenderSelectionScreen
import com.fashiontothem.ff.presentation.home.HomeScreen
import com.fashiontothem.ff.presentation.locations.StoreLocationsScreen
import com.fashiontothem.ff.presentation.pickup.PickupPointScreen
import com.fashiontothem.ff.presentation.products.ProductListingScreen
import com.fashiontothem.ff.presentation.store.StoreSelectionScreen
import com.fashiontothem.ff.util.rememberDebouncedClick

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
                },
                onNavigateToVisualSearch = {
                    navController.navigate(
                        Screen.ProductListing.createRoute(filterType = "visual")
                    ) {
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
                },
                onNavigateToFilter = {
                    navController.navigate(Screen.GenderSelection.route)
                }
            )
        }
        
        composable(
            route = Screen.GenderSelection.route,
            arguments = Screen.GenderSelection.arguments
        ) { backStackEntry ->
            val initialGenderId = backStackEntry.arguments?.getString("genderId")
            
            val debouncedBack = rememberDebouncedClick {
                navController.popBackStack()
            }
            
            val debouncedClose = rememberDebouncedClick {
                navController.popBackStack(Screen.Home.route, inclusive = false)
            }
            
            GenderSelectionScreen(
                initialGenderId = initialGenderId,
                onGenderSelected = { genderId ->
                    navController.navigate(Screen.BrandOrCategorySelection.createRoute(genderId))
                },
                onBack = debouncedBack,
                onClose = debouncedClose
            )
        }
        
        composable(
            route = Screen.BrandOrCategorySelection.route,
            arguments = Screen.BrandOrCategorySelection.arguments
        ) { backStackEntry ->
            val genderId = backStackEntry.arguments?.getString("genderId") ?: ""
            
            // Get the correct categoryLevel for the selected gender
            val categoryLevel = when (genderId) {
                ProductCategories.Gender.WOMEN.categoryId -> ProductCategories.Gender.WOMEN.categoryLevel
                ProductCategories.Gender.MEN.categoryId -> ProductCategories.Gender.MEN.categoryLevel
                else -> "1" // Fallback
            }
            
            val debouncedBack = rememberDebouncedClick {
                // Navigate back with genderId to remember selection
                navController.navigate(Screen.GenderSelection.createRoute(genderId)) {
                    popUpTo(Screen.GenderSelection.route) { inclusive = true }
                }
            }
            
            val debouncedClose = rememberDebouncedClick {
                navController.popBackStack(Screen.Home.route, inclusive = false)
            }
            
            BrandOrCategorySelectionScreen(
                genderId = genderId,
                onFilterTypeSelected = { filterType ->
                    val filterTypeString = when (filterType) {
                        FilterType.BRAND -> "brand"
                        FilterType.CATEGORY -> "category"
                    }
                    navController.navigate(
                        Screen.ProductListing.createRoute(genderId, categoryLevel, filterTypeString)
                    ) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onBack = debouncedBack,
                onClose = debouncedClose
            )
        }
        
        composable(
            route = Screen.ProductListing.route,
            arguments = Screen.ProductListing.arguments
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            val categoryLevel = backStackEntry.arguments?.getString("categoryLevel")
            val filterType = backStackEntry.arguments?.getString("filterType") ?: "none"
            
            val debouncedBack = rememberDebouncedClick {
                navController.popBackStack()
            }
            
            val debouncedHome = rememberDebouncedClick {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = false }  // Briše ceo back stack
                    launchSingleTop = true  // Sprečava dupliciranje Home screen-a
                }
            }
            
            ProductListingScreen(
                categoryId = categoryId,
                categoryLevel = categoryLevel,
                filterType = filterType,
                onBack = debouncedBack,
                onHome = debouncedHome
            )
        }
    }
}
