#!/usr/bin/env bash
# render-icons.sh — rasterize the brand W into legacy density mipmaps.
#
# The adaptive icon (API 26+) is sourced from vector drawables under
# app/src/main/res/drawable/ic_launcher_*. API 24-25 cannot use adaptive
# icons and falls back to mipmap-<density>/ic_launcher.webp, so this
# script regenerates those raster fallbacks from a single high-resolution
# master rendered with ImageMagick.
#
# Geometry mirrors drawable/ic_launcher_foreground.xml exactly. Re-run
# whenever the foreground vector changes:
#
#   ./scripts/render-icons.sh
#
# Requires: imagemagick (`brew install imagemagick`).

set -euo pipefail

if ! command -v magick >/dev/null 2>&1; then
  echo "missing 'magick' (ImageMagick). install with: brew install imagemagick" >&2
  exit 1
fi

readonly RES="$(cd "$(dirname "$0")/.."; pwd)/app/src/main/res"
readonly TMP="${TMPDIR:-/tmp}"
readonly MASTER="${TMP}/will-master.png"

# Master render at 1024x1024 — geometry scaled from the 108-unit adaptive
# viewport so the brand mark stays identical to ic_launcher_foreground.xml.
# Path: M 28,30 L 40,78 L 54,50 L 68,78 L 80,30  (× 1024/108 ≈ 9.481)
# Stroke: 11 dp × 9.481 ≈ 104 px
magick -size 1024x1024 xc:'#000000' \
  -stroke '#F0F0F0' -strokewidth 104 -fill none \
  -draw "stroke-linecap square stroke-linejoin miter polyline 266,284 379,740 512,474 645,740 758,284" \
  "${MASTER}"

readonly SIZES=(48 72 96 144 192)
readonly DENSITIES=(mdpi hdpi xhdpi xxhdpi xxxhdpi)

for i in "${!SIZES[@]}"; do
  size="${SIZES[$i]}"
  density="${DENSITIES[$i]}"
  out="${RES}/mipmap-${density}"
  half=$((size / 2))

  magick "${MASTER}" -resize "${size}x${size}" -quality 95 \
    "${out}/ic_launcher.webp"

  # Round variant: clip the square through a circular mask for launchers
  # that show round legacy icons on pre-26 devices.
  magick "${MASTER}" -resize "${size}x${size}" \
    \( +clone -threshold -1 -negate -fill white \
       -draw "circle ${half},${half} ${half},0" \) \
    -alpha off -compose copy_opacity -composite -quality 95 \
    "${out}/ic_launcher_round.webp"

  echo "rendered ${density} (${size}px)"
done
