package com.example.tounesna.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tounesna.R;
import com.example.tounesna.adapter.NotificationAdapter;
import com.example.tounesna.controller.NotificationController;
import com.example.tounesna.model.Notification;
import com.example.tounesna.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {
    
    private RecyclerView recyclerNotifications;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Button btnMarkAllRead;
    private NotificationAdapter adapter;
    private List<Notification> notifications;
    
    private SessionManager sessionManager;
    private String userId;
    private String userType;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        
        // Get user info from session
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        userType = sessionManager.getUserType();
        
        initViews();
        setupRecyclerView();
        loadNotifications();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifications");
        }
        
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }
    
    private void setupRecyclerView() {
        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(this, notifications, this);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotifications.setAdapter(adapter);
    }
    
    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerNotifications.setVisibility(View.GONE);
        btnMarkAllRead.setVisibility(View.GONE);
        
        NotificationController.getNotificationsForUser(userId, new NotificationController.NotificationsCallback() {
            @Override
            public void onSuccess(List<Notification> loadedNotifications) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (loadedNotifications.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerNotifications.setVisibility(View.GONE);
                        btnMarkAllRead.setVisibility(View.GONE);
                    } else {
                        notifications.clear();
                        notifications.addAll(loadedNotifications);
                        adapter.notifyDataSetChanged();
                        
                        recyclerNotifications.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                        
                        // Show "Mark all as read" button only if there are unread notifications
                        boolean hasUnread = false;
                        for (Notification notification : notifications) {
                            if (!notification.isRead()) {
                                hasUnread = true;
                                break;
                            }
                        }
                        btnMarkAllRead.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(NotificationsActivity.this, "Failed to load notifications: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void markAllAsRead() {
        progressBar.setVisibility(View.VISIBLE);
        
        NotificationController.markAllAsRead(userId, new NotificationController.NotificationCallback() {
            @Override
            public void onSuccess(String notificationId) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(NotificationsActivity.this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
                    loadNotifications(); // Reload to update UI
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(NotificationsActivity.this, "Failed to mark notifications as read", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read
        if (!notification.isRead()) {
            NotificationController.markAsRead(notification.getId(), new NotificationController.NotificationCallback() {
                @Override
                public void onSuccess(String notificationId) {
                    // Successfully marked as read
                }
                
                @Override
                public void onError(String message) {
                    // Silently fail - not critical
                }
            });
        }
        
        // Navigate based on notification type
        Intent intent = null;
        String type = notification.getType();
        
        if ("REQUEST_APPROVED".equals(type) || "REQUEST_REJECTED".equals(type) || "REQUEST_SENT".equals(type)) {
            // Navigate to request details or requests list
            if (notification.getRelatedRequestId() != null) {
                intent = new Intent(this, RequestsActivity.class);
                intent.putExtra("REQUEST_ID", notification.getRelatedRequestId());
            }
        } else if ("FOLLOWED_ORG_POSTED".equals(type) || "FOLLOWED_ORG_POSTED_ORG".equals(type)) {
            // Navigate to post details
            if (notification.getRelatedPostId() != null) {
                intent = new Intent(this, PostDetailActivity.class);
                intent.putExtra("POST_ID", notification.getRelatedPostId());
            }
        } else if ("REQUEST_RECEIVED".equals(type)) {
            // Navigate to requests inbox
            intent = new Intent(this, RequestsActivity.class);
        } else if ("NEW_FOLLOWER".equals(type)) {
            // Navigate to organization profile or followers list
            if (notification.getRelatedVolunteerId() != null) {
                intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("VOLUNTEER_ID", notification.getRelatedVolunteerId());
            }
        } else if ("NEW_RATING".equals(type)) {
            // Navigate to organization profile to see ratings
            if (notification.getRelatedOrganizationId() != null) {
                intent = new Intent(this, OrganizationProfileActivity.class);
                intent.putExtra("ORGANIZATION_ID", notification.getRelatedOrganizationId());
            }
        }
        
        if (intent != null) {
            startActivity(intent);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications(); // Reload when returning to this activity
    }
}
