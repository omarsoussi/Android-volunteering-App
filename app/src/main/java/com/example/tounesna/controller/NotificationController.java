package com.example.tounesna.controller;

import android.util.Log;
import androidx.annotation.NonNull;
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

public class NotificationController {
    private static final String TAG = "NotificationController";
    
    public interface NotificationCallback {
        void onSuccess(String notificationId);
        void onError(String message);
    }
    
    public interface NotificationsCallback {
        void onSuccess(List<Notification> notifications);
        void onError(String message);
    }
    
    public static void createNotification(Notification notification, NotificationCallback callback) {
        String notificationId = FirebaseManager.generateId(FirebaseManager.PATH_NOTIFICATIONS);
        notification.setId(notificationId);
        notification.setCreatedAt(System.currentTimeMillis());
        notification.setRead(false);
        
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("id", notificationId);
        notificationMap.put("userId", notification.getUserId());
        notificationMap.put("title", notification.getTitle());
        notificationMap.put("message", notification.getMessage());
        notificationMap.put("type", notification.getType());
        notificationMap.put("isRead", notification.isRead());
        notificationMap.put("createdAt", notification.getCreatedAt());
        
        FirebaseManager.getNotificationsRef().child(notificationId).setValue(notificationMap)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ Notification created: " + notificationId);
                callback.onSuccess(notificationId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Create notification failed", e);
                callback.onError("Failed to create notification: " + e.getMessage());
            });
    }
    
    public static void getNotificationsForUser(String userId, NotificationsCallback callback) {
        Query query = FirebaseManager.getNotificationsRef()
            .orderByChild("userId")
            .equalTo(userId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Notification> notifications = new ArrayList<>();
                for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                    Notification notification = snapshotToNotification(notificationSnapshot);
                    notifications.add(notification);
                }
                callback.onSuccess(notifications);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    
    public static void getUnreadNotifications(String userId, NotificationsCallback callback) {
        FirebaseManager.getNotificationsRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Notification> notifications = new ArrayList<>();
                    for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                        // Handle both String and Long IDs
                        Object uidObj = notificationSnapshot.child("userId").getValue();
                        String uid = (uidObj instanceof String) ? (String) uidObj : String.valueOf(uidObj);
                        Boolean isRead = notificationSnapshot.child("isRead").getValue(Boolean.class);
                        
                        if (userId.equals(uid) && (isRead == null || !isRead)) {
                            Notification notification = snapshotToNotification(notificationSnapshot);
                            notifications.add(notification);
                        }
                    }
                    callback.onSuccess(notifications);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void markAsRead(String notificationId, NotificationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isRead", true);
        
        FirebaseManager.getNotificationsRef().child(notificationId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ Notification marked as read: " + notificationId);
                callback.onSuccess(notificationId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Mark as read failed", e);
                callback.onError("Failed to mark as read: " + e.getMessage());
            });
    }
    
    public static void markAllAsRead(String userId, NotificationCallback callback) {
        FirebaseManager.getNotificationsRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                        // Handle both String and Long IDs
                        Object uidObj = notificationSnapshot.child("userId").getValue();
                        String uid = (uidObj instanceof String) ? (String) uidObj : String.valueOf(uidObj);
                        
                        Boolean isRead = notificationSnapshot.child("isRead").getValue(Boolean.class);
                        
                        if (userId.equals(uid) && (isRead == null || !isRead)) {
                            Object nidObj = notificationSnapshot.child("id").getValue();
                            String nid = (nidObj instanceof String) ? (String) nidObj : String.valueOf(nidObj);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("isRead", true);
                            FirebaseManager.getNotificationsRef().child(nid).updateChildren(updates);
                            count++;
                        }
                    }
                    Log.d(TAG, "✅ Marked " + count + " notifications as read");
                    callback.onSuccess("Marked " + count + " as read");
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void getUnreadCount(String userId, UnreadCountCallback callback) {
        getUnreadNotifications(userId, new NotificationsCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                callback.onSuccess(notifications.size());
            }
            
            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
    
    public interface UnreadCountCallback {
        void onSuccess(int count);
        void onError(String message);
    }
    
    private static Notification snapshotToNotification(DataSnapshot snapshot) {
        Notification notification = new Notification();
        // Handle both String and Long IDs
        Object idObj = snapshot.child("id").getValue();
        notification.setId((idObj instanceof String) ? (String) idObj : String.valueOf(idObj));
        
        Object uidObj = snapshot.child("userId").getValue();
        notification.setUserId((uidObj instanceof String) ? (String) uidObj : String.valueOf(uidObj));
        notification.setTitle(snapshot.child("title").getValue(String.class));
        notification.setMessage(snapshot.child("message").getValue(String.class));
        notification.setType(snapshot.child("type").getValue(String.class));
        
        Boolean isRead = snapshot.child("isRead").getValue(Boolean.class);
        if (isRead != null) notification.setRead(isRead);
        
        Long createdAt = snapshot.child("createdAt").getValue(Long.class);
        if (createdAt != null) notification.setCreatedAt(createdAt);
        
        return notification;
    }
}
