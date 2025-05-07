package com.example.geocaching1;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geocaching1.utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final int MAX_RETRY_COUNT = 1;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private TokenManager tokenManager;
    private OkHttpClient okHttpClient;
    private EditText newUsernameEditText, newEmailEditText;
    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private String currentUsername, currentEmail;
    private boolean isRequestInProgress = false;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化OkHttpClient
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

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
                .setTitle("Confirm Username Change")
                .setMessage("Are you sure you want to change your username from \"" + currentUsername + "\" to \"" + newUsername + "\" ?")
                .setPositiveButton("Yes", (dialog, which) ->
                        executeSecureRequest(
                                "http://192.168.189.72:8080/api/users/change-username",
                                createUsernameRequestBody(newUsername),
                                this::handleUsernameSuccess
                        )
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEmailChangeDialog(String newEmail) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Email Change")
                .setMessage("Are you sure you want to change your email from \"" + currentEmail + "\" to \"" + newEmail + "\" ?")
                .setPositiveButton("Yes", (dialog, which) ->
                        executeSecureRequest(
                                "http://192.168.189.72:8080/api/users/change-email",
                                createEmailRequestBody(newEmail),
                                this::handleEmailSuccess
                        )
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPasswordChangeDialog(String currentPassword, String newPassword) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Password Change")
                .setMessage("Are you sure you want to change your password?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // 调用 executeSecureRequest，传入正确的请求体类型
                    executeSecureRequest(
                            "http://192.168.189.72:8080/api/users/change-password",
                            createPasswordRequestBody(currentPassword, newPassword),
                            this::handlePasswordSuccess
                    );
                })
                .setNegativeButton("Cancel", null)
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
        retryCount = 0;

        final String currentToken = tokenManager.getToken();
        if (currentToken == null || !tokenManager.verifyToken(currentToken)) {
            handleInvalidToken();
            return;
        }

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + currentToken)
                .addHeader("Content-Type", "application/json")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isRequestInProgress = false;
                    setAllChangeButtonsEnabled(true);
                    errorHandler.handle(e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    runOnUiThread(() -> {
                        isRequestInProgress = false;
                        setAllChangeButtonsEnabled(true);

                        if (response.isSuccessful()) {
                            try {
                                successHandler.handle(jsonResponse);
                            } catch (JSONException e) {
                                handleParseError();
                            }
                        } else {
                            handleErrorResponse(jsonResponse);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        isRequestInProgress = false;
                        setAllChangeButtonsEnabled(true);
                        handleParseError();
                    });
                }
            }
        });
    }

    private boolean isAuthFailure(Exception error) {
        if (error instanceof IOException) {
            String message = error.getMessage();
            return message != null && message.contains("HTTP 401");
        }
        return false;
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
                executeSecureRequest(url, requestBody, successHandler, error -> {
                    if (isAuthFailure(error)) {
                        handleInvalidToken();
                    } else {
                        handleDefaultError(error);
                    }
                });
            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> {
                    handleInvalidToken();
                });
            }
        });
    }

    private void handleDefaultError(Exception error) {
        String errorMsg = "网络错误";
        if (error instanceof IOException) {
            errorMsg = error.getMessage();
        }
        showToast(errorMsg);
    }

//    private void showSuccessDialog(String title, String message) {
//        new AlertDialog.Builder(this)
//                .setTitle(title)
//                .setMessage(message)
//                .setPositiveButton("OK", (dialog, which) -> forceLogout())
//                .setCancelable(false)
//                .show();
//    }

    private void handleUsernameSuccess(JSONObject response) throws JSONException {
        String newToken = response.getString("token");
        String newUsername = response.getString("username");

        tokenManager.updateTokenAndUserInfo(newToken, newUsername, null);
        currentUsername = newUsername;

        runOnUiThread(() -> {
            newUsernameEditText.setText("");
            showSuccessDialog(
                    "用户名修改成功",
                    "安全提示：用户名是重要账户凭证\n请重新登录以继续使用",
                    true // 强制登出
            );
        });
    }

//    private void handleEmailSuccess(JSONObject response) throws JSONException {
//        String newToken = response.getString("token");
//        String newEmail = response.getString("email");
//
//        tokenManager.updateTokenAndUserInfo(newToken, null, newEmail);
//        currentEmail = newEmail;
//
//        runOnUiThread(() -> {
//            newEmailEditText.setText("");
//            showSuccessDialog("Email Changed", "Email updated successfully. Please login again.");
//        });
//    }

    // 修改handleEmailSuccess方法
// 修改 handleEmailSuccess 方法
    private void handleEmailSuccess(JSONObject response) throws JSONException {
        runOnUiThread(() -> {
            newEmailEditText.setText("");
            showSuccessDialog(
                    "邮箱修改成功",
                    "安全提示：邮箱是重要账户凭证\n请重新登录以继续使用",
                    true // 强制登出
            );
        });
    }

    // 增强 showSuccessDialog
    private void showSuccessDialog(String title, String message, boolean forceLogout) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> {
                    if (forceLogout) {
                        forceLogout();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void handlePasswordSuccess(JSONObject response) throws JSONException {
        String newToken = response.getString("token");
        tokenManager.updateToken(newToken);

        runOnUiThread(() -> {
            clearPasswordFields();
            showSuccessDialog(
                    "密码修改成功",
                    "安全提示：密码是重要账户凭证\n请重新登录以继续使用",
                    true // 强制登出
            );
        });
    }

    private void forceLogout() {
        tokenManager.clear(); // 清除 token 等登录信息

        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // 杀掉当前进程，确保彻底重启
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } else {
            // fallback：无法获取启动 Intent，退回登录页
            Intent fallbackIntent = new Intent(this, LoginActivity.class);
            fallbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(fallbackIntent);
            finish();
        }
    }


    private void handleInvalidToken() {
        runOnUiThread(() -> {
            showToast("会话已过期");

            // 清除 token 等登录信息
            tokenManager.clear();

            // 获取启动 Intent（等同于 Launcher 启动）
            Intent intent = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // 杀掉当前进程，确保彻底重启
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            } else {
                // fallback：无法获取启动 Intent，退回登录页
                Intent fallbackIntent = new Intent(this, LoginActivity.class);
                fallbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(fallbackIntent);
                finish();
            }
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

    private JSONObject createPasswordRequestBody(String oldPassword, String newPassword) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("oldPassword", oldPassword);
            requestBody.put("newPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("PasswordChange", "Request Body: " + requestBody.toString());
        return requestBody;  // 返回 JSONObject
    }


    private interface SuccessHandler {
        void handle(JSONObject response) throws JSONException;
    }

    private interface ErrorHandler {
        void handle(Exception error);
    }
}