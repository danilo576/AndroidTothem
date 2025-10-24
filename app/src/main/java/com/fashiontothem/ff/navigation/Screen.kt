package com.fashiontothem.ff.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * F&F Tothem - Navigation Screens
 * 
 * Sealed class defining all app screens and their routes
 */
sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Loading : Screen("loading")
    
    object StoreSelection : Screen("store_selection")
    
    object StoreLocations : Screen("store_locations")
    
    object Home : Screen("home")
    
    object ProductListing : Screen(
        route = "product_listing/{categoryId}/{categoryLevel}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("categoryLevel") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) {
        fun createRoute(categoryId: String, categoryLevel: String): String {
            return "product_listing/$categoryId/$categoryLevel"
        }
    }
}
