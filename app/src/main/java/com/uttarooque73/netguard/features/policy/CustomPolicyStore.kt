package com.uttarooque73.netguard.features.policy

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class CustomPolicyStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_custom_policies", Context.MODE_PRIVATE)

    fun load(): List<CustomPolicy> {
        val raw = preferences.getString("policies", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                CustomPolicy(
                    id = item.getString("id"),
                    title = item.getString("title"),
                    description = item.getString("description"),
                    enabled = item.optBoolean("enabled", true)
                )
            }
        }.getOrElse { emptyList() }
    }

    fun save(policies: List<CustomPolicy>) {
        val array = JSONArray()
        policies.takeLast(100).forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("description", it.description)
                put("enabled", it.enabled)
            })
        }
        preferences.edit().putString("policies", array.toString()).apply()
    }

    fun upsert(policy: CustomPolicy) {
        val errors = CustomPolicyValidator.validate(policy)
        require(errors.isEmpty()) { errors.joinToString("; ") }
        save(load().filterNot { it.id == policy.id } + policy)
    }

    fun delete(id: String) {
        save(load().filterNot { it.id == id })
    }
}