package com.example.tounesna.model;

/**
 * Follow Entity - Represents a volunteer following an organization
 */
public class Follow extends BaseEntity {
    
    private String volunteerId;
    
    private String organizationId;
    
    // Constructors
    public Follow() {
    }
    
    public Follow(String volunteerId, String organizationId) {
        this.volunteerId = volunteerId;
        this.organizationId = organizationId;
    }
    
    // Getters and Setters
    public String getVolunteerId() {
        return volunteerId;
    }
    
    public void setVolunteerId(String volunteerId) {
        this.volunteerId = volunteerId;
    }
    
    public String getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    
    @Override
    public String toString() {
        return "Follow{" +
                "id=" + getId() +
                ", volunteerId=" + volunteerId +
                ", organizationId=" + organizationId +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
