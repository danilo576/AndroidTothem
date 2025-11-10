package com.fashiontothem.ff.core.scanner

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Centralized event bus for hardware barcode scans emitted from Honeywell SDK callbacks.
 */
object BarcodeScannerEvents {

    private val _scans = MutableSharedFlow<String>(extraBufferCapacity = 32)

    val scans: SharedFlow<String> = _scans.asSharedFlow()

    fun emit(barcode: String) {
        if (barcode.isNotBlank()) {
            _scans.tryEmit(barcode)
        }
    }
}

