package com.example.tounesna.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tounesna.R;
import com.example.tounesna.controller.PostController;
import com.example.tounesna.model.Organization;
import com.example.tounesna.model.PostCategory;
import com.example.tounesna.model.Priority;
import com.example.tounesna.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


/**
 * CreatePostActivity - Create new volunteering posts (Organizations only)
 */
public class CreatePostActivity extends AppCompatActivity {
    
    private Spinner spinnerCategory, spinnerPriority, spinnerLocation;
    private TextInputLayout tilTitle, tilDescription, tilVolunteersNeeded, tilImageUrl;
    private TextInputEditText etTitle, etDescription, etVolunteersNeeded, etImageUrl;
    private Button btnSelectStartDate, btnSelectStartTime, btnSelectEndDate, btnSelectEndTime;
    private TextView tvSelectedStartDateTime, tvSelectedEndDateTime;
    private MultiAutoCompleteTextView mactvNeeds;
    private Button btnPost;
    private ImageView ivPostImage;
    private Button btnSelectImage;
    private ProgressBar progressBar;
    
    private SessionManager sessionManager;
    
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        
        sessionManager = new SessionManager(this);
        
        // Check if user is organization
        if (!sessionManager.isOrganization()) {
            Toast.makeText(this, "Only organizations can create posts", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupImagePicker();
        initViews();
        setupSpinners();
        setupDateTimePickers();
        setupNeeds();
        setupImageSelection();
        setupPostButton();
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (ivPostImage != null) {
                        Glide.with(this)
                            .load(selectedImageUri)
                            .into(ivPostImage);
                        ivPostImage.setVisibility(View.VISIBLE);
                    }
                }
            }
        );
    }
    
    private void initViews() {
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        
        tilTitle = findViewById(R.id.tilTitle);
        tilDescription = findViewById(R.id.tilDescription);
        tilVolunteersNeeded = findViewById(R.id.tilVolunteersNeeded);
        tilImageUrl = findViewById(R.id.tilImageUrl);
        
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etVolunteersNeeded = findViewById(R.id.etVolunteersNeeded);
        etImageUrl = findViewById(R.id.etImageUrl);
        
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectStartTime = findViewById(R.id.btnSelectStartTime);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnSelectEndTime = findViewById(R.id.btnSelectEndTime);
        
        tvSelectedStartDateTime = findViewById(R.id.tvSelectedStartDateTime);
        tvSelectedEndDateTime = findViewById(R.id.tvSelectedEndDateTime);
        
        mactvNeeds = findViewById(R.id.mactvNeeds);
        btnPost = findViewById(R.id.btnPost);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupSpinners() {
        // Category Spinner
        String[] categories = {"EVENT", "AID", "EDUCATION", "ENVIRONMENT", "HEALTH"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        
        // Priority Spinner
        String[] priorities = {"LOW", "MEDIUM", "HIGH", "VERY_HIGH"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1); // Default to MEDIUM
        
        // Location Spinner
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, 
            com.example.tounesna.util.TunisianCities.getCitiesArray());
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);
    }
    
    private void setupDateTimePickers() {
        // Start Date
        btnSelectStartDate.setOnClickListener(v -> showDatePicker(true, true));
        
        // Start Time
        btnSelectStartTime.setOnClickListener(v -> showTimePicker(true, true));
        
        // End Date
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(false, true));
        
        // End Time
        btnSelectEndTime.setOnClickListener(v -> showTimePicker(false, true));
    }
    
    private void showDatePicker(boolean isStart, boolean isDate) {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                if (isStart) {
                    if (startDateTime == null) {
                        startDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0);
                    } else {
                        startDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, 
                            startDateTime.getHour(), startDateTime.getMinute());
                    }
                    updateDateTimeDisplay(true);
                } else {
                    if (endDateTime == null) {
                        endDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0);
                    } else {
                        endDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, 
                            endDateTime.getHour(), endDateTime.getMinute());
                    }
                    updateDateTimeDisplay(false);
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    
    private void showTimePicker(boolean isStart, boolean isTime) {
        Calendar calendar = Calendar.getInstance();
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                if (isStart) {
                    if (startDateTime == null) {
                        LocalDateTime now = LocalDateTime.now();
                        startDateTime = LocalDateTime.of(now.getYear(), now.getMonth(), 
                            now.getDayOfMonth(), hourOfDay, minute);
                    } else {
                        startDateTime = LocalDateTime.of(startDateTime.getYear(), 
                            startDateTime.getMonth(), startDateTime.getDayOfMonth(), 
                            hourOfDay, minute);
                    }
                    updateDateTimeDisplay(true);
                } else {
                    if (endDateTime == null) {
                        LocalDateTime now = LocalDateTime.now();
                        endDateTime = LocalDateTime.of(now.getYear(), now.getMonth(), 
                            now.getDayOfMonth(), hourOfDay, minute);
                    } else {
                        endDateTime = LocalDateTime.of(endDateTime.getYear(), 
                            endDateTime.getMonth(), endDateTime.getDayOfMonth(), 
                            hourOfDay, minute);
                    }
                    updateDateTimeDisplay(false);
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        
        timePickerDialog.show();
    }
    
    private void updateDateTimeDisplay(boolean isStart) {
        LocalDateTime dateTime = isStart ? startDateTime : endDateTime;
        TextView textView = isStart ? tvSelectedStartDateTime : tvSelectedEndDateTime;
        
        if (dateTime != null) {
            String formatted = String.format("%04d-%02d-%02d %02d:%02d",
                dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                dateTime.getHour(), dateTime.getMinute());
            textView.setText(formatted);
            textView.setTextColor(0xFF333333);
        }
    }
    
    private void setupNeeds() {
        // Suggestions for needs
        String[] needsSuggestions = {
            "Water", "Food", "Clothes", "Medicine", "Books", "Toys",
            "Volunteers", "Money", "Transportation", "Equipment"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, needsSuggestions);
        
        mactvNeeds.setAdapter(adapter);
        mactvNeeds.setThreshold(1);
        mactvNeeds.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }
    
    private void setupImageSelection() {
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> loadImageFromUrl());
        }
    }
    
    private void loadImageFromUrl() {
        String url = etImageUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show hint about valid image URLs
        if (!isValidImageUrl(url)) {
            Toast.makeText(this, "Please use a direct image URL\n\nExamples:\n• https://i.imgur.com/abc123.jpg\n• https://picsum.photos/400/300", Toast.LENGTH_LONG).show();
        }
        
        selectedImageUri = Uri.parse(url);
        ivPostImage.setVisibility(View.VISIBLE);
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                @Override
                public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreatePostActivity.this, "❌ Failed to load image\n\nMake sure you're using a direct image link that ends with .jpg, .png, etc.", Toast.LENGTH_LONG).show();
                        ivPostImage.setVisibility(View.GONE);
                    });
                    return false;
                }
                
                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                    runOnUiThread(() -> Toast.makeText(CreatePostActivity.this, "✓ Image loaded!", Toast.LENGTH_SHORT).show());
                    return false;
                }
            })
            .into(ivPostImage);
    }
    
    private boolean isValidImageUrl(String url) {
        String urlLower = url.toLowerCase().trim();
        return urlLower.startsWith("http://") || urlLower.startsWith("https://");
    }
    
    private void setupPostButton() {
        btnPost.setOnClickListener(v -> handleCreatePost());
    }
    
    private void handleCreatePost() {
        // Clear errors
        tilTitle.setError(null);
        tilDescription.setError(null);
        tilVolunteersNeeded.setError(null);
        
        // Get values
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = spinnerLocation.getSelectedItem().toString();
        String volunteersNeededStr = etVolunteersNeeded.getText().toString().trim();
        String categoryStr = spinnerCategory.getSelectedItem().toString();
        String priorityStr = spinnerPriority.getSelectedItem().toString();
        String needsStr = mactvNeeds.getText().toString().trim();
        
        // Validate
        boolean isValid = true;
        
        if (title.isEmpty()) {
            tilTitle.setError("Title is required");
            isValid = false;
        }
        
        if (description.isEmpty()) {
            tilDescription.setError("Description is required");
            isValid = false;
        }
        
        int volunteersNeededTemp = 0;
        if (!volunteersNeededStr.isEmpty()) {
            try {
                volunteersNeededTemp = Integer.parseInt(volunteersNeededStr);
            } catch (NumberFormatException e) {
                tilVolunteersNeeded.setError("Invalid number");
                isValid = false;
            }
        }
        final int volunteersNeeded = volunteersNeededTemp;
        
        if (!isValid) {
            return;
        }
        
        // Parse category and priority
        PostCategory category = PostCategory.valueOf(categoryStr);
        Priority priority = Priority.valueOf(priorityStr);
        
        // Parse needs
        List<String> needs = new ArrayList<>();
        if (!needsStr.isEmpty()) {
            String[] needsArray = needsStr.split(",");
            for (String need : needsArray) {
                String trimmed = need.trim();
                if (!trimmed.isEmpty()) {
                    needs.add(trimmed);
                }
            }
        }
        
        // Disable button
        btnPost.setEnabled(false);
        btnPost.setText("Posting...");
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Get image URL from input field only
        String imageUrl = etImageUrl.getText().toString().trim();
        if (!imageUrl.isEmpty()) {
            // Reject Google search URLs and other search engines
            if (imageUrl.contains("google.com/search") || imageUrl.contains("bing.com/images/search") || 
                imageUrl.contains("yahoo.com/search")) {
                Toast.makeText(this, "❌ Please use a direct image URL, not a search results page.\n\nTip: Right-click on an image and select 'Copy Image Address'", Toast.LENGTH_LONG).show();
                btnPost.setEnabled(true);
                btnPost.setText("Post");
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                return;
            }
            Log.d("CreatePostActivity", "✅ Saving image URL: " + imageUrl);
        } else {
            imageUrl = null;
            Log.d("CreatePostActivity", "No image URL provided");
        }
        
        createPost(title, description, location, category, priority, volunteersNeeded, needs, imageUrl);
    }
    
    private void createPost(String title, String description, String location, 
                           PostCategory category, Priority priority, 
                           int volunteersNeeded, List<String> needs, String imageUrl) {
        com.example.tounesna.model.Post post = new com.example.tounesna.model.Post();
        post.setOrganizationId(sessionManager.getUserId());
        post.setTitle(title);
        post.setDescription(description);
        post.setLocation(location);
        post.setCategory(category);
        post.setPriority(priority);
        post.setImageUrl(imageUrl);
        
        // Convert LocalDateTime to timestamp (milliseconds)
        if (startDateTime != null) {
            post.setStartDate(convertToTimestamp(startDateTime));
        }
        if (endDateTime != null) {
            post.setEndDate(convertToTimestamp(endDateTime));
        }
        
        post.setVolunteersNeeded(volunteersNeeded);
        post.setNeeds(needs);
        
        PostController.createPost(post, new PostController.PostCallback() {
            @Override
            public void onSuccess(String postId) {
                runOnUiThread(() -> {
                    btnPost.setEnabled(true);
                    btnPost.setText(R.string.post);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreatePostActivity.this, "Posted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnPost.setEnabled(true);
                    btnPost.setText(R.string.post);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreatePostActivity.this, "Failed to create post: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    // Helper method to convert LocalDateTime to timestamp
    private long convertToTimestamp(LocalDateTime dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateTime.getYear(), dateTime.getMonthValue() - 1, dateTime.getDayOfMonth(),
                dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
        return calendar.getTimeInMillis();
    }
}
