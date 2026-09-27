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

    @Test fun cooldownExpiresAtBoundaryAndSuccessfulUnlockResetsSession() {
        var session = AppLockStateMachine.lock()
        repeat(AppLockStateMachine.MAX_FAILED_ATTEMPTS) {
            session = AppLockStateMachine.biometricFailure(session, 1_000L + it)
        }
        val unlocked = AppLockStateMachine.unlock(session, session.cooldownUntilEpochMs)
        assertEquals(AppLockState.UNLOCKED, unlocked.state)
        assertEquals(0, unlocked.failedAttempts)
        assertEquals(0L, unlocked.cooldownUntilEpochMs)
    }

    @Test fun failedAttemptDuringCooldownDoesNotExtendCooldown() {
        var session = AppLockStateMachine.lock()
        repeat(AppLockStateMachine.MAX_FAILED_ATTEMPTS) {
            session = AppLockStateMachine.biometricFailure(session, 1_000L + it)
        }
        val cooldownUntil = session.cooldownUntilEpochMs
        val blocked = AppLockStateMachine.biometricFailure(session, cooldownUntil - 1L)
        assertEquals(AppLockState.COOLDOWN, blocked.state)
        assertEquals(cooldownUntil, blocked.cooldownUntilEpochMs)
    }

    @Test fun cooldownHelpersReportOnlyActiveRemainingTime() {
        var session = AppLockStateMachine.lock()
        repeat(AppLockStateMachine.MAX_FAILED_ATTEMPTS) {
            session = AppLockStateMachine.biometricFailure(session, 1_000L + it)
        }
        assertEquals(true, AppLockStateMachine.isCooldownActive(session, session.cooldownUntilEpochMs - 1L))
        assertEquals(1L, AppLockStateMachine.remainingCooldownMs(session, session.cooldownUntilEpochMs - 1L))
        assertEquals(false, AppLockStateMachine.isCooldownActive(session, session.cooldownUntilEpochMs))
        assertEquals(0L, AppLockStateMachine.remainingCooldownMs(session, session.cooldownUntilEpochMs))
    }