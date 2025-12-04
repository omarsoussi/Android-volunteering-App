-- SQLite Database Schema for Tounesna App
-- Run this with: sqlite3 tounesna.db < create_tables.sql

-- Volunteers Table
CREATE TABLE IF NOT EXISTS volunteers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    phone TEXT,
    location TEXT,
    profilePictureUrl TEXT,
    interests TEXT,
    isApproved INTEGER DEFAULT 0,
    createdAt INTEGER,
    updatedAt INTEGER
);

-- Organizations Table
CREATE TABLE IF NOT EXISTS organizations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    domain TEXT,
    location TEXT,
    website TEXT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    phone TEXT,
    profilePictureUrl TEXT,
    memberCount INTEGER DEFAULT 0,
    foundedYear INTEGER,
    isApproved INTEGER DEFAULT 0,
    rating REAL DEFAULT 0.0,
    ratingCount INTEGER DEFAULT 0,
    followersCount INTEGER DEFAULT 0,
    tags TEXT,
    createdAt INTEGER,
    updatedAt INTEGER
);

-- Posts Table
CREATE TABLE IF NOT EXISTS posts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    organizationId INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    imageUrl TEXT,
    location TEXT,
    startDate INTEGER,
    endDate INTEGER,
    volunteersNeeded INTEGER DEFAULT 0,
    category TEXT,
    priority TEXT,
    needs TEXT,
    createdAt INTEGER,
    updatedAt INTEGER,
    FOREIGN KEY(organizationId) REFERENCES organizations(id)
);

-- Ratings Table
CREATE TABLE IF NOT EXISTS ratings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    organizationId INTEGER NOT NULL,
    volunteerId INTEGER NOT NULL,
    rating REAL NOT NULL,
    comment TEXT,
    createdAt INTEGER,
    FOREIGN KEY(organizationId) REFERENCES organizations(id),
    FOREIGN KEY(volunteerId) REFERENCES volunteers(id)
);

-- Volunteer Requests Table
CREATE TABLE IF NOT EXISTS volunteer_requests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    volunteerId INTEGER NOT NULL,
    organizationId INTEGER NOT NULL,
    postId INTEGER,
    message TEXT,
    status TEXT DEFAULT 'PENDING',
    createdAt INTEGER,
    updatedAt INTEGER,
    FOREIGN KEY(volunteerId) REFERENCES volunteers(id),
    FOREIGN KEY(organizationId) REFERENCES organizations(id),
    FOREIGN KEY(postId) REFERENCES posts(id)
);

-- Follows Table
CREATE TABLE IF NOT EXISTS follows (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    volunteerId INTEGER NOT NULL,
    organizationId INTEGER NOT NULL,
    createdAt INTEGER,
    FOREIGN KEY(volunteerId) REFERENCES volunteers(id),
    FOREIGN KEY(organizationId) REFERENCES organizations(id),
    UNIQUE(volunteerId, organizationId)
);

-- Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    userType TEXT NOT NULL,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    relatedPostId INTEGER,
    relatedRequestId INTEGER,
    relatedOrganizationId INTEGER,
    relatedVolunteerId INTEGER,
    isRead INTEGER DEFAULT 0,
    createdAt INTEGER
);

-- Post Views Table
CREATE TABLE IF NOT EXISTS post_views (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    postId INTEGER NOT NULL,
    userId INTEGER NOT NULL,
    userType TEXT NOT NULL,
    viewedAt INTEGER,
    FOREIGN KEY(postId) REFERENCES posts(id),
    UNIQUE(postId, userId, userType)
);

-- Display confirmation
SELECT 'All tables created successfully!' AS message;
