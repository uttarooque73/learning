package com.uttarooque73.netguard.compliance

import com.uttarooque73.netguard.audit.DiscoveredService

enum class BaselineStatus { PASS, FAIL, REVIEW }

data class BaselineResult(
    val checkId: String,
    val title: String,
    val status: BaselineStatus,
    val evidence: String
)

object BaselineEvaluator {
    fun evaluate(baseline: SecurityBaseline, services: List<DiscoveredService>): List<BaselineResult> =
        baseline.checks.map { check ->
            when (check.type) {
                BaselineCheckType.NO_TELNET -> {
                    val found = services.any { it.port == 23 && it.protocol == "TCP" && it.reachable }
                    BaselineResult(check.id, check.title, if (found) BaselineStatus.FAIL else BaselineStatus.PASS, if (found) "TCP/23 is reachable." else "No reachable TCP/23 service observed.")
                }
                BaselineCheckType.NO_SMB -> {
                    val found = services.any { it.port == 445 && it.protocol == "TCP" && it.reachable }
                    BaselineResult(check.id, check.title, if (found) BaselineStatus.FAIL else BaselineStatus.PASS, if (found) "TCP/445 is reachable." else "No reachable TCP/445 service observed.")
                }
                BaselineCheckType.HTTPS_ONLY_FOR_WEB -> {
                    val http = services.any { it.port == 80 && it.protocol == "TCP" && it.reachable }
                    val https = services.any { it.port == 443 && it.protocol == "TCP" && it.reachable }
                    when {
                        !http -> BaselineResult(check.id, check.title, BaselineStatus.PASS, "No reachable TCP/80 service observed.")
                        https -> BaselineResult(check.id, check.title, BaselineStatus.REVIEW, "HTTP and HTTPS are both reachable; review whether HTTP redirects to HTTPS.")
                        else -> BaselineResult(check.id, check.title, BaselineStatus.FAIL, "HTTP is reachable without an observed HTTPS service.")
                    }
                }
            }
        }
}