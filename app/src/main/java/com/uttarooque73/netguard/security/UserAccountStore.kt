package com.uttarooque73.netguard.security

import android.content.Context
import android.net.Uri
import java.io.File
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class UserProfile(
    val email: String,
    val displayName: String,
    val imagePath: String?
)

class UserAccountStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("netguard_user_account", Context.MODE_PRIVATE)
    private val random = SecureRandom()
    private val iterations = 120_000
    private val keyLength = 256

    fun exists(): Boolean = prefs.getBoolean("exists", false)

    fun isGuest(): Boolean = prefs.getBoolean("guest", false)

    fun enterGuestMode() { prefs.edit().putBoolean("guest", true).putBoolean("loggedIn", false).apply() }

    fun exitGuestMode() { prefs.edit().putBoolean("guest", false).apply() }

    fun create(email: String, password: CharArray, displayName: String): Result<UserProfile> {
        val normalized = email.trim().lowercase()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(normalized).matches()) return Result.failure(IllegalArgumentException("Enter a valid email address."))
        if (password.size < 8) return Result.failure(IllegalArgumentException("Password must be at least 8 characters."))
        if (displayName.trim().length < 2) return Result.failure(IllegalArgumentException("Enter a display name."))
        if (exists()) return Result.failure(IllegalStateException("An account already exists on this device."))
        val salt = ByteArray(16).also(random::nextBytes)
        val hash = hash(password, salt)
        prefs.edit()
            .putBoolean("exists", true)
            .putString("email", normalized)
            .putString("displayName", displayName.trim())
            .putString("salt", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            .putString("hash", android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP))
            .putBoolean("loggedIn", true)
            .putBoolean("guest", false)
            .apply()
        return Result.success(loadProfile()!!)
    }

    fun login(email: String, password: CharArray): Result<UserProfile> {
        val normalized = email.trim().lowercase()
        if (!exists()) return Result.failure(IllegalStateException("No account exists on this device."))
        if (normalized != prefs.getString("email", null)) return Result.failure(IllegalArgumentException("Invalid email or password."))
        val salt = android.util.Base64.decode(prefs.getString("salt", "") ?: "", android.util.Base64.DEFAULT)
        val expected = android.util.Base64.decode(prefs.getString("hash", "") ?: "", android.util.Base64.DEFAULT)
        val actual = hash(password, salt)
        if (!java.security.MessageDigest.isEqual(expected, actual)) return Result.failure(IllegalArgumentException("Invalid email or password."))
        prefs.edit().putBoolean("loggedIn", true).apply()
        return Result.success(loadProfile()!!)
    }

    fun logout() { prefs.edit().putBoolean("loggedIn", false).putBoolean("guest", false).apply() }
    fun isLoggedIn(): Boolean = exists() && prefs.getBoolean("loggedIn", false)

    fun updateProfile(displayName: String, imagePath: String?): UserProfile {
        prefs.edit().putString("displayName", displayName.trim()).putString("imagePath", imagePath).apply()
        return loadProfile()!!
    }

    fun loadProfile(): UserProfile? {
        if (!exists()) return null
        return UserProfile(
            prefs.getString("email", "") ?: "",
            prefs.getString("displayName", "") ?: "",
            prefs.getString("imagePath", null)
        )
    }

    fun saveProfileImage(uri: Uri): String {
        val dir = File(context.filesDir, "profile").apply { mkdirs() }
        val target = File(dir, "avatar.jpg")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to read selected image." }
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target.absolutePath
    }

    fun deleteProfileImage(path: String?) {
        path?.let { runCatching { File(it).delete() } }
    }

    private fun hash(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, keyLength)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
