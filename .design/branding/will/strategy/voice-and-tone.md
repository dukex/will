# Voice & Tone
> Brand: Will | Phase: strategy | Date: 2026-04-15

## Voice Attributes

Voice is fixed — it is who Will is regardless of context. Tone shifts by context. These four attributes define Will's voice.

---

### 1. Precise

**What it means:** Every word carries specific weight. Nothing is approximate, hedged, or padded. Will writes the way well-typed code reads — each token is doing something.

**What it does not mean:** Terse to the point of confusion. Precision is not word count minimization. It is the elimination of words that don't add information.

| Do | Don't |
|----|-------|
| "Notification captured. Tap to execute." | "Your notification has been successfully captured and is ready for your review." |
| "Will requires notification access to convert alerts into tasks." | "To get the most out of Will, we'd love access to your notifications!" |
| "3 tasks pending." | "You've got things to do today!" |
| "Grant permission" | "Allow Will to help you stay on track" |

---

### 2. Dry

**What it means:** No emotional inflation. No enthusiasm signals ("So exciting!"). No validation ("Great choice!"). Will does not perform positivity at the user. It states things.

**What it does not mean:** Cold or hostile. Dry is not rude. It is the difference between a good engineer's Slack message and a product marketing email. One says what happened. The other performs caring about you.

| Do | Don't |
|----|-------|
| "Task deleted." | "Done! That's one less thing to worry about." |
| "No notifications yet." | "All quiet! Enjoy the moment." |
| "App opened." | "You're in!" |
| "Notification listener not active." | "Looks like we're missing a permission. Let's fix that!" |

---

### 3. Uncompromising

**What it means:** Will has positions and states them. It does not qualify its point of view to avoid offending users who disagree. When the architecture makes a choice — no widgets, no app-grid as default, notifications as tasks — the copy says so without apologizing.

**What it does not mean:** Combative. Will does not attack competitors, lecture users, or write with contempt. Uncompromising is confidence in the position, not aggression toward alternatives.

| Do | Don't |
|----|-------|
| "Will does not support home screen widgets. Widgets are passive content surfaces." | "Will currently doesn't support widgets — we might add this in the future!" |
| "The app grid is a secondary surface. Your task feed is the home screen." | "We think tasks are more important than apps, but you can still find your apps by swiping up." |
| "No streaks. No badges. No engagement mechanics." | "We believe in organic productivity, not forced habits." |

---

### 4. Peer-to-peer

**What it means:** Will speaks to Lucas as a fellow builder — someone who already understands the problem, has already tried the alternatives, and is evaluating Will on its architectural merits. Will does not explain the attention economy to someone who has read the papers.

**What it does not mean:** Exclusive or gatekeeping. Peer-to-peer means assuming intelligence, not requiring credentials. A non-developer who understands the problem gets the same respect.

| Do | Don't |
|----|-------|
| "Notifications are task objects in Will's domain model. PendingIntents are stored in memory and fired on tap." | "Will magically turns your notifications into actions!" |
| "Open source. Read the code." | "We're committed to transparency and your privacy." |
| "This is the home screen now. There is no 'back to normal'." | "Customize Will to make it feel just right for you!" |

---

## Tone Spectrum

Will's voice is fixed. Tone adjusts by context.

### Default positions

| Scale | Will's default | Rationale |
|-------|---------------|-----------|
| Formal ↔ Casual | Slightly casual | Peer-to-peer, not corporate |
| Serious ↔ Playful | Serious | The tool has a position; levity dilutes it |
| Authoritative ↔ Friendly | Authoritative | Will states, not suggests |
| Technical ↔ Simple | Technical | Lucas is the audience; oversimplification is condescending |
| Reserved ↔ Enthusiastic | Reserved | Dry voice cannot be enthusiastic without breaking character |

### Context shifts

| Context | Shift from default | Example |
|---------|--------------------|---------|
| **UI copy / labels** | Maximum compression. No tone — just signal. | "3 pending" not "You have 3 pending tasks" |
| **Empty states** | Slightly more human. Still dry. No motivational language. | "No notifications captured yet. Will is listening." |
| **Error messages** | Technical precision. Say what happened and what to do. No apology performance. | "Notification listener disconnected. Re-enable in Settings → Accessibility." |
| **Task completion** | Flat acknowledgment. Resist the urge to celebrate. | "Done." — not "Nice work!" |
| **GitHub README** | Most peer-to-peer. Architecture explained as engineering decisions. Dry humor acceptable in comments. | "No Hilt — manual ViewModelProvider.Factory. We didn't need the ceremony." |
| **Onboarding** | Brief, direct, no-nonsense. Explain only what is structurally different. Skip the wellness framing. | "Will replaced your home screen. Notifications will appear here as tasks." |
| **Changelog / release notes** | Precise and honest. No hype. Breaking changes get the same prominence as features. | "Tapping a task now fires the source app's PendingIntent directly." |

---

## Style Rules

| Rule | Decision | Notes |
|------|----------|-------|
| **Contractions** | Yes | "Will isn't" not "Will is not" — formality creates distance |
| **Oxford comma** | Yes | "tasks, notifications, and app launches" — precision requires no ambiguity |
| **Exclamation marks** | Never | One exclamation mark breaks the dry voice permanently. None is the rule. |
| **Emoji** | Never in product or docs | Not in UI copy, README, release notes, or strategy. Developer tools don't emoji. |
| **First person** | "Will" as subject, not "we" | "Will converts notifications into tasks." Not "We convert..." — Will is the tool, not a team speaking. |
| **Sentence length** | Short default, medium when reasoning | Labels: ≤5 words. UI copy: ≤12 words. Explanations: one thought per sentence. No compound sentences in UI. |
| **Jargon** | Use it correctly or not at all | "PendingIntent," "domain model," "notification listener" are correct and used. "Synergy," "journey," "seamless" are never used. |
| **Capitalization** | Sentence case everywhere | "Task feed" not "Task Feed." Product name is "Will" (always capitalized). |
| **Passive voice** | Avoid | "Will captured the notification" not "The notification was captured." |

---

## Nomenclature

Will's language is part of its architecture argument. Consistent naming reinforces the product's position.

| Will calls it | Not this | Reason |
|---------------|----------|--------|
| **task** | to-do, item, action item, notification | A task is a unit of intent. "To-do" is a list app pattern. "Notification" is what it was before Will converted it. |
| **task feed** | home screen, dashboard, inbox | Feed implies a stream with arrival order. It's the primary surface. "Dashboard" is analytics. "Inbox" is email. |
| **notification** | alert, ping, buzz | Android's native term. Will uses it before conversion. After conversion: task. |
| **launcher** | home screen replacement, home app | Technically accurate Android term. Will does not say "replacement" — that frames the existing launcher as the default. |
| **tap to execute** | open, launch, go to | Execution is intentional. "Open" is passive. Will executes intent. |
| **captured** | received, synced, added | Notifications are captured from the attention economy. The word carries the position. |
| **pending** | upcoming, active, in progress | Tasks are pending execution. Neutral, technical, precise. |
| **grant** | allow, enable, turn on | "Grant permission" is the OS's language. Will matches it. No softening. |
