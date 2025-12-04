# How to View Your Database Data in SQLiteStudio

## ✅ SOLUTION: Your data IS being saved - you just need to sync it!

Your app saves data to the **device's internal storage**, not the project folder. The database file you're viewing in SQLiteStudio is initially empty because it's not the one the app uses.

## 🔄 Quick Solution - Sync Database

Run this script **ANYTIME** you want to see the latest data:

```bash
./sync_database.sh
```

This will:
1. Pull the database from the Android device
2. Copy it to `/Users/mac/AndroidStudioProjects/tounesna/tounesna.db`
3. Show you statistics (count of volunteers, organizations, etc.)

Then in **SQLiteStudio**:
1. Right-click on the `tounesna` database connection
2. Click **"Refresh"** or press **F5**
3. You'll see all your data! ✅

## 📊 Current Data in Your Database

After running sync script:
- **7 Volunteers** registered
- **1 Organization** registered
- All tables working correctly

## 🔍 Two Database Locations Explained

### 1. Project Folder (for viewing in SQLiteStudio)
- **Path**: `/Users/mac/AndroidStudioProjects/tounesna/tounesna.db`
- **Purpose**: Template and viewing copy
- **Updated**: Only when you run `./sync_database.sh`

### 2. Android Device (actual app database)
- **Path**: `/data/user/0/com.example.tounesna/databases/tounesna.db`
- **Purpose**: Real-time app database
- **Updated**: Every time you register, create posts, etc.

## 📝 Workflow

1. **Use the app** (register, create posts, etc.)
2. **Run sync script**: `./sync_database.sh`
3. **Refresh SQLiteStudio** (F5)
4. **View your data** ✅

## 🎯 Proof It's Working

Your logs show:
```
✅ Volunteer registered successfully with ID: 8
Email: helliw@gmail.com, Name: helliw
Verification: Record exists = true
```

Your database contains:
- omar soussi (omarsoussi@gmail.com)
- Yassin chayyda (yassin@gmail.com)
- vcewce cewew (hey@gmail.com)
- amine amine (amine@gmail.com)
- omar soussi (soussi@gmail.com)
- hi hi (hi@gmail.con)
- **helliw helliw (helliw@gmail.com)** ← Latest registration
- CRTMahdia organization (crt@gmail.com)

## 🚀 Alternative: Auto-Export

The app now exports the database automatically after each registration to:
```
/storage/emulated/0/Android/data/com.example.tounesna/files/database_export/tounesna_export.db
```

You can pull this file too:
```bash
/Users/mac/Library/Developer/Xamarin/android-sdk-macosx/platform-tools/adb pull /storage/emulated/0/Android/data/com.example.tounesna/files/database_export/tounesna_export.db ./tounesna.db
```

## ✅ Summary

**Your database is 100% functional!** Data is being saved correctly. You just needed to pull it from the device to view it in SQLiteStudio.

Use `./sync_database.sh` anytime you want to see the latest data.
