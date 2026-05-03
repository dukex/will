# Issues — Will
> Phase: review | Project: Will | Generated: 2026-04-16
> Critical issues block public release. Non-critical issues are shippable for internal MVP.

---

## Issues Table

| ID | Issue | Severity | File Path | Line | Expected | Actual | Remediation |
|----|-------|----------|-----------|------|----------|--------|-------------|
| R-01 | `SectionHeader` uses `WillTextTertiary` (#505050) at 11sp — 3.10:1 on #000000, fails WCAG 1.4.3 AA (4.5:1 required) | **critical** | `app/src/main/java/tech/benedicta/will/ui/SectionHeader.kt` | 18 | `color = WillTextSecondary` (#888888, 6.48:1) | `color = WillTextTertiary` (#505050, 3.10:1) | Change `WillTextTertiary` → `WillTextSecondary` in `SectionHeader.kt`. Applies to all 6 screens. Critique fix AF-02. |
| R-02 | `IntentInputSheet` placeholder uses `WillTextTertiary` (#505050) on `WillSurface2` (#161616) = ~2.6:1, fails WCAG 1.4.3 AA | **critical** | `app/src/main/java/tech/benedicta/will/ui/IntentInputSheet.kt` | 126 | `color = WillTextSecondary` (#888888, 4.61:1 on #161616) | `color = WillTextTertiary` (#505050, ~2.6:1) | Change placeholder text color to `WillTextSecondary`. Critique fix AF-01. |
| R-03 | `NlpStateRow` text in `IntentInputSheet` has no `liveRegion` — TalkBack users miss state transitions (idle → processing → suggestion), violating WCAG 4.1.3 | **critical** | `app/src/main/java/tech/benedicta/will/ui/IntentInputSheet.kt` | 168–179 | `semantics { liveRegion = LiveRegionMode.Polite; contentDescription = ... }` on the AnimatedVisibility text | No `liveRegion` set | Add `liveRegion = LiveRegionMode.Polite` to the `AnimatedVisibility` Text semantics, with dynamic `contentDescription`. Critique fix AF-03. |
| R-04 | `PrivacySettings` "history cleared" text has no live region — TalkBack users receive no announcement after delete completes, violating WCAG 4.1.3 | **critical** | `app/src/main/java/tech/benedicta/will/ui/PrivacySettings.kt` | 197 | `semantics { liveRegion = LiveRegionMode.Assertive }` on the "history cleared" Text | No `liveRegion` set | Add `liveRegion = LiveRegionMode.Assertive` to the `showCleared` Text composable. Critique fix AF-04. |
| R-05 | `TaskItem` in `TaskFeed.kt` has no `mergeDescendants = true` or `customActions` — TalkBack reads title + metadata as separate nodes with no custom actions (mark done, delete), failing design accessibility spec | **important** | `app/src/main/java/tech/benedicta/will/ui/TaskFeed.kt` | 184–215 | `Modifier.semantics(mergeDescendants = true) { contentDescription = ...; customActions = ... }` | Raw `combinedClickable` with no semantics merge | Add `semantics(mergeDescendants = true)` with `contentDescription` and `customActions` to TaskItem Row per screen-01 design spec. |
| R-06 | `bodySmall` in `WillTypography` uses Geist **Sans** — design STYLE.md maps `bodySmall` equivalent to Geist Mono (12sp metadata text). Affects `PrivacySettings.kt` confirm-delete text rendered in wrong typeface | **minor** | `app/src/main/java/tech/benedicta/will/ui/theme/Theme.kt` | 133–139 | `fontFamily = GeistMono` at 12sp for data/system text | `fontFamily = GeistSans` for `bodySmall` | Change `bodySmall` in `WillTypography` to use `GeistMono`. Alternatively, use `labelSmall` for the affected text in `PrivacySettings.kt` — `labelSmall` is already Geist Mono. |
| R-07 | `Surface(modifier = Modifier.fillMaxSize())` in `MainActivity.kt` line 325 has no explicit `tonalElevation = 0.dp` — M3 may apply tonal color shift on Surface, violating brand requirement of pure `#000000` background | **minor** | `app/src/main/java/tech/benedicta/will/MainActivity.kt` | 325 | `Surface(modifier = Modifier.fillMaxSize(), tonalElevation = 0.dp)` | `Surface(modifier = Modifier.fillMaxSize())` | Add `tonalElevation = 0.dp` to the `Surface` wrapper in `WillScreen`. |
| R-08 | `NotificationPermissionPrompt` uses `MaterialTheme.colorScheme.errorContainer` as background (a tinted surface) and M3 `Button` with default accent styling — both violate STYLE.md constraints ("no tinted surfaces", accent on interactive only with explicit override) | **minor** | `app/src/main/java/tech/benedicta/will/ui/NotificationPermissionPrompt.kt` | 18–37 | `WillBorder` as container border, `WillSurface1` as background, custom `TextButton` instead of filled `Button` | `errorContainer` tinted surface + M3 `Button` accent default | Refactor to use `WillBorder` + 1dp border on `WillBackground`, and replace `Button` with a `Row.clickable` pattern matching ActionRow style. Pre-existing component, low urgency. |

---

## Known Acceptable Gaps (Not Issues)

| Item | Decision |
|------|----------|
| NLP pipeline is regex-only | Accepted — MVP scope. No ML intent extraction required for this phase. |
| FocusDashboard bar chart uses `WillAccent` for fill | Accepted — deliberate brand stretch documented in critique P-03. Chart bars are the only color in the chart; visual clarity justifies the exception at MVP. |
| Snackbar undo re-adds task with new Room ID (PendingIntent association lost) | Accepted — documented in BUILD-LOG.md. Undo path restores data correctly for user experience; PendingIntent is ephemeral by design. |
| `data & privacy` row in `NotificationFilterSettings` had a TODO in earlier commit | Resolved — navigation wired to `PrivacySettings` in final implementation. |
