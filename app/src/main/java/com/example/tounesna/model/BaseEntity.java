package com.example.tounesna.model;

import java.io.Serializable;

/**
 * BaseEntity - Abstract base class for all entities
 * Contains common fields like id, createdAt, updatedAt, and isDeleted
 * All entities must extend this class
 */
public abstract class BaseEntity implements Serializable {
    
    private String id; // Firebase uses String IDs
    
    private Long createdAt; // Timestamp in milliseconds
    
    private Long updatedAt; // Timestamp in milliseconds
    
    private boolean isDeleted = false;
    
    protected void onCreate() {
        long now = System.currentTimeMillis();
        createdAt = now;
        updatedAt = now;
    }
    
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
