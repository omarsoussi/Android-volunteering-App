#!/bin/bash

# Script to pull the REAL database from the emulator
# This is the database your app actually uses with all your registrations!

EMULATOR_DB_PATH="/data/data/com.example.tounesna/databases/tounesna.db"
LOCAL_REAL_DB="/Users/mac/AndroidStudioProjects/tounesna/real_app_database.db"

echo "🔍 Pulling REAL Database from Emulator"
echo "======================================"
echo ""

# Find adb
ADB_PATH=$(find ~/Library/Android/sdk -name "adb" 2>/dev/null | head -1)

if [ -z "$ADB_PATH" ]; then
    echo "❌ adb not found. Trying default Android Studio location..."
    ADB_PATH="$HOME/Library/Android/sdk/platform-tools/adb"
fi

if [ ! -f "$ADB_PATH" ]; then
    echo "❌ Cannot find adb tool"
    echo "Please install Android SDK platform-tools"
    exit 1
fi

echo "✅ Found adb at: $ADB_PATH"
echo ""

# Check if emulator is running
if ! $ADB_PATH devices | grep -q "emulator"; then
    echo "❌ No emulator running. Please start your emulator first."
    exit 1
fi

echo "✅ Emulator detected"
echo ""

# Pull the database
echo "🔄 Pulling database from emulator..."
$ADB_PATH exec-out run-as com.example.tounesna cat databases/tounesna.db > "$LOCAL_REAL_DB"

if [ $? -eq 0 ] && [ -s "$LOCAL_REAL_DB" ]; then
    echo "✅ Database pulled successfully!"
    echo "📍 Saved to: $LOCAL_REAL_DB"
    echo ""
    
    # Show database contents
    echo "📊 Database Contents (REAL DATA):"
    echo "================================"
    sqlite3 "$LOCAL_REAL_DB" << EOF
.mode column
.headers on
SELECT '=== ORGANIZATIONS ===' as info;
SELECT id, name, email, isApproved, createdAt FROM organizations;
SELECT '' as info;
SELECT '=== VOLUNTEERS ===' as info;
SELECT id, name, surname, email, createdAt FROM volunteers;
SELECT '' as info;
SELECT '=== POSTS ===' as info;
SELECT COUNT(*) as total_posts FROM posts;
SELECT '' as info;
SELECT '=== REQUESTS ===' as info;
SELECT COUNT(*) as total_requests FROM volunteer_requests;
EOF
    
    echo ""
    echo "💡 To view this database in SQLite Studio:"
    echo "   Open: $LOCAL_REAL_DB"
    echo ""
    echo "✅ This is the REAL database with all your registered accounts!"
else
    echo "❌ Failed to pull database"
    echo "Make sure the app is installed and has been run at least once"
fi
