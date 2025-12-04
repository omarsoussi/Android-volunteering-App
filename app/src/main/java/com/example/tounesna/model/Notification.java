package com.example.tounesna.model;

/**
 * Notification Entity - Represents a notification for users
 */
public class Notification extends BaseEntity {
    
    public enum NotificationType {
        // Volunteer notifications
        REQUEST_SENT,           // Volunteer sent a request
        REQUEST_APPROVED,       // Request was approved
        REQUEST_REJECTED,       // Request was rejected
        FOLLOWED_ORG_POSTED,    // Organization they follow posted
        
        // Organization notifications
        REQUEST_RECEIVED,       // Received a volunteer request
        NEW_FOLLOWER,          // Someone followed the organization
        NEW_RATING,            // Someone rated the organization
        FOLLOWED_ORG_POSTED_ORG // Organization they follow posted (for orgs following orgs)
    }
    
    private String userId;           // ID of the user who receives the notification
    private String userType;       // "volunteer" or "organization"
    private String type;  // Store as string for Firebase compatibility
    private String title;
    private String message;
    private boolean isRead = false;
    
    // Related entity IDsz
    private String relatedPostId;
    private String relatedRequestId;
    private String relatedOrganizationId;
    private String relatedVolunteerId;
    
    // Constructors
    public Notification() {
    }
    
    public Notification(String userId, String userType, String type, String title, String message) {
        this.userId = userId;
        this.userType = userType;
        this.type = type;
        this.title = title;
        this.message = message;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    // Legacy compatibility with enum
    public void setType(NotificationType typeEnum) {
        this.type = typeEnum != null ? typeEnum.toString() : null;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public String getRelatedPostId() {
        return relatedPostId;
    }
    
    public void setRelatedPostId(String relatedPostId) {
        this.relatedPostId = relatedPostId;
    }
    
    public String getRelatedRequestId() {
        return relatedRequestId;
    }
    
    public void setRelatedRequestId(String relatedRequestId) {
        this.relatedRequestId = relatedRequestId;
    }
    
    public String getRelatedOrganizationId() {
        return relatedOrganizationId;
    }
    
    public void setRelatedOrganizationId(String relatedOrganizationId) {
        this.relatedOrganizationId = relatedOrganizationId;
    }
    
    public String getRelatedVolunteerId() {
        return relatedVolunteerId;
    }
    
    public void setRelatedVolunteerId(String relatedVolunteerId) {
        this.relatedVolunteerId = relatedVolunteerId;
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + getId() +
                ", userId=" + userId +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
