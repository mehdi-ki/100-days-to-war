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
mkdir -p "${STAGING}/jpackage" "${APP_DIR}/usr/lib" \
  "${APP_DIR}/usr/share/icons/hicolor/scalable/apps" "${PROJECT_ROOT}/release"

# jpackage creates an application image with its own Java runtime. The complete
# image is kept inside the AppImage so users do not need to install Java.
jpackage \
  --type app-image \
  --name "${APP_NAME}" \
  --app-version "${VERSION#v}" \
  --input "${PROJECT_ROOT}/lwjgl3/build/install/100-days-to-war/lib" \
  --main-jar "100-days-to-war.jar" \
  --main-class "com.mehdi.daystowar.desktop.DesktopLauncher" \
  --dest "${STAGING}/jpackage"

cp -R "${STAGING}/jpackage/${APP_NAME}" "${APP_DIR}/usr/lib/"
cp "${PROJECT_ROOT}/assets/100-days-to-war.svg" "${APP_DIR}/usr/share/icons/hicolor/scalable/apps/${APP_NAME}.svg"
ln -s "usr/lib/${APP_NAME}/bin/${APP_NAME}" "${APP_DIR}/AppRun"

cat > "${APP_DIR}/${APP_NAME}.desktop" <<EOF
[Desktop Entry]
Name=100 Days to War
Comment=Strategic preparation before the war
Exec=AppRun
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

# GitHub-hosted Linux runners do not guarantee libfuse2. appimagetool can
# execute from its own AppImage without mounting FUSE in that environment.
APPIMAGE_EXTRACT_AND_RUN=1 ARCH=x86_64 "${APPIMAGETOOL}" "${APP_DIR}" "${OUTPUT}"
echo "AppImage erstellt: ${OUTPUT}"
