package com.fashiontothem.ff.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.categoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "ff_tothem_category_prefs")

/**
 * F&F Tothem - Category Preferences
 * 
 * DataStore for saving category IDs and levels for Akcije and Novo categories.
 */
@Singleton
class CategoryPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.categoryDataStore
    
    companion object {
        // Default values from ProductCategories.kt
        private const val DEFAULT_ACTIONS_CATEGORY_ID = "630"
        private const val DEFAULT_ACTIONS_CATEGORY_LEVEL = "2"
        private const val DEFAULT_NEW_ITEMS_CATEGORY_ID = "223"
        private const val DEFAULT_NEW_ITEMS_CATEGORY_LEVEL = "3"
        
        val ACTIONS_CATEGORY_ID = stringPreferencesKey("actions_category_id")
        val ACTIONS_CATEGORY_LEVEL = stringPreferencesKey("actions_category_level")
        val NEW_ITEMS_CATEGORY_ID = stringPreferencesKey("new_items_category_id")
        val NEW_ITEMS_CATEGORY_LEVEL = stringPreferencesKey("new_items_category_level")
    }
    
    /**
     * Get Actions category ID.
     * Returns default value if not set.
     */
    val actionsCategoryId: Flow<String> = dataStore.data.map { preferences ->
        preferences[ACTIONS_CATEGORY_ID] ?: DEFAULT_ACTIONS_CATEGORY_ID
    }
    
    /**
     * Get Actions category level.
     * Returns default value if not set.
     */
    val actionsCategoryLevel: Flow<String> = dataStore.data.map { preferences ->
        preferences[ACTIONS_CATEGORY_LEVEL] ?: DEFAULT_ACTIONS_CATEGORY_LEVEL
    }
    
    /**
     * Get New Items category ID.
     * Returns default value if not set.
     */
    val newItemsCategoryId: Flow<String> = dataStore.data.map { preferences ->
        preferences[NEW_ITEMS_CATEGORY_ID] ?: DEFAULT_NEW_ITEMS_CATEGORY_ID
    }
    
    /**
     * Get New Items category level.
     * Returns default value if not set.
     */
    val newItemsCategoryLevel: Flow<String> = dataStore.data.map { preferences ->
        preferences[NEW_ITEMS_CATEGORY_LEVEL] ?: DEFAULT_NEW_ITEMS_CATEGORY_LEVEL
    }
    
    /**
     * Save Actions category configuration.
     */
    suspend fun saveActionsCategory(categoryId: String, categoryLevel: String) {
        dataStore.edit { preferences ->
            preferences[ACTIONS_CATEGORY_ID] = categoryId
            preferences[ACTIONS_CATEGORY_LEVEL] = categoryLevel
        }
    }
    
    /**
     * Save New Items category configuration.
     */
    suspend fun saveNewItemsCategory(categoryId: String, categoryLevel: String) {
        dataStore.edit { preferences ->
            preferences[NEW_ITEMS_CATEGORY_ID] = categoryId
            preferences[NEW_ITEMS_CATEGORY_LEVEL] = categoryLevel
        }
    }
    
    /**
     * Reset to default values.
     */
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences[ACTIONS_CATEGORY_ID] = DEFAULT_ACTIONS_CATEGORY_ID
            preferences[ACTIONS_CATEGORY_LEVEL] = DEFAULT_ACTIONS_CATEGORY_LEVEL
            preferences[NEW_ITEMS_CATEGORY_ID] = DEFAULT_NEW_ITEMS_CATEGORY_ID
            preferences[NEW_ITEMS_CATEGORY_LEVEL] = DEFAULT_NEW_ITEMS_CATEGORY_LEVEL
        }
    }
}

