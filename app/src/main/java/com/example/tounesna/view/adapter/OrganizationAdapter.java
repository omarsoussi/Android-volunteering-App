package com.example.tounesna.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tounesna.R;
import com.example.tounesna.model.Organization;

import java.util.List;

/**
 * OrganizationAdapter - RecyclerView adapter for organizations
 */
public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.OrganizationViewHolder> {
    
    public interface OnOrganizationClickListener {
        void onOrganizationClick(Organization organization);
    }
    
    private Context context;
    private List<Organization> organizations;
    private OnOrganizationClickListener listener;
    
    public OrganizationAdapter(Context context, List<Organization> organizations) {
        this.context = context;
        this.organizations = organizations;
    }
    
    public OrganizationAdapter(Context context, List<Organization> organizations, OnOrganizationClickListener listener) {
        this.context = context;
        this.organizations = organizations;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public OrganizationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_organization, parent, false);
        return new OrganizationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrganizationViewHolder holder, int position) {
        Organization org = organizations.get(position);
        
        holder.tvName.setText(org.getName());
        holder.tvDomain.setText(org.getDomain());
        holder.tvLocation.setText(org.getLocation());
        holder.tvEmail.setText(org.getEmail());
        
        // Rating
        if (org.getRating() != null && org.getRating() > 0) {
            holder.rbRating.setRating(org.getRating().floatValue());
            holder.rbRating.setVisibility(View.VISIBLE);
            
            if (org.getRatingCount() > 0) {
                holder.tvRatingCount.setText("(" + org.getRatingCount() + " ratings)");
                holder.tvRatingCount.setVisibility(View.VISIBLE);
            } else {
                holder.tvRatingCount.setVisibility(View.GONE);
            }
        } else {
            holder.rbRating.setVisibility(View.GONE);
            holder.tvRatingCount.setVisibility(View.GONE);
        }
        
        // Member count
        if (org.getMemberCount() > 0) {
            holder.tvMemberCount.setText(org.getMemberCount() + " members");
            holder.tvMemberCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvMemberCount.setVisibility(View.GONE);
        }
        
        // Followers count
        int followersCount = org.getFollowersCount();
        holder.tvFollowersCount.setText(followersCount + (followersCount == 1 ? " follower" : " followers"));
        holder.tvFollowersCount.setVisibility(View.VISIBLE);
        
        // Click listener
        if (listener != null) {
            holder.cardView.setOnClickListener(v -> listener.onOrganizationClick(org));
        }
    }
    
    @Override
    public int getItemCount() {
        return organizations.size();
    }
    
    static class OrganizationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvName;
        TextView tvDomain;
        TextView tvLocation;
        TextView tvEmail;
        RatingBar rbRating;
        TextView tvRatingCount;
        TextView tvMemberCount;
        TextView tvFollowersCount;
        
        public OrganizationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvName = itemView.findViewById(R.id.tvOrgName);
            tvDomain = itemView.findViewById(R.id.tvOrgDomain);
            tvLocation = itemView.findViewById(R.id.tvOrgLocation);
            tvEmail = itemView.findViewById(R.id.tvOrgEmail);
            rbRating = itemView.findViewById(R.id.rbOrgRating);
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            tvFollowersCount = itemView.findViewById(R.id.tvFollowersCount);
        }
    }
}
