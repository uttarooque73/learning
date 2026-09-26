package com.uttarooque73.netguard.monitor

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class MonitorStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_monitor", Context.MODE_PRIVATE)

    fun save(events: List<MonitorEvent>) {
        val json = JSONArray()
        events.takeLast(100).forEach { event ->
            json.put(JSONObject().apply {
                put("id", event.id)
                put("type", event.type.name)
                put("ipAddress", event.ipAddress)
                put("detail", event.detail)
                put("createdAtEpochMs", event.createdAtEpochMs)
            })
        }
        preferences.edit().putString("events", json.toString()).apply()
    }

    fun load(): List<MonitorEvent> {
        val raw = preferences.getString("events", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { i ->
                val item = json.getJSONObject(i)
                MonitorEvent(item.getString("id"), MonitorEventType.valueOf(item.getString("type")), item.getString("ipAddress"), item.getString("detail"), item.optLong("createdAtEpochMs"))
            }
        }.getOrElse { emptyList() }
    }
}