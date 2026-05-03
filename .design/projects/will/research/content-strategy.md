# Content Strategy — Microcopy for Will
> Phase: research | Project: Will | Generated: 2026-04-16

---

## Voice Adaptation Principles

Will's voice is: dry, functional, zero wellness language, sentence case, no exclamation marks, no emoji, no affirmations. The interface is a tool — it does not congratulate users for completing tasks or motivate them with streaks.

Core rules:
- Sentence case everywhere: "Mark done" not "Mark Done" or "MARK DONE"
- Verb-first action labels: "Delete task" not "Task deletion"
- No softening qualifiers: "Delete" not "Remove (can't undo)"
- No reassurance: "All caught up" not "Great job! You're all caught up"
- No filler words: "No tasks" not "It looks like you don't have any tasks yet!"
- Numbers and times are Geist Mono — do not spell out numerals in microcopy

Source: [Smashing Magazine microcopy guide](https://www.smashingmagazine.com/2024/06/how-improve-microcopy-ux-writing-tips-non-ux-writers/), [UX Writing Hub](https://uxwritinghub.com/what-is-microcopy/)

---

## Action Labels

### Task quick actions (swipe / overlay)
| Action | Label | Notes |
|--------|-------|-------|
| Right swipe | Done | Not "Mark as done", not "Complete" |
| Left swipe | Delete | Not "Remove", not "Dismiss" |
| Overlay button | Done | Same label, consistent |
| Overlay button | Later | Not "Snooze", not "Remind me later" |
| Overlay button | Delete | Same as swipe |

Rationale: "Done" is the shortest unambiguous affirmative. "Delete" is honest — the data goes away. "Later" defers without committing to a time, which matches the current feature scope (no snooze scheduling).

### TaskActionSheet
The action sheet appears on long-press. Labels must match the swipe actions exactly — if swipe says "Done", the sheet says "Done". Inconsistency between gesture and button label is a UX defect.

Additional sheet action not available via swipe:
- "Open app" — launches the source app (fires PendingIntent)

Do not add a label like "Open in [AppName]" — the app name is already visible in the task item. "Open app" is sufficient.

---

## NLP State Labels (IntentInputSheet)

These appear below the BasicTextField in Geist Mono at small size (12sp), WillTextTertiary color.

| State | Label |
|-------|-------|
| Idle (keyboard open, no text) | *(empty — no label visible)* |
| Processing | `processing...` |
| Suggestion ready | *(label hidden, action chips appear)* |
| No intent recognized | `not sure what to do with this` |

All in lowercase. Ellipsis ("...") for in-progress states — not animated spinner text. The ellipsis is a typographic convention for "working", not a loader.

---

## Empty States

### Task feed — first launch, no notifications captured yet
```
No tasks yet.
Notifications from your apps will appear here.
```

Two lines max. No illustration. No CTA — the user doesn't need to do anything; tasks arrive passively. The second line explains the model without being instructional.

### Task feed — all tasks cleared / done
```
Nothing pending.
```

One line. The user cleared everything. No congratulation, no prompt to add more tasks. The emptiness is the reward.

### Task feed — no tasks match filter (future feature)
```
No tasks for this filter.
```

Functional. No encouragement.

### NotificationFilterSettings — no apps in list
```
No apps have sent notifications yet.
```

The list is empty because no notifications have been captured. Accurate and brief.

### FocusDashboard — no data for today
```
No activity today.
```

One line. Does not say "Start a task to see your stats!" — that's engagement language.

Source: [NN/G Empty States](https://www.nngroup.com/articles/empty-state-interface-design/), [Pencil & Paper empty states](https://www.pencilandpaper.io/articles/empty-states)

---

## Settings Labels

### NotificationFilterSettings screen title
`Notification filters`
Not "Manage notifications" (too vague), not "Notification settings" (too close to system settings label).

### Per-app toggle row
The toggle label is the app name. No secondary descriptor needed. If the app has sent notifications, the row is visible — the context is implicit.

### PrivacySettings screen title
`Data and privacy`

### Data explanation section
```
All data stays on this device. Will does not connect to the internet.
Notification content, task history, and focus metrics are stored locally in an encrypted database.
```

Two sentences. No legalese. No "we" pronoun (Will isn't a person). Passive voice is acceptable here for accuracy.

### Delete all history button
`Delete all task history`
Not "Clear data", not "Reset". Specific enough to convey the scope of the action.

### Confirmation dialog copy
```
Delete all task history?
This removes all tasks, focus metrics, and notification history. It cannot be undone.
```
Buttons: `Delete` (destructive, red) / `Cancel`

Do not write "Are you sure?" — that's a rhetorical question, not useful information.

---

## Error States

| Scenario | Copy |
|----------|------|
| NotificationListenerService not granted | `Notification access required. Tap to open settings.` |
| AccessibilityService not enabled | `Accessibility access required to track task completion.` |
| Room insert failure | *(silent — do not surface DB errors to user in MVP)* |

Permission rationale copy is functional, not persuasive. State what is required and what it enables. Do not promise benefits ("See your productivity improve!"). Do not minimize the permission's scope.

---

## Timestamp Conventions

Timestamps are shown in Geist Mono, always as relative time for items less than 24 hours old, absolute date for older items. The format is part of the content strategy: relative time (`2m ago`, `1h ago`) communicates recency; absolute (`Apr 15`) communicates context for historical tasks.

No "just now" — use the numeric value immediately: `1m ago`, not "just now", not "moments ago".
