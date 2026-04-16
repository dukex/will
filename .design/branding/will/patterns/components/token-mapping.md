# Token Mapping — Android / Compose M3
> Brand: will | Generated: 2026-04-16
> Maps will.yml tokens → Material3 ColorScheme + Typography + Shapes

---

## Color Scheme — Compose M3

Complete `darkColorScheme()` mapping. Add to `app/src/main/java/tech/benedicta/will/ui/theme/Theme.kt`.

> **⚠ Accent leak warning:** Will's accent rule is "interactive elements only." M3 applies `primary` automatically to several components beyond explicit buttons — `Snackbar` action text, `Checkbox` checked state, `Switch` thumb, `CircularProgressIndicator` track, `Chip` selected border. Audit each M3 component you use and explicitly override `colors` parameters where M3 would apply `primary` to a non-interactive or decorative element. The rule is: if the user taps it → accent is correct. If M3 just applies accent by default to a visual element → override to neutral.

```kotlin
// Brand color constants
val WillBackground      = Color(0xFF000000)  // background — OLED true black
val WillSurface1        = Color(0xFF0D0D0D)  // card
val WillSurface2        = Color(0xFF161616)  // popover (bottom sheets, overlays)
val WillSurface3        = Color(0xFF202020)  // muted
val WillAccent          = Color(0xFF4F6EF7)  // primary / accent
val WillAccentFg        = Color(0xFFFFFFFF)  // primary-foreground (FFFFFF, not F0F0F0)
val WillTextPrimary     = Color(0xFFF0F0F0)  // foreground
val WillTextSecondary   = Color(0xFF888888)  // muted-foreground
val WillTextTertiary    = Color(0xFF505050)  // disabled only
val WillBorder          = Color(0xFF2A2A2A)  // border
val WillBorderActive    = Color(0xFF404040)  // border-active
val WillDestructive     = Color(0xFFE05252)  // destructive
val WillSuccess         = Color(0xFF4CAF74)  // success

val WillDarkColors = darkColorScheme(
    // Surfaces
    background           = WillBackground,
    surface              = WillSurface1,       // card → M3 surface
    surfaceVariant       = WillSurface2,       // popover → M3 surfaceVariant
    surfaceContainer     = WillSurface3,       // muted → M3 surfaceContainer
    surfaceContainerHigh = WillSurface2,
    inverseSurface       = WillTextPrimary,
    inverseOnSurface     = WillBackground,
    // Primary (accent — interactive only)
    primary              = WillAccent,
    onPrimary            = WillAccentFg,
    primaryContainer     = Color(0xFF161B3A),  // accent-950 — for subtle container use
    onPrimaryContainer   = WillAccent,
    // Secondary — no distinct secondary brand color; use surface-2
    secondary            = WillSurface2,
    onSecondary          = WillTextPrimary,
    secondaryContainer   = WillSurface3,
    onSecondaryContainer = WillTextPrimary,
    // Tertiary — not used; map to muted
    tertiary             = WillSurface3,
    onTertiary           = WillTextSecondary,
    tertiaryContainer    = WillSurface3,
    onTertiaryContainer  = WillTextSecondary,
    // Text
    onBackground         = WillTextPrimary,
    onSurface            = WillTextPrimary,
    onSurfaceVariant     = WillTextSecondary,
    // Borders / outlines
    outline              = WillBorder,
    outlineVariant       = WillBorderActive,
    // Error / destructive
    error                = WillDestructive,
    onError              = WillTextPrimary,
    errorContainer       = Color(0xFF3D1414),  // destructive-900 — error container bg
    onErrorContainer     = WillDestructive,
    // Scrim
    scrim                = Color(0xCC000000),  // 80% black overlay
)
```

---

## Typography — Compose M3

Bundle fonts first: `app/src/main/res/font/geist_sans_regular.ttf`, `geist_sans_medium.ttf`, `geist_mono_regular.ttf`.

```kotlin
val GeistSans = FontFamily(
    Font(R.font.geist_sans_regular, FontWeight.Normal),
    Font(R.font.geist_sans_medium,  FontWeight.Medium),
)
val GeistMono = FontFamily(
    Font(R.font.geist_mono_regular, FontWeight.Normal),
)

// Map Will's Major Second scale to M3 Typography slots
val WillTypography = Typography(
    // Display — HomeTopBar time display
    displaySmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-0.02).em
    ),
    // Headline — section titles if needed
    headlineMedium = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = (-0.01).em
    ),
    headlineSmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.01).em
    ),
    // Title — task titles (type-lg)
    titleLarge = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.01).em
    ),
    titleMedium = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Medium,  // section headers
        fontSize      = 14.sp,
        lineHeight    = 21.sp,
        letterSpacing = 0.em
    ),
    titleSmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Medium,  // type-md — section headers
        fontSize      = 13.sp,
        lineHeight    = 19.sp,
        letterSpacing = 0.em
    ),
    // Body — readable content
    bodyLarge = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.01).em
    ),
    bodyMedium = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 21.sp,
        letterSpacing = 0.em
    ),
    bodySmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.em
    ),
    // Label — Geist Mono for metadata, Geist Sans for action labels
    labelLarge = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 21.sp,
        letterSpacing = 0.em
    ),
    labelMedium = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 19.sp,
        letterSpacing = 0.em
    ),
    // labelSmall = Geist Mono — timestamps, source labels (type-xs)
    labelSmall = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.01.em
    ),
)
```

**Note:** For Geist Mono metadata that doesn't fit neatly into M3 slots, use explicit `TextStyle` in the composable rather than forcing a slot:

```kotlin
// Use directly in composable — preferred for Mono data labels
val MetadataStyle = TextStyle(
    fontFamily    = GeistMono,
    fontWeight    = FontWeight.Normal,
    fontSize      = 11.sp,
    lineHeight    = 16.sp,
    letterSpacing = 0.01.em,
    color         = WillTextSecondary
)
```

---

## Shapes — Compose M3

```kotlin
val WillShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),  // task cards — always 0dp
    small      = RoundedCornerShape(0.dp),  // inputs, badges
    medium     = RoundedCornerShape(4.dp),  // Android adaptive icon grid only
    large      = RoundedCornerShape(8.dp),  // bottom sheets (ModalBottomSheet uses large)
    extraLarge = RoundedCornerShape(8.dp),  // overlay panels
)
```

**Note:** `ModalBottomSheet` uses `Shapes.large` by default. 8dp is acceptable for bottom sheets per M3 spec. Task cards must be 0dp — override shape explicitly if needed.

---

## Updated WillTheme

```kotlin
@Composable
fun WillTheme(
    themeMode: ThemeMode = ThemeMode.DARK,  // default to DARK — Will is dark-only for MVP
    content: @Composable () -> Unit
) {
    // Will is dark-only for MVP. Light mode is post-MVP.
    // ThemeMode.SYSTEM falls back to dark until light tokens are defined.
    MaterialTheme(
        colorScheme = WillDarkColors,
        typography  = WillTypography,
        shapes      = WillShapes,
        content     = content
    )
}
```

---

## Spacing constants

Not a M3 theming concept — add as an object:

```kotlin
object Spacing {
    val xs  = 4.dp
    val sm  = 8.dp
    val md  = 12.dp
    val lg  = 16.dp
    val xl  = 24.dp
    val xxl = 32.dp
    val screenHorizontal  = 24.dp
    val cardVertical      = 12.dp
    val cardHorizontal    = 16.dp
    val touchTarget       = 48.dp
}
```

---

## shadcn/ui CSS variables (for web/README contexts)

If Will needs a web presence (GitHub README landing page, Open Graph card), these are the `:root` variables:

```css
:root {
  --background: oklch(0 0 0);
  --foreground: oklch(0.949 0 0);
  --card: oklch(0.108 0 0);
  --card-foreground: oklch(0.949 0 0);
  --popover: oklch(0.148 0 0);
  --popover-foreground: oklch(0.949 0 0);
  --primary: oklch(0.593 0.207 269.41);
  --primary-foreground: oklch(1 0 0);
  --secondary: oklch(0.148 0 0);
  --secondary-foreground: oklch(0.949 0 0);
  --accent: oklch(0.593 0.207 269.41);
  --accent-foreground: oklch(1 0 0);
  --muted: oklch(0.192 0 0);
  --muted-foreground: oklch(0.590 0 0);
  --destructive: oklch(0.629 0.178 23.73);
  --border: oklch(0.232 0 0);
  --ring: oklch(0.593 0.207 269.41);
  --radius: 0px;
  --font-display: 'Geist', Inter, Roboto, sans-serif;
  --font-body: 'Geist', Inter, Roboto, sans-serif;
  --font-mono: 'Geist Mono', 'JetBrains Mono', 'Roboto Mono', monospace;
}
```
