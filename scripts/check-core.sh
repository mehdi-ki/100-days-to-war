#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="${PROJECT_ROOT}/out/core-smoke"
rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}"

mapfile -t SOURCES < <(find "${PROJECT_ROOT}/core/src/main/java/com/mehdi/daystowar/core" \
  -name '*.java' \
  ! -name 'WarGame.java' \
  ! -name 'StrategyScreen.java' \
  -print)

javac --release 17 -encoding UTF-8 -d "${BUILD_DIR}" "${SOURCES[@]}"
javac --release 17 -encoding UTF-8 -cp "${BUILD_DIR}" -d "${BUILD_DIR}" "${PROJECT_ROOT}/scripts/Smoke.java"
java -cp "${BUILD_DIR}" Smoke
