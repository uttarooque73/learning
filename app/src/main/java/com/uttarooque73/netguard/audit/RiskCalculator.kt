package com.uttarooque73.netguard.audit

object RiskCalculator {
    fun breakdown(findings: List<Finding>): Map<FindingSeverity, Int> =
        FindingSeverity.values().associateWith { severity ->
            findings.count { it.severity == severity } * when (severity) {
                FindingSeverity.CRITICAL -> 40
                FindingSeverity.HIGH -> 25
                FindingSeverity.MEDIUM -> 12
                FindingSeverity.LOW -> 4
            }
        }

    fun score(findings: List<Finding>): Int {
        val deductions = findings.fold(0) { total, finding ->
            total + when (finding.severity) {
                FindingSeverity.CRITICAL -> 40
                FindingSeverity.HIGH -> 25
                FindingSeverity.MEDIUM -> 12
                FindingSeverity.LOW -> 4
            }
        }
        return (100 - deductions).coerceIn(0, 100)
    }
}
