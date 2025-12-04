package com.example.tounesna.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tounesna.R;
import com.example.tounesna.controller.FollowController;
import com.example.tounesna.controller.NotificationController;
import com.example.tounesna.controller.PostController;
import com.example.tounesna.model.Post;
import com.example.tounesna.model.PostCategory;
import com.example.tounesna.util.SessionManager;
import com.example.tounesna.util.TunisianCities;
import com.example.tounesna.view.adapter.PostAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * DashboardActivity - Main dashboard showing recent posts
 */
public class DashboardActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private ChipGroup chipGroupCategories;
    private ChipGroup chipGroupLocations;
    private RecyclerView rvPosts;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabCreatePost;
    private BottomNavigationView bottomNavigation;
    
    private SessionManager sessionManager;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private List<String> selectedCategories;
    private String selectedLocation;
    private List<String> followedOrgIds;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        sessionManager = new SessionManager(this);
        
        // Check if logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        initViews();
        setupToolbar();
        setupCategoryFilters();
        setupLocationFilters();
        setupRecyclerView();
        setupBottomNavigation();
        setupFAB();
        
        loadFollowedOrganizations();
        loadPosts();
        updateNotificationBadge();
    }
    
    private void updateNotificationBadge() {
        NotificationController.getUnreadCount(sessionManager.getUserId(), new NotificationController.UnreadCountCallback() {
            @Override
            public void onSuccess(int unreadCount) {
                runOnUiThread(() -> {
                    if (bottomNavigation != null) {
                        // Get or create badge for notifications
                        com.google.android.material.badge.BadgeDrawable badge = 
                            bottomNavigation.getOrCreateBadge(R.id.nav_notifications);
                        
                        if (unreadCount > 0) {
                            badge.setVisible(true);
                            badge.setNumber(unreadCount);
                            badge.setMaxCharacterCount(3); // Shows "99+" for counts > 999
                            badge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            badge.setVisible(false);
                        }
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                // Ignore errors for badge update
            }
        });
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        chipGroupLocations = findViewById(R.id.chipGroupLocations);
        rvPosts = findViewById(R.id.rvPosts);
        llEmptyState = findViewById(R.id.llEmptyState);
        fabCreatePost = findViewById(R.id.fabCreatePost);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        selectedCategories = new ArrayList<>();
        selectedLocation = null;
        followedOrgIds = new ArrayList<>();
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.volunteering_hub);
        }
    }
    
    private void setupCategoryFilters() {
        // Add "All" chip
        Chip chipAll = new Chip(this);
        chipAll.setText("All");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck all other chips
                for (int i = 1; i < chipGroupCategories.getChildCount(); i++) {
                    ((Chip) chipGroupCategories.getChildAt(i)).setChecked(false);
                }
                selectedCategories.clear();
                loadPosts();
            }
        });
        chipGroupCategories.addView(chipAll);
        
        // Add category chips
        for (PostCategory category : PostCategory.values()) {
            Chip chip = new Chip(this);
            chip.setText(category.toString());
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck "All"
                    ((Chip) chipGroupCategories.getChildAt(0)).setChecked(false);
                    selectedCategories.add(category.toString());
                } else {
                    selectedCategories.remove(category.toString());
                    // If no categories selected, check "All"
                    if (selectedCategories.isEmpty()) {
                        ((Chip) chipGroupCategories.getChildAt(0)).setChecked(true);
                    }
                }
                loadPosts();
            });
            chipGroupCategories.addView(chip);
        }
    }
    
    private void setupLocationFilters() {
        // Add "All Locations" chip
        Chip chipAll = new Chip(this);
        chipAll.setText("All Locations");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck all other chips
                for (int i = 1; i < chipGroupLocations.getChildCount(); i++) {
                    ((Chip) chipGroupLocations.getChildAt(i)).setChecked(false);
                }
                selectedLocation = null;
                loadPosts();
            }
        });
        chipGroupLocations.addView(chipAll);
        
        // Add city chips
        for (String city : TunisianCities.getCities()) {
            Chip chip = new Chip(this);
            chip.setText(city);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck "All Locations"
                    ((Chip) chipGroupLocations.getChildAt(0)).setChecked(false);
                    // Uncheck other location chips
                    for (int i = 1; i < chipGroupLocations.getChildCount(); i++) {
                        Chip c = (Chip) chipGroupLocations.getChildAt(i);
                        if (c != buttonView) {
                            c.setChecked(false);
                        }
                    }
                    selectedLocation = city;
                    loadPosts();
                } else {
                    // If unchecked and it was the selected location
                    if (city.equals(selectedLocation)) {
                        selectedLocation = null;
                        ((Chip) chipGroupLocations.getChildAt(0)).setChecked(true);
                    }
                }
            });
            chipGroupLocations.addView(chip);
        }
    }
    
    private void setupRecyclerView() {
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts, post -> {
            // Navigate to post details
            Intent intent = new Intent(DashboardActivity.this, PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getId());
            startActivity(intent);
        });
        
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(postAdapter);
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_search) {
                Intent intent = new Intent(DashboardActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_notifications) {
                Intent intent = new Intent(DashboardActivity.this, NotificationsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_requests) {
                // Navigate to appropriate requests activity based on user type
                if (sessionManager.isOrganization()) {
                    // Organizations view volunteer requests
                    Intent intent = new Intent(DashboardActivity.this, RequestsActivity.class);
                    startActivity(intent);
                } else {
                    // Volunteers create new requests
                    Intent intent = new Intent(DashboardActivity.this, CreateRequestActivity.class);
                    startActivity(intent);
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            
            return false;
        });
    }
    
    private void setupFAB() {
        // Show FAB only for organizations
        if (sessionManager.isOrganization()) {
            fabCreatePost.setVisibility(View.VISIBLE);
            fabCreatePost.setOnClickListener(v -> {
                // Navigate to CreatePostActivity
                Intent intent = new Intent(DashboardActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        } else {
            fabCreatePost.setVisibility(View.GONE);
        }
    }
    
    private void loadPosts() {
        // Load recent posts from Firebase
        PostController.getRecentPosts(50, new PostController.PostsCallback() {
            @Override
            public void onSuccess(List<Post> fetchedPosts) {
                runOnUiThread(() -> {
                    posts.clear();
                    
                    // Apply filters
                    for (Post post : fetchedPosts) {
                        boolean matchesFilters = true;
                        
                        // Location filter
                        if (selectedLocation != null && !selectedLocation.isEmpty()) {
                            if (post.getLocation() == null || !post.getLocation().equals(selectedLocation)) {
                                matchesFilters = false;
                            }
                        }
                        
                        // Category filter
                        if (!selectedCategories.isEmpty()) {
                            PostCategory postCategory = post.getCategory();
                            if (postCategory == null || !selectedCategories.contains(postCategory.toString())) {
                                matchesFilters = false;
                            }
                        }
                        
                        if (matchesFilters) {
                            posts.add(post);
                        }
                    }
                    
                    postAdapter.updatePosts(posts);
                    
                    // Show/hide empty state
                    if (posts.isEmpty()) {
                        rvPosts.setVisibility(View.GONE);
                        llEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvPosts.setVisibility(View.VISIBLE);
                        llEmptyState.setVisibility(View.GONE);
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(DashboardActivity.this, "Error loading posts: " + message, Toast.LENGTH_SHORT).show();
                    llEmptyState.setVisibility(View.VISIBLE);
                });
            }
        });
    }
    
    private void loadFollowedOrganizations() {
        if (sessionManager.isVolunteer()) {
            String volunteerId = sessionManager.getUserId();
            if (volunteerId != null) {
                FollowController.getFollowedOrganizations(volunteerId, new FollowController.FollowsCallback() {
                    @Override
                    public void onSuccess(List<com.example.tounesna.model.Follow> follows) {
                        followedOrgIds.clear();
                        for (com.example.tounesna.model.Follow follow : follows) {
                            followedOrgIds.add(follow.getOrganizationId());
                        }
                        // Reload posts with updated follow list
                        runOnUiThread(() -> loadPosts());
                    }
                    
                    @Override
                    public void onError(String message) {
                        android.util.Log.e("DashboardActivity", "Error loading follows: " + message);
                        runOnUiThread(() -> loadPosts());
                    }
                });
            }
        }
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back to login
        moveTaskToBack(true);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
        loadFollowedOrganizations();
        loadPosts(); // Refresh posts when returning to dashboard
    }
}
