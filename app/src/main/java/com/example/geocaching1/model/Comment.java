package com.example.geocaching1.model;

import java.util.Date;

public class Comment {
    private int userId;
    private String username;  // 新增
    private String geocacheCode;
    private int rating;
    private String content;
    private Date commentTime;  // 修改为 Date 类型

    // 构造函数
    public Comment(int userId, String username, String geocacheCode, int rating, String content) {
        this.userId = userId;
        this.username = username;
        this.geocacheCode = geocacheCode;
        this.rating = rating;
        this.content = content;
        this.commentTime = new Date();  // 默认使用当前时间
    }

    // Getter 方法
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getGeocacheCode() { return geocacheCode; }
    public int getRating() { return rating; }
    public String getContent() { return content; }
    public Date getCommentTime() { return commentTime; }  // 返回 Date 类型

    // Setter 方法
    public void setUsername(String username) { this.username = username; }
    public void setCommentTime(Date commentTime) {  // 设置 Date 类型的时间
        this.commentTime = commentTime;
    }
}
