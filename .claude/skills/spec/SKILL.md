---
name: spec
description: Turn a demand into a junior-ready GitHub issue spec. Trigger when the user runs `/spec`, says "specify this demand", "create a spec issue", "write a spec for X", "specifica essa demanda", "abre uma issue de spec", or asks to translate a vague feature/bug request into a fully-detailed GitHub issue. 
allowed-tools: Bash, Read, Grep, Glob, Edit, Write, Agent, AskUserQuestion
argument-hint: [what to build/fix/change]
---

Demand: 
```
$ARGUMENTS
```

If no demand text is available, STOP and reply:
`ERROR: /spec requires a demand description. Usage: /spec <what to build/fix/change>`.

## Hard Rules — Read Before Anything Else

1. **ULTRATHINK** through every phase. The output must let a junior developer implement the demand end-to-end with zero ambiguity. Shallow output is failure.
2. **Phases are sequential and mandatory.** This skill runs in 8 phases. Do not skip, reorder, or collapse them. Do not write the final spec body before exploration is complete. More phases > fewer phases — speckit-style depth is the floor, not the ceiling.
3. **No code is written by this skill.** Output is a GitHub issue only. Do not edit project source files, create branches, or run tests.
4. **GitHub is `gh` only.** Every GitHub interaction (auth check, repo view, issue create, issue edit, label list, comment, view) MUST use the `gh` CLI via Bash. Never construct GitHub URLs by hand. Never call the GitHub web API directly.
5. **Kill ambiguity with `AskUserQuestion`.** Whenever a decision in the demand has multiple reasonable interpretations AND the choice changes scope, behavior, security, or UX in a non-trivial way, you MUST use the `AskUserQuestion` tool with concrete options instead of guessing. Reserve assumptions for low-impact gaps. The goal is **zero unresolved doubts** before writing the final spec.
6. **Output language matches the demand language.** PT-BR demand → PT-BR issue. English demand → English issue. Never mix.
7. **Verify, do not assume.** Every file path, function name, class, route, table, or symbol referenced in the spec MUST be confirmed by reading the file or grepping the repo. Stale memory is forbidden — re-read.
8. **Junior-developer test.** Before posting the final issue, re-read the body and ask: "Could a junior dev who has never touched this repo implement this without asking a single question?" If no, expand until yes.

---

## Phase 1 — Pre-flight & Demand Triage

**Goal:** Fail fast on missing prerequisites; classify the demand so later phases focus the right way.

1. **Validate the demand** — non-empty, non-whitespace. If empty, STOP with the error message above.
2. **Validate environment with `gh`**:
   - `gh auth status` — if unauthenticated, STOP and instruct the user to run `gh auth login`.
   - `gh repo view --json nameWithOwner,defaultBranchRef -q '{repo:.nameWithOwner, branch:.defaultBranchRef.name}'` — capture repo and default branch. If it fails, STOP and tell the user the cwd has no GitHub remote.
3. **Detect demand language** (PT-BR vs English vs other). Lock the issue language to that.
4. **Classify the demand type** (one of: `feature`, `bug`, `refactor`, `chore`, `docs`, `infra`). The classification drives section emphasis (e.g. `bug` requires a regression test plan; `refactor` requires a "no user-visible change" note in Summary).
5. **Detect surface area** by skimming, not deep-reading: which top-level areas of the repo are likely involved (`task/`, `launcher/`, `notification/`, `overlay/`, `ui/`, `infrastructure/`, manifest, build files, design docs)? Just list them — Phase 4 confirms.

Hold context for Phase 2. Do not output anything to the user yet.

---

## Phase 2 — Clarify Critical Ambiguities (AskUserQuestion)

**Goal:** Drive the doubt count to zero before writing the spec. Use `AskUserQuestion` instead of guessing on anything that matters.

1. List every ambiguity in the demand. For each, decide:
   - **High-impact** (changes scope, security, UX, public surface, or data shape) → must be asked via `AskUserQuestion`.
   - **Low-impact** (a reasonable default exists, the choice is reversible cheaply) → record as `Assumption:` later, do not ask.
2. For high-impact ambiguities, batch them into a single `AskUserQuestion` call (do not interrogate one by one). Each question MUST provide concrete multiple-choice options — never free-form prompts. Cap at **5 questions**; if more exist, ask the 5 highest-impact and assume defaults for the rest (record as assumptions).
3. Wait for the answers. Merge them into the working understanding of the demand.
4. If the user's answer to any question fundamentally changes the demand, **return to Phase 1** and re-classify.
5. Persist the Q&A — they go into a "Clarifications resolved" section in the final issue body.

> **Skip rule:** If the demand is already crisp and unambiguous (e.g. "add a one-line copy change to the empty state"), do not ask anything. Forced questions are noise.

---

## Phase 3 — Open the GitHub Issue Skeleton

**Goal:** Get a stable issue number/URL fast so later phases write to one place. The skeleton is intentionally thin — it gets fully replaced in Phase 8.

1. **Generate a concise title** (≤ 72 chars), action-noun format, no trailing period, no emoji, no number prefix. Examples: `Add per-app notification filter`, `Fix swipe-up gesture conflict on app drawer`, `Refactor TaskRepository to support batch insert`.
2. **Resolve labels** (best-effort, never block on failure): `gh label list --limit 200 --json name -q '.[].name'`. If labels matching the demand type (`feature`, `bug`, `refactor`, etc.) exist, attach them via `--label`. Do **not** create new labels.
3. **Create the issue** with `gh issue create --title "<title>" --body-file "$TMPDIR/spec-skeleton.md" [--label ...]`. Write the skeleton body to a temp file under `$TMPDIR` first, then pass with `--body-file`:

~~~markdown
## Demand (raw)

$ARGUMENTS

---

> 🚧 **Spec in progress.** This issue was opened by `/spec` and is being expanded. Do not start implementation until this notice is removed and the structured sections below are populated.
~~~

4. Capture the issue URL and number from `gh issue create` stdout. Print one line to the user: the URL plus "exploring the codebase…". Move on immediately — no commentary.

---

## Phase 4 — Deep Project Exploration

**Goal:** Build a complete mental model of how the demand intersects the existing codebase. Gather evidence; do not write prose yet.

Use parallel sub-agents (`Agent` with `subagent_type: Explore`) for independent searches. For broad codebase scans (>5 files across unrelated areas), swarm 5–8 files per agent. Do not run independent searches sequentially.

### 4.1 Read the project map
- `CLAUDE.md` (project root) — the index. Treat it as ground truth for what exists and what the rules are.
- Every doc it points to that is relevant to the demand (e.g. `docs/01-IDEA.md`, `docs/99-TASKS.md`, the `.design/` design system).
- `README.md` if present.

### 4.2 Locate the touch points
For the demand, identify and **actually open**:
- Domain models that will change or be read.
- Repositories / DAOs / data sources involved.
- ViewModels that will consume the change.
- Composables / screens that render the affected state.
- Infrastructure layer (Room entities, SharedPreferences, services).
- Manifest entries, permissions, intent filters if relevant.
- Existing tests for the touched areas — they reveal the test conventions you must follow.

For each touch point, record: **file path, line range, and one sentence on its current behavior.** This becomes the "Files to change" table.

### 4.3 Trace the data flow end-to-end
Pick the most likely happy path for the demand and trace it: input source → repository → ViewModel → UI. Note every transformation. If the demand introduces a new flow, find the most analogous existing flow and study it as the reference.

### 4.4 Find conventions and idioms
Before proposing any pattern, confirm what the project already does:
- Architecture style (domain/infrastructure separation? manual DI? Hilt?).
- Threading (which dispatcher? injected? hardcoded?).
- Persistence (Room? SharedPreferences? both? when?).
- UI patterns (StateFlow + collectAsState? remember? LaunchedEffect?).
- Test style (unit tests against in-memory repos? Robolectric? instrumented?).
- Commit convention and changelog rules (see `CLAUDE.md` "Development workflow").
- Design system rules (see `.design/branding/will/patterns/STYLE.md` if UI is involved).

### 4.5 Identify risks and unknowns
- What breaks if this lands wrong? (data migration, permission flow, lifecycle issue, performance regression).
- What edge cases must the implementation handle? (empty state, max limits, concurrent updates, process death, permission denial, offline, dark mode, RTL, accessibility).
- Are there unanswered ambiguities still left? If any are high-impact, **return to Phase 2** and ask via `AskUserQuestion`. Do not paper over them with assumptions.

---

## Phase 5 — Architectural Design

**Goal:** Decide *how*. Pin the architecture to project conventions found in Phase 4 — no novel patterns unless explicitly justified.

### 5.1 Generate alternatives
Produce **at least 2 viable implementation approaches** plus the recommended one. For each:
- One-line summary.
- Pros (1–3 bullets).
- Cons (1–3 bullets).
- Why rejected (or why recommended) — tied to project conventions, not abstract preference.

### 5.2 Architectural improvements
Split rough edges spotted during exploration into two buckets:
- **In-scope improvements** — required to ship the demand cleanly. Justify each.
- **Out-of-scope follow-ups** — adjacent rough edges. Listed in the issue for triage, not implemented here.

### 5.3 Cross-cutting concerns
For each that applies, write one decision sentence: threading, persistence, navigation, permissions, accessibility, theming, telemetry, error handling, migration strategy.

---

## Phase 6 — Implementation Plan

**Goal:** Slice the work into atomic, reviewable steps a junior dev can execute one at a time.

For each step, capture:
- **Title** — short imperative.
- **File** — `path/to/file.kt:LL-LL` (verified to exist; line range may be approximate but the file must be real).
- **What** — the change in one sentence.
- **How** — enough detail that no creativity is required: signatures, names, fields to add, where exactly. Show short code snippets where the contract matters.
- **Test** — which test file gets the new test, and the test name (red first, per the project's TDD rule).
- **Commit message** — a conventional-commit subject, respecting the project's rule that `feat:` / `fix:` are user-facing only; internal work uses `refactor:`, `chore:`, `test:`, `docs:`, `ci:`, `style:`.

Each step must be small enough to commit atomically. The final step should always include the changelog update if `CLAUDE.md` requires one.

---

## Phase 7 — Test Plan

**Goal:** Make TDD viable. The plan must let a junior dev write red tests first.

Cover, in order:
- **Unit tests** — file path, test name, what it asserts, which existing in-memory repo it uses.
- **Integration / E2E tests** — mandatory after a full feature per project rules. File path, scenario, what it integrates.
- **Regression tests** — only if the demand is a bug. Reproduce-the-bug test goes in first.
- **Manual QA checklist** — only for changes that cannot be fully automated (UI gestures, overlay permissions, notification interception). Short, concrete steps.

---

## Phase 8 — Assemble & Post the Final Issue Body

**Goal:** Replace the skeleton body with a self-contained spec.

1. Write the final body to `$TMPDIR/spec-final.md`.
2. Run `gh issue edit <number> --body-file "$TMPDIR/spec-final.md"`. Never pass the full body via `--body` on the command line — large bodies get mangled by the shell.
3. Re-fetch the issue with `gh issue view <number>` to confirm the body was saved correctly. If sections are missing or rendering is broken, fix and re-edit.

The final body MUST follow the structure in `body-template.md` (alongside this file), in that exact order. Omit a section only if it truly does not apply, replacing it with one line explaining why.

---

## Final Validation Before You Stop

Run this checklist mentally and tighten anything that fails. Do NOT print the checklist to the user — fix and repost.

- [ ] Issue was created in Phase 3 and **edited** (not re-created) in Phase 8.
- [ ] Every file path mentioned exists (verified by `Read` or `ls`, not memory).
- [ ] Every symbol mentioned (class, function, field) was confirmed by grep or read.
- [ ] No `[NEEDS CLARIFICATION]` markers remain. High-impact ambiguities were resolved via `AskUserQuestion` in Phase 2 and listed under "Clarifications resolved". Low-impact ones are `Assumption:` lines.
- [ ] At least 2 alternatives + 1 recommendation are documented.
- [ ] Each implementation step has: file/line, what, how, test, commit message.
- [ ] Acceptance criteria are testable.
- [ ] The "junior-developer test" passes: a junior could implement this without asking anything.
- [ ] The body language matches the demand language.
- [ ] The 🚧 "Spec in progress" notice from Phase 3 is gone.

## Final Output to the User

One short message: the issue URL, one sentence on what was specced, and the count of implementation steps. Nothing else. The spec lives in the issue.