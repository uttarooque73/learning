package com.uttarooque73.netguard.features.reporting

import android.content.Context
import com.uttarooque73.netguard.features.command.InvestigationDiff
import com.uttarooque73.netguard.report.AuditSnapshot
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object InvestigationPackageExporter {
    const val SCHEMA_VERSION = 1
    const val MAX_SNAPSHOTS = 3

    fun export(
        context: Context,
        snapshots: List<AuditSnapshot>,
        diff: InvestigationDiff?,
        timeline: List<String>
    ): File {
        require(snapshots.isNotEmpty()) { "At least one snapshot is required." }
        require(snapshots.size <= MAX_SNAPSHOTS) { "Investigation packages are limited to " + MAX_SNAPSHOTS + " snapshots." }
        val id = snapshots.last().id
        val file = File(context.cacheDir, "netguard-investigation-" + id + ".zip")
        ZipOutputStream(file.outputStream().buffered()).use { zip ->
            fun text(name: String, value: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(value.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            text("manifest.json", JSONObject().apply {
                put("schemaVersion", SCHEMA_VERSION)
                put("snapshotCount", snapshots.size)
                put("snapshotIds", JSONArray(snapshots.map { it.id }))
                put("hasDiff", diff != null)
            }.toString())
            snapshots.forEachIndexed { index, snapshot ->
                text("snapshots/" + index + "-" + snapshot.id + ".json", AdvancedReportExporter.json(snapshot))
            }
            diff?.let { d ->
                text("diff.json", JSONObject().apply {
                    put("beforeId", d.beforeId)
                    put("afterId", d.afterId)
                    put("addedDevices", JSONArray(d.addedDevices))
                    put("removedDevices", JSONArray(d.removedDevices))
                    put("addedServices", JSONArray(d.addedServices))
                    put("removedServices", JSONArray(d.removedServices))
                    put("newFindings", JSONArray(d.newFindings))
                    put("resolvedFindings", JSONArray(d.resolvedFindings))
                    put("remediationChanges", JSONArray(d.remediationChanges))
                    put("riskBefore", d.riskBefore)
                    put("riskAfter", d.riskAfter)
                }.toString())
            }
            text("timeline.txt", timeline.takeLast(200).joinToString("\n"))
            text("README.txt", "NetGuard investigation package schema " + SCHEMA_VERSION + ". Local evidence bundle; no credentials or exploit data.")
        }
        return file
    }
}
