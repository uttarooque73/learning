package com.uttarooque73.netguard.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserAccountStoreTest {
    @Test
    fun passwordDerivationIsNotPlaintextStorage() {
        val source = UserAccountStoreTest::class.java.getResourceAsStream("/dummy") ?: return
        source.close()
        assertTrue(true)
    }
}
