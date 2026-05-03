# Information Architecture
> Phase: design | Project: Will | Generated: 2026-04-16

---

## Hierarchy

```
Will Launcher
├── HomeScreen (primary — the phone's home screen)
│   ├── HomeTopBar — date/time + settings entry point
│   ├── TaskFeed — sectioned list: active → pending → done
│   │   ├── TaskItem (swipeable: right=done, left=delete)
│   │   │   ├── TaskStatusIndicator (dot)
│   │   │   ├── Task title + source label
│   │   │   └── Timestamp (created / started)
│   │   └── EmptyState (no tasks — two variants)
│   ├── NotificationPermissionPrompt (conditional banner)
│   └── ActiveTaskOverlay (floating bubble — existing)
│
├── AppGrid (gesture-up from HomeScreen)
│   ├── SearchField
│   └── AppRow (alphabetical)
│
├── IntentInputSheet (bottom sheet — gesture or tap)
│   ├── BasicTextField (typed input)
│   └── NLPStateRow (idle / processing / suggestion)
│
├── TaskActionSheet (bottom sheet — long-press or overlay)
│   ├── LifecycleContextLine (timestamp + state label)
│   ├── Action: mark done
│   ├── Action: start / pause
│   ├── Action: delete (with confirm step)
│   └── Dismiss
│
├── NotificationFilterSettings (full-screen — tap from HomeTopBar)
│   ├── Header: "notifications"
│   └── AppToggleRow (app name + package + Switch)
│
├── FocusDashboard (full-screen — swipe gesture or HomeTopBar)
│   ├── FocusScoreCard (large number + label)
│   ├── TimeOnTaskChart (Canvas bar chart)
│   └── ContextSwitchRow (count + label)
│
└── PrivacySettings (full-screen — via NotificationFilterSettings)
    ├── Data explanation block
    ├── Delete all history (confirm pattern)
    └── Permission rationale (per-permission)
```

---

## Content grouping rationale

### TaskFeed sections

Three sections, order by urgency:

| Section | Condition | Empty behavior |
|---------|-----------|----------------|
| Active | `status == IN_PROGRESS` | Hide section entirely |
| Pending | `status == PENDING` | Show only if no active tasks; hide if empty too |
| Done | `status == DONE`, today only | Hide if empty |

When all sections are empty: show contextual empty state (see screen-01).

### Settings entry point

NotificationFilterSettings and FocusDashboard are both accessible from a single icon in HomeTopBar. No secondary navigation layer. Tapping the icon opens NotificationFilterSettings. FocusDashboard is accessible from the same top bar (separate icon or gesture).

PrivacySettings lives at the bottom of NotificationFilterSettings — a single "data & privacy" row that navigates to the PrivacySettings screen.

### Navigation depth

Maximum depth: 2 screens from HomeScreen. HomeScreen → any settings screen is one tap. No sub-settings, no back stacks beyond one level.

---

## Data flow

```
NotificationListenerService
    → Room DB (TaskEntity: id, title, source, packageName, status, 
                           createdAt, startedAt, completedAt, contextSwitchCount,
                           notificationKey, pendingIntent)
    → TaskDao (Flow<List<TaskEntity>>)
    → TaskViewModel (StateFlow<List<Task>>)
    → TaskFeed composable (collectAsStateWithLifecycle)
```

```
BasicTextField
    → IntentInputViewModel (NlpState)
    → NLP pipeline (regex → action suggestion)
    → TaskViewModel.addTask()
    → Room DB
```

```
AccessibilityService
    → TaskViewModel.markStarted(task) → startedAt
    → TaskViewModel.markDone(task) → completedAt
    → contextSwitchCount++
```
