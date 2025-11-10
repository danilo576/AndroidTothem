package com.fashiontothem.ff.domain.repository

/**
 * Indicates that requested product is no longer available (e.g. API returns HTTP 404).
 */
class ProductUnavailableException(
    message: String = "Product unavailable"
) : Exception(message)

