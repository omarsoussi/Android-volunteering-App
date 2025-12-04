package com.example.tounesna.model;

import java.util.ArrayList;
import java.util.List;

/**
 * VolunteerRequest Entity - Represents a request made by a volunteer
 */
public class VolunteerRequest extends BaseEntity {
    
    private String title;
    
    private String description;
    
    private String imageUrl;
    
    private String location;
    
    private Priority priority = Priority.MEDIUM;
    
    private List<String> needs = new ArrayList<>();
    
    private Volunteer volunteer;
    
    private List<String> sentToOrgIds = new ArrayList<>();
    
    // List of organization IDs this request was sent to
    private List<String> organizationIds = new ArrayList<>();
    
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    
    private String approvedByOrgId; // ID of organization that approved
    
    private String createdPostId; // ID of post created when approved
    
    // Transient fields for JDBC
    private String volunteerId;
    
    private String organizationId; // Single org ID for simple requests
    
    private String postId; // Associated post ID
    
    private String message; // Request message
    
    private String volunteerName;
    
    // Constructors
    public VolunteerRequest() {
    }
    
    public VolunteerRequest(String title, String description, Volunteer volunteer) {
        this.title = title;
        this.description = description;
        this.volunteer = volunteer;
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
    
    public Volunteer getVolunteer() {
        return volunteer;
    }
    
    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
    }
    
    public List<String> getSentToOrgIds() {
        return sentToOrgIds;
    }
    
    public void setSentToOrgIds(List<String> sentToOrgIds) {
        this.sentToOrgIds = sentToOrgIds;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
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
    
    public String getPostId() {
        return postId;
    }
    
    public void setPostId(String postId) {
        this.postId = postId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getVolunteerName() {
        return volunteerName;
    }
    
    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }
    
    public List<String> getOrganizationIds() {
        return organizationIds;
    }
    
    public void setOrganizationIds(List<String> organizationIds) {
        this.organizationIds = organizationIds;
    }
    
    public String getApprovedByOrgId() {
        return approvedByOrgId;
    }
    
    public void setApprovedByOrgId(String approvedByOrgId) {
        this.approvedByOrgId = approvedByOrgId;
    }
    
    public String getCreatedPostId() {
        return createdPostId;
    }
    
    public void setCreatedPostId(String createdPostId) {
        this.createdPostId = createdPostId;
    }
    
    @Override
    public String toString() {
        return "VolunteerRequest{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", priority=" + priority +
                ", status='" + status + '\'' +
                '}';
    }
}
