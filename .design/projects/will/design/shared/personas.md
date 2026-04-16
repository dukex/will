# Personas
> Phase: design | Project: Will | Generated: 2026-04-16
> Source: .design/branding/will/BRIEF.md

---

## Persona 1 — Primary: Lucas

**Role:** Developer / product designer  
**Age:** 25–35  
**Device:** Android, OLED screen (Pixel, Galaxy)

**Day-in-the-life:** Starts the day with a backlog of Slack messages and GitHub notifications. Uses the phone as a work tool but keeps getting pulled into social media or infinite scroll. Has tried Notion, Todoist, and several launchers — each requires too much maintenance or adds cognitive overhead.

**Frustration:** Every "minimal" launcher is either all aesthetics with no function, or productivity apps that feel like more work on top of work. He doesn't want to be managed — he wants the phone to stop wasting his time.

**Aspiration:** Pick up the phone, do the thing, put it down. The phone should match his mental model: there's a task, complete it, move on.

**Trust signals:** Open source, clear opinions, no dark patterns. Distrusts apps with engagement metrics, growth hacks, or UI that celebrates streaks.

**Discovery:** GitHub, indie hacker communities, Twitter/X tech circles, word-of-mouth from developer peers.

---

## Design implications

| Insight | Decision |
|---------|----------|
| Lucas is a developer — reads UI like code | Font semantics (Mono = system data, Sans = content) register immediately |
| Hates engagement mechanics | No streaks, no badges, no count indicators anywhere |
| Wants zero maintenance | Notification → task must be automatic, zero-touch |
| Trusts open source, distrusts dark patterns | OLED black is the engineering argument made visible — not aesthetic choice |
| "Pick up, do the thing, put it down" | Launcher must disappear. No chrome, no decorative surfaces, no app promotion |
| Voice input matters for hands-free capture | IntentInputSheet voice path must be inline — no system dialog that breaks immersion |
| Wants retrospective data | FocusDashboard accessible in one gesture, data-dense, no gamification |
| Pixel/Samsung OLED device | `#000000` background is observable — competing launchers use `#1B1B1F` |
| No onboarding tolerance | Zero first-run flows, no tooltips, assumes intelligence |
