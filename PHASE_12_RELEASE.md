# Phase 12 — Security, Performance & Release

## Security hardening
- Disable Android application backup for the MVP.
- Disable cleartext network traffic by default.
- Keep remediation guided; the app does not directly modify remote devices.
- Do not collect credentials or authentication secrets.
- Monitoring is user-configurable and uses bounded WorkManager scheduling when enabled; it does not perform unrestricted continuous background scanning.

## Privacy
Current MVP data is local-first. Stored categories include network inventory, service reachability, findings, remediation state, verification results, audit history, monitoring events, and administrative metadata.

## Performance
Discovery and service auditing already use bounded concurrency and short connection timeouts. Future background monitoring must use explicit scheduling and battery-aware Android APIs.

## Release checklist
- Review runtime permissions on each supported Android release.
- Verify backup behavior on release builds.
- Verify cleartext policy against every intended HTTP/TLS code path.
- Run unit tests, lint, debug APK assembly, and Android release build in CI. The latest main-branch CI run passed all of these checks and uploaded the debug APK.
- Perform manual testing on physical devices across supported Android versions (remaining environment-dependent release validation).
- Validate signed production release configuration and signing/secret availability before publishing.
- Review logs to ensure sensitive data is not emitted.
- Validate report/history storage size and retention limits.

## Current boundary
NetGuard remains a defensive, authorized network-audit assistant. Phase 12 hardening does not add credential collection, exploit execution, brute force, stealth/evasion, access-control bypass, or remote device administration.
