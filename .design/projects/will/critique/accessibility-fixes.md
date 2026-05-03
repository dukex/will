# Accessibility Fixes — Will
> Phase: critique | Project: Will | Date: 2026-04-16
> Standard: WCAG 2.2 AA | See also: [accessibility-audit.md](./accessibility-audit.md) | [prioritized-fixes.md](./prioritized-fixes.md)

---

## Critical Violations

### AF-01 — Placeholder text contrast fails AA in IntentInputSheet

| Field | Value |
|-------|-------|
| **Issue** | BasicTextField placeholder uses `WillTextTertiary` (`#505050`) on `#161616` (sheet background) = ~2.6:1 contrast ratio. WCAG 2.2 AA requires 4.5:1 for normal text. |
| **Severity** | Critical |
| **WCAG Criterion** | 1.4.3 Contrast (Minimum) — Level AA |
| **Affected Screen** | [IntentInputSheet](../design/screen-03-intent-input-sheet.md) |

**Remediation:**

Change placeholder `Text` color from `WillTextTertiary` to `WillTextSecondary` (`#888888`):

```kotlin
// Before (fails contrast on #161616 background)
decorationBox = { innerTextField ->
    Box {
        if (text.isEmpty()) Text(
            "what do you need to do?",
            style = bodyMedium.copy(color = WillTextTertiary)  // #505050 — FAIL
        )
        innerTextField()
    }
}

// After (passes: #888888 on #161616 = 4.61:1 ✓ AA)
decorationBox = { innerTextField ->
    Box {
        if (text.isEmpty()) Text(
            "what do you need to do?",
            style = bodyMedium.copy(color = WillTextSecondary)  // #888888 — PASS
        )
        innerTextField()
    }
}
```

Update the design spec in [screen-03-intent-input-sheet.md](../design/screen-03-intent-input-sheet.md) to replace `WillTextTertiary` with `WillTextSecondary` for the placeholder only.

---

### AF-02 — Tertiary text at small sizes fails AA in multiple screens

| Field | Value |
|-------|-------|
| **Issue** | `WillTextTertiary` (`#505050`) on `#000000` = 3.10:1. Fails AA for text below 18sp regular or 14sp bold. Used at 11sp (labelSmall) and 13sp (SectionHeader) in several screens. |
| **Severity** | Critical |
| **WCAG Criterion** | 1.4.3 Contrast (Minimum) — Level AA |
| **Affected Screens** | [HomeScreen](../design/screen-01-home-screen.md) (section headers), [FocusDashboard](../design/screen-05-focus-dashboard.md) (SectionHeader at labelSmall), [NotificationFilterSettings](../design/screen-04-notification-filter-settings.md) (section header) |

**Remediation:**

For `SectionHeader` composable at `labelSmall` (11sp): change color from `WillTextTertiary` to `WillTextSecondary` (`#888888`, 6.48:1 on `#000000` ✓ AA).

```kotlin
// Before
@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = WillTextTertiary  // #505050 — FAIL at 11sp
    )
}

// After
@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = WillTextSecondary  // #888888 — PASS
    )
}
```

**Note on design intent:** WillTextTertiary was specified to create a hierarchy between section headers and content. Using WillTextSecondary reduces that visual hierarchy slightly but maintains accessibility compliance. The hierarchy is still present: section header (WillTextSecondary, 11sp, GeistMono) vs task title (WillTextPrimary, 15sp, GeistSans) — the contrast in font size and family preserves the hierarchy without relying on a non-compliant color step.

Update the design specs in screens 01, 04, and 05 to reflect this change. Update `SectionHeader` component spec in [component-plan.md](../design/shared/component-plan.md).

---

## Major Violations



### AF-04 — "History cleared" feedback not announced to TalkBack

| Field | Value |
|-------|-------|
| **Issue** | The "history cleared" text replaces the delete row for 1.5s. Screen 06 specifies `View.announceForAccessibility()` — this is the correct intent, but the Compose equivalent must be explicit. |
| **Severity** | Major |
| **WCAG Criterion** | 4.1.3 Status Messages — Level AA |
| **Affected Screen** | [PrivacySettings](../design/screen-06-privacy-settings.md) |

**Remediation:**

Use `semantics { liveRegion = LiveRegionMode.Assertive }` on the "history cleared" `Text`, or use the Compose `AccessibilityManager.announce` equivalent:

```kotlin
// In PrivacySettings composable
val accessibilityManager = LocalContext.current.getSystemService(
    Context.ACCESSIBILITY_SERVICE
) as AccessibilityManager

// After deleteAllTasks() succeeds:
if (accessibilityManager.isEnabled) {
    val event = AccessibilityEvent.obtain(
        AccessibilityEvent.TYPE_ANNOUNCEMENT
    ).apply { text.add("history cleared") }
    accessibilityManager.sendAccessibilityEvent(event)
}
```

Alternatively, set `liveRegion = LiveRegionMode.Assertive` on the row that switches to "history cleared" — this triggers the announcement automatically when the text changes.

Update the design spec in [screen-06-privacy-settings.md](../design/screen-06-privacy-settings.md) to specify `liveRegion = LiveRegionMode.Assertive` for the post-deletion feedback text.

---

## Minor Issues (Informational)

### AF-05 — Reduced motion: tween() durations should respect system animator scale

| Field | Value |
|-------|-------|
| **Issue** | Animations use `tween(100)` hardcoded. Android's `ANIMATOR_DURATION_SCALE = 0` (reduced motion) may not fully suppress hardcoded tween durations in all Compose versions. |
| **Severity** | Minor |
| **WCAG Criterion** | 2.3.3 Animation from Interactions — Level AAA (informational for AA) |
| **Affected Screens** | All — `animateColorAsState`, `animateContentSize`, `animateItem()` |

**Remediation:** Confirm during build phase that `LocalAnimationSpec.current` or `ComposeDefaults` respects `ANIMATOR_DURATION_SCALE`. If not, wrap duration in a check: `if (animationsEnabled) tween(100) else snap()` where `animationsEnabled = LocalDensity.current.run { ... }`. This is a build-phase concern, not a design revision.

---

## Cross-references

- Priority alignment with [prioritized-fixes.md](./prioritized-fixes.md): AF-01 and AF-02 are not listed as Critical in the design critique (they are brand-detail issues there), but they are accessibility-Critical — fix before build regardless of design critique priority.
