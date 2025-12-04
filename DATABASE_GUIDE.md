# Tounesna SQLite Database Guide

## Overview
This app uses **SQLite** as its local database. All data is stored on the device in `/data/data/com.example.tounesna/databases/tounesna.db`

## Database Architecture

### Database Manager
- **Class**: `DatabaseManager.java`
- **Location**: `util/DatabaseManager.java`
- **Purpose**: Main entry point for all database operations
- **Usage**: 
  ```java
  DatabaseManager.init(context);  // Initialize once
  SQLiteDatabase db = DatabaseManager.getWritableDatabase();
  ```

### Database Helper
- **Class**: `DatabaseHelper.java`
- **Location**: `util/DatabaseHelper.java`
- **Pattern**: Singleton
- **Version**: 1
- **Tables**: 8 tables created automatically on first run

## Database Tables

### 1. volunteers
Stores volunteer user accounts
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `name` TEXT NOT NULL
- `surname` TEXT NOT NULL
- `email` TEXT UNIQUE NOT NULL
- `password` TEXT NOT NULL
- `phone` TEXT
- `location` TEXT
- `profilePictureUrl` TEXT
- `interests` TEXT (JSON array)
- `isApproved` INTEGER DEFAULT 0
- `createdAt` INTEGER
- `updatedAt` INTEGER

### 2. organizations
Stores organization accounts
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `name` TEXT NOT NULL
- `domain` TEXT
- `location` TEXT
- `website` TEXT
- `email` TEXT UNIQUE NOT NULL
- `password` TEXT NOT NULL
- `phone` TEXT
- `profilePictureUrl` TEXT
- `memberCount` INTEGER DEFAULT 0
- `foundedYear` INTEGER
- `isApproved` INTEGER DEFAULT 0
- `rating` REAL DEFAULT 0.0
- `ratingCount` INTEGER DEFAULT 0
- `followersCount` INTEGER DEFAULT 0
- `tags` TEXT (JSON array)
- `createdAt` INTEGER
- `updatedAt` INTEGER

### 3. posts
Stores volunteer opportunity posts
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `organizationId` INTEGER NOT NULL (FK → organizations.id)
- `title` TEXT NOT NULL
- `description` TEXT
- `imageUrl` TEXT (local file:// URI)
- `location` TEXT
- `startDate` INTEGER (timestamp)
- `endDate` INTEGER (timestamp)
- `volunteersNeeded` INTEGER DEFAULT 0
- `category` TEXT
- `priority` TEXT
- `needs` TEXT (JSON array)
- `createdAt` INTEGER
- `updatedAt` INTEGER

### 4. ratings
Stores ratings given by volunteers to organizations
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `organizationId` INTEGER NOT NULL (FK → organizations.id)
- `volunteerId` INTEGER NOT NULL (FK → volunteers.id)
- `rating` REAL NOT NULL (1.0 to 5.0)
- `comment` TEXT
- `createdAt` INTEGER

### 5. volunteer_requests
Stores volunteer applications
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `volunteerId` INTEGER NOT NULL (FK → volunteers.id)
- `organizationId` INTEGER NOT NULL (FK → organizations.id)
- `postId` INTEGER (FK → posts.id)
- `message` TEXT
- `status` TEXT DEFAULT 'PENDING' (PENDING/APPROVED/REJECTED)
- `createdAt` INTEGER
- `updatedAt` INTEGER

### 6. follows
Tracks volunteer-organization follows
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `volunteerId` INTEGER NOT NULL (FK → volunteers.id)
- `organizationId` INTEGER NOT NULL (FK → organizations.id)
- `createdAt` INTEGER
- **UNIQUE** constraint on (volunteerId, organizationId)

### 7. notifications
Stores user notifications
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `userId` INTEGER NOT NULL
- `userType` TEXT NOT NULL (volunteer/organization)
- `type` TEXT NOT NULL (enum: NEW_FOLLOWER, NEW_RATING, etc.)
- `title` TEXT NOT NULL
- `message` TEXT NOT NULL
- `relatedPostId` INTEGER
- `relatedRequestId` INTEGER
- `relatedOrganizationId` INTEGER
- `relatedVolunteerId` INTEGER
- `isRead` INTEGER DEFAULT 0
- `createdAt` INTEGER

### 8. post_views
Tracks post view analytics
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `postId` INTEGER NOT NULL (FK → posts.id)
- `userId` INTEGER NOT NULL
- `userType` TEXT NOT NULL
- `viewedAt` INTEGER
- **UNIQUE** constraint on (postId, userId, userType)

## Controllers (Data Access Layer)

All controllers use SQLite exclusively:

1. **AuthController** - Registration, login, user retrieval
2. **PostController** - Post CRUD operations, search, filtering
3. **RatingController** - Rating submission, organization rating calculation
4. **FollowController** - Follow/unfollow, followers management
5. **NotificationController** - Notification CRUD, triggers
6. **SearchController** - Search organizations and posts
7. **VolunteerRequestController** - Request management, status updates

## Data Operations

### Create (INSERT)
```java
ContentValues values = new ContentValues();
values.put("column", value);
long id = db.insert(TABLE_NAME, null, values);
```

### Read (SELECT)
```java
Cursor cursor = db.query(TABLE_NAME, null, "id = ?", new String[]{id}, null, null, null);
if (cursor.moveToFirst()) {
    // Read data
}
cursor.close();
```

### Update
```java
ContentValues values = new ContentValues();
values.put("column", newValue);
int rows = db.update(TABLE_NAME, values, "id = ?", new String[]{id});
```

### Delete
```java
int rows = db.delete(TABLE_NAME, "id = ?", new String[]{id});
```

## Accessing the Database

### From Device/Emulator
```bash
# Pull database from device
adb pull /data/data/com.example.tounesna/databases/tounesna.db ./

# Query it
sqlite3 tounesna.db "SELECT * FROM volunteers;"
```

### From Android Studio
1. View → Tool Windows → App Inspection
2. Select Database Inspector
3. Connect to running app
4. Browse tables and data

## JSON Fields

Some fields store JSON arrays as TEXT:
- `volunteers.interests`
- `organizations.tags`
- `posts.needs`

Serialization handled by `AuthController.listToJson()` and `AuthController.jsonToList()`

## Image Storage

Images are stored as **local file:// URIs**, not uploaded to cloud:
- Profile pictures: `file:///storage/...`
- Post images: `file:///storage/...`

## Timestamps

All timestamps are stored as INTEGER (Unix milliseconds):
```java
System.currentTimeMillis()  // Current time
```

## Initialization

Database must be initialized in `MainActivity` or `LoginActivity`:
```java
DatabaseManager.init(this);
```

## No Firebase Dependencies

This app is **100% offline** and uses **zero Firebase services**. All data is stored locally in SQLite.
