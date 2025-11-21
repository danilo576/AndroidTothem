package com.fashiontothem.ff.util

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Data class representing a network request/response pair
 */
data class NetworkLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: String,
    val method: String,
    val url: String,
    val requestHeaders: Map<String, List<String>> = emptyMap(),
    val requestBody: String? = null,
    val statusCode: Int? = null,
    val statusMessage: String? = null,
    val responseHeaders: Map<String, List<String>> = emptyMap(),
    val responseBody: String? = null,
    val responseTime: Long? = null,
    val error: String? = null
) {
    val isSuccess: Boolean
        get() = statusCode != null && statusCode in 200..299
    
    val isError: Boolean
        get() = statusCode == null || statusCode !in 200..299 || error != null
}

/**
 * Manager for network logs that stores requests/responses in memory
 * Thread-safe implementation for concurrent access
 */
class NetworkLoggerManager {
    private val lock = ReentrantReadWriteLock()
    private val _logs: SnapshotStateList<NetworkLogEntry> = mutableStateListOf()
    val logs: List<NetworkLogEntry> get() = lock.read { _logs.toList() }
    
    // Maximum number of logs to keep in memory (default 100)
    var maxLogs: Int = 100
        set(value) {
            lock.write {
                field = value
                while (_logs.size > value) {
                    _logs.removeAt(0) // Remove oldest
                }
            }
        }
    
    /**
     * Add a new request to the log
     */
    fun addRequest(
        method: String,
        url: String,
        headers: Map<String, List<String>>,
        requestBody: String?,
        timestamp: String
    ) {
        lock.write {
            val entry = NetworkLogEntry(
                timestamp = timestamp,
                method = method,
                url = url,
                requestHeaders = headers,
                requestBody = requestBody
            )
            _logs.add(entry)
            
            // Keep only the last maxLogs entries
            while (_logs.size > maxLogs) {
                _logs.removeAt(0)
            }
        }
    }
    
    /**
     * Update an existing request with response data
     */
    fun updateRequest(
        url: String,
        method: String,
        statusCode: Int,
        statusMessage: String,
        headers: Map<String, List<String>>,
        responseBody: String?,
        responseTime: Long,
        error: String? = null
    ) {
        lock.write {
            // Find the most recent matching request
            val index = _logs.indexOfLast { 
                it.url == url && 
                it.method == method && 
                it.statusCode == null // Not yet updated
            }
            
            if (index >= 0) {
                val existing = _logs[index]
                _logs[index] = existing.copy(
                    statusCode = statusCode,
                    statusMessage = statusMessage,
                    responseHeaders = headers,
                    responseBody = responseBody,
                    responseTime = responseTime,
                    error = error
                )
            } else {
                // If no matching request found, create a new entry
                val entry = NetworkLogEntry(
                    timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
                        .format(java.util.Date()),
                    method = method,
                    url = url,
                    statusCode = statusCode,
                    statusMessage = statusMessage,
                    responseHeaders = headers,
                    responseBody = responseBody,
                    responseTime = responseTime,
                    error = error
                )
                _logs.add(entry)
                
                while (_logs.size > maxLogs) {
                    _logs.removeAt(0)
                }
            }
        }
    }
    
    /**
     * Clear all logs
     */
    fun clearLogs() {
        lock.write {
            _logs.clear()
        }
    }
    
    /**
     * Get logs filtered by URL pattern
     */
    fun getLogsFiltered(urlPattern: String): List<NetworkLogEntry> {
        return lock.read {
            _logs.filter { it.url.contains(urlPattern, ignoreCase = true) }
        }
    }
    
    /**
     * Get logs filtered by status code
     */
    fun getLogsByStatus(minStatus: Int, maxStatus: Int): List<NetworkLogEntry> {
        return lock.read {
            _logs.filter { 
                it.statusCode != null && 
                it.statusCode >= minStatus && 
                it.statusCode <= maxStatus 
            }
        }
    }
    
    /**
     * Get error logs only
     */
    fun getErrorLogs(): List<NetworkLogEntry> {
        return lock.read {
            _logs.filter { it.isError }
        }
    }
}

