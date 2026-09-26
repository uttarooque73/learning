package com.uttarooque73.netguard.audit

object RiskCalculator {
    fun score(findings: List<Finding>): Int {
        val deductions = findings.sumOf { when (it.severity) {
            FindingSeverity.CRITICAL -> 40
            FindingSeverity.HIGH -> 25
            FindingSeverity.MEDIUM -> 12
            FindingSeverity.LOW -> 4
        } }
        return (100 - deductions).coerceIn(0, 100)
    }
}
