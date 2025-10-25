package com.fashiontothem.ff.presentation.pickup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F&F Tothem - Pickup Point ViewModel
 * 
 * Manages pick-up point delivery option state.
 */
@HiltViewModel
class PickupPointViewModel @Inject constructor(
    private val locationPreferences: LocationPreferences
) : ViewModel() {
    
    /**
     * Selected store name from preferences.
     */
    val selectedStoreName: Flow<String?> = locationPreferences.selectedStoreName
    
    /**
     * Pick-up point enabled flag.
     */
    val pickupPointEnabled: Flow<Boolean> = locationPreferences.pickupPointEnabled
    
    /**
     * Toggle pick-up point option.
     */
    fun setPickupPointEnabled(enabled: Boolean) {
        viewModelScope.launch {
            locationPreferences.setPickupPointEnabled(enabled)
        }
    }
    
    /**
     * Mark pickup configuration as completed.
     * Called when user clicks "Continue" button.
     */
    fun markConfigurationCompleted() {
        viewModelScope.launch {
            locationPreferences.markPickupConfigured()
        }
    }
}

