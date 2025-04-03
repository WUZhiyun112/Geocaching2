package com.example.geocaching1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class TokenManager {
    private static volatile TokenManager instance;
    private final SharedPreferences prefs;
    private volatile String currentToken;
    private final Object lock = new Object();

    // 添加回调接口
    public interface RefreshCallback {
        void onSuccess(String newToken);
        void onFailure();
    }

    private TokenManager(Context context) {
        prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        synchronized (lock) {
            currentToken = prefs.getString("JWT_TOKEN", "");
        }
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

    public String getToken() {
        synchronized (lock) {
            return currentToken;
        }
    }

    public synchronized void updateTokenAndUserInfo(String newToken, String username, String email) {
        synchronized (lock) {
            currentToken = newToken;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("JWT_TOKEN", newToken);
            if (username != null) editor.putString("USERNAME", username);
            if (email != null) editor.putString("EMAIL", email);
            editor.apply();
            Log.d("TOKEN_FLOW", "Token和用户信息更新完成");
        }
    }

    // 实现刷新令牌方法
    public void refreshToken(RefreshCallback callback) {
        synchronized (lock) {
            // 这里应该是实际的令牌刷新逻辑
            // 示例：模拟网络请求刷新令牌
            new Thread(() -> {
                try {
                    // 模拟网络延迟
                    Thread.sleep(1000);

                    // 这里应该是实际的API调用获取新令牌
                    // 示例中我们只是生成一个模拟令牌
                    String newToken = "模拟的新令牌_" + System.currentTimeMillis();

                    // 更新令牌
                    currentToken = newToken;
                    prefs.edit().putString("JWT_TOKEN", newToken).apply();

                    // 回调成功
                    callback.onSuccess(newToken);
                } catch (InterruptedException e) {
                    // 回调失败
                    callback.onFailure();
                }
            }).start();
        }
    }

    public void updateToken(String newToken) {
        synchronized (lock) {
            currentToken = newToken;
            prefs.edit().putString("JWT_TOKEN", newToken).apply();
            Log.d("TOKEN_FLOW", "Token更新完成");
        }
    }

    public boolean verifyToken(String tokenToVerify) {
        synchronized (lock) {
            boolean isValid = tokenToVerify != null && tokenToVerify.equals(currentToken);
            Log.d("TOKEN_FLOW", "Token验证: " + isValid);
            return isValid;
        }
    }

    public void clear() {
        synchronized (lock) {
            currentToken = "";
            prefs.edit().clear().apply();
        }
    }
}