#!/usr/bin/env bash
set -euo pipefail
APK="app/build/outputs/apk/debug/app-debug.apk"
if [[ ! -f "$APK" ]]; then
  echo "No existe el APK. Ejecuta primero ./build.sh" >&2
  exit 1
fi
if ! command -v adb >/dev/null 2>&1; then
  echo "ADB no está instalado o no está en PATH." >&2
  exit 127
fi
adb install -r "$APK"
