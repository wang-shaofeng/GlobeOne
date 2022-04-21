/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network_components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import ph.com.globe.data.DataScope
import ph.com.globe.model.network.NetworkConnectionStatus
import ph.com.globe.model.network.NetworkConnectionStatus.*
import javax.inject.Inject

interface NetworkStatusProvider {
    fun provideNetworkConnectionStatus(): NetworkConnectionStatus
}

@DataScope
class DefaultNetworkStatusProvider @Inject constructor(val context: Context) :
    NetworkStatusProvider {

    private val connectivityManager: ConnectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun provideNetworkConnectionStatus(): NetworkConnectionStatus = getNetworkStatus()

    private fun getNetworkStatus(): NetworkConnectionStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // this part of code is to be executed on devices running on api level less than 23

            val networkInfo =
                connectivityManager.activeNetworkInfo
                    ?: return NotConnectedToInternet // Deprecated in 29

            with(networkInfo) {
                // first we check if device is connected to internet
                if (!isConnected) {
                    return NotConnectedToInternet
                }

                return when (type) {
                    // here we check if the device is connected to a wifi network
                    ConnectivityManager.TYPE_WIFI -> {
                        ConnectedToWifi
                    }
                    // here we check if the device is connected to mobile data
                    ConnectivityManager.TYPE_MOBILE -> {
                        ConnectedToMobileData
                    }
                    // finally we provide status 'Other' as there could be more connection types (ex: ethernet)
                    ConnectivityManager.TYPE_ETHERNET -> {
                        Other
                    }
                    else -> {
                        NotConnectedToInternet
                    }
                }
            }
        } else {
            // this part of code is to be executed on devices running on api level 23 or more

            val activeNetwork = connectivityManager.activeNetwork
                ?: return NotConnectedToInternet
            val networkInfo =
                connectivityManager.getNetworkCapabilities(activeNetwork)
                    ?: return NotConnectedToInternet

            with(networkInfo) {
                // first we check if device is connected to internet
                if (!hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                    !hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ) {
                    return NotConnectedToInternet
                }

                return when {
                    // here we check if the device is connected to a wifi network
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        ConnectedToWifi
                    }
                    // here we check if the device is connected to mobile data
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        ConnectedToMobileData
                    }
                    // finally we provide status 'Other' as there could be more connection types (ex: ethernet)
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Other
                    }
                    else -> {
                        NotConnectedToInternet
                    }
                }
            }

        }
    }
}
