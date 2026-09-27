package com.uttarooque73.netguard.security

enum class AppLockState { UNLOCKED, LOCKED, COOLDOWN }

data class AppLockSession(
    val state: AppLockState = AppLockState.UNLOCKED,
    val failedAttempts: Int = 0,
    val cooldownUntilEpochMs: Long = 0L
)

object AppLockStateMachine {
    const val MAX_FAILED_ATTEMPTS = 5
    const val COOLDOWN_MS = 30_000L

    fun lock(session: AppLockSession = AppLockSession()): AppLockSession =
        session.copy(state = AppLockState.LOCKED)

    fun unlock(session: AppLockSession, nowEpochMs: Long): AppLockSession {
        if (session.state == AppLockState.COOLDOWN && nowEpochMs < session.cooldownUntilEpochMs) return session
        return AppLockSession(AppLockState.UNLOCKED)
    }

    fun isCooldownActive(session: AppLockSession, nowEpochMs: Long): Boolean =
        session.state == AppLockState.COOLDOWN && nowEpochMs < session.cooldownUntilEpochMs

    fun remainingCooldownMs(session: AppLockSession, nowEpochMs: Long): Long =
        if (isCooldownActive(session, nowEpochMs)) session.cooldownUntilEpochMs - nowEpochMs else 0L

    fun biometricFailure(session: AppLockSession, nowEpochMs: Long): AppLockSession {
        if (session.state == AppLockState.COOLDOWN && nowEpochMs < session.cooldownUntilEpochMs) return session
        val attempts = session.failedAttempts + 1
        return if (attempts >= MAX_FAILED_ATTEMPTS) {
            session.copy(
                state = AppLockState.COOLDOWN,
                failedAttempts = attempts,
                cooldownUntilEpochMs = nowEpochMs + COOLDOWN_MS
            )
        } else {
            session.copy(state = AppLockState.LOCKED, failedAttempts = attempts)
        }
    }

    fun onBackground(session: AppLockSession, policy: AppLockPolicy): AppLockSession =
        if (policy.enabled && policy.lockOnBackground) lock(session) else session
}
