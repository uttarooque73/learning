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

Phase 3 implementation has started:
- bounded TCP service audit for a conservative catalog of common ports
- service model with IP, port, protocol, service name, reachability, and timestamp
- local persistence for service inventory
- per-device service audit action in the Devices screen
- service catalog unit coverage

Remaining Phase 3 work:
- richer service evidence
- audit timestamps and scan sessions
- explicit target authorization state
- service inventory filtering/sorting
- finding generation from exposed services
- dedicated Services/Exposure screen
- integration tests on real Android network interfaces
