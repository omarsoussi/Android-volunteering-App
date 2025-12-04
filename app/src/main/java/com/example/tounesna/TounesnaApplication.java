package com.example.tounesna;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.example.tounesna.util.FirebaseManager;
import com.google.firebase.FirebaseApp;

/**
 * Application class - Initializes app-wide resources
 * Now uses Firebase Realtime Database instead of SQLite
 */
public class TounesnaApplication extends MultiDexApplication {
    
    private static final String TAG = "TounesnaApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            FirebaseManager.init(this);
            Log.d(TAG, "✅ Application started - Firebase initialized");
            Log.d(TAG, "🔥 Using Firebase Realtime Database");
            Log.d(TAG, "📍 Database URL: https://tounesna-8021d-default-rtdb.firebaseio.com");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing Firebase: " + e.getMessage(), e);
        }
    }
}
