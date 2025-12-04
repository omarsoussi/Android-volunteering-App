package com.example.tounesna.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager - Manages user session using SharedPreferences
 */
public class SessionManager {
    
    private static final String PREF_NAME = "VolunteeringSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    public static final String USER_TYPE_VOLUNTEER = "volunteer";
    public static final String USER_TYPE_ORGANIZATION = "organization";
    
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }
    
    /**
     * Create login session for volunteer
     */
    public void createVolunteerSession(String userId, String name, String email) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TYPE, USER_TYPE_VOLUNTEER);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    /**
     * Create login session for organization
     */
    public void createOrganizationSession(String userId, String name, String email) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TYPE, USER_TYPE_ORGANIZATION);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Get user ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
    
    /**
     * Get user type (volunteer or organization)
     */
    public String getUserType() {
        return prefs.getString(KEY_USER_TYPE, null);
    }
    
    /**
     * Get user name
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }
    
    /**
     * Get user email
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }
    
    /**
     * Check if user is an organization
     */
    public boolean isOrganization() {
        return USER_TYPE_ORGANIZATION.equals(getUserType());
    }
    
    /**
     * Check if user is a volunteer
     */
    public boolean isVolunteer() {
        return USER_TYPE_VOLUNTEER.equals(getUserType());
    }
    
    /**
     * Clear session data (logout)
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
