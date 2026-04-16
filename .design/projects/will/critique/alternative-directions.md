# Alternative Directions — Will
> Phase: critique | Project: Will | Date: 2026-04-16

---

## Why alternatives?

The current design is disciplined and internally consistent. These alternatives are not corrections — they are genuinely different bets on what "minimal work-surface launcher" could mean. Neither is obviously better. Both preserve the core constraints (no engagement mechanics, dark-only, OLED black, Geist type system).

---

## Direction A — Feed-as-Terminal

**Core bet:** Push the terminal analogy further into the structure, not just the aesthetics.

**What changes:**

Instead of card-per-task with 1dp borders, the TaskFeed becomes a literal line-per-task log — no card surfaces at all. Each task is a single text line:

```
●  14:32  slack       reply to marcos about deploy
●  14:28  github      review pull request #412
○  09:14  whatsapp    pay electricity bill
```

Three columns: status dot | timestamp (Geist Mono, WillTextTertiary) | source (Geist Mono, WillTextTertiary) | title (Geist Sans, WillTextPrimary). All on `#000000`, no surface, no border, no card. Sections are empty lines, not section headers.

**What stays:** OLED black, Geist type system, accent on status dot only, 48dp touch targets (full-row tap), swipe actions.

**Gain:** Extreme information density. The feed reads as a log output — reinforces the developer tool metaphor more aggressively. Every pixel is content.

**Loss:** Loss of hierarchy between task title and metadata. New users may find the columnar layout harder to parse than the card structure. The card's `#0D0D0D` surface provides a subtle scannability cue that disappears in the pure-text variant.

**When to pursue:** If user research reveals that Lucas feels the current card structure still feels "like an app" rather than a tool. The terminal direction is the maximum expression of the brand premise.

---

## Direction B — Gesture-Layered Feed

**Core bet:** The home screen is only the task feed. Everything else is behind a gesture.

**What changes:**

The HomeTopBar disappears entirely — there is no date/time row, no icon cluster. The task feed is the full screen. Access to FilterSettings, FocusDashboard, and IntentInputSheet happens via gestures:

- Swipe right from screen edge: IntentInputSheet slides in from the left
- Swipe down from top: FilterSettings appears as a full-screen overlay (replaces notification shade — requires special launcher handling)
- Long-press empty area (not a task): FocusDashboard opens
- Swipe up: AppGrid (already implemented this way)

The HomeTopBar's date/time line is removed from the persistent UI. Time is ambient — the Android status bar shows it.

**What stays:** Task feed structure, card design, all STYLE.md constraints, Geist type system.

**Gain:** This is what "edge-to-edge with no app chrome" actually means at its logical endpoint. The top bar is app chrome — it has a date (available from status bar), time (available from status bar), and two icon buttons that could be gesture-triggered. Removing it adds 48dp of content space and makes the design even more radical.

**Loss:** Discoverability drops to near-zero. Lucas may accept this; his partner may not. The gesture vocabulary (right swipe = IntentInput, not app navigation) conflicts with Android conventions for 3-button navigation users. Would require explicit gesture documentation — which contradicts the zero-onboarding bet.

**When to pursue:** If Will explicitly targets the subset of developers who prefer gesture-only interfaces and have gesture navigation enabled. Not for v1 if broad Android adoption is a goal.

---

## Relationship to current design

The current design sits between these two directions — it has the terminal aesthetic without the terminal structure (Direction A), and it has minimal chrome without zero chrome (Direction B). This middle position is the correct starting bet for v1: it makes the design distinct without requiring user retraining. Both alternatives represent paths for future major versions if the v1 design research reveals specific friction points.
