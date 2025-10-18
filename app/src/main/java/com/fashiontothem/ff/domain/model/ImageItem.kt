package com.fashiontothem.ff.domain.model

/**
 * F&F Tothem - Image Item Model
 * 
 * Domain model for a fashion image item.
 * Used in gallery, pagination, and image-heavy features.
 */
data class ImageItem(
    val id: String,
    val url: String,
    val title: String,
    val description: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val thumbnailUrl: String? = null
)

