# Contributing to Will

Thanks for considering a contribution. This document covers how to propose
changes, the engineering workflow we follow, and the constraints that make
Will *Will* (and not yet another launcher).

## Ground rules

- **Be respectful.** See [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).
- **Open an issue first** for anything beyond a one-line fix. A short design
  conversation up front avoids throwing away PRs.
- **One logical change per PR.** Refactors, features, and chores are separate
  PRs.
- **No bundled "while I was there" cleanups.** If you spotted something
  worth fixing, open a follow-up issue or a separate PR.

## Workflow: XP / TDD

We work as Extreme Programming pair programmers. The rules are
non-negotiable:

1. **Tests first.** Before any production code, write a failing test that
   reflects the user-visible behavior you want.
2. **Red → Green → Refactor.** Implement the minimum to make the test pass,
   then refactor with tests still green.
3. **Edge cases up front.** Consider null/empty/boundary cases in the test
   list, not after the fact.
4. **Baby steps.** Small commits, each one a coherent step.
5. **E2E / regression test on full features.** After implementing a user
   flow end-to-end, add an instrumented or E2E test that exercises the
   integrated path.
6. **Bug = regression test first.** Reproduce the bug with a failing test
   before fixing it.

## User Stories: INVEST

If your contribution maps to a user story, the story should be
**I**ndependent, **N**egotiable, **V**aluable, **E**stimable, **S**mall,
**T**estable. If it isn't small enough to ship in one go, split it.

## Commits

Atomic conventional commits, one logical change per commit:

- `feat:` — user-visible new capability. Goes in the changelog. Write the
  message as a user-facing description: *"now you can pin apps to the home
  screen"*, not *"add PinnedAppsRepository interface"*.
- `fix:` — user-visible bug fix.
- `refactor:` — internal restructuring with no user-visible change.
- `test:`, `docs:`, `ci:`, `style:`, `chore:` — internal work, **not**
  shown to end users.

`feat:` and `fix:` are reserved for things the end user actually sees in the
product. UI theme tweaks of the docs site, dependency bumps, CI fixes, and
test infrastructure are **not** `feat:` or `fix:`.

## Verification before opening a PR

Run all of the following locally and make sure they pass:

```bash
./gradlew test
./gradlew lint
./gradlew compileDebugKotlin
```

CI runs the same commands on every PR. Don't ask reviewers to do it for
you.

## Launcher UX constraints

Will is a **launcher**, not a regular app. These rules apply to every layout
decision:

- **No app chrome.** No `TabRow`, no `TopAppBar`, no bottom nav. Those are
  app patterns, not home-screen patterns.
- **The task feed is the home screen.** Edge-to-edge, status bar overlays
  content.
- **The app grid is secondary** — accessible via an explicit button in the
  top bar (and swipe up as a bonus gesture), never a tab.
- **Permission prompts are small banners**, never full-screen dialogs that
  block the home screen.

## Design system

A complete brand identity lives at `.design/branding/will/patterns/`. When
implementing UI, follow it:

- `background = Color(0xFF000000)` — OLED true black, never dark gray.
- Accent `#4F6EF7` only on **interactive** elements. Audit `primary` usage
  in M3 — it leaks to Snackbar / Checkbox / Switch / Chip.
- `FontWeight.Normal` (400) for content, `Medium` (500) for section headers.
  No SemiBold or Bold.
- `extraSmall = RoundedCornerShape(0.dp)` — task cards always 0dp radius.
- No shadows, no gradients, no elevation.
- 48dp minimum touch targets.
- Use the `Spacing` object from `ui/theme/Theme.kt`, not hardcoded dp.

See [`.design/branding/will/patterns/STYLE.md`](.design/branding/will/patterns/STYLE.md)
for the full contract.

## Architecture

- `task/` is pure Kotlin domain. **No Android imports** — not even
  `Context`, not even Room annotations.
- Room lives in `infrastructure/room/` with mappers to and from domain
  models.
- ViewModels expose `StateFlow`. Compose collects with
  `collectAsStateWithLifecycle`.
- Manual DI via `ViewModelProvider.Factory`. No Hilt, no Koin.

If your change crosses these boundaries, flag it in the PR — there is
usually a reason and we want to discuss it explicitly.

## Reporting bugs

Use the GitHub issue tracker. A useful report includes:

- Android version and device.
- Reproduction steps.
- Expected vs. actual behavior.
- `adb logcat` output if relevant.

For security issues see [`SECURITY.md`](SECURITY.md) — do not file public
issues for vulnerabilities.

## License

By contributing, you agree your contributions are licensed under the
[MIT License](LICENSE).
