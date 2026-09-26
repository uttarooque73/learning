# Network Security Audit & Remediation Tool — Project Plan

## Project Goal

Build an Android application that helps authorized users discover their network, audit security configuration, understand findings, follow remediation guidance, verify fixes, and generate security reports.

## Product Principle

**Discover → Audit → Explain → Remediate → Verify → Report**

The application is a defensive security auditing tool for networks and devices that the user owns or is explicitly authorized to assess.

---

## Phase 0 — Product & Architecture Foundation

- Define product scope and authorized-use boundaries.
- Define threat model and application security requirements.
- Design Android architecture, modules, navigation, data model, and storage.
- Define audit/finding/remediation/verification schemas.
- Define logging, privacy, permissions, and testing strategy.

**Deliverables:** architecture design, data model, security model, navigation map, development backlog.

## Phase 1 — Android Foundation

- Create the Android project and build configuration.
- Establish modular application architecture.
- Build navigation and reusable UI components.
- Implement permissions flow and settings.
- Implement protected local storage.
- Create the initial dashboard.

**Deliverable:** installable Android application skeleton.

## Phase 2 — Network Discovery

- Detect the active network and permitted Wi-Fi information.
- Collect available local IP, gateway, DNS, subnet, and network metadata.
- Assess active cellular transport and mobile-network state where Android exposes it.
- Assess device security features such as VPN, Private DNS, screen lock, Developer Options, and USB debugging.
- Discover authorized devices using safe local-network techniques.
- Create an asset inventory.

**Deliverable:** Network and Devices screens.

## Phase 3 — Service & Exposure Audit

- Perform authorized service/port checks.
- Identify exposed services and common protocols.
- Record target, port, service, evidence, timestamp, and scan status.
- Build device/service inventory.

**Deliverable:** raw network audit results.

## Phase 4 — Security Audit Engine

Create a modular rule-based audit engine covering supported checks such as:

- Wi-Fi security configuration
- Network configuration
- Exposed/insecure services
- TLS/HTTPS configuration
- HTTP security indicators
- DNS configuration
- Unnecessary exposure
- Other safe configuration checks

Every finding should contain:

- Finding ID
- Title
- Severity
- Confidence
- Affected asset
- Evidence
- Explanation
- Remediation guidance
- Verification method
- References where appropriate

**Deliverable:** repeatable audit engine with tests.

## Phase 5 — Findings & Risk Dashboard

- Security posture dashboard.
- Severity breakdown.
- Findings grouped by device/network.
- Finding detail screens.
- Explainable security score.
- Risk trends over time.

Scores must be traceable to actual findings rather than being an opaque rating.

**Deliverable:** complete security dashboard.

## Phase 6 — Remediation Engine

- Build remediation playbooks.
- Provide step-by-step remediation instructions.
- Provide relevant Android settings shortcuts where appropriate.
- Provide router/device-specific guidance where supported.
- Prefer guided remediation over automatic configuration changes unless a safe, explicitly authorized API is available.

**Deliverable:** remediation workflow linked to supported findings.

## Phase 7 — Verification & Before/After

- Re-run relevant checks after remediation.
- Compare before/after evidence.
- Mark findings as fixed, still present, changed, or unable to verify.
- Maintain remediation history.

**Deliverable:** closed-loop **Audit → Fix → Verify** workflow.

## Phase 8 — Reports & Audit History

- Generate technical reports.
- Generate executive summaries.
- Include assets, findings, evidence, remediation status, and verification results.
- Add audit history.
- Add export/share functionality.

**Deliverable:** shareable security audit reports.

## Phase 9 — Monitoring & Alerts

- Support recurring authorized checks.
- Detect new devices.
- Detect new exposed services.
- Detect relevant network/configuration changes.
- Alert on recurring findings.

**Deliverable:** monitoring dashboard and notifications.

## Phase 10 — Baselines & Compliance

- Allow users to define network security baselines.
- Compare audit results against a selected baseline.
- Add configurable security checklists.
- Add optional mappings to recognized security frameworks.

**Deliverable:** baseline and compliance assessment module.

## Phase 11 — Advanced Administration

- Multiple network profiles.
- Asset tags and notes.
- Advanced audit history.
- Team-oriented reporting.
- Stronger administrative audit logging.
- Optional backend synchronization.

**Deliverable:** scalable administration features.

## Phase 12 — Security, Performance & Release

- Security review of the application itself.
- Minimize Android permissions.
- Protect stored sensitive data.
- Test Android-version differences.
- Improve scan reliability and battery efficiency.
- Perform release QA and regression testing.
- Produce production release documentation.

**Deliverable:** production-ready release candidate and security test report.

---

## Core Architecture

```text
Android App
│
├── UI
│   ├── Dashboard
│   ├── Networks
│   ├── Devices
│   ├── Findings
│   ├── Remediation
│   ├── Reports
│   └── Settings
│
├── Discovery Engine
│   ├── Wi-Fi Discovery
│   ├── Network Discovery
│   └── Device Discovery
│
├── Audit Engine
│   ├── Wi-Fi Checks
│   ├── Network Checks
│   ├── Service Checks
│   ├── TLS/HTTP Checks
│   └── Configuration Checks
│
├── Finding Engine
│   ├── Severity
│   ├── Confidence
│   ├── Evidence
│   └── Risk Calculation
│
├── Remediation Engine
│   ├── Playbooks
│   ├── Guided Actions
│   └── Remediation Tracking
│
├── Verification Engine
│   ├── Rechecks
│   ├── Before/After Comparison
│   └── Verification Status
│
├── Storage
└── Reporting
```

## MVP Scope

The first useful release should cover **Phases 0–7**:

1. Discover an authorized network.
2. Inventory devices.
3. Run a limited set of safe security checks.
4. Show findings with evidence and severity.
5. Explain each issue.
6. Provide remediation guidance.
7. Re-scan and verify whether the remediation worked.

Reporting, continuous monitoring, baselines, compliance, and advanced administration follow after the MVP.

## Security & Authorization Boundary

The tool is intended for defensive assessment of networks and devices the user owns or is explicitly authorized to test. The product should focus on discovery, auditing, evidence, remediation, and verification. It should not include credential theft, Wi-Fi password cracking, exploit deployment, or unauthorized access functionality.

## Development Rule

Build each phase so that it is independently testable. Do not build the entire scanner first. Establish the data model and interfaces early so new audit checks and remediation playbooks can be added without rewriting the application.


### Current implementation status

Phase 0 — complete:
- product scope and authorized-use boundary documented
- architecture and security data model established

Phase 1 — complete:
- Android project/build configuration
- Compose dashboard and navigation
- permissions and local persistence foundations

Phase 2 — complete for MVP:
- active network inspection
- bounded local device discovery
- network/device inventory persistence
- device hostname and discovery timestamps

Phase 3 — complete for MVP:
- bounded TCP checks against a conservative service catalog
- service inventory persistence
- per-device service audit UI
- service timestamps

Phase 4 — complete for current MVP:
- deterministic service finding rules
- finding severity/confidence/evidence/remediation/verification data
- finding persistence and tests

Phase 5 — complete for current MVP:
- security posture score
- severity breakdown
- finding details and evidence
- traceable risk calculation

Phase 6 — complete for current MVP:
- guided remediation playbooks
- prerequisites and verification guidance
- remediation status persistence
- no automatic remote configuration changes

Phase 7 — complete for current MVP:
- defensive service rechecks
- before/after evidence
- verification status persistence
- finding-level Verify action
- explicit safety boundary documentation

Phase 8 — complete for current MVP:
- local audit snapshot/history storage
- deterministic text report generation
- executive summary with risk and severity counts
- finding evidence, remediation, and verification in reports
- report/history UI
- history capped to the latest 20 audits

Phase 9 — complete for current MVP:
- bounded change detection against the authorized device/service inventory
- detection of newly discovered devices
- detection of newly exposed services
- detection of services no longer reachable
- persistent monitoring event history capped at 100 events
- explicit baseline storage
- manual opt-in monitoring check from the dashboard

Phase 10 — complete for current MVP:
- configurable baseline data model
- default secure-home baseline
- evidence-backed PASS/FAIL/REVIEW evaluation
- local baseline persistence
- dashboard baseline evaluation
- explicit limitation: results are security baseline assessments, not formal certification claims

Phase 11 — complete for current MVP:
- local network profiles
- asset names, tags, and notes data model
- administrative event log capped at 200 events
- local persistence for administration metadata
- dashboard administration section
- no backend/account synchronization dependency yet

Phase 12 — hardening foundation complete:
- mobile/cellular security posture audit
- Android device security feature checks
- evidence-backed mobile remediation and verification guidance
- Android backup disabled
- cleartext traffic disabled by default
- privacy/security policy definitions
- release checks and unit test
- release hardening checklist
- bounded discovery/service audit behavior documented

Remaining release work:
- physical-device compatibility testing
- runtime permission review
- release build validation



---

## Feature Roadmap — One Feature at a Time

The following backlog is the next product-level roadmap. Each feature is implemented, tested, documented, and verified before moving to the next item.

### Feature 1 — Android Device Security Audit
**Status: In progress**

- Android version and SDK information.
- Android security patch level.
- Secure screen-lock posture.
- App-data encryption-at-rest signal.
- Debuggable Android build signal.
- Evidence, explanation, remediation, and verification for each check.
- Keep device-security checks separate from network-exposure findings.

Documentation: `docs/features/01-android-device-security-audit.md`

### Feature 2 — Installed Application Security Audit
**Status: Planned**

- Installed application inventory.
- Dangerous/runtime permission review where Android permits it.
- Debuggable application detection.
- Cleartext traffic configuration.
- Backup configuration.
- Exported component indicators.
- Sideloading/unknown-source indicators where accurately observable.
- Per-application evidence and remediation guidance.

### Feature 3 — DNS Security Audit
**Status: Planned**

- Active DNS servers.
- Private DNS state.
- DNS configuration changes.
- Baseline comparison.
- Evidence-backed DNS findings.

### Feature 4 — Gateway Security Audit
**Status: Planned**

- Gateway identity tracking.
- Gateway service exposure.
- Management interface exposure.
- Gateway change detection.
- Evidence and remediation guidance.

### Feature 5 — TLS/HTTPS Security Analyzer
**Status: Planned**

- TLS reachability.
- Certificate validity.
- Certificate hostname matching.
- Certificate expiry.
- TLS protocol observations.
- Safe HTTP-to-HTTPS checks.

### Feature 6 — HTTP Security Analyzer
**Status: Planned**

- HTTPS redirect behavior.
- HSTS.
- Content-Security-Policy.
- X-Content-Type-Options.
- Referrer-Policy.
- Safe cookie security indicators.

### Feature 7 — Wi-Fi Trust & Change Detection
**Status: Planned**

- SSID/BSSID history.
- Gateway identity history.
- Wi-Fi security-mode changes.
- New BSSID warning for a known SSID.
- Explicit uncertainty; never claim a rogue AP from one signal.

### Feature 8 — Remediation Center
**Status: Planned**

- Central remediation queue.
- Android Settings shortcuts where supported.
- Guided network/device remediation.
- Before/after verification.
- Remediation history.

### Feature 9 — Security Timeline
**Status: Planned**

- Device changes.
- Service changes.
- Network identity changes.
- Finding state changes.
- Remediation and verification events.

### Feature 10 — Advanced Reporting
**Status: Planned**

- JSON export.
- CSV export.
- PDF report.
- Shareable audit package.
- Executive and technical report views.

### Feature 11 — Configurable Security Policies
**Status: Planned**

- User-defined checks.
- Custom thresholds.
- Home/work profiles.
- Policy evaluation history.

### Feature 12 — Security Learning Mode
**Status: Planned**

- Technical explanations.
- Why the issue matters.
- Evidence interpretation.
- Defensive remediation concepts.
- Verification walkthroughs.

### Feature delivery rule

For every feature:

1. Document scope and security boundary.
2. Define data model and check IDs.
3. Implement the smallest useful version.
4. Add unit tests.
5. Integrate the UI.
6. Add remediation guidance.
7. Add verification where technically possible.
8. Run CI/build/lint tests.
9. Update this roadmap.
10. Only then start the next feature.