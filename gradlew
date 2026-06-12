#!/bin/sh
# Gradle wrapper launcher (standard).
# If gradle/wrapper/gradle-wrapper.jar is missing, run: gradle wrapper
DIR=$(cd "$(dirname "$0")" && pwd)
exec gradle "$@" 2>/dev/null || { echo "Gradle wrapper jar missing. Open the project in Android Studio (it will download it), or run 'gradle wrapper' if you have Gradle installed."; exit 1; }
