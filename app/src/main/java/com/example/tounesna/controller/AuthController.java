package com.example.tounesna.controller;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.tounesna.model.Organization;
import com.example.tounesna.model.Volunteer;
import com.example.tounesna.util.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthController {
    private static final String TAG = "AuthController";
    
    public interface AuthCallback {
        void onSuccess(String userId, String userType);
        void onError(String message);
    }
    
    public interface RegistrationCallback {
        void onSuccess(String userId);
        void onError(String message);
    }
    
    public interface UserDataCallback {
        void onVolunteerLoaded(Volunteer volunteer);
        void onOrganizationLoaded(Organization organization);
        void onError(String message);
    }
    
    private interface EmailCheckCallback {
        void onResult(boolean exists);
    }
    
    public static void registerVolunteer(Volunteer volunteer, RegistrationCallback callback) {
        String normalizedEmail = volunteer.getEmail().toLowerCase().trim();
        volunteer.setEmail(normalizedEmail);
        checkEmailExists(normalizedEmail, exists -> {
            if (exists) {
                callback.onError("Email already registered");
                return;
            }
            
            String volunteerId = FirebaseManager.generateId(FirebaseManager.PATH_VOLUNTEERS);
            volunteer.setId(volunteerId);
            volunteer.setCreatedAt(System.currentTimeMillis());
            volunteer.setUpdatedAt(System.currentTimeMillis());
            
            Map<String, Object> volunteerMap = new HashMap<>();
            volunteerMap.put("id", volunteerId);
            volunteerMap.put("name", volunteer.getName());
            volunteerMap.put("surname", volunteer.getSurname());
            volunteerMap.put("email", volunteer.getEmail());
            volunteerMap.put("password", volunteer.getPassword());
            volunteerMap.put("phone", volunteer.getPhone());
            volunteerMap.put("location", volunteer.getLocation());
            volunteerMap.put("profilePictureUrl", volunteer.getProfilePictureUrl());
            volunteerMap.put("interests", volunteer.getInterests());
            volunteerMap.put("skills", volunteer.getSkills());
            volunteerMap.put("availability", volunteer.getAvailability());
            volunteerMap.put("isApproved", true);
            volunteerMap.put("rating", 0.0);
            volunteerMap.put("ratingCount", 0);
            volunteerMap.put("createdAt", volunteer.getCreatedAt());
            volunteerMap.put("updatedAt", volunteer.getUpdatedAt());
            
            FirebaseManager.getVolunteersRef().child(volunteerId).setValue(volunteerMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess(volunteerId))
                .addOnFailureListener(e -> callback.onError("Registration failed: " + e.getMessage()));
        });
    }
    
    public static void registerOrganization(Organization organization, RegistrationCallback callback) {
        String normalizedEmail = organization.getEmail().toLowerCase().trim();
        organization.setEmail(normalizedEmail);
        checkEmailExists(normalizedEmail, exists -> {
            if (exists) {
                callback.onError("Email already registered");
                return;
            }
            
            String orgId = FirebaseManager.generateId(FirebaseManager.PATH_ORGANIZATIONS);
            organization.setId(orgId);
            organization.setCreatedAt(System.currentTimeMillis());
            organization.setUpdatedAt(System.currentTimeMillis());
            
            Map<String, Object> orgMap = new HashMap<>();
            orgMap.put("id", orgId);
            orgMap.put("name", organization.getName());
            orgMap.put("domain", organization.getDomain());
            orgMap.put("location", organization.getLocation());
            orgMap.put("website", organization.getWebsite());
            orgMap.put("email", organization.getEmail());
            orgMap.put("phone", organization.getPhone());
            orgMap.put("password", organization.getPassword());
            orgMap.put("profilePictureUrl", organization.getProfilePictureUrl());
            orgMap.put("memberCount", organization.getMemberCount());
            orgMap.put("foundedYear", organization.getFoundedYear());
            orgMap.put("isApproved", true);
            orgMap.put("rating", 0.0);
            orgMap.put("ratingCount", 0);
            orgMap.put("followersCount", 0);
            orgMap.put("tags", organization.getTags());
            orgMap.put("createdAt", organization.getCreatedAt());
            orgMap.put("updatedAt", organization.getUpdatedAt());
            
            FirebaseManager.getOrganizationsRef().child(orgId).setValue(orgMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess(orgId))
                .addOnFailureListener(e -> callback.onError("Registration failed: " + e.getMessage()));
        });
    }
    
    public static void loginVolunteer(String email, String password, AuthCallback callback) {
        String normalizedEmail = email.toLowerCase().trim();
        Log.d(TAG, "LoginVolunteer: Searching for email: " + normalizedEmail);
        Query query = FirebaseManager.getVolunteersRef().orderByChild("email").equalTo(normalizedEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "LoginVolunteer query completed. Exists: " + snapshot.exists() + ", Count: " + snapshot.getChildrenCount());
                if (snapshot.exists()) {
                    for (DataSnapshot volunteerSnapshot : snapshot.getChildren()) {
                        String foundEmail = volunteerSnapshot.child("email").getValue(String.class);
                        String storedPassword = volunteerSnapshot.child("password").getValue(String.class);
                        Log.d(TAG, "Found volunteer: " + foundEmail + ", Password match: " + password.equals(storedPassword));
                        if (password.equals(storedPassword)) {
                            // Handle both String and Long IDs
                            Object idObj = volunteerSnapshot.child("id").getValue();
                            String userId = (idObj instanceof String) ? (String) idObj : String.valueOf(idObj);
                            callback.onSuccess(userId, "volunteer");
                            return;
                        }
                    }
                    callback.onError("Invalid password");
                } else {
                    // Check if email exists in organizations collection
                    FirebaseManager.getOrganizationsRef().orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot orgSnapshot) {
                                if (orgSnapshot.exists()) {
                                    callback.onError("This email is registered as an Organization. Please select 'Organization' to login.");
                                } else {
                                    callback.onError("Email not found");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onError("Email not found");
                            }
                        });
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Login failed: " + error.getMessage());
            }
        });
    }
    
    public static void loginOrganization(String email, String password, AuthCallback callback) {
        String normalizedEmail = email.toLowerCase().trim();
        Log.d(TAG, "LoginOrganization: Searching for email: " + normalizedEmail);
        Query query = FirebaseManager.getOrganizationsRef().orderByChild("email").equalTo(normalizedEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "LoginOrganization query completed. Exists: " + snapshot.exists() + ", Count: " + snapshot.getChildrenCount());
                if (snapshot.exists()) {
                    for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                        String foundEmail = orgSnapshot.child("email").getValue(String.class);
                        String storedPassword = orgSnapshot.child("password").getValue(String.class);
                        Log.d(TAG, "Found organization: " + foundEmail + ", Password match: " + password.equals(storedPassword));
                        if (password.equals(storedPassword)) {
                            // Handle both String and Long IDs
                            Object idObj = orgSnapshot.child("id").getValue();
                            String userId = (idObj instanceof String) ? (String) idObj : String.valueOf(idObj);
                            Boolean isApproved = orgSnapshot.child("isApproved").getValue(Boolean.class);
                            if (isApproved != null && isApproved) {
                                callback.onSuccess(userId, "organization");
                            } else {
                                callback.onError("Organization not approved yet");
                            }
                            return;
                        }
                    }
                    callback.onError("Invalid password");
                } else {
                    // Check if email exists in volunteers collection
                    FirebaseManager.getVolunteersRef().orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot volSnapshot) {
                                if (volSnapshot.exists()) {
                                    callback.onError("This email is registered as a Volunteer. Please select 'Volunteer' to login.");
                                } else {
                                    callback.onError("Email not found");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onError("Email not found");
                            }
                        });
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Login failed: " + error.getMessage());
            }
        });
    }
    
    public static void getVolunteerById(String volunteerId, UserDataCallback callback) {
        FirebaseManager.getVolunteersRef().child(volunteerId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Volunteer volunteer = new Volunteer();
                        // Handle both String and Long IDs from Firebase
                        Object idObj = snapshot.child("id").getValue();
                        String id = (idObj instanceof String) ? (String) idObj : String.valueOf(idObj);
                        volunteer.setId(id);
                        volunteer.setName(snapshot.child("name").getValue(String.class));
                        volunteer.setSurname(snapshot.child("surname").getValue(String.class));
                        volunteer.setEmail(snapshot.child("email").getValue(String.class));
                        volunteer.setPhone(snapshot.child("phone").getValue(String.class));
                        volunteer.setLocation(snapshot.child("location").getValue(String.class));
                        volunteer.setProfilePictureUrl(snapshot.child("profilePictureUrl").getValue(String.class));
                        
                        // Load interests
                        DataSnapshot interestsSnapshot = snapshot.child("interests");
                        List<String> interests = new ArrayList<>();
                        if (interestsSnapshot.exists()) {
                            for (DataSnapshot interestSnapshot : interestsSnapshot.getChildren()) {
                                String interest = interestSnapshot.getValue(String.class);
                                if (interest != null) interests.add(interest);
                            }
                        }
                        volunteer.setInterests(interests);
                        
                        Long createdAt = snapshot.child("createdAt").getValue(Long.class);
                        if (createdAt != null) volunteer.setCreatedAt(createdAt);
                        callback.onVolunteerLoaded(volunteer);
                    } else {
                        callback.onError("Volunteer not found");
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void getOrganizationById(String orgId, UserDataCallback callback) {
        FirebaseManager.getOrganizationsRef().child(orgId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Organization org = new Organization();
                        // Handle both String and Long IDs from Firebase
                        Object idObj = snapshot.child("id").getValue();
                        String id = (idObj instanceof String) ? (String) idObj : String.valueOf(idObj);
                        org.setId(id);
                        org.setName(snapshot.child("name").getValue(String.class));
                        org.setDomain(snapshot.child("domain").getValue(String.class));
                        org.setLocation(snapshot.child("location").getValue(String.class));
                        org.setEmail(snapshot.child("email").getValue(String.class));
                        org.setPhone(snapshot.child("phone").getValue(String.class));
                        org.setProfilePictureUrl(snapshot.child("profilePictureUrl").getValue(String.class));
                        org.setWebsite(snapshot.child("website").getValue(String.class));
                        org.setDescription(snapshot.child("description").getValue(String.class));
                        
                        // Load tags
                        DataSnapshot tagsSnapshot = snapshot.child("tags");
                        List<String> tags = new ArrayList<>();
                        if (tagsSnapshot.exists()) {
                            for (DataSnapshot tagSnapshot : tagsSnapshot.getChildren()) {
                                String tag = tagSnapshot.getValue(String.class);
                                if (tag != null) tags.add(tag);
                            }
                        }
                        org.setTags(tags);
                        
                        Integer memberCount = snapshot.child("memberCount").getValue(Integer.class);
                        if (memberCount != null) org.setMemberCount(memberCount);
                        Integer foundedYear = snapshot.child("foundedYear").getValue(Integer.class);
                        if (foundedYear != null) org.setFoundedYear(foundedYear);
                        Integer followersCount = snapshot.child("followersCount").getValue(Integer.class);
                        if (followersCount != null) org.setFollowersCount(followersCount);
                        Integer ratingCount = snapshot.child("ratingCount").getValue(Integer.class);
                        if (ratingCount != null) org.setRatingCount(ratingCount);
                        
                        Boolean isApproved = snapshot.child("isApproved").getValue(Boolean.class);
                        if (isApproved != null) org.setApproved(isApproved);
                        Double rating = snapshot.child("rating").getValue(Double.class);
                        if (rating != null) org.setRating(rating);
                        callback.onOrganizationLoaded(org);
                    } else {
                        callback.onError("Organization not found");
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    private static void checkEmailExists(String email, EmailCheckCallback callback) {
        FirebaseManager.getVolunteersRef().orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        callback.onResult(true);
                    } else {
                        FirebaseManager.getOrganizationsRef().orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    callback.onResult(snapshot.exists());
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    callback.onResult(false);
                                }
                            });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onResult(false);
                }
            });
    }
    
    // Public helper methods for other controllers
    public static Volunteer snapshotToVolunteer(DataSnapshot snapshot) {
        Volunteer volunteer = new Volunteer();
        // Handle both String and Long IDs
        Object idObj = snapshot.child("id").getValue();
        volunteer.setId((idObj instanceof String) ? (String) idObj : String.valueOf(idObj));
        volunteer.setName(snapshot.child("name").getValue(String.class));
        volunteer.setEmail(snapshot.child("email").getValue(String.class));
        volunteer.setPhone(snapshot.child("phone").getValue(String.class));
        volunteer.setPassword(snapshot.child("password").getValue(String.class));
        volunteer.setDateOfBirth(snapshot.child("dateOfBirth").getValue(String.class));
        volunteer.setLocation(snapshot.child("location").getValue(String.class));
        
        Long createdAt = snapshot.child("createdAt").getValue(Long.class);
        if (createdAt != null) volunteer.setCreatedAt(createdAt);
        
        Long updatedAt = snapshot.child("updatedAt").getValue(Long.class);
        if (updatedAt != null) volunteer.setUpdatedAt(updatedAt);
        
        return volunteer;
    }
    
    public static Organization snapshotToOrganization(DataSnapshot snapshot) {
        Organization org = new Organization();
        // Handle both String and Long IDs
        Object idObj = snapshot.child("id").getValue();
        org.setId((idObj instanceof String) ? (String) idObj : String.valueOf(idObj));
        org.setName(snapshot.child("name").getValue(String.class));
        org.setDomain(snapshot.child("domain").getValue(String.class));
        org.setDescription(snapshot.child("description").getValue(String.class));
        org.setLocation(snapshot.child("location").getValue(String.class));
        org.setEmail(snapshot.child("email").getValue(String.class));
        org.setPhone(snapshot.child("phone").getValue(String.class));
        org.setPassword(snapshot.child("password").getValue(String.class));
        org.setRegistrationNumber(snapshot.child("registrationNumber").getValue(String.class));
        
        Boolean isApproved = snapshot.child("isApproved").getValue(Boolean.class);
        if (isApproved != null) org.setApproved(isApproved);
        
        Double rating = snapshot.child("rating").getValue(Double.class);
        if (rating != null) org.setRating(rating);
        
        Long createdAt = snapshot.child("createdAt").getValue(Long.class);
        if (createdAt != null) org.setCreatedAt(createdAt);
        
        Long updatedAt = snapshot.child("updatedAt").getValue(Long.class);
        if (updatedAt != null) org.setUpdatedAt(updatedAt);
        
        return org;
    }
}
