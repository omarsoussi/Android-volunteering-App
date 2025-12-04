package com.example.tounesna.model;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * Organization Entity - Represents an organization in the system
 */
public class Organization extends BaseEntity {
    
    private String name;
    
    private String domain;
    
    private String location;
    
    private String website;
    
    private String email;
    
    private String phone;
    
    private String password;
    
    private String profilePictureUrl;
    
    private String description;
    
    private String registrationNumber;
    
    private int memberCount = 0;
    
    private int foundedYear;
    
    private boolean isApproved = false;
    
    private Double rating = 0.0;
    
    private int ratingCount = 0;
    
    private int followersCount = 0;
    
    private List<String> tags = new ArrayList<>();
    
    // Constructors
    public Organization() {
    }
    
    public Organization(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    
    public int getMemberCount() {
        return memberCount;
    }
    
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
    
    public int getFoundedYear() {
        return foundedYear;
    }
    
    public void setFoundedYear(int foundedYear) {
        this.foundedYear = foundedYear;
    }
    
    @PropertyName("isApproved")
    public boolean isApproved() {
        return isApproved;
    }
    
    @PropertyName("isApproved")
    public void setApproved(boolean approved) {
        isApproved = approved;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public int getRatingCount() {
        return ratingCount;
    }
    
    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }
    
    public int getFollowersCount() {
        return followersCount;
    }
    
    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    
    @Override
    public String toString() {
        return "Organization{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", domain='" + domain + '\'' +
                ", email='" + email + '\'' +
                ", isApproved=" + isApproved +
                ", rating=" + rating +
                '}';
    }
}
