# Security Feature Implementation Guide

NetGuard's post-MVP security feature set is implemented as local, evidence-based modules.

## Implemented modules

### Android device security
Checks Android version, security patch level, NetGuard app-data encryption signal, debuggable build state, secure screen lock, USB debugging, Developer Options and related mobile posture signals.

### Installed application audit
Enumerates applications visible to the Android package-visibility rules and records debuggable state, backup flag, cleartext flag, exported component count and selected sensitive permissions.

The result is an audit signal, not a declaration that an application is malicious.

### DNS and gateway audit
Records active DNS servers and the default gateway. A gateway comparison can identify a change from a previously recorded value.

### TLS analyzer
For an HTTPS URL, records negotiated TLS protocol and server certificate subject, issuer and expiry.

### HTTP analyzer
Records response status, HTTP-to-HTTPS redirect behavior, HSTS, CSP, X-Content-Type-Options, Referrer-Policy, and observable Set-Cookie security flags (Secure, HttpOnly, SameSite). These are configuration observations, not automatic vulnerability claims.

### Wi-Fi trust
Compares SSID, BSSID, gateway and Wi-Fi security observations and classifies BSSID changes, security-mode changes/downgrades, and network-context changes. A change is explicitly treated as a change signal, not proof of a rogue access point.

### Remediation center
Converts findings into remediation queue items using existing playbooks when available and falls back to finding remediation/verification text.

### Security timeline
Provides persistent event storage and chronological history for device, service, network, finding, remediation, verification, policy, administration, import, mobile, baseline, and report events.

### Advanced reporting
Exports audit snapshots as JSON, CSV, PDF, and ZIP packages, with device/service inventory, findings, remediation, verification, and custom-policy data represented in the export.

### Security policies
Evaluates configurable rule objects against current evidence. Default rules cover Telnet, SMB, HTTP/HTTPS, USB debugging and secure screen lock.

### Security learning mode
Provides concise defensive explanations for evidence interpretation, remediation concepts and verification.

## Safety model

These modules do not implement credential collection, password cracking, exploit deployment, brute force, stealth/evasion, access-control bypass, or unauthorized remote control.

Network and web checks are bounded observations. Remediation remains guided unless an explicitly supported Android API can safely perform an action.

## Limitations

- Android package visibility can limit which third-party applications are observable.
- Android does not expose every security state to ordinary applications.
- Package visibility and platform-version differences can affect the set of observable application/device signals.
- Security patch level does not independently establish complete OEM patch compliance.
- Certificate/TLS observations depend on the target server and Android networking stack.
- A changed Wi-Fi identity is not proof of an attack.
- HTTP header absence is a configuration signal, not automatically a vulnerability.
- Formal compliance certification is outside the scope of these checks.
