# Feature 1 — Android Device Security Audit

## Objective

Extend NetGuard's existing mobile security audit with device-level posture signals that can be collected locally through supported Android APIs.

## Scope

Feature 1 covers:

1. Android release/version and SDK information.
2. Android security patch level.
3. Secure screen-lock posture.
4. Encryption-at-rest status for NetGuard's own application data directory.
5. Debuggable Android build signal.

Existing checks such as VPN, Private DNS, Developer Options, USB debugging, and airplane mode remain part of the mobile audit but are not duplicated here.

## Check IDs

| ID | Check | Status semantics |
| --- | --- | --- |
| MOB-DEV-005 | Android platform version | PASS when the platform version is available |
| MOB-DEV-006 | Android security patch level | PASS when Android exposes a patch level; REVIEW when unavailable |
| MOB-DEV-007 | App data encryption | PASS when NetGuard's files directory is reported encrypted; FAIL when not encrypted |
| MOB-DEV-008 | Debuggable Android build | PASS for a non-debuggable build; REVIEW for a debuggable build |

## Evidence rules

- Do not infer a vulnerability from the Android version alone.
- Do not treat a non-empty security patch string as proof that the device has the newest vendor-specific fixes.
- The app-data encryption check describes NetGuard's storage path, not every storage volume on the device.
- A debuggable OS build is a posture signal and should be reviewed in normal consumer deployments; it is not proof of compromise.

## Remediation

Remediation is guidance only. NetGuard does not modify Android security settings automatically.

- Security patch: use the device's normal system-update workflow.
- Secure lock: configure a strong PIN/password and optional biometrics.
- App-data encryption: investigate device encryption/update state if NetGuard reports the app data path is not encrypted.
- Debuggable build: use a production/non-debuggable Android build when appropriate.

## Verification

Refresh the mobile audit after remediation and compare the new evidence with the previous result.

## Security boundary

This feature is local and defensive. It does not collect credentials, attempt privilege escalation, bypass Android protections, or modify the device.

## Sources

Android documents the security patch level as a mechanism for determining which Android security updates are present, and Android publishes security bulletins with corresponding patch levels. The current audit therefore records the device-reported patch level without pretending to determine vendor-specific patch completeness. urlAndroid Security Bulletinshttps://source.android.com/docs/security/bulletin/asb-overview

Android also documents that debuggable builds are intended for debugging and differ from release builds. urlAndroid debugging documentationhttps://developer.android.com/studio/debug