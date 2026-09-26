package com.uttarooque73.netguard.features.timeline

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SecurityTimelineStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        "netguard_security_timeline",
        Context.MODE_PRIVATE
    )

    fun save(events: List<SecurityTimelineEvent>) {
        val json = JSONArray()
        events.takeLast(200).forEach {
            json.put(JSONObject().apply {
                put("id", it.id)
                put("category", it.category)
                put("title", it.title)
                put("detail", it.detail)
                put("createdAtEpochMs", it.createdAtEpochMs)
            })
        }
        preferences.edit().putString("events", json.toString()).apply()
    }

    fun load(): List<SecurityTimelineEvent> {
        val raw = preferences.getString("events", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { index ->
                val item = json.getJSONObject(index)
                SecurityTimelineEvent(
                    id = item.getString("id"),
                    category = item.getString("category"),
                    title = item.getString("title"),
                    detail = item.getString("detail"),
                    createdAtEpochMs = item.optLong("createdAtEpochMs")
                )
            }
        }.getOrElse { emptyList() }
    }
}
