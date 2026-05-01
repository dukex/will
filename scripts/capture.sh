#!/usr/bin/env bash
# capture.sh — pull a screenshot from the running emulator into
# site/public/screenshots/<name>.png.
#
# Usage:
#   ./scripts/capture.sh home
#   ./scripts/capture.sh drawer
#   ./scripts/capture.sh dashboard
#   ./scripts/capture.sh overlay
#
# Suggested flow:
#   ./scripts/seed-demo.sh
#   ./scripts/capture.sh home              # task feed + pinned apps
#   # in the emulator: tap the apps button (or swipe up)
#   ./scripts/capture.sh drawer
#   # in the emulator: tap the focus-dashboard button in the top bar
#   ./scripts/capture.sh dashboard
#   # in the emulator: tap a task whose source app is installed (Calendar)
#   ./scripts/capture.sh overlay

set -euo pipefail
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"
if [[ $# -ne 1 ]]; then
  echo "usage: $0 <name>" >&2
  exit 2
fi

readonly NAME="$1"
readonly OUT_DIR="site/public/screenshots"
readonly OUT_PATH="${OUT_DIR}/${NAME}.png"

count=$(adb devices | awk 'NR>1 && $2=="device"' | wc -l | tr -d ' ')
if [[ "$count" -eq 0 ]]; then
  echo "no adb device attached" >&2
  exit 1
fi

mkdir -p "$OUT_DIR"
adb exec-out screencap -p > "$OUT_PATH"

# rough sanity check — screencap -p outputs a PNG; first 8 bytes are the PNG magic
if ! head -c 8 "$OUT_PATH" | grep -q "PNG"; then
  echo "captured file at $OUT_PATH does not look like a PNG; check adb output" >&2
  exit 1
fi

bytes=$(wc -c < "$OUT_PATH" | tr -d ' ')
echo "wrote $OUT_PATH ($bytes bytes)"
