package com.uttarooque73.netguard.remediation

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class RemediationStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_remediation", Context.MODE_PRIVATE)

    fun save(records: List<RemediationRecord>) {
        val json = JSONArray()
        records.forEach { record ->
            json.put(JSONObject().apply {
                put("findingId", record.findingId)
                put("ipAddress", record.ipAddress)
                put("status", record.status.name)
                put("startedAtEpochMs", record.startedAtEpochMs)
            })
        }
        preferences.edit().putString("records", json.toString()).apply()
    }

    fun load(): List<RemediationRecord> {
        val raw = preferences.getString("records", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { i ->
                val item = json.getJSONObject(i)
                RemediationRecord(
                    findingId = item.getString("findingId"),
                    ipAddress = item.getString("ipAddress"),
                    status = RemediationStatus.valueOf(item.getString("status")),
                    startedAtEpochMs = item.optLong("startedAtEpochMs")
                )
            }
        }.getOrElse { emptyList() }
    }
}
