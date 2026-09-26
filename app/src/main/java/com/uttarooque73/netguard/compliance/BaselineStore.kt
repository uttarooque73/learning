package com.uttarooque73.netguard.compliance

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class BaselineStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_baseline", Context.MODE_PRIVATE)

    fun save(baseline: SecurityBaseline) {
        val json = JSONObject().apply {
            put("id", baseline.id)
            put("name", baseline.name)
            put("checks", JSONArray(baseline.checks.map { JSONObject().put("id", it.id).put("type", it.type.name).put("title", it.title).put("description", it.description) }))
        }
        preferences.edit().putString("baseline", json.toString()).apply()
    }

    fun load(): SecurityBaseline? {
        val raw = preferences.getString("baseline", null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            val checks = json.getJSONArray("checks")
            SecurityBaseline(json.getString("id"), json.getString("name"), List(checks.length()) { i ->
                val item = checks.getJSONObject(i)
                BaselineCheck(item.getString("id"), BaselineCheckType.valueOf(item.getString("type")), item.getString("title"), item.getString("description"))
            })
        }.getOrNull()
    }
}