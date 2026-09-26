package com.uttarooque73.netguard.audit

import org.junit.Assert.assertEquals
import org.junit.Test

class RiskCalculatorTest {
    @Test fun emptyFindingsHaveFullScore() = assertEquals(100, RiskCalculator.score(emptyList()))
    @Test fun highFindingDeductsTwentyFive() {
        val f = Finding("x", "x", FindingSeverity.HIGH, FindingConfidence.HIGH, "192.168.1.2", "e", "x", "r", "v")
        assertEquals(75, RiskCalculator.score(listOf(f)))
    }
    @Test fun scoreCannotGoBelowZero() {
        val f = Finding("x", "x", FindingSeverity.CRITICAL, FindingConfidence.HIGH, "192.168.1.2", "e", "x", "r", "v")
        assertEquals(0, RiskCalculator.score(List(4) { f }))
    }
}
