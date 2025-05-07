package com.example.geocaching1;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.geocaching1.utils.ApiClient;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private static final String LOGIN_URL = "http://192.168.189.72:8080/api/users/login";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    EditText usernameField;
    EditText passwordField;

    private String lastToastMessage;
    private MaterialButton loginButton;
    private MaterialButton forgotPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.button_confirm);
        forgotPasswordButton = findViewById(R.id.button_forgot_password);

        loginButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // 校验用户名和密码
            if (!validateInput(username, password)) {
                return;
            }

            // 调用登录方法
            loginUser(username, password);
        });

        forgotPasswordButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    /**
     * 校验用户名和密码
     */
// 在 LoginActivity.java 中
    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            usernameField.setError("Username is empty");
            return false;
        }
        if (password.isEmpty()) {
            passwordField.setError("Password is empty");
            return false;
        }
        if (password.length() < 8) {
            passwordField.setError("Password must be at least 8 characters long");
            return false;
        }
        return true;
    }
    private void loginUser(String username, String password) {
        Log.d(TAG, "尝试登录，用户名: " + username);
        OkHttpClient client = ApiClient.getUnsafeOkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", password);
            Log.d(TAG, "请求体: " + json.toString());
        } catch (JSONException e) {
            Log.e(TAG, "构建 JSON 失败", e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .build();

        Log.d(TAG, "发送请求至: " + LOGIN_URL);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "网络请求失败", e);
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "网络错误，请检查连接！", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "Response received, code: " + response.code());
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    Log.d(TAG, "Response body: " + responseBody);

                    try {
                        // 解析响应 JSON
                        JSONObject responseJson = new JSONObject(responseBody);
                        String token = responseJson.getString("token");
                        String username = responseJson.getString("username");
                        String email = responseJson.getString("email");
                        int userId = responseJson.getInt("userId");  // 获取后端返回的 userId

                        // 保存登录信息到 SharedPreferences
                        saveLoginInfo(token, username, email, userId);

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login successful！", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("TOKEN", token);
                            intent.putExtra("USERNAME", username);
                            intent.putExtra("EMAIL", email);
                            intent.putExtra("USER_ID", userId);
                            startActivity(intent);
                            finish();
                        });

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response JSON", e);
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "服务器响应解析失败", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e(TAG, "Login failed with code: " + response.code());

                    String errorMessage = "登录失败，请重试！";  // 默认错误消息
                    try {
                        JSONObject errorJson = new JSONObject(responseBody);
                        if (errorJson.has("message")) {
                            errorMessage = errorJson.getString("message");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing error response JSON", e);
                    }

                    String finalErrorMessage = errorMessage;
                    runOnUiThread(() -> {
                        String toastMessage;
                        switch (response.code()) {
                            case 400:
                                toastMessage = "Bad request, please check your input!";
                                break;
                            case 401:
                                toastMessage = "Incorrect username or password!";
                                break;
                            case 404:
                                toastMessage = "User not found, please register!";
                                break;
                            case 500:
                                toastMessage = "Server error, please try again later!";
                                break;
                            default:
                                toastMessage = "Login failed, please try again!";
                                break;
                        }
                        Toast.makeText(LoginActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                    });

                }
            }

        });
    }

    /**
     * 解析并显示登录错误信息
     */
    private void handleLoginError(String responseBody) {
        try {
            JSONObject errorJson = new JSONObject(responseBody);
            String errorMessage = errorJson.optString("message", "登录失败，请检查用户名或密码！");

            Log.e(TAG, "登录失败: " + errorMessage);
            showErrorToast(errorMessage);
        } catch (JSONException e) {
            Log.e(TAG, "无法解析错误信息", e);
            showErrorToast("登录失败，未知错误！");
        }
    }

    /**
     * 显示 Toast 提示
//     */
//    private void showErrorToast(String message) {
//        runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show());
//    }

    /**
     * 保存 JWT 令牌和用户信息到 SharedPreferences
     */
    private void saveLoginInfo(String token, String username, String email, int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("JWT_TOKEN", token);
        editor.putString("USERNAME", username);
        editor.putString("EMAIL", email);
        editor.putInt("USER_ID", userId);
        editor.apply();
    }


    public String getLastToastMessage() {
        return lastToastMessage;
    }

    private void showErrorToast(String message) {
        lastToastMessage = message;
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
