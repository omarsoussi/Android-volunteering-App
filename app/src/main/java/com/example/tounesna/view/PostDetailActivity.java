package com.example.tounesna.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tounesna.R;
import com.example.tounesna.controller.PostController;
import com.example.tounesna.controller.RatingController;
import com.example.tounesna.model.Post;
import com.example.tounesna.util.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * PostDetailActivity - Shows detailed information about a post
 */
public class PostDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_POST_ID = "post_id";
    
    private ImageView ivPostImage;
    private TextView tvPostTitle;
    private TextView tvPriorityBadge;
    private ImageView ivOrganizationPic;
    private TextView tvOrganizationName;
    private RatingBar rbOrganizationRating;
    private TextView tvRatingScore;
    private TextView tvRatingCount;
    private TextView tvFollowersCount;
    private MaterialCardView cardOrganizationInfo;
    private TextView tvCategoryBadge;
    private TextView tvDescription;
    private TextView tvLocation;
    private TextView tvDateTime;
    private TextView tvVolunteersNeeded;
    private LinearLayout llNeedsCheckboxes;
    private Button btnDonate;
    private Button btnHelpSMS;
    
    private SessionManager sessionManager;
    private Post post;
    private List<CheckBox> needsCheckBoxes;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        
        sessionManager = new SessionManager(this);
        needsCheckBoxes = new ArrayList<>();
        
        // Get post ID from intent
        String postId = getIntent().getStringExtra(EXTRA_POST_ID);
        
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "Invalid post ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadPost(postId);
    }
    
    private void initViews() {
        ivPostImage = findViewById(R.id.ivPostImage);
        tvPostTitle = findViewById(R.id.tvPostTitle);
        tvPriorityBadge = findViewById(R.id.tvPriorityBadge);
        ivOrganizationPic = findViewById(R.id.ivOrganizationPic);
        tvOrganizationName = findViewById(R.id.tvOrganizationName);
        rbOrganizationRating = findViewById(R.id.rbOrganizationRating);
        tvRatingScore = findViewById(R.id.tvRatingScore);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        cardOrganizationInfo = findViewById(R.id.cardOrganizationInfo);
        tvCategoryBadge = findViewById(R.id.tvCategoryBadge);
        tvDescription = findViewById(R.id.tvDescription);
        tvLocation = findViewById(R.id.tvLocation);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvVolunteersNeeded = findViewById(R.id.tvVolunteersNeeded);
        llNeedsCheckboxes = findViewById(R.id.llNeedsCheckboxes);
        btnDonate = findViewById(R.id.btnDonate);
        btnHelpSMS = findViewById(R.id.btnHelpSMS);
        
        // Setup rating bar click listener
        setupRatingBar();
        
        // Setup organization card click listener
        setupOrganizationCardClick();
    }
    
    private void setupRatingBar() {
        rbOrganizationRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && rating > 0) {
                showRatingDialog(rating);
            }
        });
    }
    
    private void setupOrganizationCardClick() {
        // Only setup click listener if the card exists in layout
        if (cardOrganizationInfo != null) {
            cardOrganizationInfo.setOnClickListener(v -> {
                if (post != null && post.getOrganizationId() != null) {
                    Intent intent = new Intent(this, OrganizationProfileActivity.class);
                    intent.putExtra(OrganizationProfileActivity.EXTRA_ORGANIZATION_ID, post.getOrganizationId());
                    startActivity(intent);
                }
            });
        }
    }
    
    private void loadPost(String postId) {
        PostController.getPostById(postId, new PostController.SinglePostCallback() {
            @Override
            public void onSuccess(Post loadedPost) {
                runOnUiThread(() -> {
                    post = loadedPost;
                    if (post != null) {
                        displayPost();
                    } else {
                        Toast.makeText(PostDetailActivity.this, "Post not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(PostDetailActivity.this, "Error loading post: " + message, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void displayPost() {
        // Title
        tvPostTitle.setText(post.getTitle());
        
        // Priority badge
        if (post.getPriority() != null) {
            tvPriorityBadge.setText(post.getPriority().toString());
            tvPriorityBadge.setVisibility(View.VISIBLE);
            
            switch (post.getPriority()) {
                case VERY_HIGH:
                    tvPriorityBadge.setText("URGENT");
                    tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_very_high);
                    break;
                case HIGH:
                    tvPriorityBadge.setText("HIGH");
                    tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_high);
                    break;
                case MEDIUM:
                    tvPriorityBadge.setText("MEDIUM");
                    tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_medium);
                    tvPriorityBadge.setTextColor(0xFF333333);
                    break;
                case LOW:
                    tvPriorityBadge.setText("LOW");
                    tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_low);
                    break;
            }
        } else {
            tvPriorityBadge.setVisibility(View.GONE);
        }
        
        // Organization
        if (post.getOrganization() != null) {
            tvOrganizationName.setText(post.getOrganization().getName());
            
            // Load organization profile picture (optional - may not exist in layout)
            if (ivOrganizationPic != null) {
                if (post.getOrganization().getProfilePictureUrl() != null && !post.getOrganization().getProfilePictureUrl().isEmpty()) {
                    Glide.with(this)
                        .load(post.getOrganization().getProfilePictureUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(ivOrganizationPic);
                } else {
                    ivOrganizationPic.setImageResource(R.drawable.ic_launcher_foreground);
                }
            }
            
            // Rating
            if (post.getOrganization().getRating() != null && post.getOrganization().getRating() > 0) {
                rbOrganizationRating.setRating(post.getOrganization().getRating().floatValue());
                rbOrganizationRating.setVisibility(View.VISIBLE);
                
                // Rating score (optional - may not exist in layout)
                if (tvRatingScore != null) {
                    tvRatingScore.setText(String.format("%.1f", post.getOrganization().getRating()));
                    tvRatingScore.setVisibility(View.VISIBLE);
                }
                
                if (tvRatingCount != null) {
                    if (post.getOrganization().getRatingCount() > 0) {
                        tvRatingCount.setText("(" + post.getOrganization().getRatingCount() + " ratings)");
                        tvRatingCount.setVisibility(View.VISIBLE);
                    } else {
                        tvRatingCount.setVisibility(View.GONE);
                    }
                }
            } else {
                rbOrganizationRating.setVisibility(View.GONE);
                if (tvRatingScore != null) {
                    tvRatingScore.setVisibility(View.GONE);
                }
                if (tvRatingCount != null) {
                    tvRatingCount.setVisibility(View.GONE);
                }
            }
            
            // Followers count (optional - may not exist in layout)
            if (tvFollowersCount != null) {
                int followersCount = post.getOrganization().getFollowersCount();
                tvFollowersCount.setText(followersCount + (followersCount == 1 ? " follower" : " followers"));
            }
        }
        
        // Category
        if (post.getCategory() != null) {
            tvCategoryBadge.setText(post.getCategory().toString());
            tvCategoryBadge.setVisibility(View.VISIBLE);
        } else {
            tvCategoryBadge.setVisibility(View.GONE);
        }
        
        // Post Image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(post.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(ivPostImage);
        } else {
            ivPostImage.setVisibility(View.GONE);
        }
        
        // Description
        tvDescription.setText(post.getDescription());
        
        // Location
        tvLocation.setText(post.getLocation());
        
        // Date/Time
        if (post.getStartDate() != null) {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            String dateTime = formatter.format(new java.util.Date(post.getStartDate()));
            if (post.getEndDate() != null) {
                dateTime += " - " + formatter.format(new java.util.Date(post.getEndDate()));
            }
            tvDateTime.setText(dateTime);
        } else {
            tvDateTime.setText("Date not specified");
        }
        
        // Volunteers needed
        tvVolunteersNeeded.setText("Volunteers needed: " + post.getVolunteersNeeded());
        
        // Needs with checkboxes
        displayNeeds();
        
        // Setup buttons
        setupButtons();
    }
    
    private void displayNeeds() {
        llNeedsCheckboxes.removeAllViews();
        needsCheckBoxes.clear();
        
        if (post.getNeeds() != null && !post.getNeeds().isEmpty()) {
            for (String need : post.getNeeds()) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(need);
                checkBox.setTextSize(14);
                checkBox.setTextColor(0xFF555555);
                checkBox.setPadding(8, 8, 8, 8);
                
                llNeedsCheckboxes.addView(checkBox);
                needsCheckBoxes.add(checkBox);
            }
        } else {
            TextView noNeeds = new TextView(this);
            noNeeds.setText("No specific needs listed");
            noNeeds.setTextColor(0xFF999999);
            noNeeds.setTextSize(14);
            llNeedsCheckboxes.addView(noNeeds);
        }
    }
    
    private void setupButtons() {
        // Donate button
        btnDonate.setOnClickListener(v -> handleDonate());
        
        // Help via SMS button
        btnHelpSMS.setOnClickListener(v -> handleHelpSMS());
    }
    
    /**
     * Handle Donate button click - Send email with selected needs
     */
    private void handleDonate() {
        if (post.getOrganization() == null || post.getOrganization().getEmail() == null) {
            Toast.makeText(this, "Organization email not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get selected needs
        List<String> selectedNeeds = new ArrayList<>();
        for (CheckBox checkBox : needsCheckBoxes) {
            if (checkBox.isChecked()) {
                selectedNeeds.add(checkBox.getText().toString());
            }
        }
        
        if (selectedNeeds.isEmpty()) {
            Toast.makeText(this, "Please select at least one item to donate", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Fetch volunteer phone number in background
        new Thread(() -> {
            String volunteerPhone = fetchVolunteerPhone();
            
            runOnUiThread(() -> {
                // Build items list
                StringBuilder itemsList = new StringBuilder();
                for (int i = 0; i < selectedNeeds.size(); i++) {
                    itemsList.append(selectedNeeds.get(i));
                    if (i < selectedNeeds.size() - 1) {
                        itemsList.append(", ");
                    }
                }
                
                // Get volunteer information
                String volunteerName = sessionManager.getUserName();
                String volunteerEmail = sessionManager.getUserEmail();
                String volunteerPhoneNumber = volunteerPhone != null ? volunteerPhone : "Not provided";
                
                // Build email subject - includes volunteer name
                String subject = "AID ON POST " + post.getTitle() + " FROM " + volunteerName + " volunteer";
                
                // Build email body - includes volunteer's contact info
                String body = "Hey there, I wanna help with " + itemsList.toString() + 
                             " here is my email " + volunteerEmail + 
                             " and phone number " + volunteerPhoneNumber;
                
                // Create email intent using ACTION_SEND to properly include subject and body
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{post.getOrganization().getEmail()});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email via..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    
    /**
     * Fetch volunteer phone number from database
     */
    private String fetchVolunteerPhone() {
        final String[] phone = new String[1];
        com.example.tounesna.controller.AuthController.getVolunteerById(sessionManager.getUserId(), new com.example.tounesna.controller.AuthController.UserDataCallback() {
            @Override
            public void onVolunteerLoaded(com.example.tounesna.model.Volunteer volunteer) {
                if (volunteer != null) {
                    phone[0] = volunteer.getPhone();
                }
            }
            
            @Override
            public void onOrganizationLoaded(com.example.tounesna.model.Organization org) {
                // Not expected
            }
            
            @Override
            public void onError(String message) {
                // Silently fail
            }
        });
        return phone[0];
    }
    
    /**
     * Handle Help via SMS button click - Send SMS to organization
     */
    private void handleHelpSMS() {
        if (post.getOrganization() == null || post.getOrganization().getPhone() == null) {
            Toast.makeText(this, "Organization phone not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get selected needs
        List<String> selectedNeeds = new ArrayList<>();
        for (CheckBox checkBox : needsCheckBoxes) {
            if (checkBox.isChecked()) {
                selectedNeeds.add(checkBox.getText().toString());
            }
        }
        
        // Build SMS body
        StringBuilder body = new StringBuilder();
        body.append("Hi, I'm ").append(sessionManager.getUserName()).append(". ");
        body.append("I want to help with ");
        
        if (!selectedNeeds.isEmpty()) {
            body.append("the following items: ");
            for (int i = 0; i < selectedNeeds.size(); i++) {
                body.append(selectedNeeds.get(i));
                if (i < selectedNeeds.size() - 1) {
                    body.append(", ");
                }
            }
        } else {
            body.append("your post: ").append(post.getTitle());
        }
        
        body.append(". Please contact me.");
        
        // Create SMS intent
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + post.getOrganization().getPhone()));
        smsIntent.putExtra("sms_body", body.toString());
        
        try {
            startActivity(smsIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show rating dialog
     */
    private void showRatingDialog(float rating) {
        // Only volunteers can rate
        if (sessionManager.isOrganization()) {
            Toast.makeText(this, "Only volunteers can rate organizations", Toast.LENGTH_SHORT).show();
            rbOrganizationRating.setRating(post.getOrganization().getRating() != null ? 
                post.getOrganization().getRating().floatValue() : 0);
            return;
        }
        
        if (post.getOrganization() == null) {
            Toast.makeText(this, "Organization not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate " + post.getOrganization().getName());
        
        // Create custom view
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 40);
        
        // Comment EditText
        final EditText etComment = new EditText(this);
        etComment.setHint("Add a comment (optional)");
        etComment.setLines(3);
        etComment.setMaxLines(5);
        etComment.setBackgroundResource(R.drawable.edittext_background);
        etComment.setPadding(16, 16, 16, 16);
        dialogLayout.addView(etComment);
        
        LinearLayout.LayoutParams etParams = (LinearLayout.LayoutParams) etComment.getLayoutParams();
        etParams.setMargins(0, 0, 0, 20);
        etComment.setLayoutParams(etParams);
        
        // Anonymous CheckBox
        final CheckBox cbAnonymous = new CheckBox(this);
        cbAnonymous.setText("Submit anonymously");
        cbAnonymous.setTextSize(14);
        cbAnonymous.setPadding(8, 8, 8, 8);
        dialogLayout.addView(cbAnonymous);
        
        builder.setView(dialogLayout);
        
        // Submit button
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String comment = etComment.getText().toString().trim();
            boolean isAnonymous = cbAnonymous.isChecked();
            
            submitRating((int) rating, comment, isAnonymous);
        });
        
        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Reset rating bar to previous value
            rbOrganizationRating.setRating(post.getOrganization().getRating() != null ? 
                post.getOrganization().getRating().floatValue() : 0);
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    /**
     * Submit rating to database
     */
    private void submitRating(int stars, String comment, boolean isAnonymous) {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to rate", Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.example.tounesna.model.Rating rating = new com.example.tounesna.model.Rating();
        rating.setVolunteerId(sessionManager.getUserId());
        rating.setOrganizationId(post.getOrganization().getId());
        rating.setRelatedPost(post);
        rating.setScore((float) stars);
        rating.setComment(comment);
        rating.setAnonymous(isAnonymous);
        
        RatingController.addRating(rating, new RatingController.RatingCallback() {
            @Override
            public void onSuccess(String ratingId) {
                runOnUiThread(() -> {
                    Toast.makeText(PostDetailActivity.this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    // Reload post to get updated rating
                    loadPost(post.getId());
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(PostDetailActivity.this, "Failed to submit rating: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
