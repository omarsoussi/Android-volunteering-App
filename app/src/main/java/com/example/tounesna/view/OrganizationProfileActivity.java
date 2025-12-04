package com.example.tounesna.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tounesna.R;
import com.example.tounesna.controller.FollowController;
import com.example.tounesna.controller.PostController;
import com.example.tounesna.controller.RatingController;
import com.example.tounesna.model.Organization;
import com.example.tounesna.model.Post;
import com.example.tounesna.model.Rating;
import com.example.tounesna.model.Volunteer;
import com.example.tounesna.util.SessionManager;
import com.example.tounesna.view.adapter.PostAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * OrganizationProfileActivity - View organization profile with rating, posts, and contact options
 */
public class OrganizationProfileActivity extends AppCompatActivity {
    
    public static final String EXTRA_ORGANIZATION_ID = "organization_id";
    
    private ImageView ivProfilePicture;
    private TextView tvOrganizationName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvLocation;
    private TextView tvFollowersCount;
    private RatingBar rbOrganizationRating;
    private TextView tvRatingScore;
    private TextView tvRatingCount;
    private Button btnRate;
    private Button btnFollow;
    private Button btnEmail;
    private Button btnSMS;
    private RecyclerView recyclerViewPosts;
    private ProgressBar progressBar;
    private TextView tvNoPosts;
    
    private SessionManager sessionManager;
    private Organization organization;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private String organizationId;
    private boolean isFollowing = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_profile);
        
        sessionManager = new SessionManager(this);
        organizationId = getIntent().getStringExtra(EXTRA_ORGANIZATION_ID);
        
        if (organizationId == null || organizationId.isEmpty()) {
            Toast.makeText(this, "Invalid organization", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupToolbar();
        setupRatingBar();
        setupButtons();
        setupRecyclerView();
        loadOrganization();
        checkFollowStatus();
        loadPosts();
    }
    
    private void initViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvOrganizationName = findViewById(R.id.tvOrganizationName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvLocation = findViewById(R.id.tvLocation);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        rbOrganizationRating = findViewById(R.id.rbOrganizationRating);
        tvRatingScore = findViewById(R.id.tvRatingScore);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        btnRate = findViewById(R.id.btnRate);
        btnFollow = findViewById(R.id.btnFollow);
        btnEmail = findViewById(R.id.btnEmail);
        btnSMS = findViewById(R.id.btnSMS);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        progressBar = findViewById(R.id.progressBar);
        tvNoPosts = findViewById(R.id.tvNoPosts);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Organization Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRatingBar() {
        rbOrganizationRating.setIsIndicator(true); // View only by default
    }
    
    private void setupButtons() {
        btnRate.setOnClickListener(v -> showRatingDialog());
        btnFollow.setOnClickListener(v -> toggleFollow());
        btnEmail.setOnClickListener(v -> sendEmail());
        btnSMS.setOnClickListener(v -> sendSMS());
        
        // Hide follow button if user is an organization
        if (sessionManager.isOrganization()) {
            btnFollow.setVisibility(View.GONE);
        }
    }
    
    private void setupRecyclerView() {
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts, post -> {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getId());
            startActivity(intent);
        });
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPosts.setAdapter(postAdapter);
    }
    
    private void loadOrganization() {
        progressBar.setVisibility(View.VISIBLE);
        
        com.example.tounesna.controller.AuthController.getOrganizationById(organizationId, new com.example.tounesna.controller.AuthController.UserDataCallback() {
            @Override
            public void onVolunteerLoaded(Volunteer volunteer) {
                // Not expected
            }
            
            @Override
            public void onOrganizationLoaded(Organization org) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (org != null) {
                        organization = org;
                        displayOrganization();
                    } else {
                        Toast.makeText(OrganizationProfileActivity.this, 
                            "Organization not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrganizationProfileActivity.this, 
                        "Error loading organization: " + message, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void displayOrganization() {
        // Name
        tvOrganizationName.setText(organization.getName());
        
        // Email
        tvEmail.setText(organization.getEmail());
        
        // Phone
        if (organization.getPhone() != null && !organization.getPhone().isEmpty()) {
            tvPhone.setText(organization.getPhone());
            tvPhone.setVisibility(View.VISIBLE);
            btnSMS.setVisibility(View.VISIBLE);
        } else {
            tvPhone.setVisibility(View.GONE);
            btnSMS.setVisibility(View.GONE);
        }
        
        // Location
        if (organization.getLocation() != null) {
            tvLocation.setText(organization.getLocation());
        }
        
        // Followers count
        int followers = organization.getFollowersCount();
        tvFollowersCount.setText(followers + (followers == 1 ? " Follower" : " Followers"));
        
        // Profile picture
        if (organization.getProfilePictureUrl() != null && !organization.getProfilePictureUrl().isEmpty()) {
            Glide.with(this)
                .load(organization.getProfilePictureUrl())
                .placeholder(R.drawable.ic_organization_placeholder)
                .into(ivProfilePicture);
        }
        
        // Rating
        updateRatingDisplay();
    }
    
    private void updateRatingDisplay() {
        if (organization.getRating() != null) {
            float rating = organization.getRating().floatValue();
            rbOrganizationRating.setRating(rating);
            tvRatingScore.setText(String.format("%.1f", rating));
            
            int count = organization.getRatingCount();
            tvRatingCount.setText("(" + count + " " + (count == 1 ? "vote" : "votes") + ")");
        } else {
            rbOrganizationRating.setRating(0);
            tvRatingScore.setText("0.0");
            tvRatingCount.setText("(0 votes)");
        }
    }
    
    private void loadPosts() {
        PostController.getPostsByOrganization(organizationId, new PostController.PostsCallback() {
            @Override
            public void onSuccess(List<Post> orgPosts) {
                runOnUiThread(() -> {
                    posts.clear();
                    posts.addAll(orgPosts);
                    postAdapter.notifyDataSetChanged();
                    
                    if (posts.isEmpty()) {
                        tvNoPosts.setVisibility(View.VISIBLE);
                        recyclerViewPosts.setVisibility(View.GONE);
                    } else {
                        tvNoPosts.setVisibility(View.GONE);
                        recyclerViewPosts.setVisibility(View.VISIBLE);
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(OrganizationProfileActivity.this, 
                        "Failed to load posts: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showRatingDialog() {
        if (!sessionManager.isVolunteer()) {
            Toast.makeText(this, "Only volunteers can rate organizations", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate " + organization.getName());
        
        // Create dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 40);
        
        // Rating bar for user input
        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setIsIndicator(false);
        ratingBar.setRating(0); // Start at 0
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        ratingBar.setLayoutParams(params);
        dialogLayout.addView(ratingBar);
        
        // Comment field
        TextView tvComment = new TextView(this);
        tvComment.setText("Comment (optional):");
        tvComment.setTextSize(14);
        tvComment.setTextColor(0xFF333333);
        tvComment.setPadding(0, 20, 0, 8);
        dialogLayout.addView(tvComment);
        
        EditText etComment = new EditText(this);
        etComment.setHint("Share your experience...");
        etComment.setMinLines(3);
        etComment.setMaxLines(5);
        dialogLayout.addView(etComment);
        
        builder.setView(dialogLayout);
        
        builder.setPositiveButton("Submit", (dialog, which) -> {
            float rating = ratingBar.getRating();
            if (rating > 0) {
                submitRating(rating, etComment.getText().toString());
            } else {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void submitRating(float ratingValue, String comment) {
        progressBar.setVisibility(View.VISIBLE);
        
        Rating rating = new Rating();
        rating.setVolunteerId(sessionManager.getUserId());
        rating.setOrganizationId(organizationId);
        rating.setScore(ratingValue);
        rating.setComment(comment);
        
        RatingController.addRating(rating, new RatingController.RatingCallback() {
            @Override
            public void onSuccess(String ratingId) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrganizationProfileActivity.this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                    // Reload organization to get updated rating
                    loadOrganization();
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrganizationProfileActivity.this, "Failed to submit rating: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void sendEmail() {
        if (organization.getEmail() == null || organization.getEmail().isEmpty()) {
            Toast.makeText(this, "No email available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{organization.getEmail()});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry from " + sessionManager.getUserName());
        
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email via..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void sendSMS() {
        if (organization.getPhone() == null || organization.getPhone().isEmpty()) {
            Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + organization.getPhone()));
        smsIntent.putExtra("sms_body", "Hi, I'm " + sessionManager.getUserName() + ". ");
        
        try {
            startActivity(smsIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void checkFollowStatus() {
        if (sessionManager.isVolunteer()) {
            FollowController.isFollowing(sessionManager.getUserId(), organizationId, new FollowController.FollowCheckCallback() {
                @Override
                public void onSuccess(boolean following) {
                    runOnUiThread(() -> {
                        isFollowing = following;
                        updateFollowButton();
                    });
                }
                
                @Override
                public void onError(String message) {
                    // Ignore error, assume not following
                    runOnUiThread(() -> {
                        isFollowing = false;
                        updateFollowButton();
                    });
                }
            });
        }
    }
    
    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("Following");
            btnFollow.setBackgroundResource(R.drawable.button_orange);
            btnFollow.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundResource(R.drawable.button_white_border);
            btnFollow.setTextColor(getResources().getColor(R.color.orange));
        }
    }
    
    private void toggleFollow() {
        if (!sessionManager.isVolunteer()) {
            Toast.makeText(this, "Only volunteers can follow organizations", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnFollow.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        
        FollowController.FollowCallback callback = new FollowController.FollowCallback() {
            @Override
            public void onSuccess(String followId) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnFollow.setEnabled(true);
                    isFollowing = !isFollowing;
                    updateFollowButton();
                    Toast.makeText(OrganizationProfileActivity.this, 
                        isFollowing ? "Following organization" : "Unfollowed organization", 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnFollow.setEnabled(true);
                    Toast.makeText(OrganizationProfileActivity.this, 
                        "Failed to update follow status: " + message, 
                        Toast.LENGTH_SHORT).show();
                });
            }
        };
        
        if (isFollowing) {
            FollowController.unfollowOrganization(sessionManager.getUserId(), organizationId, callback);
        } else {
            FollowController.followOrganization(sessionManager.getUserId(), organizationId, callback);
        }
    }
}
