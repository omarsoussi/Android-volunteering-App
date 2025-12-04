package com.example.tounesna.util;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * FirebaseManager - Centralized Firebase Realtime Database access
 */
public class FirebaseManager {
    
    private static final String TAG = "FirebaseManager";
    private static FirebaseDatabase database;
    private static boolean initialized = false;
    
    // Firebase paths
    public static final String PATH_VOLUNTEERS = "volunteers";
    public static final String PATH_ORGANIZATIONS = "organizations";
    public static final String PATH_POSTS = "posts";
    public static final String PATH_RATINGS = "ratings";
    public static final String PATH_VOLUNTEER_REQUESTS = "volunteer_requests";
    public static final String PATH_FOLLOWS = "follows";
    public static final String PATH_NOTIFICATIONS = "notifications";
    public static final String PATH_POST_VIEWS = "post_views";
    
    /**
     * Initialize Firebase Database
     */
    public static void init(Context context) {
        if (!initialized) {
            try {
                database = FirebaseDatabase.getInstance("https://tounesna-8021d-default-rtdb.firebaseio.com");
                database.setPersistenceEnabled(true); // Enable offline support
                
                // Set cache size to 10MB (helps prevent crashes on low-end devices)
                database.setPersistenceCacheSizeBytes(10 * 1024 * 1024);
                
                initialized = true;
                Log.d(TAG, "✅ Firebase Database initialized successfully");
                Log.d(TAG, "📍 Database URL: https://tounesna-8021d-default-rtdb.firebaseio.com");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error initializing Firebase", e);
                // Continue without persistence on error
                try {
                    database = FirebaseDatabase.getInstance("https://tounesna-8021d-default-rtdb.firebaseio.com");
                    initialized = true;
                    Log.w(TAG, "⚠️ Firebase initialized without persistence");
                } catch (Exception e2) {
                    Log.e(TAG, "❌ Critical: Cannot initialize Firebase at all", e2);
                    throw new RuntimeException("Failed to initialize Firebase", e2);
                }
            }
        }
    }
    
    /**
     * Get Firebase Database instance
     */
    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("FirebaseManager not initialized. Call init() first.");
        }
        return database;
    }
    
    /**
     * Get reference to volunteers path
     */
    public static DatabaseReference getVolunteersRef() {
        return getDatabase().getReference(PATH_VOLUNTEERS);
    }
    
    /**
     * Get reference to organizations path
     */
    public static DatabaseReference getOrganizationsRef() {
        return getDatabase().getReference(PATH_ORGANIZATIONS);
    }
    
    /**
     * Get reference to posts path
     */
    public static DatabaseReference getPostsRef() {
        return getDatabase().getReference(PATH_POSTS);
    }
    
    /**
     * Get reference to ratings path
     */
    public static DatabaseReference getRatingsRef() {
        return getDatabase().getReference(PATH_RATINGS);
    }
    
    /**
     * Get reference to volunteer requests path
     */
    public static DatabaseReference getVolunteerRequestsRef() {
        return getDatabase().getReference(PATH_VOLUNTEER_REQUESTS);
    }
    
    /**
     * Get reference to follows path
     */
    public static DatabaseReference getFollowsRef() {
        return getDatabase().getReference(PATH_FOLLOWS);
    }
    
    /**
     * Get reference to notifications path
     */
    public static DatabaseReference getNotificationsRef() {
        return getDatabase().getReference(PATH_NOTIFICATIONS);
    }
    
    /**
     * Get reference to post views path
     */
    public static DatabaseReference getPostViewsRef() {
        return getDatabase().getReference(PATH_POST_VIEWS);
    }
    
    /**
     * Generate unique ID
     */
    public static String generateId(String path) {
        return getDatabase().getReference(path).push().getKey();
    }
}
