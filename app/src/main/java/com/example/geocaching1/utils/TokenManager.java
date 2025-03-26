package com.example.geocaching1.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
// TokenManager.java
public class TokenManager {
    private static volatile TokenManager instance;
    private final SharedPreferences prefs;
    private String currentToken;

    private TokenManager(Context context) {
        prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        currentToken = prefs.getString("JWT_TOKEN", "");
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) {
                    instance = new TokenManager(context);
                }
            }
        }
        return instance;
    }

    public synchronized String getToken() {
        return currentToken;
    }

    public synchronized void refreshToken() {
        currentToken = prefs.getString("JWT_TOKEN", "");
    }

    // 修改updateToken方法
    public synchronized void updateToken(String newToken) {
        currentToken = newToken;
        prefs.edit()
                .putString("JWT_TOKEN", newToken)
                .commit(); // 使用commit确保立即写入
    }
    // Add this method to ensure token is fresh
    public synchronized String getFreshToken() {
        return currentToken != null ? currentToken : prefs.getString("JWT_TOKEN", "");
    }

    public synchronized void updateUserInfo(String username, String email) {
        SharedPreferences.Editor editor = prefs.edit();
        if (username != null) editor.putString("USERNAME", username);
        if (email != null) editor.putString("EMAIL", email);
        editor.apply();
    }

    public synchronized void clear() {
        currentToken = "";
        prefs.edit().clear().apply();
    }
}