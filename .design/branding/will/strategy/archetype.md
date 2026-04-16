# Archetype
> Brand: Will | Phase: strategy | Date: 2026-04-15

## Primary: Sage (70%)

**Core desire:** Truth. Understand what the home screen actually is, build from that understanding, and say so plainly.

**Rationale for Will:**

Lucas does not need motivation. He needs a tool that is not lying to him. Every competitor home screen is designed to maximize time-on-device — "engagement" in the industry's language, "theft" in Lucas's. The Sage archetype is the one that names this clearly and builds something different not as a wellness corrective but as an engineering conclusion: the home screen's engagement mechanics are design choices, and different design choices produce different outcomes.

The Sage is also why Will has a point of view at all. Will does not present itself as "one option among many." It presents a position: the phone is a tool, the home screen should behave like one, and every competitor you've tried was built on a different premise. This is not arrogance — it is analysis that holds under inspection.

**Competitive gap the Sage addresses:** No competitor speaks developer-to-developer. Wellness tools explain why distraction is bad (Sage would never — Lucas already knows). Design-first tools show rather than argue. Will argues from architecture. That is a Sage move.

**How the Sage shows up:**
- Copy that assumes the reader already understands the problem
- Architecture decisions explained as positions, not features ("notifications are task objects, not alerts — here's why")
- No metrics theater. No "X minutes saved" counters. Those are engagement mechanics wearing a productivity costume.
- The README reads like a well-structured engineering decision log

---

## Secondary: Creator (30%)

**Core desire:** Build something that did not exist before. Make it with craft.

**Rationale for Will:**

The Creator is what stops Will from being a Unix man page. Sage without Creator produces correct, cold, unusable things. The Creator is the reason Will has an aesthetic at all — not to look impressive, but because craft is how you signal that this thing was built deliberately, not assembled from defaults.

The Creator is also why Will exists at all. A pure Sage would observe the attention economy problem, write a blog post, and reference the literature. The Creator says: I am going to build the thing that should exist and doesn't. Will is a project that began as an engineering document (docs/01-IDEA.md) and became an app. That is Creator behavior operating under Sage discipline.

**Competitive gap the Creator addresses:** Nothing Launcher has the Creator's aesthetic without the Sage's function. Will reverses the ratio — function first, craft applied in service of function. That produces a different kind of object.

**How the Creator shows up:**
- Every element in the UI earns its presence — nothing decorative
- The terminal aesthetic is not cosplay; it is an honest expression of what the tool is
- Typography choices are deliberate: Geist Mono for technical labels because technical labels are technical
- The architecture (notification → Task domain model, PendingIntent store) is itself a creative act — a new way to think about what a notification is

---

## 70/30 Blend: How They Work Together for Will

The Sage prevents the Creator from going precious. Without the Sage, the terminal aesthetic becomes a mood board — dark surfaces for their own sake, minimal as an aesthetic statement. The Sage asks: does this serve the function? If not, cut it.

The Creator prevents the Sage from going cold. Without the Creator, Will is a correct analysis with a bad interface. The Sage knows what the phone should do. The Creator builds something Lucas actually wants to use.

**The blend in practice:**

| Situation | Sage contribution | Creator contribution | Outcome |
|-----------|-------------------|---------------------|---------|
| Writing UI copy | Says only what is true and necessary | Chooses words with the same precision as variable names | Dry, functional copy with no wasted syllables |
| Making an architecture decision | Derives from first principles, documents the reasoning | Builds it with craft, makes the implementation legible | Code that reads like it was inevitable |
| Designing the task feed | Defines what information Lucas needs at that moment | Decides how it should feel to interact with it | Dense information, clean structure, no friction |
| Writing the README | States the position clearly | Makes it read like something a peer wrote, not a marketing team | Trust |

---

## Shadow Traits to Actively Avoid

**Ivory tower elitism (Sage shadow)**

The Sage can become condescending. "You don't understand the attention economy like I do." Will cannot do this. Lucas is not below Will — he is Will's peer. The Sage here speaks as a fellow builder who reached the same conclusion, not as a professor correcting a student. The test: does this copy assume the reader is smart? If it explains things Lucas already knows, rewrite it.

**Perfectionism paralysis (Creator shadow)**

The Creator can get trapped refining rather than shipping. Will is MVP. It will have rough edges. The brand voice must not set an expectation of completeness that the product cannot meet. Will speaks like a tool in active development, built by someone who cares — not like a finished product with a polished marketing site. "This works. Here's how. Here's what's next." That is Creator energy without paralysis.

**Operational guard:**
- Ship a thing that works before you perfect a thing that is pretty
- Every week without a commit is a week the Creator's shadow is winning

---

## Communication Style

**What this archetype blend says:**
- States positions, not suggestions ("notifications are task objects" not "notifications can be used as tasks")
- Explains reasoning when it matters, skips reasoning when the conclusion is self-evident
- Acknowledges tradeoffs without apologizing for them ("Will does not support widgets — home screens with widgets are not working environments")
- Speaks to peers: assumes shared context, does not over-explain

**What this archetype blend does not say:**
- Motivational language of any kind
- Wellness or therapeutic vocabulary
- Competitive disparagement (the architecture speaks — Will does not need to knock Niagara)
- Features that apologize for what Will is not ("unlike other launchers...")

---

## Visual Tendencies

The terminal preset encodes the Sage+Creator blend literally.

**Sage's clarity = semantic color.** Color in Will's system is not decoration — it is information. #4F6EF7 indigo means "this is an action." #E05252 means "this is destructive." #888888 means "this is secondary." A developer reading the interface reads the color the same way they read syntax highlighting: not for beauty but for signal. No competitor uses color this way. Most use it for brand identity. Will uses it for communication.

**Creator's craft = deliberate element selection.** Every element in Will's UI must answer the question: what does this communicate, and could the user do without it? The terminal aesthetic provides the filter. A terminal has no decoration — every character is load-bearing. Will applies this discipline to its UI: if a line, a label, a margin, or a color cannot justify its presence in functional terms, it is not there. That is craft, not minimalism. Minimalism is an aesthetic. This is a standard.

**Type as architecture:** Geist Sans for readable content (Sage's clarity); Geist Mono for labels, timestamps, technical identifiers (Creator's precision signaling). The choice of monospace for technical context is not a style decision — it is a communication decision. It says: this value is exact, not approximate. Treat it accordingly.
