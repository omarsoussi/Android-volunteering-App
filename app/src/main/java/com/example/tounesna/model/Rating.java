package com.example.tounesna.model;


/**
 * Rating Entity - Represents a rating/review given by a volunteer to an organization
 */
public class Rating extends BaseEntity {
    
    private float score; // Firebase uses float for ratings
    
    private String comment;
    
    private boolean anonymous = false;
    
    private Volunteer rater;
    
    private Organization ratedOrg;
    
    private Post relatedPost;
    
    // Transient fields for JDBC
    private String volunteerId;
    
    private String organizationId;
    
    // Constructors
    public Rating() {
    }
    
    public Rating(int stars, Volunteer rater, Organization ratedOrg) {
        this.score = (float) stars;
        this.rater = rater;
        this.ratedOrg = ratedOrg;
    }
    
    // Getters and Setters
    public float getScore() {
        return score;
    }
    
    public void setScore(float score) {
        if (score < 0 || score > 5) {
            throw new IllegalArgumentException("Score must be between 0 and 5");
        }
        this.score = score;
    }
    
    // Legacy compatibility
    public int getStars() {
        return Math.round(score);
    }
    
    public void setStars(int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }
        this.score = (float) stars;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public boolean isAnonymous() {
        return anonymous;
    }
    
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
    
    public Volunteer getRater() {
        return rater;
    }
    
    public void setRater(Volunteer rater) {
        this.rater = rater;
    }
    
    public Organization getRatedOrg() {
        return ratedOrg;
    }
    
    public void setRatedOrg(Organization ratedOrg) {
        this.ratedOrg = ratedOrg;
    }
    
    public Post getRelatedPost() {
        return relatedPost;
    }
    
    public void setRelatedPost(Post relatedPost) {
        this.relatedPost = relatedPost;
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
    
    @Override
    public String toString() {
        return "Rating{" +
                "id=" + getId() +
                ", score=" + score +
                ", anonymous=" + anonymous +
                ", ratedOrg=" + (ratedOrg != null ? ratedOrg.getName() : "null") +
                '}';
    }
}
