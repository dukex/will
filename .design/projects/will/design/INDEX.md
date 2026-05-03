# Design
> Phase: design | Project: Will | Generated: 2026-04-16

## Screens

| # | Screen | File | US | Components Used |
|---|--------|------|----|-----------------|
| 01 | HomeScreen | [screen-01-home-screen.md](./screen-01-home-screen.md) | US-04, US-06 | TaskFeed, TaskItem (SwipeToDismissBox), TaskStatusIndicator, HomeTopBar, SectionHeader |
| 02 | TaskActionSheet | [screen-02-task-action-sheet.md](./screen-02-task-action-sheet.md) | US-04 | ModalBottomSheet, HorizontalDivider, action rows |
| 03 | IntentInputSheet | [screen-03-intent-input-sheet.md](./screen-03-intent-input-sheet.md) | US-03 | ModalBottomSheet, BasicTextField |
| 04 | NotificationFilterSettings | [screen-04-notification-filter-settings.md](./screen-04-notification-filter-settings.md) | US-08 | LazyColumn, AppToggleRow, Switch (color override), SectionHeader |
| 05 | FocusDashboard | [screen-05-focus-dashboard.md](./screen-05-focus-dashboard.md) | US-05 | Canvas (bar chart), Text (displaySmall), SectionHeader |
| 06 | PrivacySettings | [screen-06-privacy-settings.md](./screen-06-privacy-settings.md) | US-07 | Column, SectionHeader, destructive row |

## Shared

| Chunk | File | ~Lines |
|-------|------|--------|
| Personas | [personas.md](./shared/personas.md) | ~55 |
| Information Architecture | [information-architecture.md](./shared/information-architecture.md) | ~75 |
| Navigation | [navigation.md](./shared/navigation.md) | ~55 |
| Micro-interactions | [micro-interactions.md](./shared/micro-interactions.md) | ~55 |
| Responsive | [responsive.md](./shared/responsive.md) | ~55 |
| Component Plan | [component-plan.md](./shared/component-plan.md) | ~75 |

## Brand fidelity check

| STYLE.md constraint | Status |
|---------------------|--------|
| No gradients | Checked — none in any screen |
| No shadows or elevation | Checked — 0dp elevation everywhere |
| No rounded corners on task cards | Checked — 0dp via WillShapes.extraSmall |
| Accent on interactive elements only | Checked — bar chart uses accent as data color (justified in screen-05) |
| FontWeight max 500 | Checked — bodyLarge/bodyMedium/labelSmall all use Normal (400) or Medium (500) section headers only |
| Geist Mono for system-generated values only | Checked — timestamps, source labels, package names |
| No engagement mechanics | Checked — no streaks, no badges, no congratulatory states |
| 48dp touch targets everywhere | Checked — heightIn(min=48.dp) or IconButton on all interactive elements |
| Edge-to-edge, no app chrome | Checked — no TabRow, no BottomNavigation, no TopAppBar (just a plain row) |
| 1dp #2A2A2A borders as structural primitives | Checked — all section separators and sheet borders |
| True OLED black (#000000) as background | Checked — all screens use WillBackground |
| Sentence case | Checked — all copy in sentence case; no ALL CAPS |

## Bold bets implemented

| Bold bet | Where |
|----------|-------|
| True OLED black (#000000) — pixel off | Screen 01 HomeScreen background; all full-screen destinations |
| No visual hierarchy except content hierarchy | Screen 01 — equal-weight task cards, sections by header text only |
| Geist Mono as semantic signal | Screen 01 timestamps/source, Screen 02 lifecycle line, Screen 04 package names, Screen 05 chart labels |
| Single accent, strict semantic use | Screen 01 swipe IN_PROGRESS dot; Screen 04 Switch track; Screen 05 chart bars |
| 1dp border as only structural primitive | All screens — HorizontalDivider between every content section |
