# NetGuard Architecture

## Current implementation

Phase 1 uses a single Android application module with Jetpack Compose UI and a small domain model package. Network access and audit permissions are deliberately not requested yet.

## Planned layers

```text
UI
 ↓
Application / ViewModels
 ↓
Domain models + use cases
 ↓
Discovery / Audit / Remediation / Verification engines
 ↓
Repositories
 ↓
Protected local storage
```

## Core engines

### Discovery Engine

Responsible for collecting network information that Android permits the application to access and for authorized local-network device discovery.

### Audit Engine

Runs independent, testable audit rules and produces structured findings with evidence, severity, confidence, remediation, and verification metadata.

### Remediation Engine

Maps findings to safe, guided remediation playbooks. Automatic changes should only be introduced where an explicitly authorized and reliable API exists.

### Verification Engine

Re-runs relevant checks and compares the new evidence with the original finding to determine whether remediation succeeded.

## Design principles

- Local-first by default.
- Least-privilege Android permissions.
- Every finding must have evidence.
- Risk scores must be explainable.
- Audit rules should be independently testable.
- Remediation should be reversible where practical.
- Never assume a fix succeeded without verification.
- Network operations are intended for authorized assessment only.
