#!/bin/bash

# Database Sync Script
# This script pulls the database from the Android device and places it in your project folder
# so you can view it in SQLiteStudio

ADB="/Users/mac/Library/Developer/Xamarin/android-sdk-macosx/platform-tools/adb"
PACKAGE_NAME="com.example.tounesna"
DB_NAME="tounesna.db"
PROJECT_DB="/Users/mac/AndroidStudioProjects/tounesna/tounesna.db"

echo "=================================================="
echo "Database Sync Script"
echo "=================================================="
echo ""

# Check if device is connected
DEVICES=$($ADB devices | grep -v "List" | grep "device$" | wc -l)
if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No devices connected!"
    exit 1
fi
echo "✅ Device connected"

# Method 1: Try using run-as (works on debug builds)
echo ""
echo "Pulling database from device..."

# Try the auto-exported location first
$ADB pull /storage/emulated/0/Android/data/$PACKAGE_NAME/files/database_export/tounesna.db "$PROJECT_DB" 2>/dev/null

# If that fails, use run-as method
if [ ! -s "$PROJECT_DB" ]; then
    $ADB shell "run-as $PACKAGE_NAME cat databases/$DB_NAME" > "$PROJECT_DB" 2>/dev/null
fi

if [ $? -eq 0 ] && [ -s "$PROJECT_DB" ]; then
    echo "✅ Database synced successfully!"
    echo "📍 Location: $PROJECT_DB"
    echo ""
    echo "Database statistics:"
    sqlite3 "$PROJECT_DB" "SELECT 'Volunteers: ' || COUNT(*) FROM volunteers;"
    sqlite3 "$PROJECT_DB" "SELECT 'Organizations: ' || COUNT(*) FROM organizations;"
    sqlite3 "$PROJECT_DB" "SELECT 'Posts: ' || COUNT(*) FROM posts;"
    echo ""
    echo "🔄 Refresh SQLiteStudio to see the data!"
else
    echo "❌ Failed to pull database"
    echo ""
    echo "Alternative: Use Android Studio's Device File Explorer"
    echo "1. View → Tool Windows → Device File Explorer"
    echo "2. Navigate to: /data/data/$PACKAGE_NAME/databases/"
    echo "3. Right-click $DB_NAME → Save As"
fi
