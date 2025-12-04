package com.example.tounesna.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tounesna.R;

/**
 * Debug Activity - For testing and verifying database operations
 */
public class DebugActivity extends AppCompatActivity {
    
    private TextView tvDatabaseStats;
    private Button btnRefreshStats;
    private Button btnExportDatabase;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        
        tvDatabaseStats = findViewById(R.id.tvDatabaseStats);
        btnRefreshStats = findViewById(R.id.btnRefreshStats);
        btnExportDatabase = findViewById(R.id.btnExportDatabase);
        
        btnRefreshStats.setOnClickListener(v -> refreshStats());
        btnExportDatabase.setOnClickListener(v -> exportDatabase());
        
        // Load stats on start
        refreshStats();
    }
    
    private void refreshStats() {
        tvDatabaseStats.setText("Firebase Debug - Stats not available");
    }
    
    private void exportDatabase() {
        Toast.makeText(this, "Firebase database cannot be exported locally.\nView at: https://console.firebase.google.com/project/tounesna-8021d/database", Toast.LENGTH_LONG).show();
    }
}
