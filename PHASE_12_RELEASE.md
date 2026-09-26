# Phase 12 — Security, Performance & Release

## Security hardening
- Disable Android application backup for the MVP.
- Disable cleartext network traffic by default.
- Keep remediation guided; the app does not directly modify remote devices.
- Do not collect credentials or authentication secrets.
- Keep monitoring user-triggered rather than background activity by default.

## Privacy
Current MVP data is local-first. Stored categories include network inventory, service reachability, findings, remediation state, verification results, audit history, monitoring events, and administrative metadata.

## Performance
Discovery and service auditing already use bounded concurrency and short connection timeouts. Future background monitoring must use explicit scheduling and battery-aware Android APIs.

## Release checklist
- Review runtime permissions on each supported Android release.
- Verify backup behavior on release builds.
- Verify cleartext policy against every intended HTTP/TLS code path.
- Run unit tests and an Android release build in CI.
- Perform manual testing on physical devices across supported Android versions.
- Review logs to ensure sensitive data is not emitted.
- Validate report/history storage size and retention limits.

## Current boundary
NetGuard remains a defensive, authorized network-audit assistant. Phase 12 hardening does not add credential collection, exploit execution, brute force, stealth/evasion, access-control bypass, or remote device administration.
