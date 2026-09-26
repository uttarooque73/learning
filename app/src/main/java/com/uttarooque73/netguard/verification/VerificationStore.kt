package com.uttarooque73.netguard.verification

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class VerificationStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_verification", Context.MODE_PRIVATE)

    fun save(results: List<VerificationResult>) {
        val json = JSONArray()
        results.forEach { result ->
            json.put(JSONObject().apply {
                put("findingId", result.findingId)
                put("ipAddress", result.ipAddress)
                put("status", result.status.name)
                put("beforeEvidence", result.beforeEvidence)
                put("afterEvidence", result.afterEvidence)
                put("verifiedAtEpochMs", result.verifiedAtEpochMs)
            })
        }
        preferences.edit().putString("results", json.toString()).apply()
    }

    fun load(): List<VerificationResult> {
        val raw = preferences.getString("results", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { index ->
                val item = json.getJSONObject(index)
                VerificationResult(
                    findingId = item.getString("findingId"),
                    ipAddress = item.getString("ipAddress"),
                    status = VerificationStatus.valueOf(item.getString("status")),
                    beforeEvidence = item.getString("beforeEvidence"),
                    afterEvidence = item.getString("afterEvidence"),
                    verifiedAtEpochMs = item.optLong("verifiedAtEpochMs")
                )
            }
        }.getOrElse { emptyList() }
    }
}
