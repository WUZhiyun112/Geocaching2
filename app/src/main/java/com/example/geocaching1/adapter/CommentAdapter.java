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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.username.setText("User: " + comment.getUsername()); // 显示用户名
        holder.commentTime.setText(formatCommentTime(comment.getCommentTime())); // 格式化时间
        holder.content.setText(comment.getContent());
        holder.rating.setText("Rating: " + comment.getRating());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    private String formatCommentTime(Date commentTime) {
        if (commentTime == null) {
            return "Unknown time";
        }

        // 使用相对时间显示（如"2小时前"）
        long now = System.currentTimeMillis();
        long diff = now - commentTime.getTime();

        if (diff < 60 * 1000) {
            return "Just now";
        } else if (diff < 60 * 60 * 1000) {
            long minutes = diff / (60 * 1000);
            return minutes + " min ago";
        } else if (diff < 24 * 60 * 60 * 1000) {
            long hours = diff / (60 * 60 * 1000);
            return hours + " hours ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return sdf.format(commentTime);
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView username, commentTime, content, rating;

        public CommentViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tv_username);
            commentTime = itemView.findViewById(R.id.tv_comment_time);
            content = itemView.findViewById(R.id.tv_comment_content);
            rating = itemView.findViewById(R.id.tv_rating);
        }
    }
}