package com.uttarooque73.netguard.features.reporting

import org.json.JSONObject
import java.io.InputStream
import java.util.zip.ZipInputStream

data class ImportedAuditSummary(
    val id: String,
    val createdAtEpochMs: Long,
    val deviceCount: Int,
    val serviceCount: Int,
    val findingCount: Int
)

object AuditPackageImporter {
    fun readSummary(input: InputStream): ImportedAuditSummary {
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == "audit.json") {
                    val json = JSONObject(zip.readBytes().toString(Charsets.UTF_8))
                    return ImportedAuditSummary(
                        id = json.getString("id"),
                        createdAtEpochMs = json.getLong("createdAtEpochMs"),
                        deviceCount = json.optInt("deviceCount"),
                        serviceCount = json.optInt("serviceCount"),
                        findingCount = json.optInt("findingCount")
                    )
                }
            }
        }
        error("audit.json not found")
    }
}