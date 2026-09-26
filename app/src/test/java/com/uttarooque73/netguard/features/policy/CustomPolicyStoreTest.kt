package com.uttarooque73.netguard.features.policy

import org.junit.Assert.assertTrue
import org.junit.Test

class CustomPolicyStoreTest {
    @Test
    fun validatorRejectsIncompletePolicy() {
        val errors = CustomPolicyValidator.validate(CustomPolicy("", "", ""))
        assertTrue(errors.size >= 3)
    }
}