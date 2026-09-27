package com.uttarooque73.netguard.security

import android.telecom.Call
import android.telecom.CallScreeningService

class NetGuardCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart.orEmpty()
        val store = CallProtectionStore(this)
        val blocked = number.isNotBlank() && store.isBlocked(number)

        store.addLog(
            number.ifBlank { "Unknown number" },
            findContactName(number),
            if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) "Incoming" else "Outgoing",
            blocked
        )

        respondToCall(
            callDetails,
            CallScreeningService.CallResponse.Builder()
                .setDisallowCall(blocked)
                .setRejectCall(blocked)
                .build()
        )
    }

    private fun findContactName(number: String): String? {
        if (number.isBlank()) return null
        return UserContactStore.readPhoneContacts(this)
            .firstOrNull { CallProtectionStore.normalize(it.phoneNumber) == CallProtectionStore.normalize(number) }
            ?.name
    }
}
