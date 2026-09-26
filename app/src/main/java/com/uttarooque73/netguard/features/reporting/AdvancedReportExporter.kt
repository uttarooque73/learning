package com.uttarooque73.netguard.features.reporting

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.uttarooque73.netguard.report.AuditSnapshot
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object AdvancedReportExporter {
    const val SCHEMA_VERSION = 2

    fun json(snapshot: AuditSnapshot): String = snapshotJson(snapshot).toString()

    private fun snapshotJson(snapshot: AuditSnapshot): JSONObject = JSONObject().apply {
        put("schemaVersion", SCHEMA_VERSION)
        put("id", snapshot.id)
        put("createdAtEpochMs", snapshot.createdAtEpochMs)
        put("network", snapshot.network?.let { network -> JSONObject().apply {
            put("interfaceName", network.interfaceName)
            put("localAddress", network.localAddress)
            put("gatewayAddress", network.gatewayAddress)
            put("subnet", network.subnet)
            put("dnsServers", JSONArray(network.dnsServers))
            put("ssid", network.ssid)
            put("bssid", network.bssid)
            put("wifiSecurity", network.wifiSecurity)
        } })
        put("devices", JSONArray(snapshot.devices.map { device -> JSONObject().apply {
            put("ipAddress", device.ipAddress)
            put("hostname", device.hostname)
            put("reachable", device.reachable)
            put("discoveredAtEpochMs", device.discoveredAtEpochMs)
        } }))
        put("services", JSONArray(snapshot.services.map { service -> JSONObject().apply {
            put("ipAddress", service.ipAddress)
            put("port", service.port)
            put("protocol", service.protocol)
            put("serviceName", service.serviceName)
            put("reachable", service.reachable)
            put("discoveredAtEpochMs", service.discoveredAtEpochMs)
        } }))
        put("findings", JSONArray(snapshot.findings.map { finding -> JSONObject().apply {
            put("id", finding.id)
            put("title", finding.title)
            put("severity", finding.severity.name)
            put("confidence", finding.confidence.name)
            put("ipAddress", finding.ipAddress)
            put("evidence", finding.evidence)
            put("explanation", finding.explanation)
            put("remediation", finding.remediation)
            put("verification", finding.verification)
            put("createdAtEpochMs", finding.createdAtEpochMs)
        } }))
        put("remediations", JSONArray(snapshot.remediationRecords.map { record -> JSONObject().apply {
            put("findingId", record.findingId)
            put("ipAddress", record.ipAddress)
            put("status", record.status.name)
            put("startedAtEpochMs", record.startedAtEpochMs)
        } }))
        put("verifications", JSONArray(snapshot.verificationResults.map { result -> JSONObject().apply {
            put("findingId", result.findingId)
            put("ipAddress", result.ipAddress)
            put("status", result.status.name)
            put("beforeEvidence", result.beforeEvidence)
            put("afterEvidence", result.afterEvidence)
            put("verifiedAtEpochMs", result.verifiedAtEpochMs)
        } }))
        put("customPolicyEvaluations", JSONArray(snapshot.customPolicyEvaluations.map { evaluation -> JSONObject().apply {
            put("id", evaluation.policy.id)
            put("title", evaluation.policy.title)
            put("description", evaluation.policy.description)
            put("ruleType", evaluation.policy.ruleType.name)
            evaluation.policy.port?.let { put("port", it) }
            put("enabled", evaluation.policy.enabled)
            put("passed", evaluation.passed)
            put("evidence", evaluation.evidence)
        } }))
    }

    fun csv(snapshot: AuditSnapshot): String = buildString {
        appendLine("finding_id,title,severity,confidence,ip_address,evidence")
        snapshot.findings.forEach {
            appendLine(listOf(it.id, it.title, it.severity.name, it.confidence.name, it.ipAddress, it.evidence).joinToString(",") { value ->
                "\"" + value.replace("\"", "\"\"") + "\""
            })
        }
    }

    fun pdf(context: Context, snapshot: AuditSnapshot): File {
        val file = File(context.cacheDir, "netguard-audit-" + snapshot.id + ".pdf")
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val paint = Paint().apply { textSize = 12f }
        var pageNumber = 1
        var y = 40f
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        fun line(value: String) {
            if (y > pageHeight - 40) {
                document.finishPage(page)
                pageNumber++
                y = 40f
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            }
            page.canvas.drawText(value.take(95), 32f, y, paint)
            y += 18f
        }
        line("NetGuard Security Audit")
        line("Audit: " + snapshot.id)
        line("Devices: " + snapshot.devices.size + " Services: " + snapshot.services.size)
        line("Findings: " + snapshot.findings.size)
        snapshot.findings.forEach {
            line(it.severity.name + ": " + it.title)
            line("Asset: " + it.ipAddress)
            line("Evidence: " + it.evidence)
        }
        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    fun packageAudit(context: Context, snapshot: AuditSnapshot): File {
        val file = File(context.cacheDir, "netguard-audit-" + snapshot.id + ".zip")
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("audit.json"))
            zip.write(json(snapshot).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("findings.csv"))
            zip.write(csv(snapshot).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        return file
    }
}