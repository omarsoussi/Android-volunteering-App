#!/bin/bash

# SQLite Database Test Script for Tounesna Android App
# This script tests if data is being saved to the SQLite database

echo "=================================================="
echo "Tounesna SQLite Database Test"
echo "=================================================="
echo ""

# Set adb path
ADB="/Users/mac/Library/Developer/Xamarin/android-sdk-macosx/platform-tools/adb"

# Check if device is connected
echo "1. Checking connected devices..."
DEVICES=$($ADB devices | grep -v "List" | grep "device$" | wc -l)
if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No devices connected!"
    echo "Please start an emulator or connect a device."
    exit 1
fi
echo "✅ Device connected"
echo ""

# Install the app
echo "2. Installing latest APK..."
./gradlew installDebug
if [ $? -ne 0 ]; then
    echo "❌ Failed to install app"
    exit 1
fi
echo "✅ App installed"
echo ""

# Clear logcat and start monitoring
echo "3. Starting logcat monitoring..."
echo "   - Watch for database initialization logs"
echo "   - Watch for registration logs"
echo ""
$ADB logcat -c  # Clear logcat
echo "📱 Launch the app NOW and create a test account"
echo "   (Press Ctrl+C when done to check results)"
echo ""
echo "--- LOGCAT OUTPUT (showing database-related logs) ---"
$ADB logcat | grep -E "TounesnaApp|DatabaseManager|DatabaseHelper|AuthController"
