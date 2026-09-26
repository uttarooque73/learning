package com.uttarooque73.netguard.audit

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class FindingStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_findings", Context.MODE_PRIVATE)
    fun save(findings: List<Finding>) {
        val json = JSONArray()
        findings.forEach { f -> json.put(JSONObject().apply {
            put("id", f.id); put("title", f.title); put("severity", f.severity.name); put("confidence", f.confidence.name)
            put("ipAddress", f.ipAddress); put("evidence", f.evidence); put("explanation", f.explanation)
            put("remediation", f.remediation); put("verification", f.verification); put("createdAtEpochMs", f.createdAtEpochMs)
        }) }
        preferences.edit().putString("findings", json.toString()).apply()
    }
    fun load(): List<Finding> {
        val raw = preferences.getString("findings", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { i -> val x=json.getJSONObject(i); Finding(x.getString("id"),x.getString("title"),FindingSeverity.valueOf(x.getString("severity")),FindingConfidence.valueOf(x.getString("confidence")),x.getString("ipAddress"),x.getString("evidence"),x.getString("explanation"),x.getString("remediation"),x.getString("verification"),x.optLong("createdAtEpochMs")) }
        }.getOrElse { emptyList() }
    }
}
