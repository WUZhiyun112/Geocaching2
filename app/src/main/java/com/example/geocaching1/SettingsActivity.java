package com.example.geocaching1;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    private TokenManager tokenManager;
    private RequestQueue requestQueue;

    private EditText newUsernameEditText;
    private EditText newEmailEditText;
    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;

    private String currentUsername;
    private String currentEmail;
    private boolean isRequestInProgress = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化Volley请求队列
        requestQueue = Volley.newRequestQueue(this);

        // 初始化Token管理器
        tokenManager = TokenManager.getInstance(this);

        // 初始化视图
        initViews();

        // 获取当前用户信息
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("USERNAME", "");
        currentEmail = prefs.getString("EMAIL", "");

        // 设置按钮点击监听
        setupButtonListeners();
    }

    private void initViews() {
        newUsernameEditText = findViewById(R.id.newUsernameEditText);
        newEmailEditText = findViewById(R.id.newEmailEditText);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
    }

    private void setupButtonListeners() {
        findViewById(R.id.changeUsernameButton).setOnClickListener(v -> {
            String newUsername = newUsernameEditText.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                showUsernameChangeDialog(newUsername);
            } else {
                Toast.makeText(this, "请输入新用户名", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.changeEmailButton).setOnClickListener(v -> {
            String newEmail = newEmailEditText.getText().toString().trim();
            if (!newEmail.isEmpty()) {
                showEmailChangeDialog(newEmail);
            } else {
                Toast.makeText(this, "请输入新邮箱", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.changePasswordButton).setOnClickListener(v -> {
            String currentPass = currentPasswordEditText.getText().toString().trim();
            String newPass = newPasswordEditText.getText().toString().trim();
            String confirmPass = confirmPasswordEditText.getText().toString().trim();

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "请填写所有密码字段", Toast.LENGTH_SHORT).show();
            } else if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            } else {
                showPasswordChangeDialog(currentPass, newPass);
            }
        });
    }

    // 显示修改用户名的确认弹窗
    private void showUsernameChangeDialog(String newUsername) {
        new AlertDialog.Builder(this)
                .setTitle("确认修改用户名")
                .setMessage("确定要将用户名从 \"" + currentUsername + "\" 修改为 \"" + newUsername + "\" 吗？")
                .setPositiveButton("确定", (dialog, which) -> changeUsername(newUsername))
                .setNegativeButton("取消", null)
                .show();
    }

    // 显示修改邮箱的确认弹窗
    private void showEmailChangeDialog(String newEmail) {
        new AlertDialog.Builder(this)
                .setTitle("确认修改邮箱")
                .setMessage("确定要将邮箱从 \"" + currentEmail + "\" 修改为 \"" + newEmail + "\" 吗？")
                .setPositiveButton("确定", (dialog, which) -> changeEmail(newEmail))
                .setNegativeButton("取消", null)
                .show();
    }

    // 显示修改密码的确认弹窗
    private void showPasswordChangeDialog(String currentPassword, String newPassword) {
        new AlertDialog.Builder(this)
                .setTitle("确认修改密码")
                .setMessage("确定要修改密码吗？")
                .setPositiveButton("确定", (dialog, which) -> changePassword(currentPassword, newPassword))
                .setNegativeButton("取消", null)
                .show();
    }


    private void changeUsername(String newUsername) {
        if (isRequestInProgress) {
            Toast.makeText(this, "请等待当前操作完成", Toast.LENGTH_SHORT).show();
            return;
        }

        isRequestInProgress = true;
        findViewById(R.id.changeUsernameButton).setEnabled(false);
        findViewById(R.id.changeEmailButton).setEnabled(false);
        findViewById(R.id.changePasswordButton).setEnabled(false);

        String url = "http://192.168.147.72:8080/api/users/change-username";
        tokenManager.refreshToken(); // 确保获取最新token
        final String currentToken = tokenManager.getFreshToken();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                createUsernameRequestBody(newUsername),
                response -> {
                    isRequestInProgress = false;
                    handleUsernameResponse(response, newUsername);
                },
                error -> {
                    isRequestInProgress = false;
                    handleError(error, currentToken);
                    runOnUiThread(() -> {
                        findViewById(R.id.changeUsernameButton).setEnabled(true);
                        findViewById(R.id.changeEmailButton).setEnabled(true);
                        findViewById(R.id.changePasswordButton).setEnabled(true);
                    });
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + currentToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void handleUsernameResponse(JSONObject response, String newUsername) {
        try {
            if (response.getBoolean("success")) {
                String newToken = response.getString("token");
                tokenManager.updateToken(newToken);
                tokenManager.updateUserInfo(newUsername, null);
                currentUsername = newUsername;

                runOnUiThread(() -> {
                    Toast.makeText(this, "用户名修改成功", Toast.LENGTH_SHORT).show();
                    newUsernameEditText.setText("");

                    // 延迟1秒再启用按钮
                    new Handler().postDelayed(() -> {
                        findViewById(R.id.changeUsernameButton).setEnabled(true);
                        findViewById(R.id.changeEmailButton).setEnabled(true);
                        findViewById(R.id.changePasswordButton).setEnabled(true);
                    }, 1000);
                });
            } else {
                handleErrorResponse(response);
            }
        } catch (JSONException e) {
            handleParseError();
        }
    }
    private void changePassword(String currentPassword, String newPassword) {
        if (isRequestInProgress) {
            Toast.makeText(this, "请等待当前操作完成", Toast.LENGTH_SHORT).show();
            return;
        }

        isRequestInProgress = true;
        setAllChangeButtonsEnabled(false);

        String url = "http://192.168.147.72:8080/api/users/change-password";
        tokenManager.refreshToken();
        final String currentToken = tokenManager.getFreshToken();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                createPasswordRequestBody(currentPassword, newPassword),
                response -> {
                    isRequestInProgress = false;
                    handlePasswordResponse(response);
                },
                error -> {
                    isRequestInProgress = false;
                    handleError(error, currentToken);
                    runOnUiThread(() -> setAllChangeButtonsEnabled(true));
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + currentToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void handlePasswordResponse(JSONObject response) {
        try {
            if (response.getBoolean("success")) {
                String newToken = response.getString("token");
                tokenManager.updateToken(newToken);

                runOnUiThread(() -> {
                    Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
                    clearPasswordFields();

                    // 延迟1秒再启用按钮
                    new Handler().postDelayed(() -> setAllChangeButtonsEnabled(true), 1000);
                });

                Log.d("PASSWORD_CHANGE", "Token updated successfully");
            } else {
                handleErrorResponse(response);
            }
        } catch (JSONException e) {
            handleParseError();
        }
    }
    private void changeEmail(String newEmail) {
        if (isRequestInProgress) {
            Toast.makeText(this, "请等待当前操作完成", Toast.LENGTH_SHORT).show();
            return;
        }

        isRequestInProgress = true;
        // 禁用所有修改按钮
        setAllChangeButtonsEnabled(false);

        String url = "http://192.168.147.72:8080/api/users/change-email";
        tokenManager.refreshToken(); // 强制刷新Token
        final String currentToken = tokenManager.getFreshToken();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                createEmailRequestBody(newEmail),
                response -> {
                    isRequestInProgress = false;
                    handleEmailResponse(response, newEmail);
                },
                error -> {
                    isRequestInProgress = false;
                    handleError(error, currentToken);
                    runOnUiThread(() -> setAllChangeButtonsEnabled(true));
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + currentToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void handleEmailResponse(JSONObject response, String newEmail) {
        try {
            if (response.getBoolean("success")) {
                String newToken = response.getString("token");
                tokenManager.updateToken(newToken);
                tokenManager.updateUserInfo(null, newEmail);
                currentEmail = newEmail;

                runOnUiThread(() -> {
                    Toast.makeText(this, "邮箱修改成功", Toast.LENGTH_SHORT).show();
                    newEmailEditText.setText("");

                    // 延迟1秒再启用按钮
                    new Handler().postDelayed(() -> setAllChangeButtonsEnabled(true), 1000);
                });
            } else {
                handleErrorResponse(response);
            }
        } catch (JSONException e) {
            handleParseError();
        }
    }
    private void handleErrorResponse(JSONObject response) {
        runOnUiThread(() -> {
            try {
                String errorMsg = response.getString("message");
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e("PASSWORD_CHANGE", "Server error: " + errorMsg);
            } catch (JSONException e) {
                Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show();
                Log.e("PASSWORD_CHANGE", "Error parsing error response", e);
            }
        });
    }
    // 添加这两个缺失的方法
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

    private void handleParseError() {
        runOnUiThread(() -> {
            Toast.makeText(this, "解析响应失败", Toast.LENGTH_SHORT).show();
            Log.e("PASSWORD_CHANGE", "Failed to parse server response");
        });
    }
    private void handleError(VolleyError error, String usedToken) {
        String errorMsg = "网络错误";
        if (error.networkResponse != null) {
            errorMsg = "HTTP " + error.networkResponse.statusCode;
            if (error.networkResponse.data != null) {
                errorMsg += ": " + new String(error.networkResponse.data);
            }
        }

        final String finalMsg = errorMsg;
        runOnUiThread(() -> {
            Toast.makeText(this, finalMsg, Toast.LENGTH_LONG).show();

            // Token过期处理
            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                tokenManager.clear();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        // 调试日志
        Log.d("API_ERROR", "Used token: " + usedToken);
        Log.d("API_ERROR", "Current token: " + tokenManager.getToken());
        Log.d("API_ERROR", "Error: " + errorMsg);
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
}