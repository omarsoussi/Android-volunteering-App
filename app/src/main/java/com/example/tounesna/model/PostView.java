package com.example.tounesna.model;

/**
 * PostView - Tracks which users have viewed which posts
 */
public class PostView extends BaseEntity {
    
    private Long postId;
    private Long userId;
    private String userType; // "volunteer" or "organization"
    private long viewedAt;
    
    public PostView() {
    }
    
    public PostView(Long postId, Long userId, String userType) {
        this.postId = postId;
        this.userId = userId;
        this.userType = userType;
        this.viewedAt = System.currentTimeMillis();
    }
    
    public Long getPostId() {
        return postId;
    }
    
    public void setPostId(Long postId) {
        this.postId = postId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public long getViewedAt() {
        return viewedAt;
    }
    
    public void setViewedAt(long viewedAt) {
        this.viewedAt = viewedAt;
    }
}
