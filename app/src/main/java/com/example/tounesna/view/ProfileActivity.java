package com.example.tounesna.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.tounesna.R;
import com.example.tounesna.controller.AuthController;
import com.example.tounesna.model.Organization;
import com.example.tounesna.model.Volunteer;
import com.example.tounesna.util.SessionManager;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * ProfileActivity - Display user profile information
 */
public class ProfileActivity extends AppCompatActivity {
    
    private ImageView ivProfilePicture;
    private Button btnChangeProfilePicture;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfilePhone;
    private TextView tvProfileType;
    private LinearLayout llVolunteerInfo;
    private LinearLayout llOrganizationInfo;
    private TextView tvVolunteerSurname;
    private TextView tvVolunteerInterests;
    private TextView tvVolunteerSkills;
    private TextView tvVolunteerAvailability;
    private RatingBar rbVolunteerRating;
    private TextView tvVolunteerRatingCount;
    private TextView tvOrgDomain;
    private TextView tvOrgLocation;
    private TextView tvOrgWebsite;
    private TextView tvOrgMemberCount;
    private TextView tvOrgFoundedYear;
    private TextView tvOrgApprovalStatus;
    private TextView tvOrgTags;
    private RatingBar rbOrgRating;
    private TextView tvOrgRatingCount;
    private Button btnEditProfile;
    private Button btnLogout;
    private ProgressBar progressBar;
    
    private SessionManager sessionManager;
    private boolean isOrganization;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Volunteer currentVolunteer;
    private Organization currentOrganization;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        sessionManager = new SessionManager(this);
        
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        isOrganization = sessionManager.isOrganization();
        
        setupImagePicker();
        initViews();
        setupToolbar();
        setupButtons();
        loadProfile();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile when returning from EditProfileActivity
        loadProfile();
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(ivProfilePicture);
                    uploadAndUpdateProfilePicture();
                }
            }
        );
    }
    
    private void initViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnChangeProfilePicture = findViewById(R.id.btnChangeProfilePicture);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileType = findViewById(R.id.tvProfileType);
        llVolunteerInfo = findViewById(R.id.llVolunteerInfo);
        llOrganizationInfo = findViewById(R.id.llOrganizationInfo);
        tvVolunteerSurname = findViewById(R.id.tvVolunteerSurname);
        tvVolunteerInterests = findViewById(R.id.tvVolunteerInterests);
        tvVolunteerSkills = findViewById(R.id.tvVolunteerSkills);
        tvVolunteerAvailability = findViewById(R.id.tvVolunteerAvailability);
        rbVolunteerRating = findViewById(R.id.rbVolunteerRating);
        tvVolunteerRatingCount = findViewById(R.id.tvVolunteerRatingCount);
        tvOrgDomain = findViewById(R.id.tvOrgDomain);
        tvOrgLocation = findViewById(R.id.tvOrgLocation);
        tvOrgWebsite = findViewById(R.id.tvOrgWebsite);
        tvOrgMemberCount = findViewById(R.id.tvOrgMemberCount);
        tvOrgFoundedYear = findViewById(R.id.tvOrgFoundedYear);
        tvOrgApprovalStatus = findViewById(R.id.tvOrgApprovalStatus);
        tvOrgTags = findViewById(R.id.tvOrgTags);
        rbOrgRating = findViewById(R.id.rbOrgRating);
        tvOrgRatingCount = findViewById(R.id.tvOrgRatingCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupButtons() {
        btnChangeProfilePicture.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
        
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }
    
    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);
        
        if (isOrganization) {
            com.example.tounesna.controller.AuthController.getOrganizationById(sessionManager.getUserId(), new com.example.tounesna.controller.AuthController.UserDataCallback() {
                @Override
                public void onVolunteerLoaded(Volunteer volunteer) {
                    // Not expected
                }
                
                @Override
                public void onOrganizationLoaded(Organization org) {
                    runOnUiThread(() -> {
                        if (org != null && !org.isDeleted()) {
                            currentOrganization = org;
                            displayOrganizationProfile(org);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
                
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Error loading profile: " + message, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                }
            });
        } else {
            com.example.tounesna.controller.AuthController.getVolunteerById(sessionManager.getUserId(), new com.example.tounesna.controller.AuthController.UserDataCallback() {
                @Override
                public void onVolunteerLoaded(Volunteer volunteer) {
                    runOnUiThread(() -> {
                        if (volunteer != null && !volunteer.isDeleted()) {
                            currentVolunteer = volunteer;
                            displayVolunteerProfile(volunteer);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
                
                @Override
                public void onOrganizationLoaded(Organization org) {
                    // Not expected
                }
                
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Error loading profile: " + message, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                }
            });
        }
    }
    
    private void displayVolunteerProfile(Volunteer volunteer) {
        tvProfileType.setText("Volunteer Profile");
        tvProfileType.setBackgroundColor(0xFF4CAF50);
        
        // Load profile picture
        if (volunteer.getProfilePictureUrl() != null && !volunteer.getProfilePictureUrl().isEmpty()) {
            android.util.Log.d("ProfileActivity", "Loading volunteer profile picture: " + volunteer.getProfilePictureUrl());
            Glide.with(this)
                .load(volunteer.getProfilePictureUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(ivProfilePicture);
        } else {
            android.util.Log.d("ProfileActivity", "No profile picture URL for volunteer");
        }
        
        tvProfileName.setText(volunteer.getName());
        tvProfileEmail.setText(volunteer.getEmail());
        tvProfilePhone.setText(volunteer.getPhone());
        
        llVolunteerInfo.setVisibility(View.VISIBLE);
        llOrganizationInfo.setVisibility(View.GONE);
        
        tvVolunteerSurname.setText(volunteer.getSurname());
        
        // Interests
        if (volunteer.getInterests() != null && !volunteer.getInterests().isEmpty()) {
            tvVolunteerInterests.setText(String.join(", ", volunteer.getInterests()));
        } else {
            tvVolunteerInterests.setText("Not specified");
        }
        
        // Skills
        if (volunteer.getSkills() != null && !volunteer.getSkills().isEmpty()) {
            tvVolunteerSkills.setText(String.join(", ", volunteer.getSkills()));
        } else {
            tvVolunteerSkills.setText("Not specified");
        }
        
        // Availability
        if (volunteer.getAvailability() != null && !volunteer.getAvailability().isEmpty()) {
            tvVolunteerAvailability.setText(String.join(", ", volunteer.getAvailability()));
        } else {
            tvVolunteerAvailability.setText("Not specified");
        }
        
        // Rating
        if (volunteer.getRating() != null && volunteer.getRating() > 0) {
            rbVolunteerRating.setRating(volunteer.getRating().floatValue());
            rbVolunteerRating.setVisibility(View.VISIBLE);
            
            if (volunteer.getRatingCount() > 0) {
                tvVolunteerRatingCount.setText("(" + volunteer.getRatingCount() + " ratings)");
                tvVolunteerRatingCount.setVisibility(View.VISIBLE);
            } else {
                tvVolunteerRatingCount.setVisibility(View.GONE);
            }
        } else {
            rbVolunteerRating.setVisibility(View.GONE);
            tvVolunteerRatingCount.setText("No ratings yet");
            tvVolunteerRatingCount.setVisibility(View.VISIBLE);
        }
    }
    
    private void displayOrganizationProfile(Organization org) {
        tvProfileType.setText("Organization Profile");
        tvProfileType.setBackgroundColor(0xFFFF6600);
        
        // Load profile picture
        if (org.getProfilePictureUrl() != null && !org.getProfilePictureUrl().isEmpty()) {
            android.util.Log.d("ProfileActivity", "Loading organization profile picture: " + org.getProfilePictureUrl());
            Glide.with(this)
                .load(org.getProfilePictureUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(ivProfilePicture);
        } else {
            android.util.Log.d("ProfileActivity", "No profile picture URL for organization");
        }
        
        tvProfileName.setText(org.getName());
        tvProfileEmail.setText(org.getEmail());
        tvProfilePhone.setText(org.getPhone());
        
        llVolunteerInfo.setVisibility(View.GONE);
        llOrganizationInfo.setVisibility(View.VISIBLE);
        
        if (tvOrgDomain != null) {
            tvOrgDomain.setText(org.getDomain() != null ? org.getDomain() : "Not specified");
        }
        if (tvOrgLocation != null) {
            tvOrgLocation.setText(org.getLocation() != null ? org.getLocation() : "Not specified");
        }
        if (tvOrgWebsite != null) {
            tvOrgWebsite.setText(org.getWebsite() != null ? org.getWebsite() : "Not specified");
        }
        
        if (tvOrgMemberCount != null) {
            if (org.getMemberCount() > 0) {
                tvOrgMemberCount.setText(String.valueOf(org.getMemberCount()));
            } else {
                tvOrgMemberCount.setText("Not specified");
            }
        }
        
        if (tvOrgFoundedYear != null) {
            if (org.getFoundedYear() > 0) {
                tvOrgFoundedYear.setText(String.valueOf(org.getFoundedYear()));
            } else {
                tvOrgFoundedYear.setText("Not specified");
            }
        }
        
        // Approval status
        if (tvOrgApprovalStatus != null) {
            if (org.isApproved()) {
                tvOrgApprovalStatus.setText("Approved ✓");
                tvOrgApprovalStatus.setTextColor(0xFF4CAF50);
            } else {
                tvOrgApprovalStatus.setText("Pending Approval");
                tvOrgApprovalStatus.setTextColor(0xFFFF9800);
            }
        }
        
        // Tags
        if (tvOrgTags != null) {
            if (org.getTags() != null && !org.getTags().isEmpty()) {
                tvOrgTags.setText(String.join(", ", org.getTags()));
            } else {
                tvOrgTags.setText("No tags");
            }
        }
        
        // Rating
        if (rbOrgRating != null && org.getRating() != null && org.getRating() > 0) {
            rbOrgRating.setRating(org.getRating().floatValue());
            rbOrgRating.setVisibility(View.VISIBLE);
            
            if (tvOrgRatingCount != null) {
                if (org.getRatingCount() > 0) {
                    tvOrgRatingCount.setText("(" + org.getRatingCount() + " ratings)");
                    tvOrgRatingCount.setVisibility(View.VISIBLE);
                } else {
                    tvOrgRatingCount.setVisibility(View.GONE);
                }
            }
        } else {
            if (rbOrgRating != null) {
                rbOrgRating.setVisibility(View.GONE);
            }
            if (tvOrgRatingCount != null) {
                tvOrgRatingCount.setText("No ratings yet");
                tvOrgRatingCount.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void uploadAndUpdateProfilePicture() {
        if (selectedImageUri == null) {
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnChangeProfilePicture.setEnabled(false);
        
        // Store local URI directly
        String imageUrl = selectedImageUri.toString();
        
        // Update Firebase with local image URI
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("profilePictureUrl", imageUrl);
        
        String path = isOrganization ? "organizations" : "volunteers";
        com.example.tounesna.util.FirebaseManager.getDatabase().getReference()
            .child(path)
            .child(sessionManager.getUserId())
            .updateChildren(updates)
            .addOnCompleteListener(task -> {
                runOnUiThread(() -> {
                    if (task.isSuccessful()) {
                        if (isOrganization) {
                            currentOrganization.setProfilePictureUrl(imageUrl);
                        } else {
                            currentVolunteer.setProfilePictureUrl(imageUrl);
                        }
                        Toast.makeText(ProfileActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnChangeProfilePicture.setEnabled(true);
                        loadProfile(); // Reload to show new image
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(ProfileActivity.this, "Failed to update profile picture: " + error, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnChangeProfilePicture.setEnabled(true);
                    }
                });
            });
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                sessionManager.logout();
                navigateToLogin();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
