package com.uttarooque73.netguard.features.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LocalModelResult(val success: Boolean, val text: String)

object LocalOllamaClient {
    suspend fun generate(endpoint: String, model: String, prompt: String): LocalModelResult = withContext(Dispatchers.IO) {
        runCatching {
            val base = endpoint.trimEnd('/')
            val connection = URL(base + "/api/generate").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 3000
            connection.readTimeout = 30000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            val body = JSONObject()
                .put("model", model)
                .put("prompt", prompt)
                .put("stream", false)
                .toString()
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val response = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val payload = response?.bufferedReader()?.use { it.readText() }.orEmpty()
            connection.disconnect()
            if (payload.isBlank()) error("Local model returned an empty response.")
            val json = JSONObject(payload)
            if (json.has("error")) error(json.optString("error"))
            LocalModelResult(true, json.optString("response").ifBlank { "Local model returned no response text." })
        }.getOrElse {
            LocalModelResult(false, it.message ?: "Unable to reach the local Ollama endpoint.")
        }
    }
}
