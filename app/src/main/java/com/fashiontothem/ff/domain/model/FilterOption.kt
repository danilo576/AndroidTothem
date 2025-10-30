package com.fashiontothem.ff.domain.model

/**
 * F&F Tothem - Filter Option Models
 *
 * Domain models for filter options (brands, sizes, colors, categories)
 */

data class FilterOptions(
    val genders: List<FilterOption> = emptyList(),
    val genderParamName: String? = null, // option_key from API (e.g., "pol" or "gender")
    val brands: List<FilterOption> = emptyList(),
    val brandParamName: String? = null,
    val sizes: List<FilterOption> = emptyList(),
    val sizeParamName: String? = null,
    val colors: List<FilterOption> = emptyList(),
    val colorParamName: String? = null,
    val categories: List<FilterOption> = emptyList(),
    val categoryParamName: String? = null
)

data class FilterOption(
    val key: String,      // API key (e.g., "nike", "s", "red", "muskarci")
    val label: String,    // Display label (e.g., "Nike", "S", "Red", "Muško")
    val count: Int = 0,   // Number of products with this filter
    val imageUrl: String? = null,  // Image URL for brands
    val hexCode: String? = null     // Hex code for colors (e.g., "#FF0000" or image URL)
)

