package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialConnection())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _connectionType = MutableStateFlow(getConnectionTypeDescription())
    val connectionType: StateFlow<String> = _connectionType.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
            _connectionType.value = getConnectionTypeDescription()
        }

        override fun onLost(network: Network) {
            _isOnline.value = checkInitialConnection()
            _connectionType.value = getConnectionTypeDescription()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            _isOnline.value = hasInternet
            _connectionType.value = getConnectionTypeDescription()
        }
    }

    init {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            _isOnline.value = true
        }
    }

    private fun checkInitialConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getConnectionTypeDescription(): String {
        val activeNetwork = connectivityManager.activeNetwork ?: return "সংযোগ বিচ্ছিন্ন"
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "অজানা"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "ওয়াই-ফাই (Wi-Fi হাই স্পিড)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "মোবাইল ডেটা (4G/5G নেটওয়ার্ক)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ব্রডব্যান্ড ইন্টারনেট"
            else -> "স্থিতিশীল ইন্টারনেট"
        }
    }
}
