package com.fashiontothem.ff.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
     * Determines the appropriate start destination based on app state
     */
    @Composable
    fun getStartDestination(): String {
        val selectedStoreCode by storePreferences.selectedStoreCode.collectAsState(initial = "")
        val selectedStoreLocationId by locationPreferences.selectedStoreId.collectAsState(initial = "")
        
        return when {
            selectedStoreCode == "" -> Screen.Loading.route
            selectedStoreCode.isNullOrEmpty() -> Screen.StoreSelection.route
            selectedStoreLocationId == "" -> Screen.Loading.route
            selectedStoreLocationId.isNullOrEmpty() -> Screen.StoreLocations.route
            else -> Screen.Home.route
        }
    }
}
