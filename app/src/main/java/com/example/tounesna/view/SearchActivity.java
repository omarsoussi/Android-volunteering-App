package com.example.tounesna.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tounesna.R;
import com.example.tounesna.controller.PostController;
import com.example.tounesna.controller.SearchController;
import com.example.tounesna.model.Organization;
import com.example.tounesna.model.Post;
import com.example.tounesna.model.PostCategory;
import com.example.tounesna.util.TunisianCities;
import com.example.tounesna.view.adapter.OrganizationAdapter;
import com.example.tounesna.view.adapter.PostAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchActivity - Search organizations and posts
 */
public class SearchActivity extends AppCompatActivity {
    
    private SearchView searchView;
    private Spinner spinnerSearchType;
    private Spinner spinnerFilter;
    private Spinner spinnerLocation;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    
    private String searchType = "Posts"; // "Posts" or "Organizations"
    private String currentQuery = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        initViews();
        setupToolbar();
        setupSearchType();
        setupLocationSpinner();
        setupSearchView();
        setupRecyclerView();
        
        // Initial search for all posts
        performSearch();
    }
    
    private void initViews() {
        searchView = findViewById(R.id.searchView);
        spinnerSearchType = findViewById(R.id.spinnerSearchType);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupSearchType() {
        // Search type spinner (Posts or Organizations)
        String[] searchTypes = {"Posts", "Organizations"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, searchTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchType.setAdapter(typeAdapter);
        
        spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchType = searchTypes[position];
                setupFilterSpinner();
                performSearch();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Initial filter setup
        setupFilterSpinner();
    }
    
    private void setupLocationSpinner() {
        List<String> locationOptions = new ArrayList<>();
        locationOptions.add("All Locations");
        locationOptions.addAll(TunisianCities.getCities());
        
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, locationOptions);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);
        
        spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performSearch();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void setupFilterSpinner() {
        if (searchType.equals("Organizations")) {
            // Rating filter for organizations
            String[] ratings = {"All Ratings", "5 Stars", "4+ Stars", "3+ Stars"};
            ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ratings);
            filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFilter.setAdapter(filterAdapter);
        } else {
            // Category filter for posts
            String[] categories = {"All Categories", "EVENT", "AID", "EDUCATION", "ENVIRONMENT", "HEALTH"};
            ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
            filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFilter.setAdapter(filterAdapter);
        }
        
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performSearch();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void setupSearchView() {
        searchView.setQueryHint("Search by name, email, location...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                performSearch();
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                // Perform search on text change for real-time results
                performSearch();
                return true;
            }
        });
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void performSearch() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        
        if (searchType.equals("Organizations")) {
            searchOrganizations();
        } else {
            searchPosts();
        }
    }
    
    private void searchOrganizations() {
        // Get rating filter
        String filterText = spinnerFilter.getSelectedItem().toString();
        Double minRating = null;
        
        switch (filterText) {
            case "5 Stars":
                minRating = 5.0;
                break;
            case "4+ Stars":
                minRating = 4.0;
                break;
            case "3+ Stars":
                minRating = 3.0;
                break;
        }
        
        // Get location filter
        String locationText = spinnerLocation.getSelectedItem().toString();
        String location = locationText.equals("All Locations") ? null : locationText;
        final Double finalMinRating = minRating;
        
        SearchController.searchOrganizations(currentQuery, new SearchController.OrganizationsCallback() {
            @Override
            public void onSuccess(List<Organization> results) {
                // Filter by rating
                List<Organization> filteredResults = new ArrayList<>();
                for (Organization org : results) {
                    boolean matchesRating = finalMinRating == null || 
                        (org.getRating() != null && org.getRating() >= finalMinRating);
                    boolean matchesLocation = location == null || location.equals(org.getLocation());
                    
                    if (matchesRating && matchesLocation) {
                        filteredResults.add(org);
                    }
                }
                
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (filteredResults.isEmpty()) {
                        tvEmptyState.setText("No organizations found");
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        
                        OrganizationAdapter adapter = new OrganizationAdapter(SearchActivity.this, filteredResults, org -> {
                            // Open organization profile
                            Intent intent = new Intent(SearchActivity.this, OrganizationProfileActivity.class);
                            intent.putExtra(OrganizationProfileActivity.EXTRA_ORGANIZATION_ID, org.getId());
                            startActivity(intent);
                        });
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmptyState.setText("Error: " + message);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
            }
        });
    }
    
    private void searchPosts() {
        // Get category filter
        String filterText = spinnerFilter.getSelectedItem().toString();
        PostCategory category = null;
        
        if (!filterText.equals("All Categories")) {
            try {
                category = PostCategory.valueOf(filterText);
            } catch (IllegalArgumentException e) {
                category = null;
            }
        }
        
        // Get location filter
        String locationText = spinnerLocation.getSelectedItem().toString();
        String location = locationText.equals("All Locations") ? null : locationText;
        
        final PostCategory finalCategory = category;
        
        PostController.searchPosts(currentQuery, new PostController.PostsCallback() {
            @Override
            public void onSuccess(List<Post> results) {
                // Filter by category and location
                List<Post> filteredResults = new ArrayList<>();
                for (Post post : results) {
                    boolean matchesCategory = finalCategory == null || 
                        (post.getCategory() != null && finalCategory.toString().equals(post.getCategory()));
                    boolean matchesLocation = location == null || 
                        (post.getLocation() != null && location.equals(post.getLocation()));
                    
                    if (matchesCategory && matchesLocation) {
                        filteredResults.add(post);
                    }
                }
                
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (filteredResults.isEmpty()) {
                        tvEmptyState.setText("No posts found");
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        
                        PostAdapter adapter = new PostAdapter(filteredResults, post -> {
                            Intent intent = new Intent(SearchActivity.this, PostDetailActivity.class);
                            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getId());
                            startActivity(intent);
                        });
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmptyState.setText("Error: " + message);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
            }
        });
    }
}
