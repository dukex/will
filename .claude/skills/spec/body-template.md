# Final issue body — structure

The final GitHub issue body produced by the `/spec` skill MUST follow this structure, in this exact order. Replace `<...>` placeholders with concrete values from Phases 1–7. Omit a section only if it truly does not apply, replacing it with a single line explaining why.

---

## Summary

<2–4 sentences. What changes for the end user, in plain language. No jargon. If the demand is internal/refactor, say "no user-visible change" and explain the engineering payoff.>

## Demand (raw)

<verbatim copy of the demand — preserved for traceability>

## User Story

As a <user persona — for Will, default to **Lucas**, the developer 25–35 persona from `.design/branding/will/BRIEF.md`, unless the demand clearly targets another user>,
I want <capability>,
so that <outcome / why it matters>.

### Acceptance Criteria

- [ ] <Given/When/Then or testable bullet — one per criterion>
- [ ] ...

## Context

### Current behavior
<What the code does today, with file:line references. 3–8 bullets.>

### Why this change
<Motivation. Tie back to the demand and to project goals from `docs/01-IDEA.md` if relevant.>

## Implementation Plan (step-by-step)

> Numbered steps. Each step is small enough to commit atomically. Each step references concrete files and shows code-level guidance. A junior dev should be able to do step N without re-reading step N-2.

1. **<step title>** — `path/to/file.kt:LL-LL`
   - What: <one sentence>
   - How: <signatures, field names, where exactly>
   - Test: <test file and test name>
   - Commit: `<conventional-commit subject>`

2. **...**

## Files to Change

| File              | Change               | Why              |
| ----------------- | -------------------- | ---------------- |
| `path/to/file.kt` | new / edit / delete  | one-line reason  |

## Tests Required

Follow the project's red → green → refactor cycle from `CLAUDE.md`.

- **Unit tests**
  - `test/.../FooTest.kt` — `<test name>` — covers <what>
- **Integration / E2E tests** (mandatory after a full feature)
  - `<file>` — `<scenario>`
- **Regression tests** (only if this is a bug fix)
  - `<file>` — `<bug repro>`
- **Manual QA** (only if not fully automatable)
  - <concrete step-by-step>

## Architectural Notes

### Decisions
- **<Decision title>**: <what was decided> — *because* <reason rooted in project conventions or constraints found in exploration>.

### Alternatives considered
- **<Alternative A>** — Pros: ...; Cons: ...; **Rejected because** <reason>.
- **<Alternative B>** — Pros: ...; Cons: ...; **Rejected because** <reason>.

### Architectural improvements (in scope)
<Justified improvements bundled with this demand.>

### Suggested follow-ups (out of scope)
<Adjacent rough edges. One line each. For triage, not for this issue.>

## Edge Cases & Constraints

- <empty state, limits, concurrency, process death, permission denial, offline, dark mode, accessibility, etc.>

## Assumptions

<Every place where a low-impact ambiguity got a default. High-impact ambiguities should already have been resolved via AskUserQuestion in Phase 2.>

- **Assumption:** <statement>. *Verify:* <how the reviewer can confirm/reject>.

## Clarifications resolved

<If Phase 2 asked anything, list each question and the user's answer here. Skip the section if no questions were asked.>

- **Q:** <question>
  **A:** <user's answer>

## Definition of Done

- [ ] All acceptance criteria pass
- [ ] All tests in "Tests Required" exist and are green
- [ ] `./gradlew compileDebugKotlin` passes
- [ ] `./gradlew lint` passes (if configured)
- [ ] Changelog updated per `CLAUDE.md` rule
- [ ] Commits follow conventional-commit rules from `CLAUDE.md`
- [ ] No new TODOs left in code
- [ ] No dead code introduced

## References

- `CLAUDE.md` — project rules
- <other doc / design / spec links surfaced during exploration>
- <links to the existing analogous flow used as reference, with file:line>