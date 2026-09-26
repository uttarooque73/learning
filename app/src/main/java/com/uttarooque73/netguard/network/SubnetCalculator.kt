package com.uttarooque73.netguard.network

object SubnetCalculator {
    fun networkCidr(localIp: String, prefixLength: Int): String? {
        if (prefixLength !in 1..30) return null
        val octets = localIp.split('.').mapNotNull { it.toIntOrNull() }
        if (octets.size != 4 || octets.any { it !in 0..255 }) return null
        val hostBits = 32 - prefixLength
        val localValue = octets.fold(0L) { acc, octet -> (acc shl 8) or octet.toLong() }
        val mask = (-1L shl hostBits) and 0xffffffffL
        val network = localValue and mask
        val address = listOf(
            (network shr 24) and 255,
            (network shr 16) and 255,
            (network shr 8) and 255,
            network and 255
        ).joinToString(".")
        return "$address/$prefixLength"
    }

    fun hosts(localIp: String, prefixLength: Int): List<String> {
        if (prefixLength !in 1..30) return emptyList()
        val octets = localIp.split('.').mapNotNull { it.toIntOrNull() }
        if (octets.size != 4 || octets.any { it !in 0..255 }) return emptyList()
        val hostBits = 32 - prefixLength
        val networkSize = 1L shl hostBits
        if (networkSize > 256L) return emptyList()
        val localValue = octets.fold(0L) { acc, octet -> (acc shl 8) or octet.toLong() }
        val mask = (-1L shl hostBits) and 0xffffffffL
        val network = localValue and mask
        val first = if (networkSize > 2) network + 1 else network
        val last = if (networkSize > 2) network + networkSize - 2 else network + networkSize - 1
        return (first..last).map { value ->
            listOf((value shr 24) and 255,(value shr 16) and 255,(value shr 8) and 255,value and 255).joinToString(".")
        }.filter { it != localIp }
    }
}
