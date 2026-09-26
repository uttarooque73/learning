package com.uttarooque73.netguard.network

sealed class NetworkDiscoveryError {
    data object MissingNetwork : NetworkDiscoveryError()
    data object MissingLocalAddress : NetworkDiscoveryError()
    data object InvalidSubnet : NetworkDiscoveryError()
    data class Failed(val message: String) : NetworkDiscoveryError()
}
