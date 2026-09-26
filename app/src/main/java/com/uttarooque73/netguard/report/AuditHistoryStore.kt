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
                put("network", snapshot.network?.let { JSONObject().put("localAddress", it.localAddress).put("gatewayAddress", it.gatewayAddress).put("subnet", it.subnet) })
                put("findings", JSONArray(snapshot.findings.map { JSONObject().put("id", it.id).put("title", it.title).put("severity", it.severity.name).put("confidence", it.confidence.name).put("ipAddress", it.ipAddress).put("evidence", it.evidence).put("remediation", it.remediation).put("createdAtEpochMs", it.createdAtEpochMs) }))
                put("remediations", JSONArray(snapshot.remediationRecords.map { JSONObject().put("findingId", it.findingId).put("ipAddress", it.ipAddress).put("status", it.status.name).put("startedAtEpochMs", it.startedAtEpochMs) }))
                put("verifications", JSONArray(snapshot.verificationResults.map { JSONObject().put("findingId", it.findingId).put("ipAddress", it.ipAddress).put("status", it.status.name).put("beforeEvidence", it.beforeEvidence).put("afterEvidence", it.afterEvidence).put("verifiedAtEpochMs", it.verifiedAtEpochMs) }))
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