# NetGuard

Android network security audit and remediation assistant.

## Status

**MVP hardening — Phases 0–12 substantially implemented.**

The app currently supports local network discovery, device/service auditing, security findings, remediation guidance, verification, reports, monitoring, baselines, administration, and Android/mobile security posture checks.

## Product workflow

**Discover → Audit → Explain → Remediate → Verify → Report**

## Scope

NetGuard is designed for defensive assessment of networks and devices the user owns or is explicitly authorized to test. It is not intended for credential theft, Wi-Fi password cracking, exploit deployment, or unauthorized access.

## Roadmap

See [`plan.md`](plan.md) for the complete phased implementation plan.


## Advanced security features

The current implementation also includes:

- Android device security posture checks
- Installed application security inventory
- DNS and gateway audit
- TLS certificate/protocol observation
- HTTP security-header checks
- Wi-Fi identity/trust change detection
- Central remediation queue
- Security timeline event model
- JSON/CSV/PDF/ZIP report export and Android sharing
- Configurable security-policy evaluation
- Security Learning Mode

See [docs/SECURITY_FEATURES.md](docs/SECURITY_FEATURES.md) and [plan.md](plan.md) for scope and limitations.


### Modern feature navigation

NetGuard now separates security capabilities into dedicated screens instead of placing the full product on the dashboard. The navigation includes discovery, service inventory, security intelligence, findings, remediation, monitoring, baseline/compliance, mobile security, Wi-Fi trust, web security, policies, timeline, reports, administration, learning, and advanced analysis.
