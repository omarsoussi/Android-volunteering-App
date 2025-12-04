package com.example.tounesna.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tounesna.R;
import com.example.tounesna.model.Notification;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    
    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener listener;
    
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }
    
    public NotificationAdapter(Context context, List<Notification> notifications, OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(getTimeAgo(notification.getCreatedAt()));
        
        // Show unread indicator
        if (!notification.isRead()) {
            holder.unreadIndicator.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.unreadIndicator.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
        
        // Set icon based on notification type
        int iconRes = getIconForNotificationType(notification.getType());
        holder.ivIcon.setImageResource(iconRes);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return notifications.size();
    }
    
    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        
        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            return (days / 7) + " week" + (days / 7 == 1 ? "" : "s") + " ago";
        }
    }
    
    private int getIconForNotificationType(String type) {
        if (type == null) return R.drawable.ic_notification;
        
        switch (type) {
            case "REQUEST_SENT":
            case "REQUEST_RECEIVED":
                return R.drawable.ic_request;
            case "REQUEST_APPROVED":
                return R.drawable.ic_check;
            case "REQUEST_REJECTED":
                return R.drawable.ic_close;
            case "FOLLOWED_ORG_POSTED":
            case "FOLLOWED_ORG_POSTED_ORG":
                return R.drawable.ic_post;
            case "NEW_FOLLOWER":
                return R.drawable.ic_person_add;
            case "NEW_RATING":
                return R.drawable.ic_star;
            default:
                return R.drawable.ic_notification;
        }
    }
    
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTime;
        View unreadIndicator;
        
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }
    }
}
