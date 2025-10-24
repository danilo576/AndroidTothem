package com.fashiontothem.ff.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.Locale

/**
 * F&F Tothem - Locale Manager
 * 
 * Manages app language based on selected store locale.
 * Updates app configuration when store is selected.
 */
object LocaleManager {
    
    private const val TAG = "FFTothem_Locale"
    
    /**
     * Update app locale based on store's locale setting.
     * 
     * @param context Application context
     * @param localeString Locale string from StoreConfig (e.g., "sr_Cyrl_RS", "hr_HR")
     */
    fun updateLocale(context: Context, localeString: String?) {
        if (localeString.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ Locale string is empty, using default")
            return
        }
        
        try {
            val locale = parseLocaleString(localeString)
            Log.d(TAG, "🌍 Setting locale: $localeString → ${locale.language}_${locale.country}")
            
            Locale.setDefault(locale)
            
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            
            Log.d(TAG, "✅ Locale updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to update locale: ${e.message}", e)
        }
    }
    
    /**
     * Parse locale string to Locale object.
     * Handles formats: "sr_Cyrl_RS", "hr_HR", "en_US"
     */
    private fun parseLocaleString(localeString: String): Locale {
        val parts = localeString.split("_")
        
        return when (parts.size) {
            1 -> Locale(parts[0]) // "sr"
            2 -> Locale(parts[0], parts[1]) // "sr", "RS"
            3 -> {
                // "sr", "Cyrl", "RS" (Serbian Cyrillic)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Locale.Builder()
                        .setLanguage(parts[0])
                        .setScript(parts[1])
                        .setRegion(parts[2])
                        .build()
                } else {
                    Locale(parts[0], parts[2]) // Fallback: ignore script
                }
            }
            else -> Locale.getDefault()
        }
    }
    
    /**
     * Recreate activity to apply new locale.
     * Call this after updating locale.
     */
    fun recreateActivity(activity: Activity) {
        activity.recreate()
    }
}

