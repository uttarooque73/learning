# NetGuard release checklist

## Consumer product
- [x] Account-free core experience
- [x] Full Security Check entry point
- [x] Unified security score
- [x] Prioritized recommendations
- [x] Findings → remediation → verification workflow
- [x] Audit history and score trend data
- [x] Permission explanations and contextual permission requests
- [x] Local security alerts for actionable posture changes
- [x] Security learning and advanced investigation paths

## Privacy and permissions
- Core checks operate locally.
- Location permission is requested only when Android requires it for Wi-Fi/network inspection.
- Contacts and call-log access are optional and requested only for those features.
- Reports are exported only after an explicit user action.
- The app distinguishes observed evidence from proof of compromise.

## Store readiness
- Target SDK 35
- Release builds use a dedicated signing key and separate key password.
- CI verifies release APK signing with apksigner.
- Final store submission should include a privacy policy URL, accurate Data Safety disclosure, screenshots, feature graphic, and final application icon.
- Test the release APK on supported Android versions and at least one Wi-Fi and one cellular-only scenario.
