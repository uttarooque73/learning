package com.uttarooque73.netguard.features.network

import android.content.Context
import android.net.ConnectivityManager
import java.net.InetAddress

data class DnsGatewayAuditResult(
    val dnsServers: List<String>,
    val gateway: String?,
    val gatewayResolved: Boolean,
    val gatewayChanged: Boolean = false,
    val evidence: List<String>,
    val remediation: List<String>
)

object DnsGatewayAudit {
    fun inspect(context: Context, previousGateway: String? = null): DnsGatewayAuditResult {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork
        val lp = network?.let { cm.getLinkProperties(it) }
        val dns = lp?.dnsServers.orEmpty().map(InetAddress::getHostAddress)
        val gateway = lp?.routes?.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress
        val resolved = gateway?.let { runCatching { InetAddress.getByName(it) }.isSuccess } ?: false
        val changed = previousGateway != null && gateway != null && previousGateway != gateway
        val evidence = buildList {
            add("DNS servers: " + dns.ifEmpty { listOf("unavailable") }.joinToString())
            add("Default gateway: " + (gateway ?: "unavailable"))
            if (changed) add("Gateway changed from $previousGateway to $gateway.")
        }
        return DnsGatewayAuditResult(
            dnsServers = dns,
            gateway = gateway,
            gatewayResolved = resolved,
            gatewayChanged = changed,
            evidence = evidence,
            remediation = listOf(
                "Confirm the gateway and DNS servers belong to the expected network.",
                "Investigate unexpected gateway changes before trusting the network.",
                "Use trusted encrypted DNS/VPN controls according to your security policy."
            )
        )
    }
}