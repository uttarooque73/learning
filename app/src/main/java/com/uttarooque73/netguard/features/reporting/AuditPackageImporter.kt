package com.uttarooque73.netguard.features.reporting

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingConfidence
import com.uttarooque73.netguard.audit.FindingSeverity
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.remediation.RemediationRecord
import com.uttarooque73.netguard.remediation.RemediationStatus
import com.uttarooque73.netguard.report.AuditSnapshot
import com.uttarooque73.netguard.verification.VerificationResult
import com.uttarooque73.netguard.verification.VerificationStatus
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.zip.ZipInputStream

object AuditPackageImporter {
    private const val MAX_ENTRY_BYTES = 5L * 1024L * 1024L
    private const val MAX_TOTAL_BYTES = 10L * 1024L * 1024L
    private const val MAX_ITEMS = 10_000
    private const val MAX_TOTAL_ITEMS = 30_000
    private const val MAX_ENTRIES = 4

    fun readSnapshot(input: InputStream): AuditSnapshot {
        var totalBytes = 0L
        var entryCount = 0
        var auditJson: ByteArray? = null
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entryCount++
                require(entryCount <= MAX_ENTRIES) { "Audit package contains too many entries." }
                require(!entry.isDirectory) { "Audit package contains a directory entry." }
                require(entry.name == "audit.json" || entry.name == "findings.csv") { "Unsupported package entry: ${entry.name}" }
                if (entry.name == "audit.json") {
                    require(auditJson == null) { "Audit package contains duplicate audit.json entries." }
                    val bytes = readBounded(zip, MAX_ENTRY_BYTES) { totalBytes += it }
                    auditJson = bytes
                } else {
                    readBounded(zip, MAX_ENTRY_BYTES) { totalBytes += it }
                }
                require(totalBytes <= MAX_TOTAL_BYTES) { "Audit package exceeds the size limit." }
                zip.closeEntry()
            }
        }
        val json = JSONObject(String(requireNotNull(auditJson) { "audit.json not found" }, Charsets.UTF_8))
        require(json.optInt("schemaVersion", -1) == AdvancedReportExporter.SCHEMA_VERSION) { "Unsupported audit package schema." }
        val devices = parseDevices(json.optJSONArray("devices") ?: JSONArray())
        val services = parseServices(json.optJSONArray("services") ?: JSONArray())
        val findings = parseFindings(json.optJSONArray("findings") ?: JSONArray())
        val remediations = parseRemediations(json.optJSONArray("remediations") ?: JSONArray())
        val verifications = parseVerifications(json.optJSONArray("verifications") ?: JSONArray())
        val totalItems = devices.size + services.size + findings.size + remediations.size + verifications.size
        require(totalItems <= MAX_TOTAL_ITEMS) { "Audit package contains too many records." }
        return AuditSnapshot(
            id = requireText(json, "id"),
            createdAtEpochMs = json.getLong("createdAtEpochMs"),
            network = parseNetwork(json.optJSONObject("network")),
            devices = devices,
            services = services,
            findings = findings,
            remediationRecords = remediations,
            verificationResults = verifications
        )
    }

    fun readSummary(input: InputStream): ImportedAuditSummary {
        val snapshot = readSnapshot(input)
        return ImportedAuditSummary(snapshot.id, snapshot.createdAtEpochMs, snapshot.devices.size, snapshot.services.size, snapshot.findings.size)
    }

    private fun parseNetwork(json: JSONObject?): NetworkInfo? = json?.let {
        NetworkInfo(
            interfaceName = it.optNullableString("interfaceName"),
            localAddress = it.optNullableString("localAddress"),
            gatewayAddress = it.optNullableString("gatewayAddress"),
            subnet = it.optNullableString("subnet"),
            dnsServers = parseStrings(it.optJSONArray("dnsServers") ?: JSONArray(), MAX_ITEMS),
            ssid = it.optNullableString("ssid"),
            bssid = it.optNullableString("bssid"),
            wifiSecurity = it.optNullableString("wifiSecurity")
        )
    }

    private fun parseDevices(array: JSONArray): List<DiscoveredDevice> {
        require(array.length() <= MAX_ITEMS) { "Too many devices in audit package." }
        return List(array.length()) { i ->
            val item = array.getJSONObject(i)
            DiscoveredDevice(requireText(item, "ipAddress"), item.optNullableString("hostname"), item.optBoolean("reachable"), item.optLong("discoveredAtEpochMs"))
        }
    }

    private fun parseServices(array: JSONArray): List<DiscoveredService> {
        require(array.length() <= MAX_ITEMS) { "Too many services in audit package." }
        return List(array.length()) { i ->
            val item = array.getJSONObject(i)
            val port = item.getInt("port")
            require(port in 1..65535) { "Invalid service port." }
            DiscoveredService(requireText(item, "ipAddress"), port, item.optString("protocol", "TCP"), requireText(item, "serviceName"), item.optBoolean("reachable"), item.optLong("discoveredAtEpochMs"))
        }
    }

    private fun parseFindings(array: JSONArray): List<Finding> {
        require(array.length() <= MAX_ITEMS) { "Too many findings in audit package." }
        return List(array.length()) { i ->
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
    }

    private fun parseRemediations(array: JSONArray): List<RemediationRecord> {
        require(array.length() <= MAX_ITEMS) { "Too many remediation records in audit package." }
        return List(array.length()) { i ->
            val item = array.getJSONObject(i)
            RemediationRecord(requireText(item, "findingId"), requireText(item, "ipAddress"), enumValue(item.getString("status"), RemediationStatus.values()), item.optLong("startedAtEpochMs"))
        }
    }

    private fun parseVerifications(array: JSONArray): List<VerificationResult> {
        require(array.length() <= MAX_ITEMS) { "Too many verification records in audit package." }
        return List(array.length()) { i ->
            val item = array.getJSONObject(i)
            VerificationResult(requireText(item, "findingId"), requireText(item, "ipAddress"), enumValue(item.getString("status"), VerificationStatus.values()), item.optString("beforeEvidence"), item.optString("afterEvidence"), item.optLong("verifiedAtEpochMs"))
        }
    }

    private fun parseStrings(array: JSONArray, max: Int): List<String> {
        require(array.length() <= max) { "Too many values in audit package." }
        return List(array.length()) { array.getString(it) }
    }

    private fun requireText(json: JSONObject, key: String): String = json.optString(key).trim().also { require(it.isNotEmpty()) { "Missing required field: $key" } }

    private fun JSONObject.optNullableString(key: String): String? = if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private inline fun <reified T : Enum<T>> enumValue(value: String, values: Array<T>): T = values.firstOrNull { it.name == value } ?: error("Invalid enum value: $value")

    private fun readBounded(input: InputStream, limit: Long, onRead: (Long) -> Unit): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var count = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            count += read
            require(count <= limit) { "Audit package entry exceeds the size limit." }
            out.write(buffer, 0, read)
            onRead(read.toLong())
        }
        return out.toByteArray()
    }
}

data class ImportedAuditSummary(
    val id: String,
    val createdAtEpochMs: Long,
    val deviceCount: Int,
    val serviceCount: Int,
    val findingCount: Int
)