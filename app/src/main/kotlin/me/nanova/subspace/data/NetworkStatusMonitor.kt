package me.nanova.subspace.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val networkStatus: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network becomes available
                trySend(true)
            }

            override fun onLost(network: Network) {
                // Network is lost
                trySend(false)
            }

            // onCapabilitiesChanged can also be used for more granular checks
            // override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            //     val isInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            //     trySend(isInternet)
            // }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        // Register the callback
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Send the initial state
        val isInitialOnline = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
        trySend(isInitialOnline)

        // Unregister the callback when the flow is cancelled
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}