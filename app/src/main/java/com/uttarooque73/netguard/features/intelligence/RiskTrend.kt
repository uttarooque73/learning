package com.uttarooque73.netguard.features.intelligence

import com.uttarooque73.netguard.audit.FindingSeverity
import com.uttarooque73.netguard.report.AuditSnapshot

data class RiskTrendPoint(val timestamp: Long, val score: Int, val findings: Int)

object RiskTrendCalculator {
    fun calculate(history: List<AuditSnapshot>): List<RiskTrendPoint> =
        history.sortedBy { it.createdAtEpochMs }.map { snapshot ->
            val deductions = snapshot.findings.fold(0) { total, finding ->
                total + when (finding.severity) {
                    FindingSeverity.CRITICAL -> 40
                    FindingSeverity.HIGH -> 25
                    FindingSeverity.MEDIUM -> 12
                    FindingSeverity.LOW -> 4
                }
            }
            RiskTrendPoint(
                snapshot.createdAtEpochMs,
                (100 - deductions).coerceIn(0, 100),
                snapshot.findings.size
            )
        }
}