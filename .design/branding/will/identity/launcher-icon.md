# Launcher icon spec

The Will launcher icon is the brand mark applied to Android's adaptive icon
system. It is the single asset users see most often outside the app — it has
to read instantly, sit comfortably next to other launcher icons, and respect
every rule in `will.yml`.

## Mark

A flat geometric "W" stroke. No curves, no fills, no shadows, no decoration.

- Stroke colour: `#F0F0F0` (`foreground` token from `will.yml`)
- Stroke width: 11dp inside the 108dp adaptive viewport
- Stroke join: miter
- Stroke cap: square
- Path: `M 28,30 L 40,78 L 54,50 L 68,78 L 80,30`

The mark sits inside the 72dp safe area defined by Android's adaptive icon
spec. It tolerates every system mask (square, squircle, circle, rounded
square, teardrop) without truncation.

## Background

True black `#000000`. OLED pixel-off, zero power draw on the launcher row,
matches the home-screen surface so the icon visually merges with the shelf
of other apps when it sits on a dark wallpaper, and contrasts cleanly with
light wallpapers.

No gradient, no texture, no border.

## Monochrome variant

Identical geometry to the foreground, with the colour stripped. Android 13+
themed icons read the monochrome layer and tint it with the user's system
theme. The mark stays recognisable under any tint because it relies on
silhouette, not colour.

## Files

| layer | file |
|-------|------|
| adaptive descriptor | `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` |
| round adaptive descriptor | `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` |
| foreground vector | `app/src/main/res/drawable/ic_launcher_foreground.xml` |
| background vector | `app/src/main/res/drawable/ic_launcher_background.xml` |
| monochrome vector | `app/src/main/res/drawable/ic_launcher_monochrome.xml` |

## Constraints

- Never tint the foreground stroke. The mark is white on black, period.
- Never adopt the accent `#4F6EF7` for the launcher icon. The accent is
  reserved for *interactive* surfaces inside the app; the launcher icon is
  identity, not interaction.
- Never thicken the stroke past 12dp or thin past 9dp at the 108-unit
  viewport. Outside that band the W either reads as a slab block or as a
  hairline that disappears at small densities.
- Never replace the monochrome with a coloured silhouette. Android 13+
  themed icons must inherit the system tint; a coloured monochrome breaks
  that contract.

## Pre-API 26 fallback

API 24-25 cannot use adaptive icons and instead reads the legacy
`mipmap-<density>/ic_launcher.webp` files. Those raster fallbacks are still
the platform default art today; replacing them requires offline raster
generation and is tracked under US-09 as a follow-up subtask.

## Verification

Before shipping a change to the icon, render it against:

- a black wallpaper (icon should remain recognisable; the W must not
  disappear into the surface)
- a white or near-white wallpaper (the black background must not bleed into
  the wallpaper as a heavy block)
- a busy patterned wallpaper (no decorative element should compete with
  the W's silhouette)
- the Android 13+ themed-icon mode under at least three theme tints

If any of those reads as broken, the icon ships with a regression.
