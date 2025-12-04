package com.example.tounesna.controller;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.tounesna.model.Post;
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

public class PostController {
    private static final String TAG = "PostController";
    
    public interface PostCallback {
        void onSuccess(String postId);
        void onError(String message);
    }
    
    public interface PostsCallback {
        void onSuccess(List<Post> posts);
        void onError(String message);
    }
    
    public interface SinglePostCallback {
        void onSuccess(Post post);
        void onError(String message);
    }
    
    public static void createPost(Post post, PostCallback callback) {
        String postId = FirebaseManager.generateId(FirebaseManager.PATH_POSTS);
        post.setId(postId);
        post.setCreatedAt(System.currentTimeMillis());
        post.setUpdatedAt(System.currentTimeMillis());
        
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("id", postId);
        postMap.put("organizationId", post.getOrganizationId());
        postMap.put("title", post.getTitle());
        postMap.put("description", post.getDescription());
        postMap.put("imageUrl", post.getImageUrl());
        postMap.put("location", post.getLocation());
        postMap.put("startDate", post.getStartDate());
        postMap.put("endDate", post.getEndDate());
        postMap.put("volunteersNeeded", post.getVolunteersNeeded());
        postMap.put("category", post.getCategory() != null ? post.getCategory().toString() : null);
        postMap.put("priority", post.getPriority() != null ? post.getPriority().toString() : "MEDIUM");
        postMap.put("needs", post.getNeeds());
        postMap.put("createdAt", post.getCreatedAt());
        postMap.put("updatedAt", post.getUpdatedAt());
        
        FirebaseManager.getPostsRef().child(postId).setValue(postMap)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ Post created: " + postId);
                callback.onSuccess(postId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Create post failed", e);
                callback.onError("Failed to create post: " + e.getMessage());
            });
    }
    
    public static void getRecentPosts(int limit, PostsCallback callback) {
        FirebaseManager.getPostsRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Post> posts = new ArrayList<>();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Post post = snapshotToPost(postSnapshot);
                        posts.add(post);
                    }
                    // Sort by createdAt descending (newest first)
                    posts.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                    
                    // Limit to requested number
                    if (posts.size() > limit) {
                        posts = posts.subList(0, limit);
                    }
                    
                    Log.d("PostController", "✅ Loaded " + posts.size() + " recent posts sorted by date (newest first)");
                    callback.onSuccess(posts);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void getPostById(String postId, SinglePostCallback callback) {
        FirebaseManager.getPostsRef().child(postId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Post post = snapshotToPost(snapshot);
                        // Load organization data
                        String orgId = post.getOrganizationId();
                        if (orgId != null) {
                            AuthController.getOrganizationById(orgId, new AuthController.UserDataCallback() {
                                @Override
                                public void onVolunteerLoaded(Volunteer volunteer) {
                                    // Not expected
                                }
                                
                                @Override
                                public void onOrganizationLoaded(Organization org) {
                                    post.setOrganization(org);
                                    callback.onSuccess(post);
                                }
                                
                                @Override
                                public void onError(String message) {
                                    callback.onSuccess(post); // Return post without org data
                                }
                            });
                        } else {
                            callback.onSuccess(post);
                        }
                    } else {
                        callback.onError("Post not found");
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void getPostsByOrganization(String organizationId, PostsCallback callback) {
        Query query = FirebaseManager.getPostsRef()
            .orderByChild("organizationId")
            .equalTo(organizationId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> posts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = snapshotToPost(postSnapshot);
                    posts.add(post);
                }
                callback.onSuccess(posts);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    
    public static void searchPosts(String keyword, PostsCallback callback) {
        FirebaseManager.getPostsRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Post> posts = new ArrayList<>();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Post post = snapshotToPost(postSnapshot);
                        String title = post.getTitle() != null ? post.getTitle().toLowerCase() : "";
                        String description = post.getDescription() != null ? post.getDescription().toLowerCase() : "";
                        String location = post.getLocation() != null ? post.getLocation().toLowerCase() : "";
                        String searchKeyword = keyword.toLowerCase();
                        
                        if (title.contains(searchKeyword) || description.contains(searchKeyword) || location.contains(searchKeyword)) {
                            posts.add(post);
                        }
                    }
                    // Sort by createdAt descending (newest first)
                    posts.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                    callback.onSuccess(posts);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    private static Post snapshotToPost(DataSnapshot snapshot) {
        Post post = new Post();
        // Handle both String and Long IDs from Firebase
        Object idObj = snapshot.child("id").getValue();
        String id = (idObj instanceof String) ? (String) idObj : String.valueOf(idObj);
        post.setId(id);
        
        Object orgIdObj = snapshot.child("organizationId").getValue();
        String orgId = (orgIdObj instanceof String) ? (String) orgIdObj : String.valueOf(orgIdObj);
        post.setOrganizationId(orgId);
        
        post.setTitle(snapshot.child("title").getValue(String.class));
        post.setDescription(snapshot.child("description").getValue(String.class));
        
        // Load and validate image URL (filter out base64 data URLs)
        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
        if (imageUrl != null) {
            Log.d("PostController", "Loading imageUrl from Firebase: " + imageUrl);
            if (imageUrl.startsWith("data:")) {
                Log.w("PostController", "⚠️ Skipping base64 data URL");
            } else if (imageUrl.contains("google.com/search") || imageUrl.contains("bing.com/images/search")) {
                Log.e("PostController", "❌ Invalid search URL detected, skipping: " + imageUrl.substring(0, Math.min(100, imageUrl.length())));
            } else {
                Log.d("PostController", "✅ Valid image URL, setting to post");
                post.setImageUrl(imageUrl);
            }
        } else {
            Log.d("PostController", "No imageUrl in Firebase for this post");
        }
        
        post.setLocation(snapshot.child("location").getValue(String.class));
        
        Long startDate = snapshot.child("startDate").getValue(Long.class);
        if (startDate != null) post.setStartDate(startDate);
        
        Long endDate = snapshot.child("endDate").getValue(Long.class);
        if (endDate != null) post.setEndDate(endDate);
        
        Integer volunteersNeeded = snapshot.child("volunteersNeeded").getValue(Integer.class);
        if (volunteersNeeded != null) post.setVolunteersNeeded(volunteersNeeded);
        
        Long createdAt = snapshot.child("createdAt").getValue(Long.class);
        if (createdAt != null) post.setCreatedAt(createdAt);
        
        Long updatedAt = snapshot.child("updatedAt").getValue(Long.class);
        if (updatedAt != null) post.setUpdatedAt(updatedAt);
        
        return post;
    }
}
