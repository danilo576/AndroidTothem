package com.fashiontothem.ff.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.fashiontothem.ff.presentation.filter.FilterTab
import com.fashiontothem.ff.presentation.filter.FilterType
import com.fashiontothem.ff.presentation.filter.GenderSelectionScreen
import com.fashiontothem.ff.presentation.filter.ProductFiltersScreen
import com.fashiontothem.ff.presentation.home.HomeScreen
import com.fashiontothem.ff.presentation.locations.StoreLocationsScreen
import com.fashiontothem.ff.presentation.pickup.PickupPointScreen
import com.fashiontothem.ff.presentation.products.LoyaltyCardSuccessScreen
import com.fashiontothem.ff.presentation.products.OtherStoresScreen
import com.fashiontothem.ff.presentation.products.ProductAvailabilityScreen
import com.fashiontothem.ff.presentation.products.ProductDetailsScreen
import com.fashiontothem.ff.presentation.products.ProductDetailsViewModel
import com.fashiontothem.ff.presentation.products.ScanLoyaltyCardScreen
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
                        Screen.ProductListing.createRoute(
                            categoryId = categoryId,
                            categoryLevel = categoryLevel,
                            fromHome = true
                        )
                    )
                },
                onNavigateToFilter = {
                    navController.navigate(Screen.GenderSelection.route)
                },
                onNavigateToProductDetails = { barcode ->
                    navController.navigate(Screen.ProductDetails.createRoute(sku = barcode, fromBarcode = true)) {
                        launchSingleTop = true
                    }
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
            var pendingOpenFiltersRoute by remember { mutableStateOf<String?>(null) }
            
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
                    val route = Screen.ProductListing.createRoute(
                        categoryId = genderId,
                        categoryLevel = categoryLevel,
                        filterType = filterTypeString,
                        fromHome = false,
                        autoOpenFilters = false
                    )
                    pendingOpenFiltersRoute = route
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onBack = debouncedBack,
                onClose = debouncedClose
            )
            
            // Auto-open filters when ProductListing becomes current destination
            LaunchedEffect(pendingOpenFiltersRoute) {
                pendingOpenFiltersRoute?.let {
                    navController.currentBackStackEntryFlow.collect { entry ->
                        val route = entry.destination.route ?: ""
                        if (route.startsWith("product_listing")) {
                            navController.navigate(Screen.ProductFilters.route)
                            pendingOpenFiltersRoute = null
                            return@collect
                        }
                    }
                }
            }
        }
        
        composable(
            route = Screen.ProductListing.route,
            arguments = Screen.ProductListing.arguments
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            val categoryLevel = backStackEntry.arguments?.getString("categoryLevel")
            val filterType = backStackEntry.arguments?.getString("filterType") ?: "none"
            val fromHome = backStackEntry.arguments?.getBoolean("fromHome") ?: false
            
            val debouncedBack = rememberDebouncedClick {
                navController.popBackStack()
            }
            
            val debouncedHome = rememberDebouncedClick {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = false }
                    launchSingleTop = true
                }
            }
            
            // Store fromHome and filterType in savedStateHandle for ProductFiltersScreen
            LaunchedEffect(fromHome, filterType) {
                backStackEntry.savedStateHandle["fromHome"] = fromHome
                backStackEntry.savedStateHandle["filterType"] = filterType
            }
            
            ProductListingScreen(
                categoryId = categoryId,
                categoryLevel = categoryLevel,
                filterType = filterType,
                onBack = debouncedBack,
                onHome = debouncedHome,
                onOpenFilters = {
                    navController.navigate(Screen.ProductFilters.route)
                },
                onNavigateToProductDetails = { sku, shortDescription, brandLabel ->
                    navController.navigate(Screen.ProductDetails.createRoute(sku, shortDescription, brandLabel))
                }
            )
        }
        
        composable(Screen.ProductFilters.route) {
            val parentEntry = remember(navController.currentBackStackEntry) {
                try {
                    navController.getBackStackEntry(Screen.ProductListing.route)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            
            if (parentEntry == null) {
                return@composable
            }
            
            val productListingViewModel: com.fashiontothem.ff.presentation.products.ProductListingViewModel = 
                hiltViewModel(parentEntry)
            
            val uiState by productListingViewModel.uiState.collectAsStateWithLifecycle()
            val isLoadingFilters by productListingViewModel.isLoadingFilters.collectAsStateWithLifecycle()
            
            val fromHome = parentEntry.savedStateHandle.get<Boolean>("fromHome")
                ?: parentEntry.arguments?.getBoolean("fromHome")
                ?: false
            val filterType = parentEntry.savedStateHandle.get<String>("filterType")
                ?: parentEntry.arguments?.getString("filterType")
            val savedTabOrdinal = parentEntry.savedStateHandle.get<Int>("lastFilterTab")
            val initialTab = savedTabOrdinal?.let { ordinal ->
                com.fashiontothem.ff.presentation.filter.FilterTab.entries.getOrNull(ordinal)
            }
            var lastSelectedTab by remember { mutableStateOf<FilterTab?>(null) }
            
            val debouncedClose = rememberDebouncedClick {
                navController.popBackStack()
            }
            
            ProductFiltersScreen(
                availableFilters = uiState.availableFilters,
                activeFilters = uiState.activeFilters, // Use active_filters from API response
                isLoadingFilters = isLoadingFilters,
                fromHome = fromHome,
                filterType = filterType,
                initialTab = initialTab, // ✅ Remember last tab
                onApplyFilters = { genders, categories, brands, sizes, colors ->
                    val filters = com.fashiontothem.ff.domain.repository.ProductFilters(
                        genders = genders,
                        categories = categories,
                        brands = brands,
                        sizes = sizes,
                        colors = colors
                    )
                    productListingViewModel.applyFilters(filters)
                },
                onClose = {
                    // Save last tab before closing
                    lastSelectedTab?.let { tab ->
                        parentEntry.savedStateHandle["lastFilterTab"] = tab.ordinal
                    }
                    debouncedClose()
                },
                onNavigateToHome = {
                    // First pop filter screen, then navigate to home
                    navController.popBackStack()
                    // Clear navigation stack and go to home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onTabChanged = { tab ->
                    lastSelectedTab = tab
                }
            )
        }
        
        composable(
            route = Screen.ProductDetails.route,
            arguments = Screen.ProductDetails.arguments
        ) { backStackEntry ->
            val sku = backStackEntry.arguments?.getString("sku")?.takeIf { it.isNotEmpty() }
            val shortDescription = backStackEntry.arguments?.getString("shortDescription")?.takeIf { it.isNotEmpty() }
            val brandLabel = backStackEntry.arguments?.getString("brandLabel")?.takeIf { it.isNotEmpty() }
            val fromBarcode = backStackEntry.arguments?.getBoolean("fromBarcode") ?: false
            
            val debouncedBack = rememberDebouncedClick {
                navController.popBackStack()
            }
            
            ProductDetailsScreen(
                sku = sku,
                shortDescription = shortDescription,
                brandLabel = brandLabel,
                isBarcodeScan = fromBarcode,
                onBack = debouncedBack,
                onClose = debouncedBack, // X button now goes back instead of home
                onCheckAvailability = {
                    navController.navigate(Screen.ProductAvailability.route)
                }
            )
        }

        composable(Screen.ProductAvailability.route) { backStackEntry ->
            val parentEntry = remember(navController.currentBackStackEntry) {
                try {
                    navController.getBackStackEntry(Screen.ProductDetails.route)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }

            if (parentEntry == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                return@composable
            }

            val productDetailsViewModel: ProductDetailsViewModel = hiltViewModel(parentEntry)

            val uiState by productDetailsViewModel.uiState.collectAsStateWithLifecycle()

            ProductAvailabilityScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onClose = { navController.popBackStack() },
                onDeliverToPickupPoint = { /* TODO: Hook into pickup flow */ },
                onOrderOnline = { 
                    navController.navigate(Screen.ScanLoyaltyCard.route)
                },
                onViewMoreStores = { 
                    navController.navigate(Screen.OtherStores.route)
                }
            )
        }

        composable(Screen.OtherStores.route) { backStackEntry ->
            val parentEntry = remember(navController.currentBackStackEntry) {
                try {
                    navController.getBackStackEntry(Screen.ProductAvailability.route)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }

            if (parentEntry == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                return@composable
            }

            val productDetailsViewModel: ProductDetailsViewModel = hiltViewModel(
                navController.getBackStackEntry(Screen.ProductDetails.route)
            )

            val uiState by productDetailsViewModel.uiState.collectAsStateWithLifecycle()

            OtherStoresScreen(
                uiState = uiState,
                selectedStoreId = uiState.selectedStoreId,
                onBack = { navController.popBackStack() },
                onClose = { navController.popBackStack() }
            )
        }

        composable(Screen.ScanLoyaltyCard.route) { backStackEntry ->
            val parentEntry = remember(navController.currentBackStackEntry) {
                try {
                    navController.getBackStackEntry(Screen.ProductDetails.route)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }

            if (parentEntry == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                return@composable
            }

            val productDetailsViewModel: ProductDetailsViewModel = hiltViewModel(parentEntry)
            val uiState by productDetailsViewModel.uiState.collectAsStateWithLifecycle()

            ScanLoyaltyCardScreen(
                uiState = uiState,
                viewModel = productDetailsViewModel,
                onClose = { 
                    // Go back one step (to ProductAvailabilityScreen)
                    navController.popBackStack()
                },
                onCardScanned = { cardNumber ->
                    android.util.Log.d("FFNavGraph", "Navigating to LoyaltyCardSuccess with card: $cardNumber")
                    navController.navigate(Screen.LoyaltyCardSuccess.createRoute(cardNumber))
                }
            )
        }

        composable(Screen.LoyaltyCardSuccess.route) { backStackEntry ->
            val cardNumber = backStackEntry.arguments?.getString("cardNumber") ?: ""
            LoyaltyCardSuccessScreen(
                scannedCardNumber = cardNumber,
                onClose = {
                    // Navigate to HomeScreen by clearing back stack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

