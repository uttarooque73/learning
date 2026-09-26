# Phase 7 — Verification & Before/After

NetGuard verification is intentionally limited to defensive rechecks of services already represented by a finding.

## Workflow

1. A finding is created from an observed service.
2. The user performs the recommended remediation outside NetGuard.
3. The user selects Verify now.
4. NetGuard rechecks the specific finding's known service on the affected IP.
5. NetGuard stores before/after evidence and a verification status.

## Statuses

- FIXED — the previously observed service is no longer reachable.
- STILL_PRESENT — the service remains reachable.
- CHANGED — the current state differs from the expected baseline in a way that needs review.
- UNABLE_TO_VERIFY — NetGuard has no verification rule or the recheck could not be completed.

## Safety boundary

Verification does not exploit services, attempt authentication, brute-force credentials, bypass access controls, or modify remote systems. It performs a bounded TCP reachability recheck against an already identified service.

Only use NetGuard against networks and devices you own or are explicitly authorized to assess.
