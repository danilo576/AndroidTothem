package com.fashiontothem.ff.domain.repository

/**
 * Exception thrown when the requested quantity is not available in stock
 */
class QuantityNotAvailableException(
    message: String = "The requested qty is not available"
) : Exception(message)

