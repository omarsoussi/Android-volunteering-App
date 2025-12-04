#!/bin/bash

# Auto-watch script - continuously syncs database from device
# Run this in a separate terminal while using the app

ADB="/Users/mac/Library/Developer/Xamarin/android-sdk-macosx/platform-tools/adb"
PROJECT_DB="/Users/mac/AndroidStudioProjects/tounesna/tounesna.db"

echo "🔄 Auto-sync started - watching for database changes..."
echo "📍 Syncing to: $PROJECT_DB"
echo "⏱️  Checking every 3 seconds"
echo ""
echo "Press Ctrl+C to stop"
echo ""

while true; do
    # Try pulling from auto-sync location first (app's external files dir)
    $ADB pull /storage/emulated/0/Android/data/com.example.tounesna/files/database_export/tounesna.db "$PROJECT_DB" 2>/dev/null 1>/dev/null
    
    # If that fails, try run-as method
    if [ $? -ne 0 ] || [ ! -s "$PROJECT_DB" ]; then
        $ADB shell "run-as com.example.tounesna cat databases/tounesna.db" > "$PROJECT_DB" 2>/dev/null
    fi
    
    # Show timestamp
    COUNT=$(sqlite3 "$PROJECT_DB" "SELECT COUNT(*) FROM volunteers;" 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo "[$(date '+%H:%M:%S')] ✅ Synced - $COUNT volunteers in database"
    fi
    
    sleep 3
done
