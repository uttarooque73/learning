package com.uttarooque73.netguard.admin

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AdminStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_admin", Context.MODE_PRIVATE)

    fun saveProfiles(profiles: List<NetworkProfile>) {
        val json = JSONArray(profiles.map { JSONObject().put("id", it.id).put("name", it.name).put("ssid", it.ssid).put("subnet", it.subnet).put("notes", it.notes).put("createdAtEpochMs", it.createdAtEpochMs).put("updatedAtEpochMs", it.updatedAtEpochMs) })
        preferences.edit().putString("profiles", json.toString()).apply()
    }

    fun loadProfiles(): List<NetworkProfile> {
        val raw = preferences.getString("profiles", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { i ->
                val item = json.getJSONObject(i)
                NetworkProfile(item.getString("id"), item.getString("name"), item.optString("ssid").takeIf(String::isNotEmpty), item.optString("subnet").takeIf(String::isNotEmpty), item.optString("notes"), item.optLong("createdAtEpochMs"), item.optLong("updatedAtEpochMs"))
            }
        }.getOrElse { emptyList() }
    }

    fun saveAssets(assets: List<AssetMetadata>) {
        val json = JSONArray(assets.map { JSONObject().put("ipAddress", it.ipAddress).put("name", it.name).put("tags", JSONArray(it.tags)).put("notes", it.notes).put("updatedAtEpochMs", it.updatedAtEpochMs) })
        preferences.edit().putString("assets", json.toString()).apply()
    }

    fun loadAssets(): List<AssetMetadata> {
        val raw = preferences.getString("assets", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { i ->
                val item = json.getJSONObject(i)
                val tags = item.optJSONArray("tags")?.let { array -> List(array.length()) { array.getString(it) } } ?: emptyList()
                AssetMetadata(item.getString("ipAddress"), item.optString("name"), tags, item.optString("notes"), item.optLong("updatedAtEpochMs"))
            }
        }.getOrElse { emptyList() }
    }

    fun saveEvents(events: List<AdminEvent>) {
        val json = JSONArray(events.takeLast(200).map { JSONObject().put("id", it.id).put("type", it.type.name).put("subject", it.subject).put("detail", it.detail).put("createdAtEpochMs", it.createdAtEpochMs) })
        preferences.edit().putString("events", json.toString()).apply()
    }

    fun loadEvents(): List<AdminEvent> {
        val raw = preferences.getString("events", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { i ->
                val item = json.getJSONObject(i)
                AdminEvent(item.getString("id"), AdminEventType.valueOf(item.getString("type")), item.getString("subject"), item.getString("detail"), item.optLong("createdAtEpochMs"))
            }
        }.getOrElse { emptyList() }
    }
}