package com.example.tounesna.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tounesna.R;
import com.example.tounesna.controller.AuthController;
import com.example.tounesna.model.Organization;
import com.example.tounesna.model.Volunteer;
import com.example.tounesna.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity - User login screen
 */
public class LoginActivity extends AppCompatActivity {
    
    private RadioGroup rgUserType;
    private RadioButton rbVolunteer, rbOrganization;
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private TextView tvForgotPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    
    private SessionManager sessionManager;
    private Toast currentToast; // Single toast instance to prevent queuing
    private boolean isLoggingIn = false; // Prevent multiple login attempts
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Database is auto-initialized in TounesnaApplication
        sessionManager = new SessionManager(this);
        
        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard();
            return;
        }
        
        initViews();
        setupListeners();
        
        // Check if coming from registration
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("registered_email")) {
            String email = intent.getStringExtra("registered_email");
            etEmail.setText(email);
        }
    }
    
    private void initViews() {
        rgUserType = findViewById(R.id.rgUserType);
        rbVolunteer = findViewById(R.id.rbVolunteer);
        rbOrganization = findViewById(R.id.rbOrganization);
        
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
    }
    
    private void setupListeners() {
        // Login button
        btnLogin.setOnClickListener(v -> handleLogin());
        
        // Register link
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Forgot password
        tvForgotPassword.setOnClickListener(v -> {
            showToast("Forgot password feature coming soon");
        });
    }
    
    /**
     * Show toast message, cancelling any previous toast to prevent queue overflow
     */
    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }
    
    /**
     * Show long toast message
     */
    private void showLongToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        currentToast.show();
    }
    
    private void handleLogin() {
        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);
        
        // Get values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isVolunteer = rbVolunteer.isChecked();
        
        // Validate
        boolean isValid = true;
        
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        }
        
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        }
        
        if (!isValid) {
            return;
        }
        
        // Prevent multiple login attempts
        if (isLoggingIn) {
            showToast("Login in progress...");
            return;
        }
        
        isLoggingIn = true;
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");
        
        // Login with Firebase async callbacks
        if (isVolunteer) {
            loginVolunteer(email, password);
        } else {
            loginOrganization(email, password);
        }
    }
    
    private void loginVolunteer(String email, String password) {
        AuthController.loginVolunteer(email, password, new AuthController.AuthCallback() {
            @Override
            public void onSuccess(String userId, String userType) {
                // Fetch full volunteer data
                AuthController.getVolunteerById(userId, new AuthController.UserDataCallback() {
                    @Override
                    public void onVolunteerLoaded(com.example.tounesna.model.Volunteer volunteer) {
                        runOnUiThread(() -> {
                            android.util.Log.d("LoginActivity", "Volunteer loaded: " + volunteer.getName() + ", Email: " + volunteer.getEmail());
                            isLoggingIn = false;
                            btnLogin.setEnabled(true);
                            btnLogin.setText(R.string.login);
                            
                            // Save session
                            String name = volunteer.getName() != null ? volunteer.getName() : "User";
                            sessionManager.createVolunteerSession(userId, name, volunteer.getEmail());
                            android.util.Log.d("LoginActivity", "Session created, navigating to dashboard");
                            
                            showToast("Welcome, " + name + "!");
                            navigateToDashboard();
                        });
                    }
                    
                    @Override
                    public void onOrganizationLoaded(com.example.tounesna.model.Organization org) {
                        // Not expected
                    }
                    
                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            android.util.Log.e("LoginActivity", "Error loading volunteer profile: " + message);
                            isLoggingIn = false;
                            btnLogin.setEnabled(true);
                            btnLogin.setText(R.string.login);
                            showLongToast("Error loading profile: " + message);
                        });
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    android.util.Log.e("LoginActivity", "Volunteer login error: " + message);
                    isLoggingIn = false;
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.login);
                    showLongToast(message);
                });
            }
        });
    }
    
    private void loginOrganization(String email, String password) {
        AuthController.loginOrganization(email, password, new AuthController.AuthCallback() {
            @Override
            public void onSuccess(String userId, String userType) {
                // Fetch full organization data
                AuthController.getOrganizationById(userId, new AuthController.UserDataCallback() {
                    @Override
                    public void onVolunteerLoaded(com.example.tounesna.model.Volunteer volunteer) {
                        // Not expected
                    }
                    
                    @Override
                    public void onOrganizationLoaded(com.example.tounesna.model.Organization org) {
                        runOnUiThread(() -> {
                            android.util.Log.d("LoginActivity", "Organization loaded: " + org.getName() + ", Email: " + org.getEmail());
                            isLoggingIn = false;
                            btnLogin.setEnabled(true);
                            btnLogin.setText(R.string.login);
                            
                            // Check if approved
                            if (!org.isApproved()) {
                                android.util.Log.w("LoginActivity", "Organization not approved");
                                showApprovalDialog();
                                return;
                            }
                            
                            // Save session
                            String name = org.getName() != null ? org.getName() : "Organization";
                            sessionManager.createOrganizationSession(userId, name, org.getEmail());
                            android.util.Log.d("LoginActivity", "Session created, navigating to dashboard");
                            
                            showToast("Welcome, " + name + "!");
                            navigateToDashboard();
                        });
                    }
                    
                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            android.util.Log.e("LoginActivity", "Error loading organization profile: " + message);
                            isLoggingIn = false;
                            btnLogin.setEnabled(true);
                            btnLogin.setText(R.string.login);
                            showLongToast("Error loading profile: " + message);
                        });
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    android.util.Log.e("LoginActivity", "Organization login error: " + message);
                    isLoggingIn = false;
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.login);
                    showLongToast(message);
                });
            }
        });
    }
    
    private void showApprovalDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Pending Approval")
            .setMessage("Your organization is under review. Please wait for admin approval.")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
            .show();
    }
    
    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
