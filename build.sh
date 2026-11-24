#!/usr/bin/env bash
set -euo pipefail

if ! command -v gradle >/dev/null 2>&1; then
  echo "Gradle no está instalado. Se requiere Gradle 8.13 y JDK 17." >&2
  exit 127
fi

JAVA_MAJOR="$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F. '{print $1}')"
if [[ -z "${JAVA_MAJOR}" || "${JAVA_MAJOR}" -lt 17 ]]; then
  echo "Se requiere JDK 17 o superior." >&2
  exit 2
fi

gradle --no-daemon :app:assembleDebug
printf '\nAPK generado en: %s\n' "app/build/outputs/apk/debug/app-debug.apk"
