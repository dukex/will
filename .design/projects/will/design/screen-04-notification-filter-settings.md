# Screen 04 — NotificationFilterSettings
> Phase: design | Project: Will | Generated: 2026-04-16
> US: US-08 (per-app notification filtering)

---

## Purpose

Full-screen settings screen listing all apps that have ever sent notifications to Will. Each app has an on/off toggle. Disabled apps stop generating tasks from notifications. Accessible via filter icon in HomeTopBar.

---

## User flow position

Full-screen destination. Navigated to from HomeTopBar filter icon. Back navigates to HomeScreen. PrivacySettings accessible via a row at the bottom of this screen.

---

## Layout

```
┌──────────────────────────────────────────┐
│  [safeDrawing.top padding]               │
│                                          │
│  ← notifications                         │  ← back + title row (bodyLarge, WillTextPrimary)
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│                                          │
│  apps sending notifications              │  ← SectionHeader (labelSmall, GeistMono, WillTextTertiary)
│                                          │
├──────────────────────────────────────────┤
│  Slack                         ●         │  ← AppToggleRow (app name + Switch, enabled)
│  com.slack.android              ○        │
├──────────────────────────────────────────┤
│  WhatsApp                      ●         │
│  com.whatsapp                   ○        │
├──────────────────────────────────────────┤
│  GitHub                        ○         │  ← disabled
│  com.github.android             ○        │
├──────────────────────────────────────────┤
│  Gmail                         ●         │
│  com.google.android.gm          ○        │
├──────────────────────────────────────────┤
│  ...                                     │
│                                          │
├──────────────────────────────────────────┤
│                                          │
│  data & privacy                          │  ← PrivacySettings entry row (bodyMedium, WillTextTertiary)
│                                          │
└──────────────────────────────────────────┘
```

**No card containers.** Full-width rows on `WillBackground` (`#000000`). This matches the launcher UX constraint: no decorative containers around main content.

**Header row:** `←` is `IconButton` (48dp) + `Text("notifications", style = bodyLarge)`. 24dp horizontal padding on both.

---

## AppToggleRow anatomy

```
Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 24.dp, vertical = 12.dp)) {
    Column(Modifier.weight(1f)) {
        Text(appName, style = bodyMedium, color = WillTextPrimary)
        Text(packageName, style = labelSmall, color = WillTextSecondary)  // GeistMono via labelSmall
    }
    Switch(
        checked = isEnabled,
        onCheckedChange = onToggle,
        colors = SwitchDefaults.colors(
            checkedThumbColor = WillTextPrimary,        // #F0F0F0
            checkedTrackColor = WillAccent,             // #4F6EF7
            uncheckedThumbColor = WillTextTertiary,     // #505050
            uncheckedTrackColor = Color.Transparent,
            uncheckedBorderColor = WillBorder           // #2A2A2A
        )
    )
}
```

**Row tap propagation:** Tapping anywhere on the row (not just the Switch thumb) toggles the switch. `Row.clickable { onToggle(!isEnabled) }`.

**Sort order:** Apps with `isEnabled = true` first, then alphabetical by app name within each group.

---

## Components used

- `LazyColumn` (full screen, full width)
- `AppToggleRow` (inline composable)
- `SectionHeader` (labelSmall, GeistMono, WillTextTertiary)
- `Switch` with explicit color overrides (no M3 accent leak)
- `HorizontalDivider(color = WillBorder, thickness = 1.dp)` between rows
- Back arrow: `IconButton` + `Icons.AutoMirrored.Outlined.ArrowBack` (24dp)

---

## States

### Default — apps loaded
List shows all apps that have ever triggered notifications via `WillNotificationListenerService`. Toggle state loaded from `AppNotificationFilter` persistence (Room or SharedPrefs).

### Empty — no apps have sent notifications yet
```
[centered, full screen below header]
  "no apps yet"             (bodyLarge, WillTextSecondary)
  "notifications will appear here once apps send them"  (labelSmall, GeistMono, WillTextTertiary)
```

### Loading — initial list load
Show nothing (empty list) — same behavior as HomeScreen. First Room/SharedPrefs emission renders list. No skeleton.

### Error
Log and show nothing. Filter settings are non-critical — the full notification pipeline still works without this screen.

---

## Persistence

App filter state persists via `AppNotificationFilter` (packageName + isEnabled). Storage: Room `app_filters` table or SharedPreferences — both acceptable. ViewModel persists across configuration changes.

`WillNotificationListenerService` reads the filter before inserting a task:
```kotlin
if (filterRepository.isEnabled(packageName)) {
    taskRepository.insertTask(task)
}
```

---

## Interactions

| Trigger | Effect |
|---------|--------|
| Tap row / Switch | Toggle state, persist, `flat-press` on row |
| Tap back arrow | Navigate back to HomeScreen |
| Tap "data & privacy" | Navigate to PrivacySettings |

---

## Accessibility

**List items:** `Modifier.semantics(mergeDescendants = true)` on each row.
```kotlin
contentDescription = "${appName}. notifications ${if (isEnabled) "on" else "off"}"
customActions = listOf(
    CustomAccessibilityAction(if (isEnabled) "turn off" else "turn on") { 
        onToggle(!isEnabled); true 
    }
)
```

**Switch inside row:** `semantics { invisibleToUser() }` since the row semantics cover the toggle action.

**Section header:** `semantics { heading() }`.

**Back button:** `contentDescription = "back to home"`.

---

## Image resources

None. App icons not shown — name + package only. STYLE.md: "no decorative containers." Showing app icons would introduce unnecessary visual decoration and require icon loading logic.

**Icons used:**
- `Icons.AutoMirrored.Outlined.ArrowBack` (24dp, WillTextSecondary) — back button
- `Icons.Outlined.ChevronRight` (24dp, WillTextTertiary) — on the "data & privacy" row
