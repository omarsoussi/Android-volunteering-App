package com.example.tounesna.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tounesna.R;
import com.example.tounesna.controller.AuthController;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RegisterActivity - User registration screen
 */
public class RegisterActivity extends AppCompatActivity {
    
    private RadioGroup rgUserType;
    private RadioButton rbVolunteer, rbOrganization;
    private TextInputLayout tilName, tilSurname, tilEmail, tilPhone, tilPassword, tilProfilePictureUrl;
    private TextInputEditText etName, etSurname, etEmail, etPhone, etPassword, etProfilePictureUrl;
    private TextView tvInterestsLabel;
    private View llInterests;
    private CheckBox cbEvents, cbAid, cbEducation, cbEnvironment, cbHealth;
    private ImageView ivProfilePicture;
    private Button btnSelectImage;
    private Button btnRegister;
    private TextView tvLoginLink;
    
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        setupImagePicker();
        initViews();
        setupListeners();
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
        rgUserType = findViewById(R.id.rgUserType);
        rbVolunteer = findViewById(R.id.rbVolunteer);
        rbOrganization = findViewById(R.id.rbOrganization);
        
        tilName = findViewById(R.id.tilName);
        tilSurname = findViewById(R.id.tilSurname);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilProfilePictureUrl = findViewById(R.id.tilProfilePictureUrl);
        
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etProfilePictureUrl = findViewById(R.id.etProfilePictureUrl);
        
        tvInterestsLabel = findViewById(R.id.tvInterestsLabel);
        llInterests = findViewById(R.id.llInterests);
        
        cbEvents = findViewById(R.id.cbEvents);
        cbAid = findViewById(R.id.cbAid);
        cbEducation = findViewById(R.id.cbEducation);
        cbEnvironment = findViewById(R.id.cbEnvironment);
        cbHealth = findViewById(R.id.cbHealth);
        
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }
    
    private void setupListeners() {
        // User type selection
        rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbVolunteer) {
                showVolunteerFields();
            } else {
                showOrganizationFields();
            }
        });
        
        // Image selection button
        btnSelectImage.setOnClickListener(v -> loadImageFromUrl());
        
        // Register button
        btnRegister.setOnClickListener(v -> handleRegister());
        
        // Login link
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void showVolunteerFields() {
        tilSurname.setVisibility(View.VISIBLE);
        tvInterestsLabel.setVisibility(View.VISIBLE);
        llInterests.setVisibility(View.VISIBLE);
        tilName.setHint(getString(R.string.name));
    }
    
    private void showOrganizationFields() {
        tilSurname.setVisibility(View.GONE);
        tvInterestsLabel.setVisibility(View.VISIBLE);
        llInterests.setVisibility(View.VISIBLE);
        tilName.setHint("Organization Name");
    }
    
    private void handleRegister() {
        // Clear previous errors
        tilName.setError(null);
        tilSurname.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        
        // Get values
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isVolunteer = rbVolunteer.isChecked();
        
        // Validate required fields
        boolean isValid = true;
        
        if (name.isEmpty()) {
            tilName.setError(isVolunteer ? "Name is required" : "Organization name is required");
            isValid = false;
        }
        
        if (isVolunteer && surname.isEmpty()) {
            tilSurname.setError("Surname is required");
            isValid = false;
        }
        
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!email.contains("@") || !email.contains(".")) {
            tilEmail.setError("Invalid email format");
            isValid = false;
        }
        
        if (phone.isEmpty()) {
            tilPhone.setError("Phone is required");
            isValid = false;
        } else if (phone.length() != 8) {
            tilPhone.setError("Phone must be 8 digits");
            isValid = false;
        }
        
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }
        
        if (!isValid) {
            return;
        }
        
        // Database is auto-initialized in TounesnaApplication
        
        // Get selected interests
        List<String> interests = getSelectedInterests();
        
        // Disable button to prevent double submission
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");
        
        // Get profile picture URL from input field only
        String profilePictureUrl = etProfilePictureUrl.getText().toString().trim();
        if (profilePictureUrl.isEmpty()) {
            profilePictureUrl = null;
        }
        
        registerUser(isVolunteer, name, surname, email, phone, password, interests, profilePictureUrl);
    }
    
    private void loadImageFromUrl() {
        String url = etProfilePictureUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show hint about valid image URLs
        if (!isValidImageUrl(url)) {
            Toast.makeText(this, "Please use a direct image URL (ending with .jpg, .png, .webp, etc.)\n\nExample: https://i.imgur.com/abc123.jpg", Toast.LENGTH_LONG).show();
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
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Failed to load image. Please check the URL.\n\nTip: Use direct image links like:\n• https://i.imgur.com/abc123.jpg\n• https://picsum.photos/200", Toast.LENGTH_LONG).show());
                    return false;
                }
                
                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Image loaded successfully!", Toast.LENGTH_SHORT).show());
                    return false;
                }
            })
            .into(ivProfilePicture);
    }
    
    private boolean isValidImageUrl(String url) {
        String urlLower = url.toLowerCase().trim();
        return urlLower.startsWith("http://") || urlLower.startsWith("https://");
    }
    
    private void registerUser(boolean isVolunteer, String name, String surname, String email, 
                              String phone, String password, List<String> interests, String profilePictureUrl) {
        // Register with Firebase async callbacks
        String normalizedEmail = email.toLowerCase().trim();
        if (isVolunteer) {
            com.example.tounesna.model.Volunteer volunteer = new com.example.tounesna.model.Volunteer();
            volunteer.setName(name);
            volunteer.setSurname(surname != null ? surname : "");
            volunteer.setEmail(normalizedEmail);
            volunteer.setPhone(phone);
            volunteer.setPassword(password);
            volunteer.setLocation(""); // Will be set in profile edit
            volunteer.setInterests(interests != null ? interests : new ArrayList<>());
            volunteer.setSkills(new ArrayList<>()); // Will be set in profile edit
            List<String> defaultAvailability = new ArrayList<>();
            defaultAvailability.add("Weekends");
            volunteer.setAvailability(defaultAvailability);
            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                volunteer.setProfilePictureUrl(profilePictureUrl);
            }
            
            android.util.Log.d("RegisterActivity", "Registering volunteer: " + email + " with name: " + name);
            
            AuthController.registerVolunteer(volunteer, new AuthController.RegistrationCallback() {
                @Override
                public void onSuccess(String userId) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        btnRegister.setText(R.string.register);
                        
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    });
                }
                
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        btnRegister.setText(R.string.register);
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } else {
            com.example.tounesna.model.Organization organization = new com.example.tounesna.model.Organization();
            organization.setName(name);
            organization.setEmail(normalizedEmail);
            organization.setPhone(phone);
            organization.setPassword(password);
            organization.setDomain("Community Service"); // Default value
            organization.setLocation(""); // Will be set in profile edit
            organization.setWebsite(""); // Will be set in profile edit
            organization.setDescription(""); // Will be set in profile edit
            organization.setTags(interests != null ? interests : new ArrayList<>());
            organization.setMemberCount(0);
            organization.setFoundedYear(2024); // Default
            organization.setFollowersCount(0);
            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                organization.setProfilePictureUrl(profilePictureUrl);
            }
            
            android.util.Log.d("RegisterActivity", "Registering organization: " + email + " with name: " + name);
            
            AuthController.registerOrganization(organization, new AuthController.RegistrationCallback() {
                @Override
                public void onSuccess(String userId) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        btnRegister.setText(R.string.register);
                        
                        String message = "Registration successful! Please wait for admin approval.";
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        navigateToLogin();
                    });
                }
                
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        btnRegister.setText(R.string.register);
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }
    
    private void navigateToLogin() {
        // Navigate to login
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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
