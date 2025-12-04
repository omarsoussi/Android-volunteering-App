# Tounesna Database - Important Information

## ⚠️ WHERE IS YOUR DATABASE?

### The file `/Users/mac/AndroidStudioProjects/tounesna/tounesna.db` is **NOT** used by your app!

Your Android app's **REAL** database is located on the emulator at:
```
/data/user/0/com.example.tounesna/databases/tounesna.db
```

## Why Two Different Locations?

1. **Project file** (`/Users/mac/AndroidStudioProjects/tounesna/tounesna.db`):
   - This is an old/test file in your project folder
   - It's NOT connected to your app
   - Editing this does NOTHING to your running app

2. **Emulator database** (`/data/user/0/com.example.tounesna/databases/tounesna.db`):
   - This is the REAL database your app uses
   - Created by Android when app first runs
   - All data is saved HERE, not in your project folder

## How Android SQLite Works

Android apps use **SQLite** which is **ALREADY BUILT INTO ANDROID**:
- ✅ No external library needed
- ✅ Works automatically through `SQLiteOpenHelper`
- ✅ Database is created on emulator/device, NOT on your Mac

Your app uses:
```java
DatabaseHelper extends SQLiteOpenHelper
DatabaseManager.init(context)  // Called in TounesnaApplication.onCreate()
```

## How to View Your Real Database

### Option 1: Android Studio Database Inspector
1. Open Android Studio
2. View → Tool Windows → App Inspection
3. Select "Database Inspector" tab
4. Your database shows here while app is running

### Option 2: Using adb (command line)
```bash
# Pull database from emulator to your Mac
adb shell "run-as com.example.tounesna cat /data/user/0/com.example.tounesna/databases/tounesna.db" > ~/Desktop/real_tounesna.db

# Then open with SQLite browser on Mac
```

### Option 3: Check Logs
Your app logs database operations:
```bash
./gradlew installDebug
# Then check Android Studio Logcat for:
# - "DatabaseHelper" tags
# - "DatabaseManager" tags  
# - Shows database path and operations
```

## Your Database IS Working!

Evidence it's working:
1. ✅ Sample organizations are inserted (Red Crescent, TAE, Green Tunisia)
2. ✅ Login works (reads from database)
3. ✅ Posts are created and displayed (writes/reads database)
4. ✅ Images are saved with posts
5. ✅ Version 2 schema includes imageUrl column

## Current Database Schema (Version 2)

### Tables Created:
1. `volunteers` - User accounts for volunteers
2. `organizations` - Organization accounts
3. `posts` - Volunteer opportunity posts
4. `ratings` - Organization ratings
5. `volunteer_requests` - Requests from volunteers (now includes imageUrl)
6. `follows` - Volunteer following organizations
7. `notifications` - Push notifications
8. `post_views` - Post view tracking

### Recent Changes:
- ✅ Added `imageUrl` column to `posts` table
- ✅ Added `imageUrl` column to `volunteer_requests` table
- ✅ Database version upgraded from 1 → 2
- ✅ Auto-upgrade migrates existing data

## Don't Edit the Project Folder Database!

The file at `/Users/mac/AndroidStudioProjects/tounesna/tounesna.db` is disconnected from your app.

**To modify data, you must:**
1. Use the app UI (register, create posts, etc.)
2. OR modify code to insert data programmatically
3. OR use Android Studio's Database Inspector
4. OR use adb to push a modified database to emulator

## Summary

✅ **Your database IS configured correctly**
✅ **SQLite is already installed in Android**
✅ **Data IS being saved**
✅ **The "problem" is understanding WHERE the data is**

The database works perfectly - you were just looking in the wrong location!
