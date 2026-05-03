# Strengths — Will
> Phase: critique | Project: Will | Date: 2026-04-16

---

## What works and must be preserved

### 1. Brand contract discipline

The design executed on STYLE.md with 25/25 brand contract compliance. Not a single gradient, shadow, rounded corner on a task card, or accent on a non-interactive element across six screens. This level of constraint adherence is rare in a first-pass design — it means the build phase will not need to fight the design to implement the brand.

### 2. Confirm-delete pattern consistency

Screen 02 (TaskActionSheet) and Screen 06 (PrivacySettings) both implement confirm-delete as an inline state change rather than a dialog. This is the right choice: a destructive action confirmation as a modal dialog is an engagement mechanic (it demands attention, creates anxiety, adds friction). The inline pattern is direct and calm, matching the STYLE.md voice precisely. Preserve this pattern as-is — do not revert to `AlertDialog` under implementation time pressure.

### 3. Geist Mono as semantic signal — execution is exact

The Mono/Sans split is applied correctly on every screen without exception: timestamps, source labels, package names, chart axis labels — all Mono. Task titles, action labels, section headers, explanatory copy — all Sans. This is a structural decision that makes the product legible to the target persona the same way syntax highlighting makes code legible to a developer. It is the design's most architecturally interesting choice.

### 4. Empty states

All empty states are two lines maximum, declarative, and contain no illustration. "no tasks yet" / "nothing yet" / "no apps yet" — these are correct and on-voice. The temptation to add a small illustration or a CTA ("add your first task →") must be resisted permanently. The absence communicates the brand: the screen does not perform helpfulness.

### 5. PrivacySettings as trust surface

Screen 06 is the strongest trust signal in the product. It names specific permissions, provides single-sentence rationales, and uses the exact voice of a developer who respects the user's intelligence: "to capture notifications as tasks," "to detect when tasks are completed." No marketing language. No "we take your privacy seriously." Preserve this verbatim — it is the best copy in the product.

### 6. TaskItem lifecycle context in TaskActionSheet

The lifecycle context line — `"in progress · started 14:32 · 2m ago"` in Geist Mono, WillTextTertiary — is a small design decision with high utility. The user sees the task's actual state in context, which makes the action options ("pause" vs "mark done") semantically grounded. This is good information architecture at the component level.

### 7. Zero device mockups, zero lifestyle photography, zero illustration

The design is consistent with the imagery style spec: the UI is the imagery. This commitment must hold through Play Store screenshots, README assets, and any future marketing materials. The moment a device frame appears around a Will screenshot, the brand argument collapses.

### 8. Nielsen score on Heuristic 8 — Aesthetic and minimalist design: 5/5

The design earned a perfect score on the most subjective heuristic because it passed the hardest test: every element serves the primary goal and nothing competes with content. This is not easy to achieve and even harder to maintain under stakeholder pressure to add features. The current design provides an excellent baseline to defend against scope creep.
