package com.fashiontothem.ff.domain.model

/**
 * Domain model for an image item.
 * 
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

