#!/bin/bash

# Script to update the pre-populated database
# This allows you to modify the database and push it to your app

DB_PATH="/Users/mac/AndroidStudioProjects/tounesna/tounesna.db"
ASSETS_PATH="/Users/mac/AndroidStudioProjects/tounesna/app/src/main/assets/prepopulated_db.db"

echo "🔧 Tounesna Database Update Script"
echo "=================================="
echo ""

# Check if database exists
if [ ! -f "$DB_PATH" ]; then
    echo "❌ Database not found at: $DB_PATH"
    exit 1
fi

echo "✅ Found database at: $DB_PATH"
echo ""

# Show database stats
echo "📊 Current Database Contents:"
echo "----------------------------"
sqlite3 "$DB_PATH" << EOF
SELECT 'Organizations: ' || COUNT(*) FROM organizations;
SELECT 'Volunteers: ' || COUNT(*) FROM volunteers;
SELECT 'Posts: ' || COUNT(*) FROM posts;
SELECT 'Requests: ' || COUNT(*) FROM volunteer_requests;
EOF
echo ""

# Copy to assets
echo "🔄 Copying database to assets folder..."
cp "$DB_PATH" "$ASSETS_PATH"

if [ $? -eq 0 ]; then
    echo "✅ Database copied to: $ASSETS_PATH"
    echo ""
    echo "📱 Next steps:"
    echo "1. Uninstall the app from emulator to clear old data"
    echo "2. Run: ./gradlew installDebug"
    echo "3. The app will use your updated database"
    echo ""
    echo "💡 To edit the database, use:"
    echo "   sqlite3 $DB_PATH"
else
    echo "❌ Failed to copy database"
    exit 1
fi
