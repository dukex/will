# Security Policy

## Sensitive permissions

Will requests permissions that carry real privacy implications:

| Permission | Why it is needed |
|---|---|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Reads notification content to create tasks |
| `SYSTEM_ALERT_WINDOW` | Renders the active-task overlay bubble above other apps |
| `AccessibilityService` *(planned)* | Detects task completion inside third-party apps |

**All data stays on device.** There is no backend, no telemetry, no analytics.
Notification content is never transmitted anywhere.

## Reporting a vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

Please report security issues privately:

1. Open a [GitHub Security Advisory](https://github.com/dukex/will/security/advisories/new)
   on this repository (preferred — keeps the report confidential until a fix
   is ready).
2. Alternatively, contact the maintainer directly via the email listed on the
   GitHub profile.

Include in your report:

- A description of the vulnerability and its potential impact.
- Steps to reproduce or a proof-of-concept.
- Android version and device model if relevant.

## Response timeline

- **Acknowledgement:** within 72 hours of receiving the report.
- **Initial assessment:** within 7 days.
- **Fix or mitigation:** timeline communicated after assessment, based on
  severity.

We follow responsible disclosure: once a fix is released, we will publicly
credit the reporter (unless they prefer to remain anonymous).

## Supported versions

Only the latest release on the `main` branch receives security fixes. Older
versions are not maintained.