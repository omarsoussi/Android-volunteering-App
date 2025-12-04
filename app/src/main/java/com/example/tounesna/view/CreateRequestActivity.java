package com.example.tounesna.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tounesna.R;
import com.example.tounesna.controller.FollowController;
import com.example.tounesna.controller.VolunteerRequestController;
import com.example.tounesna.model.Organization;
import com.example.tounesna.model.Priority;
import com.example.tounesna.util.SessionManager;
import com.example.tounesna.util.TunisianCities;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CreateRequestActivity - Volunteers can create requests to send to organizations
 */
public class CreateRequestActivity extends AppCompatActivity {
    
    private EditText etTitle;
    private EditText etDescription;
    private EditText etImageUrl;
    private Spinner spinnerLocation;
    private Spinner spinnerPriority;
    private MultiAutoCompleteTextView mactvNeeds;
    private TextView tvSelectedOrgs;
    private Button btnSelectOrganizations;
    private Button btnSendRequest;
    private ImageView ivRequestImage;
    private Button btnSelectImage;
    private ProgressBar progressBar;
    
    private SessionManager sessionManager;
    private List<Organization> allOrganizations;
    private List<Organization> followedOrganizations;
    private List<String> selectedOrganizationIds;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);
        
        sessionManager = new SessionManager(this);
        selectedOrganizationIds = new ArrayList<>();
        allOrganizations = new ArrayList<>();
        followedOrganizations = new ArrayList<>();
        
        // Only volunteers can create requests
        if (sessionManager.isOrganization()) {
            Toast.makeText(this, "Only volunteers can create requests", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupImagePicker();
        initViews();
        setupToolbar();
        setupLocationSpinner();
        setupPrioritySpinner();
        setupNeedsAutoComplete();
        setupImageSelection();
        loadFollowedOrganizations();
        setupSubmitButton();
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (ivRequestImage != null) {
                        Glide.with(this)
                            .load(selectedImageUri)
                            .into(ivRequestImage);
                        ivRequestImage.setVisibility(View.VISIBLE);
                    }
                }
            }
        );
    }
    
    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etImageUrl = findViewById(R.id.etImageUrl);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        mactvNeeds = findViewById(R.id.mactvNeeds);
        tvSelectedOrgs = findViewById(R.id.tvSelectedOrgs);
        btnSelectOrganizations = findViewById(R.id.btnSelectOrganizations);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        ivRequestImage = findViewById(R.id.ivRequestImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        progressBar = findViewById(R.id.progressBar);
        
        btnSelectOrganizations.setOnClickListener(v -> showOrganizationSelector());
        btnSelectImage.setOnClickListener(v -> loadImageFromUrl());
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("New Request");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupLocationSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, TunisianCities.getCitiesArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter);
    }
    
    private void setupPrioritySpinner() {
        String[] priorities = {"LOW", "MEDIUM", "HIGH", "VERY_HIGH"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);
        spinnerPriority.setSelection(1); // Default to MEDIUM
    }
    
    private void setupNeedsAutoComplete() {
        String[] needsSuggestions = {
            "Water", "Food", "Clothes", "Medical Supplies", "Volunteers",
            "Transportation", "Shelter", "Blankets", "Medicine", "Money",
            "Books", "Toys", "Tools", "Electronics", "Furniture"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, needsSuggestions);
        mactvNeeds.setAdapter(adapter);
        mactvNeeds.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }
    
    private void setupImageSelection() {
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            });
        }
    }
    
    private void loadFollowedOrganizations() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Get followed organization IDs
        FollowController.getFollowedOrganizations(sessionManager.getUserId(), new FollowController.FollowsCallback() {
            @Override
            public void onSuccess(List<com.example.tounesna.model.Follow> follows) {
                List<String> followedOrgIds = new ArrayList<>();
                for (com.example.tounesna.model.Follow follow : follows) {
                    followedOrgIds.add(follow.getOrganizationId());
                }
                
                // Load all organizations
                com.example.tounesna.controller.SearchController.getAllOrganizations(new com.example.tounesna.controller.SearchController.OrganizationsCallback() {
                    @Override
                    public void onSuccess(List<Organization> organizations) {
                        allOrganizations.clear();
                        allOrganizations.addAll(organizations);
                        followedOrganizations.clear();
                        
                        for (Organization org : allOrganizations) {
                            if (followedOrgIds.contains(org.getId())) {
                                followedOrganizations.add(org);
                            }
                        }
                        
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (allOrganizations.isEmpty()) {
                                Toast.makeText(CreateRequestActivity.this, "No organizations available", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateRequestActivity.this, "Failed to load organizations: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateRequestActivity.this, "Failed to load followed organizations: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showOrganizationSelector() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Organizations");
        
        // Create custom view
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        
        // Add search by location section
        TextView tvLocationFilter = new TextView(this);
        tvLocationFilter.setText("Filter by Location:");
        tvLocationFilter.setTextSize(14);
        tvLocationFilter.setPadding(0, 0, 0, 8);
        layout.addView(tvLocationFilter);
        
        Spinner spinnerFilterLocation = new Spinner(this);
        List<String> locationOptions = new ArrayList<>();
        locationOptions.add("All Locations");
        locationOptions.addAll(TunisianCities.getCities());
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locationOptions);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterLocation.setAdapter(locationAdapter);
        layout.addView(spinnerFilterLocation);
        
        // Followed organizations section
        if (!followedOrganizations.isEmpty()) {
            TextView tvFollowed = new TextView(this);
            tvFollowed.setText("\nFollowed Organizations:");
            tvFollowed.setTextSize(16);
            tvFollowed.setTypeface(null, android.graphics.Typeface.BOLD);
            tvFollowed.setPadding(0, 16, 0, 8);
            layout.addView(tvFollowed);
        }
        
        // Create checkboxes for all organizations
        final List<Organization> displayedOrgs = new ArrayList<>();
        final boolean[] checkedItems = new boolean[allOrganizations.size()];
        
        // Update displayed organizations based on location filter
        spinnerFilterLocation.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                displayedOrgs.clear();
                String selectedLocation = position == 0 ? null : locationOptions.get(position);
                
                // Add followed organizations first
                for (Organization org : followedOrganizations) {
                    if (selectedLocation == null || selectedLocation.equals(org.getLocation())) {
                        displayedOrgs.add(org);
                    }
                }
                
                // Add other organizations
                for (Organization org : allOrganizations) {
                    if (!followedOrganizations.contains(org)) {
                        if (selectedLocation == null || selectedLocation.equals(org.getLocation())) {
                            displayedOrgs.add(org);
                        }
                    }
                }
                
                updateOrgCheckboxes(layout, displayedOrgs, checkedItems);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedOrganizationIds.clear();
            for (int i = 0; i < displayedOrgs.size(); i++) {
                if (checkedItems[i]) {
                    selectedOrganizationIds.add(displayedOrgs.get(i).getId());
                }
            }
            updateSelectedOrgsText();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
        
        // Trigger initial load
        spinnerFilterLocation.setSelection(0);
    }
    
    private void updateOrgCheckboxes(LinearLayout layout, List<Organization> orgs, boolean[] checkedItems) {
        // Remove old checkboxes (keep first 2 views: location label and spinner)
        while (layout.getChildCount() > 2) {
            layout.removeViewAt(2);
        }
        
        for (int i = 0; i < orgs.size(); i++) {
            Organization org = orgs.get(i);
            android.widget.CheckBox checkBox = new android.widget.CheckBox(this);
            checkBox.setText(org.getName() + (org.getLocation() != null ? " (" + org.getLocation() + ")" : ""));
            checkBox.setChecked(checkedItems[i]);
            
            final int index = i;
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkedItems[index] = isChecked);
            
            layout.addView(checkBox);
        }
    }
    
    private void updateSelectedOrgsText() {
        if (selectedOrganizationIds.isEmpty()) {
            tvSelectedOrgs.setText("No organizations selected");
            tvSelectedOrgs.setTextColor(0xFF999999);
        } else {
            List<String> selectedNames = new ArrayList<>();
            for (String id : selectedOrganizationIds) {
                for (Organization org : allOrganizations) {
                    if (org.getId().equals(id)) {
                        selectedNames.add(org.getName());
                        break;
                    }
                }
            }
            tvSelectedOrgs.setText(selectedNames.size() + " organization(s) selected: " + String.join(", ", selectedNames));
            tvSelectedOrgs.setTextColor(0xFF333333);
        }
    }
    
    private void setupSubmitButton() {
        btnSendRequest.setOnClickListener(v -> {
            if (validateForm()) {
                submitRequest();
            }
        });
    }
    
    private boolean validateForm() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }
        
        if (selectedOrganizationIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one organization", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void submitRequest() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = spinnerLocation.getSelectedItem().toString();
        String priorityStr = spinnerPriority.getSelectedItem().toString();
        String needsStr = mactvNeeds.getText().toString().trim();
        
        // Parse priority
        Priority priority = Priority.valueOf(priorityStr);
        
        // Parse needs
        List<String> needs = new ArrayList<>();
        if (!TextUtils.isEmpty(needsStr)) {
            String[] needsArray = needsStr.split(",");
            for (String need : needsArray) {
                needs.add(need.trim());
            }
        }
        
        // Disable button and show progress
        btnSendRequest.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        
        // Create request
        com.example.tounesna.model.VolunteerRequest request = new com.example.tounesna.model.VolunteerRequest();
        request.setVolunteerId(sessionManager.getUserId());
        request.setTitle(title);
        request.setDescription(description);
        request.setLocation(location);
        request.setPriority(priority);
        request.setNeeds(needs);
        request.setOrganizationIds(selectedOrganizationIds);
        
        // Get image URL from input field only
        String imageUrl = etImageUrl.getText().toString().trim();
        if (!imageUrl.isEmpty()) {
            request.setImageUrl(imageUrl);
        }
        
        VolunteerRequestController.createRequest(request, new VolunteerRequestController.RequestCallback() {
            @Override
            public void onSuccess(String requestId) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSendRequest.setEnabled(true);
                    Toast.makeText(CreateRequestActivity.this, "Request sent successfully!", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSendRequest.setEnabled(true);
                    Toast.makeText(CreateRequestActivity.this, "Failed to send request: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void loadImageFromUrl() {
        String url = etImageUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show hint about valid image URLs
        if (!isValidImageUrl(url)) {
            Toast.makeText(this, "Please use a direct image URL\n\nExamples:\n• https://i.imgur.com/abc123.jpg\n• https://picsum.photos/300/200", Toast.LENGTH_LONG).show();
        }
        
        selectedImageUri = Uri.parse(url);
        ivRequestImage.setVisibility(View.VISIBLE);
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                @Override
                public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateRequestActivity.this, "Failed to load image. Please use a direct image URL.", Toast.LENGTH_LONG).show();
                        ivRequestImage.setVisibility(View.GONE);
                    });
                    return false;
                }
                
                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                    runOnUiThread(() -> Toast.makeText(CreateRequestActivity.this, "Image loaded!", Toast.LENGTH_SHORT).show());
                    return false;
                }
            })
            .into(ivRequestImage);
    }
    
    private boolean isValidImageUrl(String url) {
        String urlLower = url.toLowerCase().trim();
        return urlLower.startsWith("http://") || urlLower.startsWith("https://");
    }
}
