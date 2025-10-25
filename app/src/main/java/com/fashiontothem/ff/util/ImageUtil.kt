package com.fashiontothem.ff.util

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * F&F Tothem - Image Utilities
 * 
 * Helper functions for image processing
 */
object ImageUtil {
    
    /**
     * Convert Bitmap to Base64 string
     * 
     * @param bitmap The bitmap to convert
     * @param quality JPEG compression quality (0-100)
     * @return Base64 encoded string
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}

