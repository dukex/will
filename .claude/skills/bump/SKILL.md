---
description: Bump the will project version, update CHANGELOG, create a git tag and push
allowed-tools: Read Edit Write Bash
---

Bump the version in `app/build.gradle.kts`, update `site/docs/changelog.md`, update the project `CHANGELOG.md`, commit, tag, and push.

## Steps

**1. Find commits since last bump**

```bash
git describe --tags --abbrev=0 2>/dev/null
```

If a tag exists, collect commits since it:

```bash
git log <last_tag>..HEAD --oneline
```

If no tag exists, collect all commits:

```bash
git log --oneline
```

**2. Determine bump type**

Analyze the commits and pre-select the most appropriate bump, then use `AskUserQuestion` to confirm:

- **patch** (x.x.X) — only `fix:` commits
- **minor** (x.X.0) — any `feat:` commit
- **major** (X.0.0) — only when there is a `feat!:` or `fix!:` commit (breaking change marker)

**3. Read current version**

Read `app/build.gradle.kts`, extract `versionCode` and `versionName`, compute the new version by incrementing the chosen segment and resetting lower ones to 0.

`versionCode` - Is an integer that should be incremented by 1 for every release.
`versionName` - Is a string that follows the format "MAJOR.MINOR.PATCH". Depending on the bump type, increment the appropriate segment and reset lower segments to 0.

**4. Update `app/build.gradle.kts`**

Edit the `versionCode` and/or `versionName` field to the new value.

**5. Update `site/docs/changelog.md` and the `CHANGELOG.md`**

Read the existing `site/docs/changelog.md` (create it if missing using Keep a Changelog format). Prepend a new section at the top:

```markdown
## [<new_version>] - <YYYY-MM-DD>

### Added

- <feat: commits listed here>

### Fixed

- <fix: commits listed here>

### Changed

- <other relevant commits>
```

Only include `feat:`, `fix:`, and breaking change commits. Skip `chore:`, `docs:`, `test:`, `ci:`, `refactor:` unless they carry user-visible impact. Strip the conventional commit prefix — write the message as a clean changelog line.

If `site/docs/changelog.md` did not exist, also add a header:

```markdown
# Changelog

All notable changes to this project will be documented in this file.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
Versioning: [Semantic Versioning](https://semver.org/spec/v2.0.0.html)
```

The same rules apply to `CHANGELOG.md`

**6. Commit, tag, push**

```bash
git add app/build.gradle.kts site/docs/changelog.md CHANGELOG.md
git commit -m "chore: bump version to v<new_version>"
git tag v<new_version>
git push origin main --tags
```

**7. Confirm**

Print: `Bumped to v<new_version> — tag pushed.`
