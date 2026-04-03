#!/bin/bash
set -e

export ANDROID_SDK_ROOT=/home/runner/android-sdk
export ANDROID_HOME=/home/runner/android-sdk
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

echo "==> Java: $(java -version 2>&1 | head -1)"

# Install Android SDK if not present
if [ ! -d "$ANDROID_SDK_ROOT/platforms/android-35" ]; then
    echo "==> Android SDK not found. Installing..."
    mkdir -p $ANDROID_SDK_ROOT/cmdline-tools

    CMDTOOLS_ZIP=/tmp/cmdline-tools.zip
    if [ ! -f "$CMDTOOLS_ZIP" ]; then
        curl -L "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -o $CMDTOOLS_ZIP
    fi

    cd /tmp && unzip -q $CMDTOOLS_ZIP
    mv /tmp/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest
    cd -

    echo "==> Accepting licenses..."
    yes | sdkmanager --licenses > /dev/null 2>&1 || true

    echo "==> Installing SDK packages..."
    sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"

    echo "==> SDK installed."
else
    echo "==> Android SDK already present."
fi

# Ensure gradle-wrapper.jar is present (not persistent across sessions)
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "==> Downloading gradle-wrapper.jar..."
    curl -L "https://github.com/gradle/gradle/raw/v8.9.0/gradle/wrapper/gradle-wrapper.jar" \
        -o gradle/wrapper/gradle-wrapper.jar
fi

# Ensure local.properties is correct
echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
echo "==> local.properties updated."

# Run the build
echo "==> Starting Gradle build (signed release)..."
./gradlew --no-daemon --no-configuration-cache :androidApp:assembleRelease

echo "==> Build complete!"
find . -name "*.apk" -path "*/outputs/*" 2>/dev/null
