package com.fashiontothem.ff.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

/**
 * Custom interceptor that pretty prints JSON responses in logs
 * Uses peekBody() to avoid consuming the response body
 */
class JsonPrettyPrintInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        val contentType = response.body?.contentType()
        val isJson = contentType?.subtype?.equals("json", ignoreCase = true) == true
        
        if (isJson) {
            try {
                // Use peekBody() to read without consuming the body
                val bodyString = response.peekBody(Long.MAX_VALUE).string()
                
                if (!bodyString.isNullOrBlank()) {
                    val prettyJson = try {
                        // Try to parse as JSON and pretty print
                        val trimmed = bodyString.trim()
                        when {
                            trimmed.startsWith('[') -> {
                                JSONArray(trimmed).toString(2)
                            }
                            trimmed.startsWith('{') -> {
                                JSONObject(trimmed).toString(2)
                            }
                            else -> bodyString
                        }
                    } catch (e: Exception) {
                        // If JSON parsing fails, return original
                        bodyString
                    }
                    
                    // Log pretty formatted JSON
                    Log.d("OkHttp", "═══════════════════════════════════════════════════════════")
                    Log.d("OkHttp", "Response Body (Pretty Formatted):")
                    Log.d("OkHttp", prettyJson)
                    Log.d("OkHttp", "═══════════════════════════════════════════════════════════")
                }
            } catch (e: Exception) {
                Log.e("OkHttp", "Error reading response body for pretty print", e)
            }
        }
        
        return response
    }
}

