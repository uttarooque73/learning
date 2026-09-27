package com.uttarooque73.netguard.security

import android.content.Context
import android.provider.ContactsContract
import java.util.UUID

data class SavedContact(
    val id: String,
    val name: String,
    val phoneNumber: String
)

class UserContactStore(context: Context) {
    private val prefs = context.getSharedPreferences("netguard_contacts", Context.MODE_PRIVATE)
    private val key = "items"

    fun load(): List<SavedContact> {
        return prefs.getStringSet(key, emptySet()).orEmpty().mapNotNull { encoded ->
            val parts = encoded.split("|", limit = 3)
            if (parts.size == 3) SavedContact(parts[0], parts[1], parts[2]) else null
        }.sortedBy { it.name.lowercase() }
    }

    fun add(name: String, phoneNumber: String): SavedContact {
        val contact = SavedContact(UUID.randomUUID().toString(), name.trim(), phoneNumber.trim())
        val values = load().map { it.id + "|" + it.name + "|" + it.phoneNumber }.toMutableSet()
        values.add(contact.id + "|" + contact.name + "|" + contact.phoneNumber)
        prefs.edit().putStringSet(key, values).apply()
        return contact
    }

    fun remove(id: String) {
        prefs.edit().putStringSet(
            key,
            load().filterNot { it.id == id }.map { it.id + "|" + it.name + "|" + it.phoneNumber }.toSet()
        ).apply()
    }

    companion object {
        fun readPhoneContacts(context: Context): List<SavedContact> {
            val result = mutableListOf<SavedContact>()
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val number = cursor.getString(numberIndex)?.trim().orEmpty()
                    if (number.isNotEmpty()) {
                        result += SavedContact(
                            cursor.getString(idIndex) ?: UUID.randomUUID().toString(),
                            cursor.getString(nameIndex)?.trim().orEmpty().ifBlank { "Unknown contact" },
                            number
                        )
                    }
                }
            }
            return result.distinctBy { it.name.lowercase() + "|" + it.phoneNumber }
        }
    }
}
