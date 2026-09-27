package com.uttarooque73.netguard.report

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AuditHistoryStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_audit_history", Context.MODE_PRIVATE)

    fun save(snapshot: AuditSnapshot) {
        val root = runCatching { JSONArray(preferences.getString("snapshots", "[]")) }.getOrElse { JSONArray() }
        for (i in root.length() - 1 downTo 0) {
            if (root.optJSONObject(i)?.optString("id") == snapshot.id) root.remove(i)
        }
        root.put(JSONObject().apply {
                put("id", snapshot.id)
                put("createdAtEpochMs", snapshot.createdAtEpochMs)
                put("deviceCount", snapshot.devices.size)
                put("serviceCount", snapshot.services.size)
                put("findingCount", snapshot.findings.size)
                put("network", snapshot.network?.let { network ->
                    JSONObject().apply {
                        put("interfaceName", network.interfaceName)
                        put("localAddress", network.localAddress)
                        put("gatewayAddress", network.gatewayAddress)
                        put("subnet", network.subnet)
                        put("dnsServers", JSONArray(network.dnsServers))
                        put("ssid", network.ssid)
                        put("bssid", network.bssid)
                        put("wifiSecurity", network.wifiSecurity)
                    }
                })
                put("devices", JSONArray(snapshot.devices.map { device ->
                    JSONObject().put("ipAddress", device.ipAddress).put("hostname", device.hostname)
                        .put("reachable", device.reachable).put("discoveredAtEpochMs", device.discoveredAtEpochMs)
                }))
                put("services", JSONArray(snapshot.services.map { service ->
                    JSONObject().put("ipAddress", service.ipAddress).put("port", service.port)
                        .put("protocol", service.protocol).put("serviceName", service.serviceName)
                        .put("reachable", service.reachable).put("discoveredAtEpochMs", service.discoveredAtEpochMs)
                }))
                put("findings", JSONArray(snapshot.findings.map { JSONObject().put("id", it.id).put("title", it.title).put("severity", it.severity.name).put("confidence", it.confidence.name).put("ipAddress", it.ipAddress).put("evidence", it.evidence).put("remediation", it.remediation).put("createdAtEpochMs", it.createdAtEpochMs) }))
                put("remediations", JSONArray(snapshot.remediationRecords.map { JSONObject().put("findingId", it.findingId).put("ipAddress", it.ipAddress).put("status", it.status.name).put("startedAtEpochMs", it.startedAtEpochMs) }))
                put("verifications", JSONArray(snapshot.verificationResults.map { JSONObject().put("findingId", it.findingId).put("ipAddress", it.ipAddress).put("status", it.status.name).put("beforeEvidence", it.beforeEvidence).put("afterEvidence", it.afterEvidence).put("verifiedAtEpochMs", it.verifiedAtEpochMs) }))
                put("customPolicyEvaluations", JSONArray(snapshot.customPolicyEvaluations.map { evaluation ->
                    JSONObject().put("id", evaluation.policy.id)
                        .put("title", evaluation.policy.title)
                        .put("description", evaluation.policy.description)
                        .put("ruleType", evaluation.policy.ruleType.name)
                        .put("port", evaluation.policy.port)
                        .put("enabled", evaluation.policy.enabled)
                        .put("passed", evaluation.passed)
                        .put("evidence", evaluation.evidence)
                }))
        })
        while (root.length() > 20) root.remove(0)
        preferences.edit().putString("snapshots", root.toString()).apply()
    }

    fun load(): List<AuditHistoryEntry> {
        val raw = preferences.getString("snapshots", null) ?: return emptyList()
        return runCatching {
            val root = JSONArray(raw)
            List(root.length()) { i ->
                val item = root.getJSONObject(i)
                AuditHistoryEntry(item.getString("id"), item.optLong("createdAtEpochMs"), item.optInt("deviceCount"), item.optInt("serviceCount"), item.optInt("findingCount"))
            }
        }.getOrElse { emptyList() }
    }
}

data class AuditHistoryEntry(
    val id: String,
    val createdAtEpochMs: Long,
    val deviceCount: Int,
    val serviceCount: Int,
    val findingCount: Int
)