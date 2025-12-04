package com.example.tounesna.controller;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.tounesna.model.Organization;
import com.example.tounesna.model.Post;
import com.example.tounesna.model.Volunteer;
import com.example.tounesna.util.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SearchController {
    private static final String TAG = "SearchController";
    
    public interface OrganizationsCallback {
        void onSuccess(List<Organization> organizations);
        void onError(String message);
    }
    
    public interface VolunteersCallback {
        void onSuccess(List<Volunteer> volunteers);
        void onError(String message);
    }
    
    public static void searchOrganizations(String keyword, OrganizationsCallback callback) {
        FirebaseManager.getOrganizationsRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Organization> organizations = new ArrayList<>();
                    for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                        Organization org = AuthController.snapshotToOrganization(orgSnapshot);
                        
                        String name = org.getName() != null ? org.getName().toLowerCase() : "";
                        String description = org.getDescription() != null ? org.getDescription().toLowerCase() : "";
                        String location = org.getLocation() != null ? org.getLocation().toLowerCase() : "";
                        String tags = org.getTags() != null ? String.join(" ", org.getTags()).toLowerCase() : "";
                        String searchKeyword = keyword.toLowerCase();
                        
                        if (name.contains(searchKeyword) || description.contains(searchKeyword) || 
                            location.contains(searchKeyword) || tags.contains(searchKeyword)) {
                            organizations.add(org);
                        }
                    }
                    callback.onSuccess(organizations);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void searchVolunteers(String keyword, VolunteersCallback callback) {
        FirebaseManager.getVolunteersRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Volunteer> volunteers = new ArrayList<>();
                    for (DataSnapshot volSnapshot : snapshot.getChildren()) {
                        Volunteer volunteer = AuthController.snapshotToVolunteer(volSnapshot);
                        
                        String name = volunteer.getName() != null ? volunteer.getName().toLowerCase() : "";
                        String skills = volunteer.getSkills() != null ? String.join(" ", volunteer.getSkills()).toLowerCase() : "";
                        String searchKeyword = keyword.toLowerCase();
                        
                        if (name.contains(searchKeyword) || skills.contains(searchKeyword)) {
                            volunteers.add(volunteer);
                        }
                    }
                    callback.onSuccess(volunteers);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
    
    public static void getAllOrganizations(OrganizationsCallback callback) {
        FirebaseManager.getOrganizationsRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Organization> organizations = new ArrayList<>();
                    for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                        Organization org = AuthController.snapshotToOrganization(orgSnapshot);
                        // Only return approved organizations
                        if (org.isApproved()) {
                            organizations.add(org);
                        }
                    }
                    callback.onSuccess(organizations);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
}
