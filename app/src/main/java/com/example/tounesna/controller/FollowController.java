package com.example.tounesna.controller;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.tounesna.model.Follow;
import com.example.tounesna.model.Notification;
import com.example.tounesna.util.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowController {
    private static final String TAG = "FollowController";
    
    public interface FollowCallback {
        void onSuccess(String followId);
        void onError(String message);
    }
    
    public interface FollowsCallback {
        void onSuccess(List<Follow> follows);
        void onError(String message);
    }
    
    public interface FollowCheckCallback {
        void onSuccess(boolean isFollowing);
        void onError(String message);
    }
    
    public static void followOrganization(String volunteerId, String organizationId, FollowCallback callback) {
        String followId = FirebaseManager.generateId(FirebaseManager.PATH_FOLLOWS);
        
        Map<String, Object> followMap = new HashMap<>();
        followMap.put("id", followId);
        followMap.put("volunteerId", volunteerId);
        followMap.put("organizationId", organizationId);
        followMap.put("createdAt", System.currentTimeMillis());
        
        FirebaseManager.getFollowsRef().child(followId).setValue(followMap)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ Follow created: " + followId);
                
                // Increment organization's follower count
                incrementFollowerCount(organizationId);
                
                // Send notification to organization
                sendFollowNotification(volunteerId, organizationId);
                
                callback.onSuccess(followId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Follow failed", e);
                callback.onError("Failed to follow: " + e.getMessage());
            });
    }
    
    private static void incrementFollowerCount(String organizationId) {
        FirebaseManager.getOrganizationsRef().child(organizationId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Integer currentCount = snapshot.child("followersCount").getValue(Integer.class);
                        int newCount = (currentCount != null ? currentCount : 0) + 1;
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("followersCount", newCount);
                        
                        FirebaseManager.getOrganizationsRef().child(organizationId).updateChildren(updates)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Follower count updated to " + newCount))
                            .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to update follower count", e));
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "❌ Failed to read organization for follower count", error.toException());
                }
            });
    }
    
    private static void decrementFollowerCount(String organizationId) {
        FirebaseManager.getOrganizationsRef().child(organizationId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Integer currentCount = snapshot.child("followersCount").getValue(Integer.class);
                        int newCount = Math.max((currentCount != null ? currentCount : 0) - 1, 0);
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("followersCount", newCount);
                        
                        FirebaseManager.getOrganizationsRef().child(organizationId).updateChildren(updates)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Follower count updated to " + newCount))
                            .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to update follower count", e));
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "❌ Failed to read organization for follower count", error.toException());
                }
            });
    }
    
    private static void sendFollowNotification(String volunteerId, String organizationId) {
        Notification notification = new Notification();
        notification.setUserId(organizationId);
        notification.setUserType("organization");
        notification.setType("NEW_FOLLOWER");
        notification.setTitle("New Follower!");
        notification.setMessage("A volunteer started following your organization");
        notification.setRelatedVolunteerId(volunteerId);
        
        NotificationController.createNotification(notification, new NotificationController.NotificationCallback() {
            @Override
            public void onSuccess(String notificationId) {
                Log.d(TAG, "✅ Follow notification sent: " + notificationId);
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "⚠️ Failed to send follow notification: " + message);
            }
        });
    }
    
    public static void unfollowOrganization(String volunteerId, String organizationId, FollowCallback callback) {
        FirebaseManager.getFollowsRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot followSnapshot : snapshot.getChildren()) {
                        // Handle both String and Long IDs
                        Object vidObj = followSnapshot.child("volunteerId").getValue();
                        String vid = (vidObj instanceof String) ? (String) vidObj : String.valueOf(vidObj);
                        
                        Object oidObj = followSnapshot.child("organizationId").getValue();
                        String oid = (oidObj instanceof String) ? (String) oidObj : String.valueOf(oidObj);
                        
                        if (volunteerId.equals(vid) && organizationId.equals(oid)) {
                            Object fidObj = followSnapshot.child("id").getValue();
                            String followId = (fidObj instanceof String) ? (String) fidObj : String.valueOf(fidObj);
                            FirebaseManager.getFollowsRef().child(followId).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✅ Unfollowed: " + followId);
                                    
                                    // Decrement organization's follower count
                                    decrementFollowerCount(organizationId);
                                    
                                    callback.onSuccess(followId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ Unfollow failed", e);
                                    callback.onError("Failed to unfollow: " + e.getMessage());
                                });
                            return;
                        }
                    }
                    callback.onError("Follow relationship not found");
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void isFollowing(String volunteerId, String organizationId, FollowCheckCallback callback) {
        FirebaseManager.getFollowsRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot followSnapshot : snapshot.getChildren()) {
                        // Handle both String and Long IDs
                        Object vidObj = followSnapshot.child("volunteerId").getValue();
                        String vid = (vidObj instanceof String) ? (String) vidObj : String.valueOf(vidObj);
                        
                        Object oidObj = followSnapshot.child("organizationId").getValue();
                        String oid = (oidObj instanceof String) ? (String) oidObj : String.valueOf(oidObj);
                        
                        if (volunteerId.equals(vid) && organizationId.equals(oid)) {
                            callback.onSuccess(true);
                            return;
                        }
                    }
                    callback.onSuccess(false);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void getFollowedOrganizations(String volunteerId, FollowsCallback callback) {
        Query query = FirebaseManager.getFollowsRef()
            .orderByChild("volunteerId")
            .equalTo(volunteerId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Follow> follows = new ArrayList<>();
                for (DataSnapshot followSnapshot : snapshot.getChildren()) {
                    Follow follow = snapshotToFollow(followSnapshot);
                    follows.add(follow);
                }
                callback.onSuccess(follows);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    
    public static void getFollowers(String organizationId, FollowsCallback callback) {
        Query query = FirebaseManager.getFollowsRef()
            .orderByChild("organizationId")
            .equalTo(organizationId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Follow> follows = new ArrayList<>();
                for (DataSnapshot followSnapshot : snapshot.getChildren()) {
                    Follow follow = snapshotToFollow(followSnapshot);
                    follows.add(follow);
                }
                callback.onSuccess(follows);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    
    private static Follow snapshotToFollow(DataSnapshot snapshot) {
        Follow follow = new Follow();
        // Handle both String and Long IDs
        Object idObj = snapshot.child("id").getValue();
        follow.setId((idObj instanceof String) ? (String) idObj : String.valueOf(idObj));
        
        Object vidObj = snapshot.child("volunteerId").getValue();
        follow.setVolunteerId((vidObj instanceof String) ? (String) vidObj : String.valueOf(vidObj));
        
        Object oidObj = snapshot.child("organizationId").getValue();
        follow.setOrganizationId((oidObj instanceof String) ? (String) oidObj : String.valueOf(oidObj));
        
        Long createdAt = snapshot.child("createdAt").getValue(Long.class);
        if (createdAt != null) follow.setCreatedAt(createdAt);
        
        return follow;
    }
}
