package com.uttarooque73.netguard.network

import android.net.wifi.WifiInfo

object WifiSecurityClassifier {
    fun classify(securityType: Int): String = when (securityType) {
        WifiInfo.SECURITY_TYPE_OPEN -> "Open"
        WifiInfo.SECURITY_TYPE_WEP -> "WEP"
        WifiInfo.SECURITY_TYPE_PSK -> "WPA2-PSK"
        WifiInfo.SECURITY_TYPE_SAE -> "WPA3-SAE"
        WifiInfo.SECURITY_TYPE_EAP -> "WPA2-Enterprise"
        WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE -> "WPA3-Enterprise"
        WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE_192_BIT -> "WPA3-Enterprise 192-bit"
        WifiInfo.SECURITY_TYPE_OWE -> "OWE"
        WifiInfo.SECURITY_TYPE_WAPI_PSK -> "WAPI-PSK"
        WifiInfo.SECURITY_TYPE_WAPI_CERT -> "WAPI-Certificate"
        WifiInfo.SECURITY_TYPE_PASSPOINT_R1_R2 -> "Passpoint R1/R2"
        WifiInfo.SECURITY_TYPE_PASSPOINT_R3 -> "Passpoint R3"
        WifiInfo.SECURITY_TYPE_DPP -> "DPP"
        else -> "Unknown"
    }
}
