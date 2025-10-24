package com.fashiontothem.ff.navigation

import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences

/**
 * F&F Tothem - Navigation Manager
 * 
 * Handles navigation logic based on app state
 */
class NavigationManager(
    private val storePreferences: StorePreferences,
    private val locationPreferences: LocationPreferences
) {
    
    /**
     * Determines the appropriate start destination based on app state.
     * ALWAYS starts with Loading screen to avoid flicker from Flow emissions.
     * LoadingScreen handles navigation to the correct screen.
     */
    fun getStartDestination(): String {
        // Always start with Loading to avoid navigation flicker
        // LoadingScreen will read preferences and navigate appropriately
        return Screen.Loading.route
    }
}
