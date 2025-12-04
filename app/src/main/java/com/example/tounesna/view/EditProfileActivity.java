package com.example.tounesna.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.tounesna.R;
import com.example.tounesna.model.Organization;
import com.example.tounesna.model.Volunteer;
import com.example.tounesna.util.SessionManager;
import com.example.tounesna.util.TunisianCities;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePicture;
    private Button btnChangeProfilePicture;
    private TextInputLayout tilName, tilSurname, tilEmail, tilPhone, tilProfilePictureUrl;
    private TextInputEditText etName, etSurname, etEmail, etPhone, etProfilePictureUrl;
    private Spinner spinnerLocation;
    private CheckBox cbEvents, cbAid, cbEducation, cbEnvironment, cbHealth;
    private Button btnSaveProfile;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private boolean isOrganization;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Volunteer currentVolunteer;
    private Organization currentOrganization;
    private String currentProfilePictureUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        isOrganization = sessionManager.isOrganization();

        setupImagePicker();
        initViews();
        setupToolbar();
        setupListeners();
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
                }
            }
        );
    }

    private void initViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnChangeProfilePicture = findViewById(R.id.btnChangeProfilePicture);
        tilName = findViewById(R.id.tilName);
        tilSurname = findViewById(R.id.tilSurname);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilProfilePictureUrl = findViewById(R.id.tilProfilePictureUrl);
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etProfilePictureUrl = findViewById(R.id.etProfilePictureUrl);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        cbEvents = findViewById(R.id.cbEvents);
        cbAid = findViewById(R.id.cbAid);
        cbEducation = findViewById(R.id.cbEducation);
        cbEnvironment = findViewById(R.id.cbEnvironment);
        cbHealth = findViewById(R.id.cbHealth);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.progressBar);

        // Setup location spinner
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TunisianCities.getCitiesArray());
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);

        // Show/hide surname for volunteers only
        if (!isOrganization) {
            tilSurname.setVisibility(View.VISIBLE);
            tilName.setHint("Name");
            spinnerLocation.setVisibility(View.GONE);
        } else {
            tilSurname.setVisibility(View.GONE);
            tilName.setHint("Organization Name");
            spinnerLocation.setVisibility(View.VISIBLE);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnChangeProfilePicture.setOnClickListener(v -> loadImageFromUrl());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
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
                            populateOrganizationData(org);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            finish();
                        }
                    });
                }
                
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Error loading profile: " + message, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finish();
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
                            populateVolunteerData(volunteer);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            finish();
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
                        Toast.makeText(EditProfileActivity.this, "Error loading profile: " + message, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finish();
                    });
                }
            });
        }
    }    private void populateVolunteerData(Volunteer volunteer) {
        etName.setText(volunteer.getName());
        etSurname.setText(volunteer.getSurname());
        etEmail.setText(volunteer.getEmail());
        etPhone.setText(volunteer.getPhone());

        // Load profile picture
        if (volunteer.getProfilePictureUrl() != null && !volunteer.getProfilePictureUrl().isEmpty()) {
            currentProfilePictureUrl = volunteer.getProfilePictureUrl();
            etProfilePictureUrl.setText(volunteer.getProfilePictureUrl());
            Glide.with(this)
                .load(volunteer.getProfilePictureUrl())
                .circleCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivProfilePicture);
        }

        // Set interests
        if (volunteer.getInterests() != null) {
            for (String interest : volunteer.getInterests()) {
                switch (interest) {
                    case "Events":
                        cbEvents.setChecked(true);
                        break;
                    case "Aid":
                        cbAid.setChecked(true);
                        break;
                    case "Education":
                        cbEducation.setChecked(true);
                        break;
                    case "Environment":
                        cbEnvironment.setChecked(true);
                        break;
                    case "Health":
                        cbHealth.setChecked(true);
                        break;
                }
            }
        }
    }

    private void populateOrganizationData(Organization org) {
        etName.setText(org.getName());
        etEmail.setText(org.getEmail());
        etPhone.setText(org.getPhone());

        // Set location
        if (org.getLocation() != null) {
            int position = TunisianCities.getCities().indexOf(org.getLocation());
            if (position >= 0) {
                spinnerLocation.setSelection(position);
            }
        }

        // Load profile picture
        if (org.getProfilePictureUrl() != null && !org.getProfilePictureUrl().isEmpty()) {
            currentProfilePictureUrl = org.getProfilePictureUrl();
            etProfilePictureUrl.setText(org.getProfilePictureUrl());
            Glide.with(this)
                .load(org.getProfilePictureUrl())
                .circleCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivProfilePicture);
        }

        // Set tags
        if (org.getTags() != null) {
            for (String tag : org.getTags()) {
                switch (tag) {
                    case "Events":
                        cbEvents.setChecked(true);
                        break;
                    case "Aid":
                        cbAid.setChecked(true);
                        break;
                    case "Education":
                        cbEducation.setChecked(true);
                        break;
                    case "Environment":
                        cbEnvironment.setChecked(true);
                        break;
                    case "Health":
                        cbHealth.setChecked(true);
                        break;
                }
            }
        }
    }

    private void saveProfile() {
        // Clear errors
        tilName.setError(null);
        tilSurname.setError(null);
        tilPhone.setError(null);

        // Get values
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate
        boolean isValid = true;

        if (name.isEmpty()) {
            tilName.setError(isOrganization ? "Organization name is required" : "Name is required");
            isValid = false;
        }

        if (!isOrganization && surname.isEmpty()) {
            tilSurname.setError("Surname is required");
            isValid = false;
        }

        if (phone.isEmpty()) {
            tilPhone.setError("Phone is required");
            isValid = false;
        } else if (phone.length() != 8) {
            tilPhone.setError("Phone must be 8 digits");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Get selected interests/tags
        List<String> interests = getSelectedInterests();

        // Disable button
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");
        progressBar.setVisibility(View.VISIBLE);

        // Get profile picture URL from input field or use current URL
        String profilePictureUrl = etProfilePictureUrl.getText().toString().trim();
        if (profilePictureUrl.isEmpty()) {
            profilePictureUrl = currentProfilePictureUrl;
        }
        
        android.util.Log.d("EditProfile", "Saving profile picture URL: " + profilePictureUrl);
        
        updateProfile(name, surname, phone, interests, profilePictureUrl);
    }
    
    private void loadImageFromUrl() {
        String url = etProfilePictureUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show hint about valid image URLs
        if (!isValidImageUrl(url)) {
            Toast.makeText(this, "Please use a direct image URL\n\nExamples:\n• https://i.imgur.com/abc123.jpg\n• https://picsum.photos/200", Toast.LENGTH_LONG).show();
        }
        
        selectedImageUri = Uri.parse(url);
        Glide.with(this)
            .load(url)
            .circleCrop()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                @Override
                public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                    runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Failed to load image. Use a direct image URL.", Toast.LENGTH_LONG).show());
                    return false;
                }
                
                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                    runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Image loaded!", Toast.LENGTH_SHORT).show());
                    return false;
                }
            })
            .into(ivProfilePicture);
    }
    
    private boolean isValidImageUrl(String url) {
        String urlLower = url.toLowerCase().trim();
        return urlLower.startsWith("http://") || urlLower.startsWith("https://");
    }

    private void updateProfile(String name, String surname, String phone, List<String> interests, String profilePictureUrl) {
        com.google.firebase.database.DatabaseReference ref;
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        
        if (isOrganization) {
            String location = spinnerLocation.getSelectedItem().toString();
            ref = com.example.tounesna.util.FirebaseManager.getDatabase().getReference()
                .child("organizations")
                .child(sessionManager.getUserId());
            
            updates.put("name", name);
            updates.put("phone", phone);
            updates.put("location", location);
            updates.put("tags", interests);
            updates.put("updatedAt", System.currentTimeMillis());
            if (profilePictureUrl != null) {
                android.util.Log.d("EditProfile", "Adding profilePictureUrl to updates (org): " + profilePictureUrl);
                updates.put("profilePictureUrl", profilePictureUrl);
            }
        } else {
            ref = com.example.tounesna.util.FirebaseManager.getDatabase().getReference()
                .child("volunteers")
                .child(sessionManager.getUserId());
            
            updates.put("name", name);
            updates.put("surname", surname);
            updates.put("phone", phone);
            updates.put("interests", interests);
            updates.put("updatedAt", System.currentTimeMillis());
            if (profilePictureUrl != null) {
                android.util.Log.d("EditProfile", "Adding profilePictureUrl to updates (volunteer): " + profilePictureUrl);
                updates.put("profilePictureUrl", profilePictureUrl);
            }
        }
        
        ref.updateChildren(updates).addOnCompleteListener(task -> {
            runOnUiThread(() -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("Save Changes");
                    progressBar.setVisibility(View.GONE);
                    finish();
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + error, Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("Save Changes");
                    progressBar.setVisibility(View.GONE);
                }
            });
        });
    }

    private List<String> getSelectedInterests() {
        List<String> interests = new ArrayList<>();

        if (cbEvents.isChecked()) interests.add("Events");
        if (cbAid.isChecked()) interests.add("Aid");
        if (cbEducation.isChecked()) interests.add("Education");
        if (cbEnvironment.isChecked()) interests.add("Environment");
        if (cbHealth.isChecked()) interests.add("Health");

        return interests;
    }
}
