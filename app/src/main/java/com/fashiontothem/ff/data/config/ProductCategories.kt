package com.fashiontothem.ff.data.config

/**
 * F&F Tothem - Product Categories Configuration
 * 
 * Centralized configuration for all product categories and types
 * used throughout the app for navigation and filtering.
 */
object ProductCategories {
    
    /**
     * Product Category Configuration
     * 
     * @param categoryId The unique identifier for the category
     * @param categoryLevel The hierarchy level of the category
     * @param displayName Human-readable name for logging/debugging
     */
    data class Category(
        val categoryId: String,
        val categoryLevel: String,
        val displayName: String
    )
    
    /**
     * Gender categories for filtering
     */
    object Gender {
        /**
         * Žene (Women)
         * Category ID: 4
         */
        val WOMEN = Category(
            categoryId = "4",
            categoryLevel = "2",
            displayName = "Žene (Women)"
        )
        
        /**
         * Muškarci (Men)
         * Category ID: 5
         */
        val MEN = Category(
            categoryId = "5",
            categoryLevel = "2",
            displayName = "Muškarci (Men)"
        )
    }
    
    /**
     * Main product categories available in the app
     */
    object Main {
        /**
         * Novo (New Items)
         * Category ID: 223, Level: 3
         */
        val NEW_ITEMS = Category(
            categoryId = "223",
            categoryLevel = "3",
            displayName = "Novo (New Items)"
        )
        
        /**
         * Akcije (Sale/Promotions)
         * Category ID: 630, Level: 2
         */
        val ACTIONS = Category(
            categoryId = "630",
            categoryLevel = "2",
            displayName = "Akcije (Sale)"
        )
    }
}

