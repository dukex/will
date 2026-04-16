# STYLE.md — Will
> Agent contract | Brand: will | Generated: 2026-04-16
> Source of truth: `will.yml`

---

## Intensity

| Dial | Value | What it means |
|------|-------|---------------|
| Variance | 2/10 | Near-zero visual variance. One background, one surface, one accent. No decorative hierarchy. |
| Motion | 2/10 | Tap response only. No ambient animation, no scanlines, no entrance effects. The screen does not move unless the user moves it. |
| Density | 8/10 | Information-dense. Tight line heights (1.45–1.50). 8dp gaps. Every dp earns its place. |

---

## Philosophy

Will is built on one premise: the Android home screen is not neutral. Every design decision in every competing launcher is an argument about what the phone is for. Will makes a different argument, and the visual language is how it makes it.

**Flat-pane structure is the architectural argument.** A terminal pane has no chrome — just content delimited by borders. Will's UI is the same: task cards defined by 1dp borders on true black, no elevation, no shadows, no surface hierarchy through decoration. The phone is a work surface. It looks like one.

**Semantic color is the communication system.** `#4F6EF7` cold indigo means one thing: this element is interactive. If something is not interactive, it does not have the accent color. The indigo appears on tap targets, active indicators, focus rings, and action buttons. Everywhere else: neutral grays. This is syntax highlighting applied to a launcher — the color is not decoration, it is information.

**Monospace as signal, not aesthetic.** Geist Mono appears on timestamps, source labels, and task IDs. It does not appear on task titles or action labels. The choice of font communicates the category of the content: Mono says "this value is exact and system-generated." Sans says "this is content the user reads." A developer reads this the same way they read a codebase — font choice is semantics.

**The terminal preset's structure without its aesthetics.** Will borrows flat panes, border-defined hierarchy, and information density from terminal design. It discards terminal green, phosphor glow, scanlines, and ASCII decorations. Those elements perform terminal culture. Will inhabits it.

---

## Patterns

### Card (task card — primary UI element)

| Property | Value |
|----------|-------|
| Background | `#0D0D0D` |
| Border | `1dp solid #2A2A2A` |
| Border (active/focus) | `1dp solid #404040` |
| Border radius | `0dp` |
| Shadow | none |
| Padding | `12dp` vertical, `16dp` horizontal |
| Divider between cards | `1dp #2A2A2A` horizontal rule, no gap |
| Typography — title | Geist Sans 400, 15sp, `#F0F0F0`, `−0.01em` tracking |
| Typography — body | Geist Sans 400, 12sp, `#888888` |
| Typography — metadata | Geist Mono 400, 11sp, `#888888`, `+0.01em` tracking |

### Button — primary

| Property | Value |
|----------|-------|
| Background | `#4F6EF7` |
| Text | Geist Sans 400, `#FFFFFF`, 16sp+, sentence case |
| Border radius | `0dp` |
| Shadow | none |
| ⚠ Constraint | Accent as button background requires `#FFFFFF` text at 16sp+ for AA compliance (3.92:1). At 14sp, use accent as border/indicator instead. |

### Button — secondary

| Property | Value |
|----------|-------|
| Background | transparent |
| Border | `1dp solid #2A2A2A` |
| Text | Geist Sans 400, `#F0F0F0`, sentence case |
| Border radius | `0dp` |
| Shadow | none |

### Button — destructive

| Property | Value |
|----------|-------|
| Background | transparent |
| Border | `1dp solid #E05252` |
| Text | Geist Sans 400, `#E05252`, sentence case |
| Border radius | `0dp` |

### Input / Search field

| Property | Value |
|----------|-------|
| Background | `#0D0D0D` |
| Border | none at rest |
| Font | Geist Sans 400, `#F0F0F0`, 14sp |
| Placeholder | Geist Sans 400, `#888888`, 14sp |
| Border radius | `0dp` |
| Focus | default Android cursor, `#4F6EF7` ring on keyboard navigation |

### Badge / metadata label

| Property | Value |
|----------|-------|
| Shape | no background, no border, text label only |
| Font | Geist Mono 400, `#888888`, 11sp, `+0.01em` tracking |
| Decoration | none — no colored dots, no count bubbles |

### Bottom sheet / overlay panel

| Property | Value |
|----------|-------|
| Background | `#161616` (surface-2) |
| Border | `1dp solid #2A2A2A` at top |
| Border radius | `0dp` (or platform-standard 8dp top corners if required by M3 ModalBottomSheet) |
| Handle indicator | `#2A2A2A` — minimal, not styled |

### Layout

| Property | Value |
|----------|-------|
| Background | `#000000` (OLED true black) |
| Screen horizontal padding | `24dp` |
| App chrome | none — no TabRow, no TopAppBar, no BottomNavigation |
| Section header spacing | `8dp` above, `4dp` below |
| Between task cards | `1dp` divider only, no gap |
| Content padding top | `WindowInsets.safeDrawing` + `8dp` |

---

## Constraints

**Never:**
- Gradients anywhere — backgrounds, cards, buttons, icons
- Shadows or elevation — borders define structure only
- Rounded corners on task cards — `0dp` is a functional statement
- Accent color (`#4F6EF7`) on non-interactive elements — decoration
- FontWeight above 500 anywhere — no SemiBold (600), no Bold (700)
- Geist Mono for readable content (titles, labels, action text)
- Engagement mechanics — streaks, badges, count indicators, achievement UI
- Wellness language — "calm," "balance," "mindful," "focus mode," "streak"
- Device mockups or lifestyle photography
- CRT scanlines, noise textures, grain overlays — functional tool, not retro cosplay
- ASCII decorations — Will is not performing terminal aesthetics
- Exclamation marks in any copy — one breaks the voice permanently
- Emoji in any product or documentation surface
- Tinted surfaces — neutral gray steps only, no blue info cards or yellow warning cards

**Always:**
- True black (`#000000`) as launcher background — OLED pixel off = zero power
- Geist Mono for system-generated values: timestamps, source labels, task IDs
- Geist Sans for human-readable content: task titles, labels, action text
- `1dp border #2A2A2A` as the structural boundary of all containers
- Sentence case everywhere — "Will" capitalized, everything else lowercase
- Semantic color — `#4F6EF7` means interactive, `#E05252` means destructive
- Edge-to-edge layout, no app chrome
- `48dp` minimum touch target on all interactive elements
- Focus rings using `#4F6EF7` for keyboard/TalkBack navigation

---

## Effects

### Interaction vocabulary

| Verb | Description |
|------|-------------|
| `flat-press` | opacity drops to 0.6–0.7, no scale, no translate |
| `border-brighten` | border transitions from `#2A2A2A` to `#404040` |
| `opacity-dim` | secondary action dims to indicate non-primary state |

### State table

| State | Card | Button primary | Button secondary | Link |
|-------|------|----------------|------------------|------|
| Default | `#2A2A2A` border | `#4F6EF7` bg | transparent + `#2A2A2A` border | `#888888` |
| Hover | `#404040` border | opacity 0.85 | border `#404040` | `#F0F0F0` |
| Pressed | opacity 0.7 | opacity 0.6 | opacity 0.6 | opacity 0.7 |
| Focus | `#4F6EF7` ring | `#4F6EF7` ring | `#4F6EF7` ring | `#4F6EF7` ring |
| Disabled | opacity 0.3, `#505050` text | opacity 0.3 | opacity 0.3 | `#505050` |

### Transitions

All transitions: `100ms ease-out`. Responsive, not animated. The UI should feel like it reacts, not performs.

### Ambient

None. The home screen must not draw attention to itself. No scanlines, no cursor blink, no typewriter effect, no entrance animation.

---

## Bold Bets

These are Will's most distinctive visual choices. Each one is specific enough to implement or violate.

### 1. True OLED black as the home screen
`background: #000000` — not dark gray, not near-black. Pixel off on OLED. Every competing launcher uses a surface value (dark gray, `#1B1B1F`, `#121212`). Will is the only one that makes the engineering argument explicit in the visual language. The phone gets out of the way by turning off.

**Implementation:** Set `background = Color(0xFF000000)` in the M3 `darkColorScheme()`. Also set `windowBackground` in the Activity to match — prevents flash on launch.

### 2. No visual hierarchy except content hierarchy
Will has exactly three surface values (`#000000`, `#0D0D0D`, `#161616`) and they encode physical depth: launcher floor → task card → bottom sheet. There is no color-coded hierarchy, no bold type hierarchy, no size hierarchy in cards. Every task card is the same visual weight until the user decides otherwise. This is the architectural argument that engagement mechanics are design choices — Will removes them.

**Implementation:** `FontWeight.Normal` (400) everywhere except section headers (500). No `FontWeight.SemiBold`. No `FontWeight.Bold`. Ever.

### 3. Geist Mono as a semantic signal
Timestamps and source labels appear in Geist Mono. Task titles appear in Geist Sans. The user never has to read the label to know the category of information — the font tells them. This is syntax highlighting applied to a launcher.

**Implementation:** Two `FontFamily` objects in theme: `GeistSans` and `GeistMono`. Rule: if the value is system-generated or frequently changing, it goes in `GeistMono`. If it's content the user reads, `GeistSans`.

### 4. Single accent, strict semantic use
`#4F6EF7` cold indigo appears on interactive elements only. Not on decorative elements, not on informational labels, not on anything static. If an element has the accent, it does something. If it doesn't do something, it doesn't have the accent.

**Implementation:** Map `primary = Color(0xFF4F6EF7)` in M3 color scheme. When writing a new composable, ask: does this element invite interaction? If no → no accent. If yes → accent only on the interactive target itself, not its container.

### 5. 1dp border as the only structural primitive
No shadows, no elevation, no tinted surfaces, no background variation to imply depth. The task card boundary is a 1dp border at `#2A2A2A`. When a card is focused, that border becomes `#404040`. That is the entire interaction vocabulary for card structure.

**Implementation:** `HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)` between task items. Card containers use `border = BorderStroke(1.dp, Color(0xFF2A2A2A))`. Zero `elevation` parameters.

---

## Implementation

### Android / Compose M3

**Color scheme (replace WillTheme entirely):**

```kotlin
val WillBackground    = Color(0xFF000000)
val WillSurface1      = Color(0xFF0D0D0D)
val WillSurface2      = Color(0xFF161616)
val WillSurface3      = Color(0xFF202020)
val WillAccent        = Color(0xFF4F6EF7)
val WillAccentOnDark  = Color(0xFFFFFFFF)
val WillTextPrimary   = Color(0xFFF0F0F0)
val WillTextSecondary = Color(0xFF888888)
val WillTextTertiary  = Color(0xFF505050)
val WillBorder        = Color(0xFF2A2A2A)
val WillBorderActive  = Color(0xFF404040)
val WillDestructive   = Color(0xFFE05252)
val WillSuccess       = Color(0xFF4CAF74)

val WillDarkColors = darkColorScheme(
    background           = WillBackground,
    surface              = WillSurface1,
    surfaceVariant       = WillSurface2,
    surfaceContainer     = WillSurface3,
    primary              = WillAccent,
    onPrimary            = WillAccentOnDark,
    onBackground         = WillTextPrimary,
    onSurface            = WillTextPrimary,
    onSurfaceVariant     = WillTextSecondary,
    outline              = WillBorder,
    outlineVariant       = WillBorderActive,
    error                = WillDestructive,
    onError              = WillTextPrimary,
)
```

**Typography (Geist Sans + Geist Mono):**

```kotlin
// res/font/geist_sans_regular.ttf + geist_sans_medium.ttf
// res/font/geist_mono_regular.ttf
val GeistSans = FontFamily(
    Font(R.font.geist_sans_regular, FontWeight.Normal),
    Font(R.font.geist_sans_medium, FontWeight.Medium),
)
val GeistMono = FontFamily(
    Font(R.font.geist_mono_regular, FontWeight.Normal),
)

val WillTypography = Typography(
    bodyLarge   = TextStyle(fontFamily = GeistSans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = (-0.01).em),
    bodyMedium  = TextStyle(fontFamily = GeistSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall   = TextStyle(fontFamily = GeistMono, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge  = TextStyle(fontFamily = GeistSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    labelMedium = TextStyle(fontFamily = GeistSans, fontWeight = FontWeight.Medium,  fontSize = 13.sp, lineHeight = 19.sp),
    labelSmall  = TextStyle(fontFamily = GeistMono, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.01.em),
    displaySmall = TextStyle(fontFamily = GeistSans, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.02).em),
)
```

**Shapes (zero radius for task cards):**

```kotlin
val WillShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),  // task cards — never rounded
    small      = RoundedCornerShape(0.dp),
    medium     = RoundedCornerShape(4.dp),  // Android adaptive icon grid only
    large      = RoundedCornerShape(8.dp),  // bottom sheets
    extraLarge = RoundedCornerShape(8.dp),
)
```

**Spacing constants:**

```kotlin
object WillSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val screenHorizontal = 24.dp
    val cardPaddingVertical = 12.dp
    val cardPaddingHorizontal = 16.dp
    val touchTarget = 48.dp
}
```

**HorizontalDivider (the only structural primitive):**

```kotlin
HorizontalDivider(
    color = MaterialTheme.colorScheme.outline,  // #2A2A2A
    thickness = 1.dp
)
```

**No elevation, anywhere:**

```kotlin
// Cards: no elevation parameter
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface  // #0D0D0D
    ),
    shape = MaterialTheme.shapes.extraSmall,  // 0dp
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
)
```
