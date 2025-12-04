package com.example.tounesna.controller;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.tounesna.model.Notification;
import com.example.tounesna.model.Rating;
import com.example.tounesna.util.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingController {
    private static final String TAG = "RatingController";
    
    public interface RatingCallback {
        void onSuccess(String ratingId);
        void onError(String message);
    }
    
    public interface RatingsCallback {
        void onSuccess(List<Rating> ratings);
        void onError(String message);
    }
    
    public interface AverageRatingCallback {
        void onSuccess(double averageRating, int totalRatings);
        void onError(String message);
    }
    
    public static void addRating(Rating rating, RatingCallback callback) {
        String ratingId = FirebaseManager.generateId(FirebaseManager.PATH_RATINGS);
        rating.setId(ratingId);
        rating.setCreatedAt(System.currentTimeMillis());
        
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("id", ratingId);
        ratingMap.put("volunteerId", rating.getVolunteerId());
        ratingMap.put("organizationId", rating.getOrganizationId());
        ratingMap.put("score", rating.getScore());
        ratingMap.put("comment", rating.getComment());
        ratingMap.put("createdAt", rating.getCreatedAt());
        
        FirebaseManager.getRatingsRef().child(ratingId).setValue(ratingMap)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ Rating added: " + ratingId);
                
                // Update organization's average rating
                updateOrganizationRating(rating.getOrganizationId(), new RatingCallback() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "✅ Organization rating updated");
                        
                        // Send notification to organization
                        sendRatingNotification(rating);
                        
                        callback.onSuccess(ratingId);
                    }
                    
                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "⚠️ Rating added but failed to update organization average: " + message);
                        // Still report success since rating was saved
                        callback.onSuccess(ratingId);
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Add rating failed", e);
                callback.onError("Failed to add rating: " + e.getMessage());
            });
    }
    
    private static void updateOrganizationRating(String organizationId, RatingCallback callback) {
        getAverageRating(organizationId, new AverageRatingCallback() {
            @Override
            public void onSuccess(double averageRating, int totalRatings) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("rating", (float) averageRating);
                updates.put("totalRatings", totalRatings);
                
                FirebaseManager.getOrganizationsRef().child(organizationId).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Updated org " + organizationId + " rating to " + averageRating + " (" + totalRatings + " ratings)");
                        callback.onSuccess(organizationId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to update organization rating", e);
                        callback.onError(e.getMessage());
                    });
            }
            
            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
    
    public static void getRatingsForOrganization(String organizationId, RatingsCallback callback) {
        Query query = FirebaseManager.getRatingsRef()
            .orderByChild("organizationId")
            .equalTo(organizationId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Rating> ratings = new ArrayList<>();
                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    Rating rating = snapshotToRating(ratingSnapshot);
                    ratings.add(rating);
                }
                callback.onSuccess(ratings);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    
    public static void getAverageRating(String organizationId, AverageRatingCallback callback) {
        Query query = FirebaseManager.getRatingsRef()
            .orderByChild("organizationId")
            .equalTo(organizationId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onSuccess(0.0, 0);
                    return;
                }
                
                double totalScore = 0.0;
                int count = 0;
                
                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    Float score = ratingSnapshot.child("score").getValue(Float.class);
                    if (score != null) {
                        totalScore += score;
                        count++;
                    }
                }
                
                double average = count > 0 ? totalScore / count : 0.0;
                callback.onSuccess(average, count);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    
    public static void checkIfUserRated(String volunteerId, String organizationId, RatingsCallback callback) {
        FirebaseManager.getRatingsRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Rating> userRatings = new ArrayList<>();
                    for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                        // Handle both String and Long IDs
                        Object vidObj = ratingSnapshot.child("volunteerId").getValue();
                        String vid = (vidObj instanceof String) ? (String) vidObj : String.valueOf(vidObj);
                        
                        Object oidObj = ratingSnapshot.child("organizationId").getValue();
                        String oid = (oidObj instanceof String) ? (String) oidObj : String.valueOf(oidObj);
                        
                        if (volunteerId.equals(vid) && organizationId.equals(oid)) {
                            Rating rating = snapshotToRating(ratingSnapshot);
                            userRatings.add(rating);
                        }
                    }
                    callback.onSuccess(userRatings);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    private static void sendRatingNotification(Rating rating) {
        Notification notification = new Notification();
        notification.setUserId(rating.getOrganizationId());
        notification.setUserType("organization");
        notification.setType("NEW_RATING");
        notification.setTitle("New Rating Received!");
        notification.setMessage("A volunteer rated your organization " + rating.getScore() + " stars");
        notification.setRelatedVolunteerId(rating.getVolunteerId());
        
        NotificationController.createNotification(notification, new NotificationController.NotificationCallback() {
            @Override
            public void onSuccess(String notificationId) {
                Log.d(TAG, "✅ Rating notification sent: " + notificationId);
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "⚠️ Failed to send rating notification: " + message);
            }
        });
    }
    
    private static Rating snapshotToRating(DataSnapshot snapshot) {
        Rating rating = new Rating();
        // Handle both String and Long IDs
        Object idObj = snapshot.child("id").getValue();
        rating.setId((idObj instanceof String) ? (String) idObj : String.valueOf(idObj));
        
        Object vidObj = snapshot.child("volunteerId").getValue();
        rating.setVolunteerId((vidObj instanceof String) ? (String) vidObj : String.valueOf(vidObj));
        
        Object oidObj = snapshot.child("organizationId").getValue();
        rating.setOrganizationId((oidObj instanceof String) ? (String) oidObj : String.valueOf(oidObj));
        
        Float score = snapshot.child("score").getValue(Float.class);
        if (score != null) rating.setScore(score);
        
        rating.setComment(snapshot.child("comment").getValue(String.class));
        
        Long createdAt = snapshot.child("createdAt").getValue(Long.class);
        if (createdAt != null) rating.setCreatedAt(createdAt);
        
        return rating;
    }
}
