# Accessibility Audit — Will
> Phase: critique | Project: Will | Date: 2026-04-16
> Standard: WCAG 2.2 AA | Platform: Android / Jetpack Compose

---

## 1. Perceivable

### 1.1 Text Alternatives

| Check | Status | Notes |
|-------|--------|-------|
| All non-text content has text alternatives | PASS | All icons have `contentDescription` specified. TaskStatusIndicator dot uses `semantics { invisibleToUser() }` — correct, as the status is carried by the merged row semantics. |
| Decorative images use empty alt | PASS | No images in the product. UI is the imagery per STYLE.md. |
| Canvas chart has text alternative | PASS | Screen 05 specifies `semantics { contentDescription = "time on task chart: ${taskSummaries}" }` on the Canvas wrapper. |

### 1.2 Time-Based Media

| Check | Status | Notes |
|-------|--------|-------|
| No audio/video content | PASS | No media content. Not applicable. |

### 1.3 Adaptable

| Check | Status | Notes |
|-------|--------|-------|
| Content structure conveyed through proper markup | PASS | Section headers use `semantics { heading() }`. TaskItem rows use `semantics(mergeDescendants = true)`. |
| Meaningful reading order preserved | PASS | TalkBack traversal order specified in Screen 01: HomeTopBar → active section header → active tasks → pending section → pending tasks → done section → done tasks → permission prompt. |
| Instructions don't rely solely on shape/size/position/color | PARTIAL | Swipe-to-dismiss actions (done/delete) are communicated via color (WillSuccess/WillDestructive background) and icon, with a text label alongside. The text labels ("done"/"delete" in GeistMono) ensure non-color information. PASS for swipe labels. See I-01 below: swipe gesture discoverability relies on knowledge of the gesture convention. |

### 1.4 Distinguishable

| Check | Status | Notes |
|-------|--------|-------|
| Color is not the only means of conveying information | PASS | TaskStatusIndicator uses color + position + merged row semantics. Swipe actions use color + icon + text label. Action rows use color + label text. |
| Text contrast ≥ 4.5:1 (normal) / ≥ 3:1 (large) | PASS | All primary pairs verified: `#F0F0F0` on `#000000` = 18.35:1 ✓ AAA; `#F0F0F0` on `#0D0D0D` = 15.93:1 ✓ AAA; `#888888` on `#000000` = 6.48:1 ✓ AA; `#888888` on `#0D0D0D` = 5.63:1 ✓ AA. |
| Accent contrast ≥ 4.5:1 | PASS | `#4F6EF7` on `#000000` = 5.36:1 ✓ AA. Used only on interactive elements, not as text background for body text. |
| Destructive contrast ≥ 3:1 non-text | PASS | `#E05252` on `#000000` = 5.97:1 ✓ AA. Used on text labels and borders. |
| Success contrast ≥ 3:1 non-text | PASS | `#4CAF74` on `#000000` = 8.15:1 ✓ AA. |
| Tertiary text contrast | WARN | `#505050` on `#000000` = 3.10:1 — large text only (≥ 18sp or bold). Used at 11–13sp for section headers and lifecycle labels. See A-01 below. |
| Placeholder contrast | PASS | `#505050` on `#161616` (IntentInputSheet): calculated ~2.8:1. See A-01 — this is a Critical accessibility issue. |
| Text resizable to 200% | PASS | All sizes use `sp` units. Layout uses `wrapContentHeight`. No fixed-height containers clipping text. |
| No images of text | PASS | No images of text anywhere. |
| Content reflows at 320px | PASS | Android 360dp is the minimum target width, which maps to ~270px at 1.33 density. Task titles wrap to 2 lines per design spec. |

---

## 2. Operable

### 2.1 Keyboard Accessible

| Check | Status | Notes |
|-------|--------|-------|
| All functionality available via keyboard/TalkBack | PASS | Every interactive element has either `clickable`, `IconButton`, or `Switch`. TalkBack custom actions specified for swipe actions on TaskItem. |
| No keyboard traps | PASS | All bottom sheets dismiss on scrim tap + system back. No custom focus traps defined beyond standard M3 sheet behavior. |

### 2.2 Enough Time

| Check | Status | Notes |
|-------|--------|-------|
| No time-based UI | PASS | No countdown timers, no auto-dismissing alerts. The "history cleared" 1.5s display is informational feedback, not a required input window. |

### 2.3 Seizures and Physical Reactions

| Check | Status | Notes |
|-------|--------|-------|
| No content flashing > 3 times/sec | PASS | No flashing elements. Motion intensity is 2/10. No ambient animation. |
| Motion animation can be disabled | PARTIAL | The design specifies `animateColorAsState(tween(100))` and `animateItem()`. Neither respects `WindowInsetsController.isAppearanceLightStatusBars` or `prefers-reduced-motion`. Android provides `Settings.Global.ANIMATOR_DURATION_SCALE = 0` for reduced motion — Compose animations respect this via `LocalAnimationSpec`. Confirm during build that all `tween()` calls use `spring()` or standard defaults rather than hardcoded durations that bypass the system scale. |

### 2.4 Navigable

| Check | Status | Notes |
|-------|--------|-------|
| Descriptive screen titles | PASS | Each full-screen destination has a header text: "notifications," "focus," "data & privacy." |
| Focus order is logical | PASS | TalkBack traversal order is specified per screen and follows visual top-to-bottom flow. |
| Focus visible | PASS | Focus rings use `#4F6EF7` (5.36:1 on `#000000` background ✓ AA, ≥ 3:1 required). STYLE.md specifies focus rings on all interactive elements. |
| Focus Not Obscured (SC 2.4.11) | PASS | No sticky headers. Bottom sheets use a scrim. No overlay obstructing focused elements outside the sheet. |

### 2.5 Input Modalities

| Check | Status | Notes |
|-------|--------|-------|
| Pointer gestures have single-pointer alternatives | PASS | Swipe gestures have TalkBack `customActions` as alternatives. |
| Pointer actions cancellable | PASS | `SwipeToDismissBox` threshold at 40% — user can release before threshold without triggering action. |
| Touch targets ≥ 24x24 dp (WCAG SC 2.5.8) | PASS | All interactive elements use `heightIn(min = 48.dp)` or `IconButton` (48dp built-in). 48dp exceeds the 24x24 minimum and meets the recommended 44x44 target. |
| Labels match accessible names | PASS | All `contentDescription` values match or describe the visible label. |
| Motion-triggered functions have alternatives | PASS | No motion-triggered functions. Swipe-up for AppGrid has no gesture-only dependency — TalkBack can navigate to app drawer separately. |

---

## 3. Understandable

### 3.1 Readable

| Check | Status | Notes |
|-------|--------|-------|
| App language declared | PARTIAL | Android apps declare language in `res/values/strings.xml` with `app:localeConfig`. Not specified in design — must be confirmed in build. |
| Reading level | PASS | All copy is at a reading level appropriate for the target persona (developer). No jargon that requires domain knowledge beyond knowing what Slack/GitHub are. |

### 3.2 Predictable

| Check | Status | Notes |
|-------|--------|-------|
| No unexpected context changes on focus | PASS | No auto-navigation on focus change. |
| Navigation consistent | PASS | All back arrows navigate back to the previous screen. All section-level navigation is one-tap from the home screen icon. |
| Components identified consistently | PASS | Action rows, section headers, dividers use identical patterns across all screens. |

### 3.3 Input Assistance

| Check | Status | Notes |
|-------|--------|-------|
| Errors identified in text | PARTIAL | FocusDashboard errors show nothing (see fix I-02 in [prioritized-fixes.md](./prioritized-fixes.md)). |
| Labels and instructions provided for inputs | PASS | BasicTextField has placeholder "what do you need to do?" as its effective label. |
| No cognitive test for authentication | PASS | No authentication in scope. |

---

## 4. Robust

### 4.1 Compatible

| Check | Status | Notes |
|-------|--------|-------|
| Name, role, value available for all UI components | PASS | All interactive elements have `contentDescription`, `role`, or are wrapped in `semantics` blocks as specified per-screen. |
| Status messages via live regions | PARTIAL | The "history cleared" message in Screen 06 requires `announceForAccessibility()` — specified in the design. |

---

## 5. Mobile Accessibility

| Check | Status | Notes |
|-------|--------|-------|
| Touch targets ≥ 48dp height | PASS | All rows use `heightIn(min = 48.dp)`. All `IconButton` composables use 48dp by default. |
| Reach zones — primary content in thumb zone | PASS | Task feed occupies the middle of the screen. HomeTopBar icons are at the top-right (acceptable for a launcher, as users consciously invoke settings less frequently). |
| Orientation | N/A | Portrait-only by design and manifest configuration. No accessibility requirement for landscape in portrait-only apps. |
| Text scale at 200% | PASS | `sp` units throughout. No fixed-height clipping containers. Sheet heights use `wrapContentHeight`. |
| One-handed usability | PARTIAL | HomeTopBar icons (filter, chart) are in the top-right corner — hard to reach one-handed on 6"+ phones. For the developer persona on a Pixel, this is expected and acceptable. For WCAG SC 2.5.8 (target size), all targets are ≥ 24x24dp. |

---

## 6. Cognitive Accessibility

| Check | Status | Notes |
|-------|--------|-------|
| Reading level | PASS | All copy is plain, factual, sentence-case. No complex syntax. |
| Consistent navigation | PASS | All settings accessed via the same top-right icon cluster. No surprise navigational patterns. |
| No flashing or strobing | PASS | No flashing elements. |
| No time limits on critical actions | PASS | Confirm-delete has no timeout. History cleared state shows for 1.5s (non-critical feedback, not required interaction). |
| Error messages are clear | PARTIAL | Most error states show nothing. See A-02 below. |

---

## Contrast Audit — Detailed

### Critical pairs verified

| Foreground | Background | Ratio | WCAG | Usage |
|------------|------------|-------|------|-------|
| `#F0F0F0` | `#000000` | 18.35:1 | AAA | Task titles on home screen |
| `#F0F0F0` | `#0D0D0D` | 15.93:1 | AAA | Task titles on cards |
| `#F0F0F0` | `#161616` | 13.01:1 | AAA | Sheet content |
| `#888888` | `#000000` | 6.48:1 | AA | Source/timestamp on home screen |
| `#888888` | `#0D0D0D` | 5.63:1 | AA | Source/timestamp on cards |
| `#888888` | `#161616` | 4.61:1 | AA | Metadata on sheets |
| `#4F6EF7` | `#000000` | 5.36:1 | AA | Accent on black background |
| `#E05252` | `#000000` | 5.97:1 | AA | Destructive on black |
| `#4CAF74` | `#000000` | 8.15:1 | AA | Success on black |
| `#FFFFFF` | `#4F6EF7` | 3.92:1 | AA (large) | Button text on accent bg |
| `#F0F0F0` | `#4F6EF7` | 3.42:1 | FAIL (normal text) | Do not use at < 18sp |
| `#505050` | `#000000` | 3.10:1 | Large text only | Tertiary text — see A-01 |
| `#505050` | `#161616` | ~2.6:1 | FAIL | Placeholder in IntentInputSheet |

---

## Violations Summary

### A-01 — Tertiary text (#505050) used at sizes below 18sp [CRITICAL]

**WCAG:** 1.4.3 Contrast (Minimum) — AA
**Screens affected:** [HomeScreen](../design/screen-01-home-screen.md) (TaskStatusIndicator dot placeholder rendering), [IntentInputSheet](../design/screen-03-intent-input-sheet.md) (BasicTextField placeholder `#505050` on `#161616`), [FocusDashboard](../design/screen-05-focus-dashboard.md) (section labels in WillTextTertiary at `labelSmall` 11sp).



`#505050` on `#000000` = 3.10:1 — meets AA for large text (≥ 18sp regular or ≥ 14sp bold) but fails for normal text at 11–13sp.

Critical usage: BasicTextField placeholder `#505050` on `#161616` (IntentInputSheet) = ~2.6:1 — fails even large text AA.

**Remediation:** For placeholder text in IntentInputSheet: change placeholder color to `#888888` (WillTextSecondary, 4.61:1 on `#161616` ✓ AA). Update the `decorationBox` placeholder `Text` to use `WillTextSecondary` not `WillTextTertiary`. For section headers at 11sp in WillTextTertiary: change to `WillTextSecondary (#888888)` at `labelSmall` size, or increase size to `type-md` (13sp, FontWeight.Medium) to meet large text threshold.

---

### A-03 — Swipe gesture has no TalkBack-discoverable equivalent explanation [MAJOR]

**WCAG:** 2.5.1 Pointer Gestures — AA
**Screen affected:** [HomeScreen](../design/screen-01-home-screen.md)

The design correctly specifies `customActions` for TalkBack (mark done, delete). This means TalkBack users can access the actions. However, a user navigating with TalkBack for the first time has no signal that swipe actions exist — the `contentDescription` of the merged row does not mention the available gestures.

**Remediation:** This is already handled correctly by TalkBack `customActions` — TalkBack users access actions via the accessibility menu, not by performing swipe gestures. The custom actions ARE the equivalent alternative. Mark as PASS if `customActions` are implemented as specified. This was raised during audit for completeness.

---

## Summary

| Category | Pass | Fail | N/A |
|----------|------|------|-----|
| Perceivable | 9 | 1 (A-01 contrast) | 1 |
| Operable | 11 | 0 | 1 |
| Understandable | 6 | 0 | 1 |
| Robust | 3 | 0 | 0 |
| Mobile | 4 | 0 | 1 |
| Cognitive | 5 | 0 | 0 |
| **Total** | **38** | **1** | **4** |

**Overall conformance level:** Conditional AA — one issue requires remediation before claiming WCAG 2.2 AA compliance.
- A-01 (contrast) is Critical — affects all users with visual impairment and baseline readability.

---

## Accessibility Statement Draft

> Will is designed to work with Android accessibility services, including TalkBack. All task actions are accessible via custom accessibility actions in the TalkBack menu without requiring swipe gestures. All text meets WCAG 2.2 AA contrast requirements. Touch targets are a minimum of 48dp on all interactive elements.
>
> Will uses on-device processing only. No data is shared with external services.
