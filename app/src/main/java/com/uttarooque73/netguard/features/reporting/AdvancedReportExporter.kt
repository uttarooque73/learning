package com.uttarooque73.netguard.features.reporting

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.uttarooque73.netguard.report.AuditSnapshot
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object AdvancedReportExporter {
    fun json(snapshot: AuditSnapshot): String = buildString {
        append("{")
        append("\"id\":\"").append(escape(snapshot.id)).append("\",")
        append("\"createdAtEpochMs\":").append(snapshot.createdAtEpochMs).append(",")
        append("\"deviceCount\":").append(snapshot.devices.size).append(",")
        append("\"serviceCount\":").append(snapshot.services.size).append(",")
        append("\"findingCount\":").append(snapshot.findings.size).append(",")
        append("\"findings\":[")
        snapshot.findings.forEachIndexed { index, finding ->
            if (index > 0) append(",")
            append("{\"id\":\"").append(escape(finding.id)).append("\",")
            append("\"title\":\"").append(escape(finding.title)).append("\",")
            append("\"severity\":\"").append(finding.severity.name).append("\",")
            append("\"ipAddress\":\"").append(escape(finding.ipAddress)).append("\"}")
        }
        append("]}")
    }

    fun csv(snapshot: AuditSnapshot): String = buildString {
        appendLine("finding_id,title,severity,confidence,ip_address,evidence")
        snapshot.findings.forEach {
            appendLine(listOf(it.id,it.title,it.severity.name,it.confidence.name,it.ipAddress,it.evidence).joinToString(",") { value ->
                "\"" + value.replace("\"","\"\"") + "\""
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
            zip.write(json(snapshot).toByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("findings.csv"))
            zip.write(csv(snapshot).toByteArray())
            zip.closeEntry()
        }
        return file
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}