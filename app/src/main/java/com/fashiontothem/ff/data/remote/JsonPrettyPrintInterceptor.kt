package com.fashiontothem.ff.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Custom interceptor that pretty prints JSON responses and request bodies in logs
 * Uses peekBody() to avoid consuming the response body
 */
class JsonPrettyPrintInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var requestBodyString: String? = null
        
        // Log request body for POST/PUT/PATCH requests
        if (request.method in listOf("POST", "PUT", "PATCH")) {
            try {
                val requestBody = request.body
                if (requestBody != null) {
                    // Create a copy of the request body to read without consuming it
                    val buffer = Buffer()
                    requestBody.writeTo(buffer)
                    requestBodyString = buffer.readUtf8()
                    
                    if (!requestBodyString.isNullOrBlank()) {
                        val prettyRequestJson = try {
                            val trimmed = requestBodyString.trim()
                            when {
                                trimmed.startsWith('[') -> {
                                    JSONArray(trimmed).toString(2)
                                }
                                trimmed.startsWith('{') -> {
                                    JSONObject(trimmed).toString(2)
                                }
                                else -> requestBodyString
                            }
                        } catch (e: Exception) {
                            requestBodyString
                        }
                        
                        Log.d("OkHttp", "═══════════════════════════════════════════════════════════")
                        Log.d("OkHttp", "Request Body (${request.method} ${request.url}):")
                        Log.d("OkHttp", prettyRequestJson)
                        Log.d("OkHttp", "═══════════════════════════════════════════════════════════")
                    }
                    
                    // Recreate request body since we consumed it
                    val newRequestBody = okhttp3.RequestBody.create(
                        requestBody.contentType(),
                        requestBodyString
                    )
                    val newRequest = request.newBuilder()
                        .method(request.method, newRequestBody)
                        .build()
                    
                    val response = chain.proceed(newRequest)
                    logResponseBody(response)
                    return response
                }
            } catch (e: IOException) {
                Log.e("OkHttp", "Error reading request body", e)
            } catch (e: Exception) {
                Log.e("OkHttp", "Error formatting request body", e)
            }
        }
        
        val response = chain.proceed(request)
        logResponseBody(response)
        return response
    }
    
    private fun logResponseBody(response: Response) {
        try {
            val responseBody = response.peekBody(Long.MAX_VALUE)
            val bodyString = responseBody.string()
            
            if (!bodyString.isNullOrBlank()) {
                val contentType = response.body?.contentType()
                val isJson = contentType?.subtype?.equals("json", ignoreCase = true) == true
                
                val prettyResponse = if (isJson) {
                    try {
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
                } else {
                    // For non-JSON responses (e.g., HTML errors), log as-is but limit length
                    if (bodyString.length > 2000) {
                        bodyString.take(2000) + "\n... (truncated, total length: ${bodyString.length})"
                    } else {
                        bodyString
                    }
                }
                
                // Log response body
                Log.d("OkHttp", "═══════════════════════════════════════════════════════════")
                Log.d("OkHttp", "Response Body (${response.code} ${response.message}):")
                Log.d("OkHttp", prettyResponse)
                Log.d("OkHttp", "═══════════════════════════════════════════════════════════")
            }
        } catch (e: Exception) {
            Log.e("OkHttp", "Error reading response body for pretty print", e)
        }
    }
}

