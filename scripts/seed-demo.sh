#!/usr/bin/env bash
# seed-demo.sh — install Will (debug), grant the notification listener,
# and fire the demo seed broadcast so the emulator shows the Lucas dataset.
#
# Usage:
#   ./scripts/seed-demo.sh            # install + seed
#   ./scripts/seed-demo.sh --no-install  # skip install (only seed)
#
# Requires: an emulator already running (`adb devices` must list one).

set -euo pipefail

readonly PACKAGE="tech.benedicta.will"
readonly ACTION="${PACKAGE}.SEED_DEMO"
readonly LISTENER_COMPONENT="${PACKAGE}/.notification.WillNotificationListenerService"
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"

skip_install=0
for arg in "$@"; do
  case "$arg" in
    --no-install) skip_install=1 ;;
    *) echo "unknown flag: $arg" >&2; exit 2 ;;
  esac
done

require_emulator() {
  local count
  count=$(adb devices | awk 'NR>1 && $2=="device"' | wc -l | tr -d ' ')
  if [[ "$count" -eq 0 ]]; then
    echo "no adb device attached. start the emulator and retry." >&2
    exit 1
  fi
}

install_debug() {
  echo "[seed-demo] building :app:assembleDebug"
  ./gradlew :app:assembleDebug --quiet
  echo "[seed-demo] installing debug APK"
  adb install -r -t app/build/outputs/apk/debug/app-debug.apk >/dev/null
}

grant_listener() {
  echo "[seed-demo] granting notification listener access"
  # cmd notification allow_listener is supported on Android 11+
  adb shell cmd notification allow_listener "$LISTENER_COMPONENT" >/dev/null 2>&1 || \
    echo "[seed-demo] could not auto-grant listener; grant it manually under Settings → Special app access"
}

fire_seed() {
  echo "[seed-demo] firing $ACTION"
  # -f 32 = FLAG_INCLUDE_STOPPED_PACKAGES; needed when the app has never been opened
  adb shell am broadcast -a "$ACTION" -p "$PACKAGE" -f 32 >/dev/null
  # give the seeder a beat to write to Room before any subsequent capture
  sleep 1
}

open_will() {
  echo "[seed-demo] opening Will"
  adb shell am start -n "${PACKAGE}/.MainActivity" >/dev/null
  # let MainActivity reach onResume so the StateFlow is observing Room
  sleep 1
}

main() {
  require_emulator
  if [[ "$skip_install" -eq 0 ]]; then
    install_debug
    grant_listener
  fi
  open_will
  fire_seed
  echo "[seed-demo] done. capture screens with ./scripts/capture.sh <name>"
}

main
