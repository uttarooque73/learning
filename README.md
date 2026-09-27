# NetGuard

**A practical security checkup for your Android phone and the networks you use.**

NetGuard helps people answer three simple questions:

1. **Is my phone or current network showing security issues?**
2. **What evidence supports the warning?**
3. **What can I do about it, and did the problem improve after the fix?**

The app is designed to be useful without an account. Core checks and security evidence stay on the device unless the user explicitly exports or shares a report.

## Status

**Consumer-product hardening in progress.** The core audit, evidence, remediation, verification, monitoring, mobile posture, reporting, and advanced investigation capabilities are implemented. The current work is focused on making those capabilities easier for everyday users to discover and act on.

The app currently supports local network discovery, device/service auditing, security findings, remediation guidance, verification, reports, monitoring, baselines, administration, and Android/mobile security posture checks.

## Product workflow

**Check → Understand → Fix → Verify → Repeat**

The advanced workflow remains:

**Discover → Audit → Explain → Remediate → Verify → Report**

The home experience should prioritize the first workflow so a new user can get useful security information quickly without learning networking terminology first.

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


## Who it is for

- People who want a quick security check of their Android device.
- People who want to understand the security of the Wi-Fi/network they are currently using.
- Home users who want to discover devices and exposed services on their own network.
- Security learners who want evidence-based explanations rather than opaque “magic” scores.
- Advanced users who want deeper audit, monitoring, reporting, and investigation tools.

## Privacy and trust

- No account is required for the core product.
- Network and device observations are processed locally.
- Permissions are requested when a feature actually needs them.
- The app distinguishes an observation from proof of compromise.
- Reports are generated locally and shared only when the user chooses to export/share them.

## What makes the app useful

NetGuard is not intended to replace a full mobile antivirus, EDR, firewall, or professional penetration-testing suite. Its purpose is to turn common security questions into understandable checks with evidence and next actions.

Examples include:

- **Phone security posture:** developer options, USB debugging, screen lock, encryption-related posture, platform/patch signals, VPN and Private DNS observations.
- **Wi-Fi/network check:** network identity, gateway/DNS observations, device discovery, service exposure, Wi-Fi trust changes, TLS/HTTP signals.
- **Installed-app review:** debuggable builds, backup/cleartext flags, exported components, sensitive permissions, installer source, target SDK and version information.
- **Action loop:** findings → remediation guidance → verification → report.
- **Ongoing awareness:** monitoring, baselines, timelines and security intelligence for users who want deeper visibility.
