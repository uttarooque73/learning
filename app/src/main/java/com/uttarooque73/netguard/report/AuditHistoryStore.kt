package com.uttarooque73.netguard.report

import android.content.Context
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingConfidence
import com.uttarooque73.netguard.audit.FindingSeverity
import com.uttarooque73.netguard.features.policy.CustomPolicy
import com.uttarooque73.netguard.features.policy.CustomPolicyEvaluation
import com.uttarooque73.netguard.features.policy.CustomPolicyRuleType
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.remediation.RemediationRecord
import com.uttarooque73.netguard.remediation.RemediationStatus
import com.uttarooque73.netguard.verification.VerificationResult
import com.uttarooque73.netguard.verification.VerificationStatus
import org.json.JSONArray
import org.json.JSONObject

class AuditHistoryStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_audit_history", Context.MODE_PRIVATE)

    fun save(snapshot: AuditSnapshot) {
        val root = runCatching { JSONArray(preferences.getString("snapshots", "[]")) }.getOrElse { JSONArray() }
        for (i in root.length() - 1 downTo 0) {
            if (root.optJSONObject(i)?.optString("id") == snapshot.id) root.remove(i)
        }
        root.put(snapshotJson(snapshot))
        while (root.length() > MAX_SNAPSHOTS) root.remove(0)
        preferences.edit().putString("snapshots", root.toString()).apply()
    }

    fun load(): List<AuditHistoryEntry> {
        val raw = preferences.getString("snapshots", null) ?: return emptyList()
        return runCatching {
            val root = JSONArray(raw)
            List(root.length()) { i ->
                val item = root.getJSONObject(i)
                AuditHistoryEntry(
                    id = item.getString("id"),
                    createdAtEpochMs = item.optLong("createdAtEpochMs"),
                    deviceCount = item.optInt("deviceCount"),
                    serviceCount = item.optInt("serviceCount"),
                    findingCount = item.optInt("findingCount")
                )
            }
        }.getOrElse { emptyList() }
    }

    fun loadSnapshot(id: String): AuditSnapshot? {
        val raw = preferences.getString("snapshots", null) ?: return null
        return runCatching {
            val root = JSONArray(raw)
            (0 until root.length())
                .map { root.getJSONObject(it) }
                .firstOrNull { it.optString("id") == id }
                ?.let(::snapshotFromJson)
        }.getOrNull()
    }

    private fun snapshotJson(snapshot: AuditSnapshot): JSONObject = JSONObject().apply {
        put("id", snapshot.id)
        put("createdAtEpochMs", snapshot.createdAtEpochMs)
        put("deviceCount", snapshot.devices.size)
        put("serviceCount", snapshot.services.size)
        put("findingCount", snapshot.findings.size)
        put("network", snapshot.network?.let(::networkJson))
        put("devices", JSONArray(snapshot.devices.map(::deviceJson)))
        put("services", JSONArray(snapshot.services.map(::serviceJson)))
        put("findings", JSONArray(snapshot.findings.map(::findingJson)))
        put("remediations", JSONArray(snapshot.remediationRecords.map(::remediationJson)))
        put("verifications", JSONArray(snapshot.verificationResults.map(::verificationJson)))
        put("customPolicyEvaluations", JSONArray(snapshot.customPolicyEvaluations.map(::customPolicyEvaluationJson)))
    }

    private fun snapshotFromJson(item: JSONObject): AuditSnapshot = AuditSnapshot(
        id = requireText(item, "id"),
        createdAtEpochMs = item.getLong("createdAtEpochMs"),
        network = item.optJSONObject("network")?.let(::networkFromJson),
        devices = parseDevices(item.optJSONArray("devices") ?: JSONArray()),
        services = parseServices(item.optJSONArray("services") ?: JSONArray()),
        findings = parseFindings(item.optJSONArray("findings") ?: JSONArray()),
        remediationRecords = parseRemediations(item.optJSONArray("remediations") ?: JSONArray()),
        verificationResults = parseVerifications(item.optJSONArray("verifications") ?: JSONArray()),
        customPolicyEvaluations = parseCustomPolicies(item.optJSONArray("customPolicyEvaluations") ?: JSONArray())
    )

    private fun networkJson(value: NetworkInfo) = JSONObject().apply {
        put("interfaceName", value.interfaceName)
        put("localAddress", value.localAddress)
        put("gatewayAddress", value.gatewayAddress)
        put("subnet", value.subnet)
        put("dnsServers", JSONArray(value.dnsServers))
        put("ssid", value.ssid)
        put("bssid", value.bssid)
        put("wifiSecurity", value.wifiSecurity)
    }

    private fun deviceJson(value: DiscoveredDevice) = JSONObject().apply {
        put("ipAddress", value.ipAddress)
        put("hostname", value.hostname)
        put("reachable", value.reachable)
        put("discoveredAtEpochMs", value.discoveredAtEpochMs)
    }

    private fun serviceJson(value: DiscoveredService) = JSONObject().apply {
        put("ipAddress", value.ipAddress)
        put("port", value.port)
        put("protocol", value.protocol)
        put("serviceName", value.serviceName)
        put("reachable", value.reachable)
        put("discoveredAtEpochMs", value.discoveredAtEpochMs)
    }

    private fun findingJson(value: Finding) = JSONObject().apply {
        put("id", value.id)
        put("title", value.title)
        put("severity", value.severity.name)
        put("confidence", value.confidence.name)
        put("ipAddress", value.ipAddress)
        put("evidence", value.evidence)
        put("explanation", value.explanation)
        put("remediation", value.remediation)
        put("verification", value.verification)
        put("createdAtEpochMs", value.createdAtEpochMs)
    }

    private fun remediationJson(value: RemediationRecord) = JSONObject().apply {
        put("findingId", value.findingId)
        put("ipAddress", value.ipAddress)
        put("status", value.status.name)
        put("startedAtEpochMs", value.startedAtEpochMs)
    }

    private fun verificationJson(value: VerificationResult) = JSONObject().apply {
        put("findingId", value.findingId)
        put("ipAddress", value.ipAddress)
        put("status", value.status.name)
        put("beforeEvidence", value.beforeEvidence)
        put("afterEvidence", value.afterEvidence)
        put("verifiedAtEpochMs", value.verifiedAtEpochMs)
    }

    private fun customPolicyEvaluationJson(value: CustomPolicyEvaluation) = JSONObject().apply {
        put("id", value.policy.id)
        put("title", value.policy.title)
        put("description", value.policy.description)
        put("ruleType", value.policy.ruleType.name)
        put("port", value.policy.port)
        put("enabled", value.policy.enabled)
        put("passed", value.passed)
        put("evidence", value.evidence)
    }

    private fun networkFromJson(item: JSONObject) = NetworkInfo(
        interfaceName = item.optNullableString("interfaceName"),
        localAddress = item.optNullableString("localAddress"),
        gatewayAddress = item.optNullableString("gatewayAddress"),
        subnet = item.optNullableString("subnet"),
        dnsServers = parseStrings(item.optJSONArray("dnsServers") ?: JSONArray()),
        ssid = item.optNullableString("ssid"),
        bssid = item.optNullableString("bssid"),
        wifiSecurity = item.optNullableString("wifiSecurity")
    )

    private fun parseDevices(array: JSONArray) = List(array.length()) { i ->
        val item = array.getJSONObject(i)
        DiscoveredDevice(requireText(item, "ipAddress"), item.optNullableString("hostname"), item.optBoolean("reachable"), item.optLong("discoveredAtEpochMs"))
    }

    private fun parseServices(array: JSONArray) = List(array.length()) { i ->
        val item = array.getJSONObject(i)
        DiscoveredService(requireText(item, "ipAddress"), item.getInt("port"), item.optString("protocol", "TCP"), requireText(item, "serviceName"), item.optBoolean("reachable"), item.optLong("discoveredAtEpochMs"))
    }

    private fun parseFindings(array: JSONArray) = List(array.length()) { i ->
        val item = array.getJSONObject(i)
        Finding(
            id = requireText(item, "id"),
            title = requireText(item, "title"),
            severity = enumValue(item.getString("severity"), FindingSeverity.values()),
            confidence = enumValue(item.getString("confidence"), FindingConfidence.values()),
            ipAddress = requireText(item, "ipAddress"),
            evidence = item.optString("evidence"),
            explanation = item.optString("explanation"),
            remediation = item.optString("remediation"),
            verification = item.optString("verification"),
            createdAtEpochMs = item.optLong("createdAtEpochMs")
        )
    }

    private fun parseRemediations(array: JSONArray) = List(array.length()) { i ->
        val item = array.getJSONObject(i)
        RemediationRecord(requireText(item, "findingId"), requireText(item, "ipAddress"), enumValue(item.getString("status"), RemediationStatus.values()), item.optLong("startedAtEpochMs"))
    }

    private fun parseVerifications(array: JSONArray) = List(array.length()) { i ->
        val item = array.getJSONObject(i)
        VerificationResult(requireText(item, "findingId"), requireText(item, "ipAddress"), enumValue(item.getString("status"), VerificationStatus.values()), item.optString("beforeEvidence"), item.optString("afterEvidence"), item.optLong("verifiedAtEpochMs"))
    }

    private fun parseCustomPolicies(array: JSONArray) = List(array.length()) { i ->
        val item = array.getJSONObject(i)
        val ruleType = enumValue(item.optString("ruleType", "INFORMATIONAL"), CustomPolicyRuleType.values())
        val port = if (item.has("port") && !item.isNull("port")) item.getInt("port") else null
        CustomPolicyEvaluation(
            policy = CustomPolicy(
                id = requireText(item, "id"),
                title = requireText(item, "title"),
                description = requireText(item, "description"),
                ruleType = ruleType,
                port = port,
                enabled = item.optBoolean("enabled", true)
            ),
            passed = item.optBoolean("passed"),
            evidence = item.optString("evidence")
        )
    }

    private fun parseStrings(array: JSONArray): List<String> = List(array.length()) { array.getString(it) }

    private fun requireText(json: JSONObject, key: String): String =
        json.optString(key).trim().also { require(it.isNotEmpty()) { "Missing required field: $key" } }

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private inline fun <reified T : Enum<T>> enumValue(value: String, values: Array<T>): T =
        values.firstOrNull { it.name == value } ?: error("Invalid enum value: $value")

    private companion object {
        const val MAX_SNAPSHOTS = 20
    }
}

data class AuditHistoryEntry(
    val id: String,
    val createdAtEpochMs: Long,
    val deviceCount: Int,
    val serviceCount: Int,
    val findingCount: Int
)
