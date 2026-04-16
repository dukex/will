# Research Index — Will Project
> Phase: research | Project: Will | Generated: 2026-04-16

---

## What this research covers

Android launcher MVP research for US-03 through US-08. Covers UX patterns for task-based launchers, competitor interaction analysis, Compose-specific technical patterns, accessibility requirements, content strategy, and reference API specifications.

This research does not cover brand-level decisions (already decided), light theme, landscape layout, cloud sync, or tablet layout — all out of scope.

---

## Files

| File | Contents | Priority reading for |
|------|----------|----------------------|
| [ux-patterns.md](./ux-patterns.md) | Feed patterns, swipe interactions, bottom sheet patterns, settings patterns, empty states, dashboard patterns | All screens |
| [competitor-ux.md](./competitor-ux.md) | Niagara Launcher, Minimalist Phone, Linear Mobile — deep interaction analysis, comparison matrix, opportunity gaps | TaskItem, TaskFeed, HomeTopBar |
| [technical-research.md](./technical-research.md) | SwipeToDismissBox, ModalBottomSheet, SpeechRecognizer, AccessibilityService, Canvas charts, Room+Flow, ML Kit vs regex | All components |
| [accessibility-patterns.md](./accessibility-patterns.md) | Touch target requirements, TalkBack traversal, semantics for swipe actions, focus management in bottom sheets, dynamic content descriptions | TaskItem, TaskActionSheet, IntentInputSheet |
| [content-strategy.md](./content-strategy.md) | Action labels, NLP feedback states, empty state copy, settings labels, error states, timestamp conventions | All UI text |
| [reference-specs.md](./reference-specs.md) | API URLs with key takeaways and Will-specific application notes | Implementation phase |
| [recommendations.md](./recommendations.md) | Adopt/Adapt/Avoid synthesis — NLP approach, chart library, swipe threshold, SpeechRecognizer pattern, settings navigation | Architecture decisions |

---

## Key decisions this research supports

| Decision | Finding | See |
|----------|---------|-----|
| NLP approach | Regex pipeline for MVP — ML Kit is beta, 5.6MB, wrong entity types | [recommendations.md](./recommendations.md), [technical-research.md](./technical-research.md) |
| Chart rendering | Custom Canvas — no library. `drawRect()` + `drawWithCache` | [recommendations.md](./recommendations.md), [technical-research.md](./technical-research.md) |
| Voice input | `SpeechRecognizer` direct (not system dialog intent) | [recommendations.md](./recommendations.md), [reference-specs.md](./reference-specs.md) |
| Swipe threshold | 40% width, bidirectional: right=done, left=delete | [ux-patterns.md](./ux-patterns.md), [reference-specs.md](./reference-specs.md) |
| Settings navigation | Single tap from HomeTopBar — no intermediate screens | [ux-patterns.md](./ux-patterns.md), [recommendations.md](./recommendations.md) |
| Accessibility | `mergeDescendants + customActions` on all TaskItems | [accessibility-patterns.md](./accessibility-patterns.md) |
| Empty states | Hide empty sections entirely — no "nothing here" text per section | [ux-patterns.md](./ux-patterns.md), [content-strategy.md](./content-strategy.md) |
| NLP state labels | Lowercase, Geist Mono, ellipsis for in-progress | [content-strategy.md](./content-strategy.md) |

---

## Gaps not covered by this research

- Per-app deep link patterns for specific third-party apps (WhatsApp, banking apps) — requires per-app investigation
- AccessibilityService completion signal patterns per target app — requires runtime testing
- Specific regex patterns for the NLP pipeline — requires user testing with real task inputs
- Room migration strategy for adding `startedAt` / `completedAt` fields — straightforward nullable column additions, no research needed
