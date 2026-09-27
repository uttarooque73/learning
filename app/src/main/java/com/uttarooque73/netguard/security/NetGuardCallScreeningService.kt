package com.uttarooque73.netguard.security

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService

class NetGuardCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart.orEmpty()
        val incoming = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            callDetails.callDirection == Call.Details.DIRECTION_INCOMING

        // CallScreeningService can observe outgoing calls, but disallow/reject responses
        // only apply to incoming calls. Keep outgoing calls allowed and record them.
        val blocked = incoming && number.isNotBlank() && CallProtectionStore(this).isBlocked(number)
        val store = CallProtectionStore(this)

        store.addLog(
            number.ifBlank { "Unknown number" },
            findContactName(number),
            if (incoming) "Incoming" else "Outgoing",
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
        return UserContactStore(this).findSavedByNumber(number)?.name
    }
}
