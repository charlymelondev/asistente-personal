#!/bin/bash
# Usage: ./release.sh 1.1.0
# Builds APK, creates GitHub release, pushes backend to update /api/version

set -e

VERSION="$1"
if [ -z "$VERSION" ]; then
  echo "Usage: ./release.sh <version>  (e.g., ./release.sh 1.1.0)"
  exit 1
fi

# Parse version code from version string (1.1.0 → 110, 1.2.3 → 123)
IFS='.' read -r major minor patch <<< "$VERSION"
VERSION_CODE=$((major * 100 + minor * 10 + patch))

echo "=== Pollito al Rescate — Release v$VERSION (code: $VERSION_CODE) ==="

# 1. Update Android version
echo "[1/5] Updating Android version..."
GRADLE="android/app/build.gradle.kts"
sed -i "s/versionCode = [0-9]*/versionCode = $VERSION_CODE/" "$GRADLE"
sed -i "s/versionName = \"[^\"]*\"/versionName = \"$VERSION\"/" "$GRADLE"

# 2. Update backend version env
echo "[2/5] Updating backend version..."
# Update .env if it exists
if [ -f "backend/.env" ]; then
  sed -i "s/^APP_VERSION=.*/APP_VERSION=$VERSION/" backend/.env 2>/dev/null || echo "APP_VERSION=$VERSION" >> backend/.env
  sed -i "s/^APP_VERSION_CODE=.*/APP_VERSION_CODE=$VERSION_CODE/" backend/.env 2>/dev/null || echo "APP_VERSION_CODE=$VERSION_CODE" >> backend/.env
fi

# 3. Build APK
echo "[3/5] Building APK..."
cd android
export ANDROID_HOME="$LOCALAPPDATA/Android/Sdk"
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew assembleDebug --quiet
cd ..
cp android/app/build/outputs/apk/debug/app-debug.apk "PollitoAlRescate.apk"
echo "    APK: PollitoAlRescate.apk"

# 4. Commit and push (triggers Render redeploy)
echo "[4/5] Pushing to GitHub..."
git add -A
git commit -m "Release v$VERSION"
git push

# 5. Create GitHub Release with APK
echo "[5/5] Creating GitHub Release..."
gh release create "v$VERSION" "PollitoAlRescate.apk" \
  --title "Pollito al Rescate v$VERSION" \
  --notes "Release v$VERSION (version code $VERSION_CODE)" \
  --latest

echo ""
echo "=== Done! ==="
echo "  - GitHub Release: https://github.com/charlymelondev/asistente-personal/releases/tag/v$VERSION"
echo "  - Render will auto-deploy the backend in ~1 min"
echo "  - Users will see the update dialog next time they open the app"
