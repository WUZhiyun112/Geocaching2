package com.example.geocaching1;

public class FollowRequest {
    private Integer userId;
    private String geocacheCode;
    private boolean isFollowed;

    // Getter 和 Setter 方法
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGeocacheCode() {
        return geocacheCode;
    }

    public void setGeocacheCode(String geocacheCode) {
        this.geocacheCode = geocacheCode;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }
}
