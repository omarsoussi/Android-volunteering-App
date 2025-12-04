package com.example.tounesna.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Post Entity - Represents a volunteering post/opportunity
 */
public class Post extends BaseEntity {
    
    private String title;
    
    private String description;
    
    private String imageUrl;
    
    private String location;
    
    private Long startDate; // Timestamp in milliseconds
    
    private Long endDate; // Timestamp in milliseconds
    
    private int volunteersNeeded;
    
    private PostCategory category;
    
    private Priority priority = Priority.MEDIUM;
    
    private List<String> needs = new ArrayList<>();
    
    private Organization organization;
    
    // Transient fields for JDBC (not JPA mapped)
    private String organizationId;
    
    private String organizationName;
    
    // Constructors
    public Post() {
    }
    
    public Post(String title, String description, Organization organization) {
        this.title = title;
        this.description = description;
        this.organization = organization;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Long getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }
    
    public Long getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }
    
    public int getVolunteersNeeded() {
        return volunteersNeeded;
    }
    
    public void setVolunteersNeeded(int volunteersNeeded) {
        this.volunteersNeeded = volunteersNeeded;
    }
    
    public PostCategory getCategory() {
        return category;
    }
    
    public void setCategory(PostCategory category) {
        this.category = category;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public List<String> getNeeds() {
        return needs;
    }
    
    public void setNeeds(List<String> needs) {
        this.needs = needs;
    }
    
    public Organization getOrganization() {
        return organization;
    }
    
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
    
    public String getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    
    public String getOrganizationName() {
        return organizationName;
    }
    
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    
    @Override
    public String toString() {
        return "Post{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", category=" + category +
                ", priority=" + priority +
                ", volunteersNeeded=" + volunteersNeeded +
                '}';
    }
}
