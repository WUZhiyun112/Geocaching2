package com.example.geocaching1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import okhttp3.Authenticator;

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
    public Map<String, String> getAuthHeader() {
        Map<String, String> headers = new HashMap<>();
        synchronized (lock) {
            try {
                // 检查currentToken是否为空
                if (currentToken == null || currentToken.isEmpty()) {
                    Log.e("TokenManager", "Token is null or empty");
                    return headers;
                }

                // 确保令牌正确编码并移除非法字符
                String cleanToken = currentToken.replaceAll("[^\\x20-\\x7e]", "");

                // 将清理后的token添加到授权头
                headers.put("Authorization", "Bearer " + cleanToken);
                headers.put("Content-Type", "application/json");
            } catch (Exception e) {
                Log.e("TokenManager", "Token encoding failed", e);
            }
        }
        return headers;
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
    public Authenticator getAuthenticator() {
        return (route, response) -> {
            // 刷新令牌
            final CountDownLatch latch = new CountDownLatch(1);
            final String[] newToken = {null};

            refreshToken(new RefreshCallback() {
                @Override
                public void onSuccess(String token) {
                    newToken[0] = token;
                    latch.countDown();
                }

                @Override
                public void onFailure() {
                    latch.countDown();
                }
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                return null;
            }

            if (newToken[0] != null) {
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newToken[0])
                        .build();
            }
            return null;
        };
    }

    public boolean verifyToken(String tokenToVerify) {
        if (tokenToVerify == null || tokenToVerify.isEmpty()) {
            return false;
        }

        // 检查JWT基本格式
        if (!tokenToVerify.matches("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$")) {
            Log.e("TokenManager", "Malformed token detected");
            return false;
        }

        synchronized (lock) {
            return tokenToVerify.equals(currentToken);
        }
    }
    public void clear() {
        SharedPreferences.Editor editor = prefs.edit();  // 修改这一行，使用已有的 prefs
        editor.remove("JWT_TOKEN");
        editor.remove("USERNAME");
        editor.remove("EMAIL");
        editor.remove("USER_ID");
        editor.apply();
        currentToken = null;  // 同时把内存中的 token 清掉
    }


}