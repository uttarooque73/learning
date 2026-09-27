package com.uttarooque73.netguard.security

import android.content.Context
import java.util.UUID

data class CallProtectionLog(
    val id: String, val number: String, val contactName: String?,
    val direction: String, val blocked: Boolean, val timestamp: Long
)

class CallProtectionStore(context: Context) {
    private val prefs = context.getSharedPreferences("netguard_call_protection", Context.MODE_PRIVATE)
    private val blockedKey = "blocked_numbers"
    private val logsKey = "call_logs"

    fun blockedNumbers(): Set<String> = prefs.getStringSet(blockedKey, emptySet()).orEmpty()
    fun isBlocked(number: String): Boolean = normalize(number) in blockedNumbers()

    fun block(number: String) {
        val updated = blockedNumbers().toMutableSet()
        updated.add(normalize(number))
        prefs.edit().putStringSet(blockedKey, updated).apply()
    }

    fun unblock(number: String) {
        val updated = blockedNumbers().toMutableSet()
        updated.remove(normalize(number))
        prefs.edit().putStringSet(blockedKey, updated).apply()
    }

    fun addLog(number: String, contactName: String?, direction: String, blocked: Boolean) {
        val entry = listOf(UUID.randomUUID(), number, contactName.orEmpty(), direction, blocked, System.currentTimeMillis()).joinToString("|")
        val logs = prefs.getStringSet(logsKey, emptySet()).orEmpty().toMutableSet()
        logs.add(entry)
        while (logs.size > 200) logs.remove(logs.minByOrNull { it.substringAfterLast("|").toLongOrNull() ?: Long.MAX_VALUE })
        prefs.edit().putStringSet(logsKey, logs).apply()
    }

    fun logs(): List<CallProtectionLog> = prefs.getStringSet(logsKey, emptySet()).orEmpty().mapNotNull {
        val p = it.split("|", limit = 6)
        if (p.size != 6) null else CallProtectionLog(p[0], p[1], p[2].ifBlank { null }, p[3], p[4].toBoolean(), p[5].toLongOrNull() ?: return@mapNotNull null)
    }.sortedByDescending { it.timestamp }

    fun clearLogs() { prefs.edit().remove(logsKey).apply() }

    companion object {
        fun normalize(number: String): String = number.filter { it.isDigit() }.takeLast(15)
    }
}
