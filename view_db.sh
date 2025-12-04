#!/bin/bash

# ONE-COMMAND DATABASE VIEWER
# Just run: ./view_db.sh

ADB="/Users/mac/Library/Developer/Xamarin/android-sdk-macosx/platform-tools/adb"
PROJECT_DB="/Users/mac/AndroidStudioProjects/tounesna/tounesna.db"

clear
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║           TOUNESNA DATABASE VIEWER                             ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Pull latest database
echo "📥 Pulling latest database from device..."
$ADB pull /storage/emulated/0/Android/data/com.example.tounesna/files/database_export/tounesna.db "$PROJECT_DB" 2>/dev/null 1>/dev/null

if [ ! -s "$PROJECT_DB" ]; then
    echo "⚠️  Export not found, pulling from app storage..."
    $ADB shell "run-as com.example.tounesna cat databases/tounesna.db" > "$PROJECT_DB" 2>/dev/null
fi

if [ ! -s "$PROJECT_DB" ]; then
    echo "❌ Could not access database. Is the app running?"
    exit 1
fi

echo "✅ Database synced!"
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "                    📊 DATABASE CONTENTS"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Show statistics
VOL_COUNT=$(sqlite3 "$PROJECT_DB" "SELECT COUNT(*) FROM volunteers;")
ORG_COUNT=$(sqlite3 "$PROJECT_DB" "SELECT COUNT(*) FROM organizations;")
POST_COUNT=$(sqlite3 "$PROJECT_DB" "SELECT COUNT(*) FROM posts;")
RATING_COUNT=$(sqlite3 "$PROJECT_DB" "SELECT COUNT(*) FROM ratings;")

echo "📈 STATISTICS:"
echo "   • Volunteers: $VOL_COUNT"
echo "   • Organizations: $ORG_COUNT"
echo "   • Posts: $POST_COUNT"
echo "   • Ratings: $RATING_COUNT"
echo ""

# Show latest 10 volunteers
echo "👥 LATEST VOLUNTEERS (Last 10):"
echo "─────────────────────────────────────────────────────────────"
sqlite3 "$PROJECT_DB" "SELECT printf('%3d | %-15s | %-30s', id, substr(name || ' ' || surname, 1, 15), email) FROM volunteers ORDER BY id DESC LIMIT 10;"
echo ""

# Show all organizations
echo "🏢 ORGANIZATIONS:"
echo "─────────────────────────────────────────────────────────────"
ORG_RESULT=$(sqlite3 "$PROJECT_DB" "SELECT printf('%3d | %-20s | %-30s', id, substr(name, 1, 20), email) FROM organizations ORDER BY id;")
if [ -z "$ORG_RESULT" ]; then
    echo "   (No organizations yet)"
else
    echo "$ORG_RESULT"
fi
echo ""

# Show latest posts
echo "📝 LATEST POSTS:"
echo "─────────────────────────────────────────────────────────────"
POST_RESULT=$(sqlite3 "$PROJECT_DB" "SELECT printf('%3d | %-30s | %s', p.id, substr(p.title, 1, 30), o.name) FROM posts p LEFT JOIN organizations o ON p.organizationId = o.id ORDER BY p.id DESC LIMIT 5;")
if [ -z "$POST_RESULT" ]; then
    echo "   (No posts yet)"
else
    echo "$POST_RESULT"
fi
echo ""

echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "✅ Database location: $PROJECT_DB"
echo "🔄 Refresh SQLiteStudio (F5) to see this data in GUI"
echo ""
echo "💡 TIP: Run './view_db.sh' anytime to see latest data"
echo ""
