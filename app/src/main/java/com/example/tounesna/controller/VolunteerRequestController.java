package com.example.tounesna.controller;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.tounesna.model.Notification;
import com.example.tounesna.model.VolunteerRequest;
import com.example.tounesna.util.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolunteerRequestController {
    private static final String TAG = "VolunteerRequestController";
    
    public interface RequestCallback {
        void onSuccess(String requestId);
        void onError(String message);
    }
    
    public interface RequestsCallback {
        void onSuccess(List<VolunteerRequest> requests);
        void onError(String message);
    }
    
    public interface SingleRequestCallback {
        void onSuccess(VolunteerRequest request);
        void onError(String message);
    }
    
    public static void createRequest(VolunteerRequest request, RequestCallback callback) {
        // Handle multiple organizations - create a separate request for each
        List<String> orgIds = request.getOrganizationIds();
        
        if (orgIds == null || orgIds.isEmpty()) {
            // Fallback to single organizationId if available
            String singleOrgId = request.getOrganizationId();
            if (singleOrgId != null && !singleOrgId.isEmpty()) {
                orgIds = new ArrayList<>();
                orgIds.add(singleOrgId);
            } else {
                callback.onError("No organization selected");
                return;
            }
        }
        
        // Create a request for each organization
        for (String organizationId : orgIds) {
            String requestId = FirebaseManager.generateId(FirebaseManager.PATH_VOLUNTEER_REQUESTS);
            long createdAt = System.currentTimeMillis();
            
            Log.d(TAG, "🔧 Creating request for organizationId: " + organizationId);
            
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("id", requestId);
            requestMap.put("volunteerId", request.getVolunteerId());
            requestMap.put("organizationId", organizationId);  // Single org ID
            requestMap.put("postId", request.getPostId());
            requestMap.put("title", request.getTitle());
            requestMap.put("description", request.getDescription());
            requestMap.put("location", request.getLocation());
            requestMap.put("priority", request.getPriority() != null ? request.getPriority().toString() : "MEDIUM");
            requestMap.put("needs", request.getNeeds());
            requestMap.put("message", request.getMessage());
            requestMap.put("imageUrl", request.getImageUrl());
            requestMap.put("status", "PENDING");
            requestMap.put("createdAt", createdAt);
            
            Log.d(TAG, "📦 Saving request map: " + requestMap.toString());
            
            FirebaseManager.getVolunteerRequestsRef().child(requestId).setValue(requestMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Request created: " + requestId + " for org: " + organizationId);
                    
                    // Send notifications for this organization
                    VolunteerRequest singleOrgRequest = new VolunteerRequest();
                    singleOrgRequest.setId(requestId);
                    singleOrgRequest.setVolunteerId(request.getVolunteerId());
                    singleOrgRequest.setOrganizationId(organizationId);
                    singleOrgRequest.setPostId(request.getPostId());
                    sendRequestNotifications(singleOrgRequest);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Create request failed for org " + organizationId, e);
                });
        }
        
        // Call success callback after initiating all requests
        callback.onSuccess("Requests sent to " + orgIds.size() + " organization(s)");
    }
    
    public static void getRequestsForOrganization(String organizationId, RequestsCallback callback) {
        Log.d(TAG, "📥 Fetching requests for organization: " + organizationId);
        
        // TEMPORARY: Using manual filter until Firebase indexing is complete
        // This fetches all requests and filters client-side to work around indexing delays
        Log.d(TAG, "⚠️ Using manual filter (fallback) due to Firebase indexing delay");
        fetchAllAndFilter(organizationId, callback);
    }
    
    private static void fetchAllAndFilter(String organizationId, RequestsCallback callback) {
        FirebaseManager.getVolunteerRequestsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<VolunteerRequest> requests = new ArrayList<>();
                Log.d(TAG, "Fetching all " + snapshot.getChildrenCount() + " requests for manual filtering");
                
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    VolunteerRequest request = snapshotToRequest(requestSnapshot);
                    if (organizationId.equals(request.getOrganizationId())) {
                        requests.add(request);
                        Log.d(TAG, "  ✓ Matched: " + request.getId() + " - " + request.getTitle());
                    }
                }
                
                Log.d(TAG, "✅ Manually filtered " + requests.size() + " requests");
                callback.onSuccess(requests);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Failed to fetch all requests: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
    
    public static void getRequestsByVolunteer(String volunteerId, RequestsCallback callback) {
        Query query = FirebaseManager.getVolunteerRequestsRef()
            .orderByChild("volunteerId")
            .equalTo(volunteerId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<VolunteerRequest> requests = new ArrayList<>();
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    VolunteerRequest request = snapshotToRequest(requestSnapshot);
                    requests.add(request);
                }
                callback.onSuccess(requests);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    
    public static void updateRequestStatus(String requestId, String newStatus, RequestCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        
        FirebaseManager.getVolunteerRequestsRef().child(requestId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ Request status updated: " + requestId);
                callback.onSuccess(requestId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Update status failed", e);
                callback.onError("Failed to update status: " + e.getMessage());
            });
    }
    
    public static void getRequestById(String requestId, SingleRequestCallback callback) {
        FirebaseManager.getVolunteerRequestsRef().child(requestId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        VolunteerRequest request = snapshotToRequest(snapshot);
                        callback.onSuccess(request);
                    } else {
                        callback.onError("Request not found");
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void approveRequest(String requestId, String orgId, RequestCallback callback) {
        // First, get the full request data
        getRequestById(requestId, new SingleRequestCallback() {
            @Override
            public void onSuccess(VolunteerRequest request) {
                // Update status to APPROVED
                updateRequestStatus(requestId, "APPROVED", new RequestCallback() {
                    @Override
                    public void onSuccess(String id) {
                        // Update approved organization ID
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("approvedByOrgId", orgId);
                        FirebaseManager.getVolunteerRequestsRef().child(requestId).updateChildren(updates);
                        
                        // Create a post from this request
                        createPostFromRequest(request, orgId, callback);
                    }
                    
                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                callback.onError("Failed to get request: " + message);
            }
        });
    }
    
    private static void createPostFromRequest(VolunteerRequest request, String orgId, RequestCallback callback) {
        // Create a Post object from the request
        com.example.tounesna.model.Post post = new com.example.tounesna.model.Post();
        post.setOrganizationId(orgId);
        post.setTitle(request.getTitle() != null ? request.getTitle() : "Volunteer Request");
        post.setDescription(request.getDescription() != null ? request.getDescription() : "");
        post.setLocation(request.getLocation() != null ? request.getLocation() : "");
        post.setPriority(request.getPriority() != null ? request.getPriority() : com.example.tounesna.model.Priority.MEDIUM);
        post.setNeeds(request.getNeeds() != null ? request.getNeeds() : new ArrayList<>());
        post.setImageUrl(request.getImageUrl());
        post.setVolunteersNeeded(10); // Default value
        post.setCategory(com.example.tounesna.model.PostCategory.AID); // Default category
        
        // Set dates (use current time for start, 7 days later for end)
        long now = System.currentTimeMillis();
        long oneWeekLater = now + (7 * 24 * 60 * 60 * 1000L);
        post.setStartDate(now);
        post.setEndDate(oneWeekLater);
        
        // Create the post using PostController
        PostController.createPost(post, new PostController.PostCallback() {
            @Override
            public void onSuccess(String postId) {
                Log.d(TAG, "✅ Post created from approved request: " + postId);
                callback.onSuccess(request.getId());
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "❌ Failed to create post from request: " + message);
                callback.onError("Request approved but failed to create post: " + message);
            }
        });
    }
    
    public static void rejectRequest(String requestId, RequestCallback callback) {
        updateRequestStatus(requestId, "REJECTED", callback);
    }
    
    private static void sendRequestNotifications(VolunteerRequest request) {
        // Notification to organization
        Notification orgNotification = new Notification();
        orgNotification.setUserId(request.getOrganizationId());
        orgNotification.setUserType("organization");
        orgNotification.setType("REQUEST_RECEIVED");
        orgNotification.setTitle("New Volunteer Request");
        orgNotification.setMessage("You have received a new volunteer request");
        orgNotification.setRelatedRequestId(request.getId());
        orgNotification.setRelatedVolunteerId(request.getVolunteerId());
        orgNotification.setRelatedPostId(request.getPostId());
        
        NotificationController.createNotification(orgNotification, new NotificationController.NotificationCallback() {
            @Override
            public void onSuccess(String notificationId) {
                Log.d(TAG, "✅ Organization notification sent: " + notificationId);
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "⚠️ Failed to send organization notification: " + message);
            }
        });
        
        // Confirmation notification to volunteer
        Notification volunteerNotification = new Notification();
        volunteerNotification.setUserId(request.getVolunteerId());
        volunteerNotification.setUserType("volunteer");
        volunteerNotification.setType("REQUEST_SENT");
        volunteerNotification.setTitle("Request Sent Successfully");
        volunteerNotification.setMessage("Your volunteer request has been sent to the organization");
        volunteerNotification.setRelatedRequestId(request.getId());
        volunteerNotification.setRelatedOrganizationId(request.getOrganizationId());
        volunteerNotification.setRelatedPostId(request.getPostId());
        
        NotificationController.createNotification(volunteerNotification, new NotificationController.NotificationCallback() {
            @Override
            public void onSuccess(String notificationId) {
                Log.d(TAG, "✅ Volunteer confirmation notification sent: " + notificationId);
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "⚠️ Failed to send volunteer confirmation: " + message);
            }
        });
    }
    
    private static VolunteerRequest snapshotToRequest(DataSnapshot snapshot) {
        VolunteerRequest request = new VolunteerRequest();
        // Handle both String and Long IDs
        Object idObj = snapshot.child("id").getValue();
        request.setId((idObj instanceof String) ? (String) idObj : String.valueOf(idObj));
        
        Object vidObj = snapshot.child("volunteerId").getValue();
        request.setVolunteerId((vidObj instanceof String) ? (String) vidObj : String.valueOf(vidObj));
        
        Object oidObj = snapshot.child("organizationId").getValue();
        request.setOrganizationId((oidObj instanceof String) ? (String) oidObj : String.valueOf(oidObj));
        
        Object pidObj = snapshot.child("postId").getValue();
        request.setPostId((pidObj instanceof String) ? (String) pidObj : String.valueOf(pidObj));
        
        // Read title, description, location
        request.setTitle(snapshot.child("title").getValue(String.class));
        request.setDescription(snapshot.child("description").getValue(String.class));
        request.setLocation(snapshot.child("location").getValue(String.class));
        request.setMessage(snapshot.child("message").getValue(String.class));
        request.setImageUrl(snapshot.child("imageUrl").getValue(String.class));
        request.setStatus(snapshot.child("status").getValue(String.class));
        
        // Read priority
        String priorityStr = snapshot.child("priority").getValue(String.class);
        if (priorityStr != null) {
            try {
                request.setPriority(com.example.tounesna.model.Priority.valueOf(priorityStr));
            } catch (IllegalArgumentException e) {
                request.setPriority(com.example.tounesna.model.Priority.MEDIUM);
            }
        }
        
        // Read needs list
        if (snapshot.child("needs").exists()) {
            List<String> needs = new ArrayList<>();
            for (DataSnapshot needSnapshot : snapshot.child("needs").getChildren()) {
                String need = needSnapshot.getValue(String.class);
                if (need != null) {
                    needs.add(need);
                }
            }
            request.setNeeds(needs);
        }
        
        Long createdAt = snapshot.child("createdAt").getValue(Long.class);
        if (createdAt != null) request.setCreatedAt(createdAt);
        
        return request;
    }
}
