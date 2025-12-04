package com.example.tounesna.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tounesna.R;
import com.example.tounesna.controller.VolunteerRequestController;
import com.example.tounesna.model.Priority;
import com.example.tounesna.model.VolunteerRequest;
import com.example.tounesna.util.SessionManager;
import com.example.tounesna.util.TunisianCities;
import com.example.tounesna.view.adapter.VolunteerRequestAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * RequestsActivity - Organizations view and manage volunteer requests with advanced filtering
 */
public class RequestsActivity extends AppCompatActivity {
    
    private static final String TAG = "RequestsActivity";
    
    private Spinner spinnerLocation;
    private Spinner spinnerPriority;
    private Spinner spinnerSortOrder;
    private Button btnDateFilter;
    private Button btnClearFilters;
    private ChipGroup chipGroupNeeds;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout tvEmptyState;
    private TextView tvSelectedDates;
    
    private SessionManager sessionManager;
    private VolunteerRequestAdapter adapter;
    private List<VolunteerRequest> allRequests;
    private List<VolunteerRequest> filteredRequests;
    
    // Filter values
    private String selectedLocation = null;
    private Priority selectedPriority = null;
    private List<String> selectedNeeds = new ArrayList<>();
    private String sortOrder = "recent"; // recent, old, priority_high, priority_low
    private Long startDate = null;
    private Long endDate = null;
    private boolean isFirstLoad = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        
        sessionManager = new SessionManager(this);
        
        // Only organizations can view requests
        if (!sessionManager.isOrganization()) {
            Toast.makeText(this, "Only organizations can view requests", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupToolbar();
        
        try {
            setupFilters();
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up filters: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        
        try {
            setupRecyclerView();
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up recycler view: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        
        loadRequests();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Only reload requests after first load (skip initial onCreate + onResume duplicate)
        if (!isFirstLoad) {
            loadRequests();
        }
        isFirstLoad = false;
    }
    
    private void initViews() {
        spinnerLocation = findViewById(R.id.spinnerLocation);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);
        btnDateFilter = findViewById(R.id.btnDateFilter);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        chipGroupNeeds = findViewById(R.id.chipGroupNeeds);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvSelectedDates = findViewById(R.id.tvSelectedDates);
        
        // Check for null views
        if (spinnerLocation == null || spinnerPriority == null || spinnerSortOrder == null ||
            btnDateFilter == null || btnClearFilters == null || chipGroupNeeds == null ||
            recyclerView == null || progressBar == null || tvEmptyState == null || tvSelectedDates == null) {
            Toast.makeText(this, "Error loading layout", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Volunteer Requests");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupFilters() {
        // Location filter
        List<String> locations = new ArrayList<>();
        locations.add("All Locations");
        locations.addAll(TunisianCities.getCities());
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);
        spinnerLocation.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = position == 0 ? null : locations.get(position);
                applyFilters();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Priority filter
        List<String> priorities = new ArrayList<>();
        priorities.add("All Priorities");
        priorities.add("VERY_HIGH");
        priorities.add("HIGH");
        priorities.add("MEDIUM");
        priorities.add("LOW");
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = position == 0 ? null : Priority.valueOf(priorities.get(position));
                applyFilters();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Sort order filter
        List<String> sortOptions = new ArrayList<>();
        sortOptions.add("Recent to Old");
        sortOptions.add("Old to Recent");
        sortOptions.add("Priority High to Low");
        sortOptions.add("Priority Low to High");
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortAdapter);
        spinnerSortOrder.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: sortOrder = "recent"; break;
                    case 1: sortOrder = "old"; break;
                    case 2: sortOrder = "priority_high"; break;
                    case 3: sortOrder = "priority_low"; break;
                }
                applyFilters();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Needs chips
        setupNeedsChips();
        
        // Date filter button
        btnDateFilter.setOnClickListener(v -> showDateRangePicker());
        
        // Clear filters button
        btnClearFilters.setOnClickListener(v -> clearAllFilters());
    }
    
    private void setupNeedsChips() {
        try {
            String[] commonNeeds = {"Food", "Water", "Clothes", "Shelter", "Medical", "Education", "Transport"};
            for (String need : commonNeeds) {
                Chip chip = new Chip(this);
                chip.setText(need);
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedNeeds.add(need);
                    } else {
                        selectedNeeds.remove(need);
                    }
                    applyFilters();
                });
                chipGroupNeeds.addView(chip);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error creating chips: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void setupRecyclerView() {
        allRequests = new ArrayList<>();
        filteredRequests = new ArrayList<>();
        adapter = new VolunteerRequestAdapter(this, filteredRequests, this::showRequestDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        
        String orgId = sessionManager.getUserId();
        Log.d(TAG, "Loading requests for organization: " + orgId);
        
        VolunteerRequestController.getRequestsForOrganization(orgId, new VolunteerRequestController.RequestsCallback() {
            @Override
            public void onSuccess(List<VolunteerRequest> requests) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Received " + requests.size() + " requests");
                    for (VolunteerRequest req : requests) {
                        Log.d(TAG, "  Request ID: " + req.getId() + ", Status: " + req.getStatus() + ", OrgID: " + req.getOrganizationId());
                    }
                    allRequests.clear();
                    allRequests.addAll(requests);
                    progressBar.setVisibility(View.GONE);
                    applyFilters();
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to load requests: " + message);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RequestsActivity.this, "Failed to load requests: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void applyFilters() {
        if (allRequests == null) {
            return;
        }
        
        filteredRequests.clear();
        
        for (VolunteerRequest request : allRequests) {
            // Only show pending requests (hide approved/rejected)
            if (!"PENDING".equals(request.getStatus())) {
                continue;
            }
            
            // Location filter
            if (selectedLocation != null && !selectedLocation.equals(request.getLocation())) {
                continue;
            }
            
            // Priority filter
            if (selectedPriority != null && request.getPriority() != selectedPriority) {
                continue;
            }
            
            // Needs filter
            if (!selectedNeeds.isEmpty()) {
                boolean hasMatchingNeed = false;
                if (request.getNeeds() != null) {
                    for (String need : selectedNeeds) {
                        if (request.getNeeds().contains(need)) {
                            hasMatchingNeed = true;
                            break;
                        }
                    }
                }
                if (!hasMatchingNeed) {
                    continue;
                }
            }
            
            // Date range filter
            if (startDate != null && request.getCreatedAt() < startDate) {
                continue;
            }
            if (endDate != null && request.getCreatedAt() > endDate) {
                continue;
            }
            
            filteredRequests.add(request);
        }
        
        // Apply sorting
        sortRequests();
        
        adapter.notifyDataSetChanged();
        
        if (filteredRequests.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void sortRequests() {
        switch (sortOrder) {
            case "recent":
                Collections.sort(filteredRequests, (r1, r2) -> 
                    Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                break;
            case "old":
                Collections.sort(filteredRequests, (r1, r2) -> 
                    Long.compare(r1.getCreatedAt(), r2.getCreatedAt()));
                break;
            case "priority_high":
                Collections.sort(filteredRequests, (r1, r2) -> 
                    Integer.compare(getPriorityValue(r2.getPriority()), getPriorityValue(r1.getPriority())));
                break;
            case "priority_low":
                Collections.sort(filteredRequests, (r1, r2) -> 
                    Integer.compare(getPriorityValue(r1.getPriority()), getPriorityValue(r2.getPriority())));
                break;
        }
    }
    
    private int getPriorityValue(Priority priority) {
        if (priority == null) return 0;
        switch (priority) {
            case VERY_HIGH: return 4;
            case HIGH: return 3;
            case MEDIUM: return 2;
            case LOW: return 1;
            default: return 0;
        }
    }
    
    private void showDateRangePicker() {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog startDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar startCal = Calendar.getInstance();
            startCal.set(year, month, dayOfMonth, 0, 0, 0);
            startDate = startCal.getTimeInMillis();
            
            // Now pick end date
            DatePickerDialog endDialog = new DatePickerDialog(this, (view2, year2, month2, dayOfMonth2) -> {
                Calendar endCal = Calendar.getInstance();
                endCal.set(year2, month2, dayOfMonth2, 23, 59, 59);
                endDate = endCal.getTimeInMillis();
                
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvSelectedDates.setText("From: " + sdf.format(startDate) + " To: " + sdf.format(endDate));
                tvSelectedDates.setVisibility(View.VISIBLE);
                applyFilters();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            
            endDialog.setTitle("Select End Date");
            endDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        
        startDialog.setTitle("Select Start Date");
        startDialog.show();
    }
    
    private void clearAllFilters() {
        spinnerLocation.setSelection(0);
        spinnerPriority.setSelection(0);
        spinnerSortOrder.setSelection(0);
        selectedLocation = null;
        selectedPriority = null;
        selectedNeeds.clear();
        sortOrder = "recent";
        startDate = null;
        endDate = null;
        tvSelectedDates.setVisibility(View.GONE);
        
        // Uncheck all chips
        for (int i = 0; i < chipGroupNeeds.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupNeeds.getChildAt(i);
            chip.setChecked(false);
        }
        
        applyFilters();
    }
    
    private void showRequestDialog(VolunteerRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request Details");
        
        // Create dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 40);
        
        // Title
        TextView tvTitle = new TextView(this);
        tvTitle.setText("Title: " + request.getTitle());
        tvTitle.setTextSize(16);
        tvTitle.setTextColor(0xFF333333);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        dialogLayout.addView(tvTitle);
        
        addSpace(dialogLayout, 12);
        
        // Volunteer name
        if (request.getVolunteer() != null) {
            TextView tvVolunteer = new TextView(this);
            tvVolunteer.setText("From: " + request.getVolunteer().getName() + " " + 
                              request.getVolunteer().getSurname());
            tvVolunteer.setTextSize(14);
            tvVolunteer.setTextColor(0xFF666666);
            dialogLayout.addView(tvVolunteer);
            
            addSpace(dialogLayout, 8);
        }
        
        // Priority
        TextView tvPriority = new TextView(this);
        tvPriority.setText("Priority: " + request.getPriority());
        tvPriority.setTextSize(14);
        tvPriority.setTextColor(0xFFFF6600);
        tvPriority.setTypeface(null, android.graphics.Typeface.BOLD);
        dialogLayout.addView(tvPriority);
        
        addSpace(dialogLayout, 8);
        
        // Location
        TextView tvLocation = new TextView(this);
        tvLocation.setText("Location: " + request.getLocation());
        tvLocation.setTextSize(14);
        tvLocation.setTextColor(0xFF666666);
        dialogLayout.addView(tvLocation);
        
        addSpace(dialogLayout, 12);
        
        // Description
        TextView tvDescription = new TextView(this);
        tvDescription.setText("Description:\n" + request.getDescription());
        tvDescription.setTextSize(14);
        tvDescription.setTextColor(0xFF555555);
        dialogLayout.addView(tvDescription);
        
        addSpace(dialogLayout, 12);
        
        // Needs
        if (request.getNeeds() != null && !request.getNeeds().isEmpty()) {
            TextView tvNeeds = new TextView(this);
            StringBuilder needsText = new StringBuilder("Needs:\n");
            for (String need : request.getNeeds()) {
                needsText.append("• ").append(need).append("\n");
            }
            tvNeeds.setText(needsText.toString());
            tvNeeds.setTextSize(14);
            tvNeeds.setTextColor(0xFF555555);
            dialogLayout.addView(tvNeeds);
        }
        
        builder.setView(dialogLayout);
        
        // Approve button - creates post automatically from organization account
        builder.setPositiveButton("Approve & Create Post", (dialog, which) -> {
            approveRequest(request);
        });
        
        // Reject button
        builder.setNegativeButton("Reject", (dialog, which) -> {
            rejectRequest(request);
        });
        
        // Cancel button
        builder.setNeutralButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void addSpace(LinearLayout layout, int dpHeight) {
        View space = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            (int) (dpHeight * getResources().getDisplayMetrics().density)
        );
        space.setLayoutParams(params);
        layout.addView(space);
    }
    
    private void approveRequest(VolunteerRequest request) {
        progressBar.setVisibility(View.VISIBLE);
        
        VolunteerRequestController.approveRequest(request.getId(), sessionManager.getUserId(), new VolunteerRequestController.RequestCallback() {
            @Override
            public void onSuccess(String requestId) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RequestsActivity.this, "Request approved! Post created from your organization account.", 
                                 Toast.LENGTH_LONG).show();
                    loadRequests(); // Refresh list
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RequestsActivity.this, "Failed to approve request: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void rejectRequest(VolunteerRequest request) {
        progressBar.setVisibility(View.VISIBLE);
        
        VolunteerRequestController.rejectRequest(request.getId(), new VolunteerRequestController.RequestCallback() {
            @Override
            public void onSuccess(String requestId) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RequestsActivity.this, "Request rejected", Toast.LENGTH_SHORT).show();
                    loadRequests(); // Refresh list
                });
            }
            
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RequestsActivity.this, "Failed to reject request: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
