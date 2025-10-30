package com.fashiontothem.ff.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fashiontothem.ff.domain.repository.ProductFilters
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F&F Tothem - Filter Preferences
 *
 * Manages product filter state persistence using DataStore
 */
private val Context.filterDataStore: DataStore<Preferences> by preferencesDataStore(name = "filter_preferences")

@Singleton
class FilterPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.filterDataStore

    companion object {
        private val FILTER_CATEGORIES = stringPreferencesKey("filter_categories")
        private val FILTER_BRANDS = stringPreferencesKey("filter_brands")
        private val FILTER_SIZES = stringPreferencesKey("filter_sizes")
        private val FILTER_COLORS = stringPreferencesKey("filter_colors")
    }

    /**
     * Flow of current filter state
     * Returns null if no filters are saved
     */
    val currentFilters: Flow<ProductFilters?> = dataStore.data.map { preferences ->
        val categories = preferences[FILTER_CATEGORIES]?.takeIf { it.isNotBlank() }?.split(",")?.toSet() ?: emptySet()
        val brands = preferences[FILTER_BRANDS]?.takeIf { it.isNotBlank() }?.split(",")?.toSet() ?: emptySet()
        val sizes = preferences[FILTER_SIZES]?.takeIf { it.isNotBlank() }?.split(",")?.toSet() ?: emptySet()
        val colors = preferences[FILTER_COLORS]?.takeIf { it.isNotBlank() }?.split(",")?.toSet() ?: emptySet()
        
        // Return null if all filters are empty
        if (categories.isEmpty() && brands.isEmpty() && sizes.isEmpty() && colors.isEmpty()) {
            null
        } else {
            ProductFilters(
                categories = categories,
                brands = brands,
                sizes = sizes,
                colors = colors
            )
        }
    }

    /**
     * Save filter state
     */
    suspend fun saveFilters(filters: ProductFilters) {
        dataStore.edit { preferences ->
            // Only save non-empty filters, remove empty ones
            if (filters.categories.isNotEmpty()) {
                preferences[FILTER_CATEGORIES] = filters.categories.joinToString(",")
            } else {
                preferences.remove(FILTER_CATEGORIES)
            }
            
            if (filters.brands.isNotEmpty()) {
                preferences[FILTER_BRANDS] = filters.brands.joinToString(",")
            } else {
                preferences.remove(FILTER_BRANDS)
            }
            
            if (filters.sizes.isNotEmpty()) {
                preferences[FILTER_SIZES] = filters.sizes.joinToString(",")
            } else {
                preferences.remove(FILTER_SIZES)
            }
            
            if (filters.colors.isNotEmpty()) {
                preferences[FILTER_COLORS] = filters.colors.joinToString(",")
            } else {
                preferences.remove(FILTER_COLORS)
            }
        }
    }

    /**
     * Clear all filters
     */
    suspend fun clearFilters() {
        dataStore.edit { preferences ->
            preferences.remove(FILTER_CATEGORIES)
            preferences.remove(FILTER_BRANDS)
            preferences.remove(FILTER_SIZES)
            preferences.remove(FILTER_COLORS)
        }
    }
}

