/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.content.Context
import android.net.wifi.WifiManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface GatewayConstructorModule {
    @Binds
    @Singleton
    fun provideGatewayConstructor(gatewayConstructor: DefaultGatewayIpAddressProvider): GatewayIpAddressProvider
}

interface GatewayIpAddressProvider {
    fun getGateway(): String
}

class DefaultGatewayIpAddressProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : GatewayIpAddressProvider {
    override fun getGateway(): String = "http://${context.getWifiGateway()}"
}

fun Context.getWifiGateway(): String? {
    val wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val dhcp = wifi.dhcpInfo
    return dhcp.gateway.takeIf { it != 0 }?.let { intToIp(dhcp.gateway) }
}

fun intToIp(address: Int): String {
    var addr = address
    return (addr and 0xFF).toString() + "." +
            (8.let { addr = addr ushr it; addr } and 0xFF) + "." +
            (8.let { addr = addr ushr it; addr } and 0xFF) + "." +
            (8.let { addr = addr ushr it; addr } and 0xFF)
}
