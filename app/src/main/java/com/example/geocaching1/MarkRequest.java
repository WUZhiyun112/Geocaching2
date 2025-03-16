package com.example.geocaching1;

public class MarkRequest {
    private Integer userId;
    private String geocacheCode;
    private boolean isMarked;

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

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        isMarked = marked;
    }
}
