/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.gideongeng.music.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.gideongeng.music.constants.OfflineModeKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Simple NetworkConnectivityObserver based on OuterTune's implementation
 * Provides network connectivity monitoring for auto-play functionality.
 * When Offline Mode is enabled via [OfflineModeKey], all connectivity signals
 * are forced to false regardless of the actual network state.
 */
class NetworkConnectivityObserver(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStatus = Channel<Boolean>(Channel.CONFLATED)

    /** Emits true when connected AND offline mode is disabled. */
    val networkStatus = combine(
        _networkStatus.receiveAsFlow(),
        context.dataStore.data.map { prefs -> prefs[OfflineModeKey] ?: false }
    ) { isConnected, offlineMode ->
        if (offlineMode) false else isConnected
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkStatus.trySend(true)
        }

        override fun onLost(network: Network) {
            _networkStatus.trySend(false)
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            // Fallback: assume connected if registration fails
            _networkStatus.trySend(true)
        }

        // Send initial state
        _networkStatus.trySend(checkPhysicalConnection())
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    /**
     * Synchronously returns the effective connectivity state.
     * Returns false if Offline Mode is enabled OR if physically disconnected.
     */
    fun isCurrentlyConnected(): Boolean {
        if (context.dataStore.get(OfflineModeKey, false)) return false
        return checkPhysicalConnection()
    }

    /** Checks the actual hardware network state without consulting Offline Mode. */
    private fun checkPhysicalConnection(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            val hasInternet = networkCapabilities
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            val isValidated = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                networkCapabilities
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
            } else {
                true
            }

            hasInternet && isValidated
        } catch (e: Exception) {
            false
        }
    }
}
