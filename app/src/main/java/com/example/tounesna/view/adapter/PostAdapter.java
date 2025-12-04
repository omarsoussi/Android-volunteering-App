package com.example.tounesna.view.adapter;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tounesna.R;
import com.example.tounesna.model.Post;
import com.example.tounesna.model.Priority;

import java.util.List;

/**
 * PostAdapter - RecyclerView adapter for displaying posts
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    
    private List<Post> posts;
    private OnPostClickListener listener;
    
    public interface OnPostClickListener {
        void onPostClick(Post post);
    }
    
    public PostAdapter(List<Post> posts, OnPostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, listener);
    }
    
    @Override
    public int getItemCount() {
        return posts.size();
    }
    
    public void updatePosts(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }
    
    static class PostViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView ivPostImage;
        private TextView tvPostTitle;
        private TextView tvPriorityBadge;
        private TextView tvOrganizationName;
        private RatingBar rbOrganizationRating;
        private TextView tvRatingCount;
        private TextView tvPostDescription;
        private TextView tvPostLocation;
        private TextView tvPostDate;
        private TextView tvCategoryBadge;
        
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvPriorityBadge = itemView.findViewById(R.id.tvPriorityBadge);
            tvOrganizationName = itemView.findViewById(R.id.tvOrganizationName);
            rbOrganizationRating = itemView.findViewById(R.id.rbOrganizationRating);
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount);
            tvPostDescription = itemView.findViewById(R.id.tvPostDescription);
            tvPostLocation = itemView.findViewById(R.id.tvPostLocation);
            tvPostDate = itemView.findViewById(R.id.tvPostDate);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
        }
        
        public void bind(Post post, OnPostClickListener listener) {
            // Set title
            tvPostTitle.setText(post.getTitle());
            
            // Set post image if available
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                Log.d("PostAdapter", "Loading image for post " + post.getId() + ": " + post.getImageUrl());
                ivPostImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                    .load(post.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivPostImage);
            } else {
                Log.d("PostAdapter", "No image URL for post " + post.getId());
                ivPostImage.setVisibility(View.GONE);
            }
            
            // Set organization name
            if (post.getOrganization() != null) {
                tvOrganizationName.setText(post.getOrganization().getName());
                
                // Set organization rating
                if (post.getOrganization().getRating() != null) {
                    rbOrganizationRating.setRating(post.getOrganization().getRating().floatValue());
                    rbOrganizationRating.setVisibility(View.VISIBLE);
                    
                    if (post.getOrganization().getRatingCount() > 0) {
                        tvRatingCount.setText("(" + post.getOrganization().getRatingCount() + ")");
                        tvRatingCount.setVisibility(View.VISIBLE);
                    } else {
                        tvRatingCount.setVisibility(View.GONE);
                    }
                } else {
                    rbOrganizationRating.setVisibility(View.GONE);
                    tvRatingCount.setVisibility(View.GONE);
                }
            } else {
                tvOrganizationName.setText("Unknown Organization");
                rbOrganizationRating.setVisibility(View.GONE);
                tvRatingCount.setVisibility(View.GONE);
            }
            
            // Set description
            if (post.getDescription() != null && !post.getDescription().isEmpty()) {
                tvPostDescription.setText(post.getDescription());
                tvPostDescription.setVisibility(View.VISIBLE);
            } else {
                tvPostDescription.setVisibility(View.GONE);
            }
            
            // Set location
            if (post.getLocation() != null && !post.getLocation().isEmpty()) {
                tvPostLocation.setText(post.getLocation());
            } else {
                tvPostLocation.setText("Location not specified");
            }
            
            // Set date (relative time)
            tvPostDate.setText(getRelativeTime(post.getCreatedAt()));
            
            // Set priority badge
            setPriorityBadge(post.getPriority());
            
            // Set category badge
            if (post.getCategory() != null) {
                tvCategoryBadge.setText(post.getCategory().toString());
                tvCategoryBadge.setVisibility(View.VISIBLE);
            } else {
                tvCategoryBadge.setVisibility(View.GONE);
            }
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
        }
        
        private void setPriorityBadge(Priority priority) {
            if (priority == null) {
                tvPriorityBadge.setVisibility(View.GONE);
                return;
            }
            
            tvPriorityBadge.setVisibility(View.VISIBLE);
            
            switch (priority) {
                case VERY_HIGH:
                    tvPriorityBadge.setText("URGENT");
                    tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_very_high);
                    break;
                case HIGH:
                    tvPriorityBadge.setText("HIGH");
                    tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_high);
                    break;
                case MEDIUM:
                    tvPriorityBadge.setText("MEDIUM");
                    tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_medium);
                    tvPriorityBadge.setTextColor(0xFF333333); // Dark text for yellow background
                    break;
                case LOW:
                    tvPriorityBadge.setText("LOW");
                    tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_low);
                    break;
            }
        }
        
        private String getRelativeTime(Long timestamp) {
            if (timestamp == null) {
                return "Unknown";
            }
            
            long now = System.currentTimeMillis();
            long seconds = (now - timestamp) / 1000;
            
            if (seconds < 60) {
                return "Just now";
            } else if (seconds < 3600) {
                long minutes = seconds / 60;
                return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
            } else if (seconds < 86400) {
                long hours = seconds / 3600;
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (seconds < 604800) {
                long days = seconds / 86400;
                return days + (days == 1 ? " day ago" : " days ago");
            } else if (seconds < 2592000) {
                long weeks = seconds / 604800;
                return weeks + (weeks == 1 ? " week ago" : " weeks ago");
            } else {
                long months = seconds / 2592000;
                return months + (months == 1 ? " month ago" : " months ago");
            }
        }
    }
}
