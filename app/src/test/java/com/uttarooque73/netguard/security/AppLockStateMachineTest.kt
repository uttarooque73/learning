package com.uttarooque73.netguard.security

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLockStateMachineTest {
    @Test fun repeatedFailuresEnterCooldown() {
        var session = AppLockStateMachine.lock()
        repeat(AppLockStateMachine.MAX_FAILED_ATTEMPTS) {
            session = AppLockStateMachine.biometricFailure(session, 1_000L + it)
        }
        assertEquals(AppLockState.COOLDOWN, session.state)
    }

    @Test fun backgroundLocksWhenPolicyRequiresIt() {
        val policy = AppLockPolicy(enabled = true, lockOnBackground = true)
        val session = AppLockStateMachine.onBackground(AppLockSession(), policy)
        assertEquals(AppLockState.LOCKED, session.state)
    }

    @Test fun cooldownCannotBeBypassedBeforeExpiry() {
        var session = AppLockStateMachine.lock()
        repeat(AppLockStateMachine.MAX_FAILED_ATTEMPTS) {
            session = AppLockStateMachine.biometricFailure(session, 1_000L + it)
        }
        val blocked = AppLockStateMachine.unlock(session, 2_000L)
        assertEquals(AppLockState.COOLDOWN, blocked.state)
    }
}