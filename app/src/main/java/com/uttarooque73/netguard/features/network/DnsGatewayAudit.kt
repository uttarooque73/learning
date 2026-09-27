package com.uttarooque73.netguard.features.network

import android.content.Context
import android.net.ConnectivityManager
import java.net.InetAddress

data class DnsGatewayAuditResult(
    val dnsServers: List<String>,
    val gateway: String?,
    val gatewayResolved: Boolean,
    val gatewayChanged: Boolean = false,
    val privateDnsActive: Boolean? = null,
    val privateDnsServerName: String? = null,
    val evidence: List<String>,
    val remediation: List<String>
)

object DnsGatewayAudit {
    fun inspect(context: Context, previousGateway: String? = null): DnsGatewayAuditResult {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork
        val lp = network?.let { cm.getLinkProperties(it) }
        val dns = lp?.dnsServers.orEmpty().mapNotNull(InetAddress::getHostAddress)
        val gateway = lp?.routes?.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress
        val resolved = gateway?.let { runCatching { InetAddress.getByName(it) }.isSuccess } ?: false
        val changed = previousGateway != null && gateway != null && previousGateway != gateway
        val privateDnsActive = if (android.os.Build.VERSION.SDK_INT >= 28) lp?.isPrivateDnsActive else null
        val privateDnsServerName = if (android.os.Build.VERSION.SDK_INT >= 28) lp?.privateDnsServerName else null
        val evidence = buildList {
            add("DNS servers: " + dns.ifEmpty { listOf("unavailable") }.joinToString())
            add("Default gateway: " + (gateway ?: "unavailable"))
            if (changed) add("Gateway changed from $previousGateway to $gateway.")
            if (privateDnsActive == true) add("Android Private DNS is active.")
            if (privateDnsServerName != null) add("Private DNS server name: $privateDnsServerName")
            if (privateDnsActive == false) add("Android Private DNS is not active.")
        }
        return DnsGatewayAuditResult(
            dnsServers = dns,
            gateway = gateway,
            gatewayResolved = resolved,
            gatewayChanged = changed,
            privateDnsActive = privateDnsActive,
            privateDnsServerName = privateDnsServerName,
            evidence = evidence,
            remediation = listOf(
                "Confirm the gateway and DNS servers belong to the expected network.",
                "Investigate unexpected gateway changes before trusting the network.",
                "Use trusted encrypted DNS/VPN controls according to your security policy."
            )
        )
    }
}