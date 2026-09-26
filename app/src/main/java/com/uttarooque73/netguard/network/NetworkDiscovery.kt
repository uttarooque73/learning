package com.uttarooque73.netguard.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface

class NetworkDiscovery(private val context: Context) {

    fun inspect(): NetworkInfo {
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity.activeNetwork
        val linkProperties: LinkProperties? = network?.let { connectivity.getLinkProperties(it) }

        val interfaceName = linkProperties?.interfaceName
        val localAddress = linkProperties?.linkAddresses
            ?.map { it.address }
            ?.filterIsInstance<Inet4Address>()
            ?.firstOrNull()
            ?.hostAddress

        val gatewayAddress = linkProperties?.routes
            ?.firstOrNull { it.isDefaultRoute }
            ?.gateway
            ?.hostAddress

        val subnet = linkProperties?.linkAddresses
            ?.firstOrNull { it.address is Inet4Address }
            ?.let { "${it.address.hostAddress}/${it.prefixLength}" }

        val dnsServers = linkProperties?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList()

        val wifi = context.applicationContext.getSystemService(WifiManager::class.java)
        @Suppress("DEPRECATION")
        val connection = wifi.connectionInfo

        @Suppress("DEPRECATION")
        val ssid = connection?.ssid?.takeUnless { it == "<unknown ssid>" }?.trim('"')
        @Suppress("DEPRECATION")
        val bssid = connection?.bssid?.takeUnless { it == "02:00:00:00:00:00" }

        return NetworkInfo(
            interfaceName = interfaceName,
            localAddress = localAddress,
            gatewayAddress = gatewayAddress,
            subnet = subnet,
            dnsServers = dnsServers,
            ssid = ssid,
            bssid = bssid,
            wifiSecurity = null
        )
    }

    fun localIpv4Interfaces(): List<String> = NetworkInterface.getNetworkInterfaces()
        ?.toList()
        ?.flatMap { networkInterface ->
            networkInterface.inetAddresses.toList()
                .filterIsInstance<Inet4Address>()
                .mapNotNull { it.hostAddress }
        }
        ?: emptyList()
}
