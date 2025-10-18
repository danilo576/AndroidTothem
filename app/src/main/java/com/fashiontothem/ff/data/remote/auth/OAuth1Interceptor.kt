package com.fashiontothem.ff.data.remote.auth

import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * F&F Tothem - OAuth1 Interceptor
 * 
 * Adds OAuth1 authorization header to requests for Fashion & Friends API.
 */
class OAuth1Interceptor(
    private val consumerKey: String,
    private val consumerSecret: String,
    private val accessToken: String,
    private val tokenSecret: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url
        
        // Generate OAuth1 parameters
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = UUID.randomUUID().toString().replace("-", "")
        
        val params = sortedMapOf(
            "oauth_consumer_key" to consumerKey,
            "oauth_token" to accessToken,
            "oauth_signature_method" to "HMAC-SHA1",
            "oauth_timestamp" to timestamp,
            "oauth_nonce" to nonce,
            "oauth_version" to "1.0"
        )
        
        // Add query parameters to OAuth params for signature
        url.queryParameterNames.forEach { name ->
            url.queryParameter(name)?.let { value ->
                params[name] = value
            }
        }
        
        // Build base string
        val method = original.method
        val baseUrl = "${url.scheme}://${url.host}${url.encodedPath}"
        val paramString = params.map { "${encode(it.key)}=${encode(it.value)}" }
            .joinToString("&")
        
        val baseString = "$method&${encode(baseUrl)}&${encode(paramString)}"
        
        // Generate signature
        val signingKey = "${encode(consumerSecret)}&${encode(tokenSecret)}"
        val signature = generateSignature(baseString, signingKey)
        
        // Build Authorization header
        val authParams = mapOf(
            "oauth_consumer_key" to consumerKey,
            "oauth_token" to accessToken,
            "oauth_signature_method" to "HMAC-SHA1",
            "oauth_timestamp" to timestamp,
            "oauth_nonce" to nonce,
            "oauth_version" to "1.0",
            "oauth_signature" to signature
        )
        
        val authHeader = "OAuth " + authParams.map { 
            "${encode(it.key)}=\"${encode(it.value)}\"" 
        }.joinToString(", ")
        
        // Add Authorization header
        val request = original.newBuilder()
            .header("Authorization", authHeader)
            .build()
        
        return chain.proceed(request)
    }
    
    private fun generateSignature(baseString: String, signingKey: String): String {
        val mac = Mac.getInstance("HmacSHA1")
        val secretKey = SecretKeySpec(signingKey.toByteArray(), "HmacSHA1")
        mac.init(secretKey)
        val bytes = mac.doFinal(baseString.toByteArray())
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }
    
    private fun encode(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~")
    }
}

