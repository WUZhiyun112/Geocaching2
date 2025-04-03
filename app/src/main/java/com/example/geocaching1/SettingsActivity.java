package com.example.geocaching1;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.geocaching1.utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final int MAX_RETRY_COUNT = 1;

    private TokenManager tokenManager;
    private RequestQueue requestQueue;
    private EditText newUsernameEditText, newEmailEditText;
    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private String currentUsername, currentEmail;
    private boolean isRequestInProgress = false;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        requestQueue = Volley.newRequestQueue(this);
        tokenManager = TokenManager.getInstance(this);
        initViews();
        loadUserInfo();
        setupButtonListeners();
    }

    private void initViews() {
        newUsernameEditText = findViewById(R.id.newUsernameEditText);
        newEmailEditText = findViewById(R.id.newEmailEditText);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("USERNAME", "");
        currentEmail = prefs.getString("EMAIL", "");
    }

    private void setupButtonListeners() {
        findViewById(R.id.changeUsernameButton).setOnClickListener(v -> {
            String newUsername = newUsernameEditText.getText().toString().trim();
            if (newUsername.isEmpty()) {
                showToast("请输入新用户名");
            } else {
                showUsernameChangeDialog(newUsername);
            }
        });

        findViewById(R.id.changeEmailButton).setOnClickListener(v -> {
            String newEmail = newEmailEditText.getText().toString().trim();
            if (newEmail.isEmpty()) {
                showToast("请输入新邮箱");
            } else {
                showEmailChangeDialog(newEmail);
            }
        });

        findViewById(R.id.changePasswordButton).setOnClickListener(v -> {
            String currentPass = currentPasswordEditText.getText().toString().trim();
            String newPass = newPasswordEditText.getText().toString().trim();
            String confirmPass = confirmPasswordEditText.getText().toString().trim();

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                showToast("请填写所有密码字段");
            } else if (!newPass.equals(confirmPass)) {
                showToast("两次输入的新密码不一致");
            } else {
                showPasswordChangeDialog(currentPass, newPass);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showUsernameChangeDialog(String newUsername) {
        new AlertDialog.Builder(this)
                .setTitle("确认修改用户名")
                .setMessage("确定要将用户名从 \"" + currentUsername + "\" 修改为 \"" + newUsername + "\" 吗？")
                .setPositiveButton("确定", (dialog, which) ->
                        executeSecureRequest(
                                "http://192.168.72.72:8080/api/users/change-username",
                                createUsernameRequestBody(newUsername),
                                this::handleUsernameSuccess
                        )
                )
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEmailChangeDialog(String newEmail) {
        new AlertDialog.Builder(this)
                .setTitle("确认修改邮箱")
                .setMessage("确定要将邮箱从 \"" + currentEmail + "\" 修改为 \"" + newEmail + "\" 吗？")
                .setPositiveButton("确定", (dialog, which) ->
                        executeSecureRequest(
                                "http://192.168.72.72:8080/api/users/change-email",
                                createEmailRequestBody(newEmail),
                                this::handleEmailSuccess
                        )
                )
                .setNegativeButton("取消", null)
                .show();
    }

    private void showPasswordChangeDialog(String currentPassword, String newPassword) {
        new AlertDialog.Builder(this)
                .setTitle("确认修改密码")
                .setMessage("确定要修改密码吗？")
                .setPositiveButton("确定", (dialog, which) ->
                        executeSecureRequest(
                                "http://192.168.72.72:8080/api/users/change-password",
                                createPasswordRequestBody(currentPassword, newPassword),
                                this::handlePasswordSuccess
                        )
                )
                .setNegativeButton("取消", null)
                .show();
    }

    private void executeSecureRequest(String url, JSONObject requestBody,
                                      SuccessHandler successHandler) {
        executeSecureRequest(url, requestBody, successHandler, error -> {
            if (isAuthFailure(error)) {
                handleTokenRefreshAndRetry(url, requestBody, successHandler);
            } else {
                handleDefaultError(error);
            }
        });
    }

    private void executeSecureRequest(String url, JSONObject requestBody,
                                      SuccessHandler successHandler,
                                      ErrorHandler errorHandler) {
        if (isRequestInProgress) {
            showToast("请等待当前操作完成");
            return;
        }

        isRequestInProgress = true;
        setAllChangeButtonsEnabled(false);
        retryCount = 0; // 重置重试计数器

        final String currentToken = tokenManager.getToken();
        if (currentToken == null || !tokenManager.verifyToken(currentToken)) {
            handleInvalidToken();
            return;
        }

        makeApiRequest(url, requestBody, successHandler, errorHandler, currentToken);
    }

    private void makeApiRequest(String url, JSONObject requestBody,
                                SuccessHandler successHandler,
                                ErrorHandler errorHandler,
                                String token) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    isRequestInProgress = false;
                    try {
                        Log.d(TAG, "API响应: " + response);
                        if (response.getBoolean("success")) {
                            successHandler.handle(response);
                        } else {
                            handleErrorResponse(response);
                        }
                    } catch (JSONException e) {
                        handleParseError();
                    } finally {
                        runOnUiThread(() -> setAllChangeButtonsEnabled(true));
                    }
                },
                error -> {
                    isRequestInProgress = false;
                    Log.e(TAG, "请求错误: ", error);
                    errorHandler.handle(error);
                    runOnUiThread(() -> setAllChangeButtonsEnabled(true));
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private boolean isAuthFailure(VolleyError error) {
        return error instanceof AuthFailureError ||
                (error.networkResponse != null && error.networkResponse.statusCode == 401);
    }

    private void handleTokenRefreshAndRetry(String url, JSONObject requestBody,
                                            SuccessHandler successHandler) {
        if (retryCount >= MAX_RETRY_COUNT) {
            handleInvalidToken();
            return;
        }

        retryCount++;
        showToast("正在自动刷新令牌...");

        tokenManager.refreshToken(new TokenManager.RefreshCallback() {
            @Override
            public void onSuccess(String newToken) {
                makeApiRequest(url, requestBody, successHandler, error -> {
                    if (isAuthFailure(error)) {
                        handleInvalidToken();
                    } else {
                        handleDefaultError(error);
                    }
                }, newToken);
            }

            @Override
            public void onFailure() {
                handleInvalidToken();
            }
        });
    }

    private void handleDefaultError(VolleyError error) {
        String errorMsg = "网络错误";
        if (error.networkResponse != null) {
            errorMsg = "HTTP " + error.networkResponse.statusCode;
            if (error.networkResponse.data != null) {
                errorMsg += ": " + new String(error.networkResponse.data);
            }
        }
        showToast(errorMsg);
    }

    private void handleUsernameSuccess(JSONObject response) throws JSONException {
        String newToken = response.getString("token");
        String newUsername = response.getString("username");

        tokenManager.updateTokenAndUserInfo(newToken, newUsername, null);
        currentUsername = newUsername;

        runOnUiThread(() -> {
            showToast("用户名修改成功");
            newUsernameEditText.setText("");
        });
    }

    private void handleEmailSuccess(JSONObject response) throws JSONException {
        String newToken = response.getString("token");
        String newEmail = response.getString("email");

        tokenManager.updateTokenAndUserInfo(newToken, null, newEmail);
        currentEmail = newEmail;

        runOnUiThread(() -> {
            showToast("邮箱修改成功");
            newEmailEditText.setText("");
        });
    }

    private void handlePasswordSuccess(JSONObject response) throws JSONException {
        String newToken = response.getString("token");
        tokenManager.updateToken(newToken);

        runOnUiThread(() -> {
            showToast("密码修改成功");
            clearPasswordFields();
        });
    }

    private void handleInvalidToken() {
        runOnUiThread(() -> {
            showToast("会话已过期，请重新登录");
            tokenManager.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void setAllChangeButtonsEnabled(boolean enabled) {
        findViewById(R.id.changeUsernameButton).setEnabled(enabled);
        findViewById(R.id.changeEmailButton).setEnabled(enabled);
        findViewById(R.id.changePasswordButton).setEnabled(enabled);
    }

    private void clearPasswordFields() {
        currentPasswordEditText.setText("");
        newPasswordEditText.setText("");
        confirmPasswordEditText.setText("");
    }

    private void handleErrorResponse(JSONObject response) {
        runOnUiThread(() -> {
            try {
                showToast(response.getString("message"));
            } catch (JSONException e) {
                showToast("操作失败");
            }
        });
    }

    private void handleParseError() {
        runOnUiThread(() -> showToast("解析响应失败"));
    }

    private JSONObject createUsernameRequestBody(String newUsername) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("newUsername", newUsername);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestBody;
    }

    private JSONObject createEmailRequestBody(String newEmail) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("newEmail", newEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestBody;
    }

    private JSONObject createPasswordRequestBody(String currentPassword, String newPassword) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("currentPassword", currentPassword);
            requestBody.put("newPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestBody;
    }

    private interface SuccessHandler {
        void handle(JSONObject response) throws JSONException;
    }

    private interface ErrorHandler {
        void handle(VolleyError error);
    }
}