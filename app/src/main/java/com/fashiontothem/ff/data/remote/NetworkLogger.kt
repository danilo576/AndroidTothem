package com.fashiontothem.ff.data.remote

import com.fashiontothem.ff.util.NetworkLoggerManager
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Network Logger interceptor that logs all HTTP requests and responses
 * and stores them in memory for QA inspection via UI
 */
class NetworkLogger(
    private val networkLoggerManager: NetworkLoggerManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        
        // Log request and get request body string (may modify request if body was read)
        val (requestBodyString, requestToProceed) = logRequest(request, timestamp)
        
        // Execute request
        val response = try {
            chain.proceed(requestToProceed)
        } catch (e: Exception) {
            logError(request, e, timestamp, startTime)
            throw e
        }
        
        // Log response
        val responseTime = System.currentTimeMillis() - startTime
        logResponse(response, timestamp, responseTime, requestBodyString)
        
        return response
    }
    
    private fun logRequest(request: okhttp3.Request, timestamp: String): Pair<String?, okhttp3.Request> {
        val method = request.method
        val url = request.url.toString()
        val headers = request.headers.toMultimap()
        
        // Read request body if present and recreate request
        var requestBodyString: String? = null
        var requestToProceed = request
        
        if (request.body != null && request.method in listOf("POST", "PUT", "PATCH")) {
            try {
                val buffer = Buffer()
                request.body?.writeTo(buffer)
                requestBodyString = buffer.readUtf8()
                
                // Recreate request body since we consumed it
                val contentType = request.body?.contentType()
                val newRequestBody = okhttp3.RequestBody.create(contentType, requestBodyString)
                requestToProceed = request.newBuilder()
                    .method(request.method, newRequestBody)
                    .build()
            } catch (e: Exception) {
                Timber.tag("NetworkLogger").e(e, "Error reading request body")
            }
        }
        
        // Format request body for logging
        val formattedRequestBody = requestBodyString?.let { body ->
            try {
                val trimmed = body.trim()
                when {
                    trimmed.startsWith('[') -> JSONArray(trimmed).toString(2)
                    trimmed.startsWith('{') -> JSONObject(trimmed).toString(2)
                    else -> body
                }
            } catch (e: Exception) {
                body
            }
        }
        
        // Log to Timber
        Timber.tag("NetworkLogger").d("═══════════════════════════════════════════════════════════")
        Timber.tag("NetworkLogger").d("→ REQUEST [$timestamp]")
        Timber.tag("NetworkLogger").d("$method $url")
        Timber.tag("NetworkLogger").d("Headers: $headers")
        if (formattedRequestBody != null) {
            Timber.tag("NetworkLogger").d("Body:\n$formattedRequestBody")
        }
        Timber.tag("NetworkLogger").d("═══════════════════════════════════════════════════════════")
        
        // Store in manager
        networkLoggerManager.addRequest(
            method = method,
            url = url,
            headers = headers,
            requestBody = formattedRequestBody ?: requestBodyString,
            timestamp = timestamp
        )
        
        return Pair(requestBodyString, requestToProceed)
    }
    
    private fun logResponse(
        response: Response,
        timestamp: String,
        responseTime: Long,
        requestBodyString: String?
    ) {
        val code = response.code
        val message = response.message
        val url = response.request.url.toString()
        val method = response.request.method
        val headers = response.headers.toMultimap()
        
        // Read response body
        var responseBodyString: String? = null
        var formattedResponseBody: String? = null
        
        try {
            val responseBody = response.peekBody(Long.MAX_VALUE)
            responseBodyString = responseBody.string()
            
            if (!responseBodyString.isNullOrBlank()) {
                val contentType = response.body?.contentType()
                val isJson = contentType?.subtype?.equals("json", ignoreCase = true) == true
                
                formattedResponseBody = if (isJson) {
                    try {
                        val trimmed = responseBodyString.trim()
                        when {
                            trimmed.startsWith('[') -> JSONArray(trimmed).toString(2)
                            trimmed.startsWith('{') -> JSONObject(trimmed).toString(2)
                            else -> responseBodyString
                        }
                    } catch (e: Exception) {
                        responseBodyString
                    }
                } else {
                    // Limit non-JSON responses
                    if (responseBodyString.length > 2000) {
                        responseBodyString.take(2000) + "\n... (truncated, total length: ${responseBodyString.length})"
                    } else {
                        responseBodyString
                    }
                }
            }
        } catch (e: Exception) {
            Timber.tag("NetworkLogger").e(e, "Error reading response body")
        }
        
        // Log to Timber
        val statusEmoji = when {
            code in 200..299 -> "✅"
            code in 300..399 -> "⚠️"
            code in 400..499 -> "❌"
            code >= 500 -> "🔥"
            else -> "❓"
        }
        
        Timber.tag("NetworkLogger").d("═══════════════════════════════════════════════════════════")
        Timber.tag("NetworkLogger").d("← RESPONSE [$timestamp] ${responseTime}ms")
        Timber.tag("NetworkLogger").d("$statusEmoji $code $message")
        Timber.tag("NetworkLogger").d("$method $url")
        Timber.tag("NetworkLogger").d("Headers: $headers")
        if (formattedResponseBody != null) {
            Timber.tag("NetworkLogger").d("Body:\n$formattedResponseBody")
        }
        Timber.tag("NetworkLogger").d("═══════════════════════════════════════════════════════════")
        
        // Store in manager
        networkLoggerManager.updateRequest(
            url = url,
            method = method,
            statusCode = code,
            statusMessage = message,
            headers = headers,
            responseBody = formattedResponseBody ?: responseBodyString,
            responseTime = responseTime
        )
    }
    
    private fun logError(
        request: okhttp3.Request,
        error: Exception,
        timestamp: String,
        startTime: Long
    ) {
        val responseTime = System.currentTimeMillis() - startTime
        val url = request.url.toString()
        val method = request.method
        
        Timber.tag("NetworkLogger").e("═══════════════════════════════════════════════════════════")
        Timber.tag("NetworkLogger").e("❌ ERROR [$timestamp] ${responseTime}ms")
        Timber.tag("NetworkLogger").e("$method $url")
        Timber.tag("NetworkLogger").e("Error: ${error.message}")
        Timber.tag("NetworkLogger").e(error)
        Timber.tag("NetworkLogger").e("═══════════════════════════════════════════════════════════")
        
        // Store error in manager
        networkLoggerManager.updateRequest(
            url = url,
            method = method,
            statusCode = -1,
            statusMessage = error.message ?: "Network Error",
            headers = emptyMap(),
            responseBody = null,
            responseTime = responseTime,
            error = error.message
        )
    }
}

