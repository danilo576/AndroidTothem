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
    
    object NoInternet : Screen("no_internet")
    
    object StoreSelection : Screen("store_selection")
    
    object StoreLocations : Screen("store_locations")
    
    object PickupPoint : Screen("pickup_point")
    
    object Home : Screen("home")
    
    object GenderSelection : Screen(
        route = "gender_selection?genderId={genderId}",
        arguments = listOf(
            navArgument("genderId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        fun createRoute(genderId: String? = null): String {
            return if (genderId != null) {
                "gender_selection?genderId=$genderId"
            } else {
                "gender_selection"
            }
        }
    }
    
    object BrandOrCategorySelection : Screen(
        route = "brand_or_category_selection/{genderId}",
        arguments = listOf(
            navArgument("genderId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(genderId: String): String {
            return "brand_or_category_selection/$genderId"
        }
    }
    
    object ProductListing : Screen(
        route = "product_listing?categoryId={categoryId}&categoryLevel={categoryLevel}&filterType={filterType}&fromHome={fromHome}&autoOpenFilters={autoOpenFilters}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument("categoryLevel") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument("filterType") {
                type = NavType.StringType
                defaultValue = "none"
            },
            navArgument("fromHome") {
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument("autoOpenFilters") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) {
        fun createRoute(
            categoryId: String? = null, 
            categoryLevel: String? = null, 
            filterType: String = "none",
            fromHome: Boolean = false,
            autoOpenFilters: Boolean = false
        ): String {
            val params = mutableListOf<String>()
            categoryId?.let { params.add("categoryId=$it") }
            categoryLevel?.let { params.add("categoryLevel=$it") }
            params.add("filterType=$filterType")
            params.add("fromHome=$fromHome")
            params.add("autoOpenFilters=$autoOpenFilters")
            return "product_listing?${params.joinToString("&")}"
        }
    }
    
    object ProductFilters : Screen("product_filters")
}
