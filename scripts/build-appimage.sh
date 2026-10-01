#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_NAME="100-Days-to-War"
VERSION="v0.2.0"
ARCH="x86_64"
OUTPUT="${PROJECT_ROOT}/release/${APP_NAME}-${VERSION}-${ARCH}.AppImage"
STAGING="${PROJECT_ROOT}/release/appimage-staging"

if [[ "${1:-}" != "--skip-build" ]]; then
  command -v gradle >/dev/null 2>&1 || {
    echo "Gradle 9.8 oder neuer wird für den Build benötigt." >&2
    exit 1
  }
  gradle -p "${PROJECT_ROOT}" :lwjgl3:installDist --no-daemon
fi

APP_DIR="${STAGING}/${APP_NAME}.AppDir"
rm -rf "${STAGING}"
mkdir -p "${APP_DIR}/usr/bin" "${APP_DIR}/usr/share/icons/hicolor/scalable/apps" "${PROJECT_ROOT}/release"

cp -R "${PROJECT_ROOT}/lwjgl3/build/install/100-days-to-war/." "${APP_DIR}/usr/bin/"
cp "${PROJECT_ROOT}/assets/100-days-to-war.svg" "${APP_DIR}/usr/share/icons/hicolor/scalable/apps/${APP_NAME}.svg"

cat > "${APP_DIR}/${APP_NAME}.desktop" <<EOF
[Desktop Entry]
Name=100 Days to War
Comment=Strategic preparation before the war
Exec=100-days-to-war
Icon=${APP_NAME}
Type=Application
Categories=Game;StrategyGame;
Terminal=false
EOF

ln -s "usr/share/icons/hicolor/scalable/apps/${APP_NAME}.svg" "${APP_DIR}/${APP_NAME}.svg"

APPIMAGETOOL="${APPIMAGETOOL:-${PROJECT_ROOT}/appimagetool}"
command -v "${APPIMAGETOOL}" >/dev/null 2>&1 || {
  echo "appimagetool wurde nicht gefunden. Setze APPIMAGETOOL auf die ausführbare Datei." >&2
  exit 1
}

ARCH=x86_64 "${APPIMAGETOOL}" "${APP_DIR}" "${OUTPUT}"
echo "AppImage erstellt: ${OUTPUT}"
