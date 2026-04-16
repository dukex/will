# Brand Platform
> Brand: Will | Phase: strategy | Date: 2026-04-15

## Purpose (Why)

The home screen is not neutral. It is designed — by engineers following engagement metrics — to maximize time spent on the device. Every notification badge, every feed scroll, every autoplay is a deliberate architectural decision made by someone whose success metric is opposite to yours.

Will exists because that premise should be optional, not mandatory. A phone is hardware. The home screen is software. Software can be rewritten.

---

## Vision

A phone that behaves like the other tools in a developer's environment: opens when needed, does the specific thing, gets out of the way. No surface engineered to steal the next five minutes.

---

## Mission

Replace the Android home screen with an interface where every element serves the user's intent. Convert notifications into actionable task objects. Remove every surface with no function. Ship it open source so the architecture can be inspected, forked, and trusted.

---

## Values

These are behavioral commitments. Each one traces to a specific product decision — they are not aspirational statements.

### 1. Architecture over aesthetics

Will does not pursue minimalism as a visual goal. It pursues minimalism as an engineering outcome: every element that serves no function gets removed. The result looks minimal. That is a byproduct, not the objective.

**In practice:** A feature is not added because it looks clean. A feature is added because Lucas needs it to execute an intent. If a visual element cannot justify its presence in functional terms, it is not shipped.

### 2. No engagement mechanics, ever

No streaks. No usage badges. No "you've been productive today" banners. No notifications Will generates to bring you back to Will. The home screen built on engagement mechanics cannot credibly claim to oppose them. Will does not ship engagement mechanics in any form, by any name.

**In practice:** If a proposed feature would increase daily opens, time-in-app, or return visits as a *goal* — it is rejected. Utility-driven opens are fine. Engagement-driven opens are not the same thing.

### 3. Open source is non-negotiable

The claim "we have no dark patterns" is a marketing statement from a closed-source product. It is a verifiable fact from an open-source one. Will's architecture is public because Lucas's trust is conditional on auditability. The source code is the brand promise made legible.

**In practice:** No closed features. No analytics that cannot be read in the codebase. No proprietary backend services that process user data without the code being readable.

### 4. Assume intelligence

Will does not explain to Lucas why distraction is bad. It does not onboard him with a screen about focus and wellbeing. It assumes he already knows — that is why he is here. Every interaction assumes a user who understands the problem and wants the tool, not the lecture.

**In practice:** No motivational copy. No tooltips explaining concepts he grasps. No progress meters gamifying his own behavior back to him. Help text is brief and technical. If a feature requires a long explanation to justify, question the feature.

### 5. Ship before you perfect

Will is the work of people who understand that a correct, shipping product serves Lucas better than a perfect, theoretical one. The Creator's shadow is paralysis. The Sage's standard is not perfection — it is soundness. Sound architecture, shipped. Improved iteratively.

**In practice:** MVPs ship with known rough edges documented as issues, not hidden. Changelogs are honest. "This is incomplete" is acceptable copy. "This is perfect" is not.

---

## Promise

When you pick up your phone with an intent, Will gets you to execution and back to your work — without generating a new reason to stay.

That is the entire contract. Will does not promise productivity, focus scores, or a better relationship with your phone. It promises a home screen that does not work against you.
