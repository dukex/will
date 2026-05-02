# Wallpaper legibility audit — 2026-05-01

Three composites against the actual `mipmap-xxxhdpi/ic_launcher.webp`:

| file | wallpaper | result |
|------|-----------|--------|
| `black-with-icon.png` | solid `#000000` | passes — black icon background merges with wallpaper, W silhouette dominates |
| `light-with-icon.png` | solid `#F5F5F5` | passes — black icon plate contrasts cleanly, W reads at glance |
| `pattern-with-icon.png` | grey checkerboard | passes — black plate punches through pattern noise, W stays readable |

Programmatic audit only (rendered through ImageMagick at `192px` on a
600x300 surface). Real-device verification is still useful — particularly
on punch-hole / curved-edge devices where the launcher applies its own
chrome — but the brand mark holds shape across the three risk classes.

## Re-run

```bash
mkdir -p $TMPDIR/will-audit && cd $TMPDIR/will-audit
magick -size 600x300 xc:'#000000' black.png
magick -size 600x300 xc:'#F5F5F5' light.png
magick -size 600x300 pattern:checkerboard -modulate 100,0,100 -evaluate multiply 0.5 pattern.png
for bg in black light pattern; do
  magick "$bg".png \
    \( /path/to/will/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp -resize 192x192 \) \
    -gravity center -composite "${bg}-with-icon.png"
done
```
