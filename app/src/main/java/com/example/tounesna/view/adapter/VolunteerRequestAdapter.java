package com.example.tounesna.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tounesna.R;
import com.example.tounesna.model.VolunteerRequest;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * VolunteerRequestAdapter - RecyclerView adapter for volunteer requests
 */
public class VolunteerRequestAdapter extends RecyclerView.Adapter<VolunteerRequestAdapter.RequestViewHolder> {
    
    private Context context;
    private List<VolunteerRequest> requests;
    private OnRequestClickListener listener;
    
    public interface OnRequestClickListener {
        void onRequestClick(VolunteerRequest request);
    }
    
    public VolunteerRequestAdapter(Context context, List<VolunteerRequest> requests, 
                                  OnRequestClickListener listener) {
        this.context = context;
        this.requests = requests;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_volunteer_request, parent, false);
        return new RequestViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        VolunteerRequest request = requests.get(position);
        
        holder.tvTitle.setText(request.getTitle());
        holder.tvDescription.setText(request.getDescription());
        holder.tvLocation.setText(request.getLocation());
        
        // Priority badge
        if (request.getPriority() != null) {
            holder.tvPriorityBadge.setText(request.getPriority().toString());
            holder.tvPriorityBadge.setVisibility(View.VISIBLE);
            
            switch (request.getPriority()) {
                case VERY_HIGH:
                    holder.tvPriorityBadge.setText("URGENT");
                    holder.tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_very_high);
                    break;
                case HIGH:
                    holder.tvPriorityBadge.setText("HIGH");
                    holder.tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_high);
                    break;
                case MEDIUM:
                    holder.tvPriorityBadge.setText("MEDIUM");
                    holder.tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_medium);
                    holder.tvPriorityBadge.setTextColor(0xFF333333);
                    break;
                case LOW:
                    holder.tvPriorityBadge.setText("LOW");
                    holder.tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_low);
                    break;
            }
        } else {
            holder.tvPriorityBadge.setVisibility(View.GONE);
        }
        
        // Volunteer name
        if (request.getVolunteer() != null) {
            holder.tvVolunteerName.setText("From: " + request.getVolunteer().getName() + 
                                          " " + request.getVolunteer().getSurname());
            holder.tvVolunteerName.setVisibility(View.VISIBLE);
        } else {
            holder.tvVolunteerName.setVisibility(View.GONE);
        }
        
        // Date
        if (request.getCreatedAt() != null) {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MMM dd, yyyy");
            holder.tvDate.setText(formatter.format(new java.util.Date(request.getCreatedAt())));
        }
        
        // Click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRequestClick(request);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return requests.size();
    }
    
    static class RequestViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle;
        TextView tvPriorityBadge;
        TextView tvVolunteerName;
        TextView tvDescription;
        TextView tvLocation;
        TextView tvDate;
        
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvTitle = itemView.findViewById(R.id.tvRequestTitle);
            tvPriorityBadge = itemView.findViewById(R.id.tvPriorityBadge);
            tvVolunteerName = itemView.findViewById(R.id.tvVolunteerName);
            tvDescription = itemView.findViewById(R.id.tvRequestDescription);
            tvLocation = itemView.findViewById(R.id.tvRequestLocation);
            tvDate = itemView.findViewById(R.id.tvRequestDate);
        }
    }
}
