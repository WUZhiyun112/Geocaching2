package com.example.geocaching1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geocaching1.R;
import com.example.geocaching1.model.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyCommentsAdapter extends RecyclerView.Adapter<MyCommentsAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public MyCommentsAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        // Data binding
        holder.code.setText("Geocache: " + comment.getGeocacheCode());
        holder.commentTime.setText(formatCommentTime(comment.getCommentTime()));
        holder.content.setText(comment.getContent());

        // Convert numeric rating to stars (1-5)
        holder.rating.setText(getStarRating(comment.getRating()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    // Format comment time (English)
    private String formatCommentTime(Date commentTime) {
        if (commentTime == null) {
            return "Unknown time";
        }

        long now = System.currentTimeMillis();
        long commentMillis = commentTime.getTime();
        long diff = now - commentMillis;

        // For comments within 1 month (30 days)
        if (diff < 30L * 24 * 60 * 60 * 1000) {
            return getRelativeTimeEnglish(diff);
        } else {
            // For older comments show full date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            return sdf.format(commentTime);
        }
    }

    private String getRelativeTimeEnglish(long diffMillis) {
        long seconds = diffMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else {
            return days + (days == 1 ? " day ago" : " days ago");
        }
    }

    // Convert numeric rating to star symbols
    private String getStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        // Add filled stars
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        // Add empty stars
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView code, commentTime, content, rating;

        public CommentViewHolder(View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.tv_my_geocache_code);
            commentTime = itemView.findViewById(R.id.tv_my_comment_time);
            content = itemView.findViewById(R.id.tv_my_comment_content);
            rating = itemView.findViewById(R.id.tv_my_rating);
        }
    }
}