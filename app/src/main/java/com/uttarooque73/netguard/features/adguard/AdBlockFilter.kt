package com.uttarooque73.netguard.features.adguard

/**
 * Pure, deterministic domain filter used by the VPN DNS path.
 *
 * A DNS-only filter can block known ad/tracker endpoints, but it does not
 * remove first-party/in-page ads or encrypted/direct-IP traffic that bypasses
 * DNS inspection.
 */
object AdBlockFilter {
    fun isBlocked(host: String, rules: List<AdBlockRule>): Boolean {
        return rules.any { it.enabled && AdBlockCatalog.matches(host, it) }
    }
}
