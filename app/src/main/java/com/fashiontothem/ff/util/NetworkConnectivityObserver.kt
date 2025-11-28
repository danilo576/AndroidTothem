package com.fashiontothem.ff.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * F&F Tothem - Network Connectivity Observer
 * 
 * Monitors internet connectivity (WiFi, Ethernet, Cellular)
 */
class NetworkConnectivityObserver(context: Context) {
    
    private val connectivityManager: ConnectivityManager? = try {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    } catch (e: Exception) {
        Log.e("FFTothem_Network", "Failed to get ConnectivityManager: ${e.message}", e)
        null
    }
    
    /**
     * Observe network connectivity changes.
     * Emits true when connected, false when disconnected.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun observe(): Flow<Boolean> = callbackFlow {
        // Check if ConnectivityManager is available
        if (connectivityManager == null) {
            Log.e("FFTothem_Network", "ConnectivityManager is null, sending false")
            try {
                trySend(false)
            } catch (e: Exception) {
                // Ignore
            }
            return@callbackFlow
        }
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            private val networks = mutableSetOf<Network>()
            
            override fun onAvailable(network: Network) {
                networks.add(network)
                Log.d("FFTothem_Network", "Network available: $network, Total: ${networks.size}")
                try {
                    trySend(true)
                } catch (e: Exception) {
                    // Channel might be closed, ignore
                    Log.d("FFTothem_Network", "Failed to send network available: ${e.message}")
                }
            }
            
            override fun onLost(network: Network) {
                networks.remove(network)
                Log.d("FFTothem_Network", "Network lost: $network, Remaining: ${networks.size}")
                try {
                    trySend(networks.isNotEmpty())
                } catch (e: Exception) {
                    // Channel might be closed, ignore
                    Log.d("FFTothem_Network", "Failed to send network lost: ${e.message}")
                }
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                Log.d("FFTothem_Network", "Network capabilities changed: hasInternet=$hasInternet")
                try {
                    trySend(hasInternet)
                } catch (e: Exception) {
                    // Channel might be closed, ignore
                    Log.d("FFTothem_Network", "Failed to send network capabilities changed: ${e.message}")
                }
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        
        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            Log.e("FFTothem_Network", "Failed to register network callback: ${e.message}", e)
            // Send false and close channel if registration fails
            try {
                trySend(false)
            } catch (sendException: Exception) {
                // Ignore
            }
            awaitClose { /* Nothing to clean up */ }
            return@callbackFlow
        }
        
        // Send initial state
        val isConnected = try {
            if (connectivityManager != null) {
                val network = connectivityManager.activeNetwork
                if (network != null) {
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FFTothem_Network", "Error checking initial network state: ${e.message}", e)
            false
        }
        Log.d("FFTothem_Network", "Initial network state: $isConnected")
        try {
            trySend(isConnected)
        } catch (e: Exception) {
            // Channel might be closed, ignore
            Log.d("FFTothem_Network", "Failed to send initial network state: ${e.message}")
        }
        
        awaitClose {
            Log.d("FFTothem_Network", "Unregistering network callback")
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Callback might already be unregistered, ignore
                Log.d("FFTothem_Network", "Failed to unregister network callback: ${e.message}")
            }
        }
    }.distinctUntilChanged()
    
    /**
     * Check current network availability.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun isNetworkAvailable(): Boolean {
        if (connectivityManager == null) {
            Log.e("FFTothem_Network", "ConnectivityManager is null in isNetworkAvailable()")
            return false
        }
        
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e("FFTothem_Network", "Error checking network availability: ${e.message}", e)
            false
        }
    }
}

