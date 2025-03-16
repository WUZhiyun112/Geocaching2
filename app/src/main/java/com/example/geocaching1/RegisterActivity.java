package com.example.geocaching1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geocaching1.utils.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.content.Intent;

public class RegisterActivity extends AppCompatActivity {

//    private static final String REGISTER_URL = "http://10.0.2.2:8080/api/users/register";
private static final String REGISTER_URL = "http://192.168.70.72:8080/api/users/register";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private boolean isRegistering = false; // 防止多次提交
    private Handler handler = new Handler(Looper.getMainLooper()); // 处理防抖
    private Runnable resetRegisteringFlag; // 延迟任务
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 设置返回按钮
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }

        // 设置注册按钮
        Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(v -> {


            if (!isNetworkAvailable()) {
                Toast.makeText(this, "网络不可用，请检查连接", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRegistering) {
                Toast.makeText(this, "请勿重复提交，请稍后再试！", Toast.LENGTH_SHORT).show();
                return;
            }

            EditText emailField = findViewById(R.id.editTextEmail);
            EditText usernameField = findViewById(R.id.editTextUsername);
            EditText passwordField = findViewById(R.id.editTextPassword);

            String email = emailField.getText().toString().trim();
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "所有字段必须填写", Toast.LENGTH_SHORT).show();
                return;
            }

            isRegistering = true; // 标记正在注册，防止短时间内重复提交
            registerUser(email, username, password);

            // 1 秒后允许再次提交
            resetRegisteringFlag = () -> isRegistering = false;
            handler.postDelayed(resetRegisteringFlag, 1000);
        });
    }

    private void registerUser(String email, String username, String password) {
        OkHttpClient client = ApiClient.getUnsafeOkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("username", username);
            json.put("password", password);
        } catch (JSONException e) {
            Log.e("RegisterActivity", "JSON 构造失败: " + e.getMessage());
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(REGISTER_URL)
                .post(body)
                .build();
        Log.d("RegisterActivity", "即将发送注册请求: " + request.toString());
        Log.d("RegisterActivity", "请求体: " + json.toString());


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("RegisterActivity", "注册请求失败: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "网络错误，注册失败！", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("RegisterActivity", "收到服务器响应");

                int statusCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "No Response Body";
                String responseHeaders = response.headers().toString();

                Log.d("RegisterActivity", "HTTP 状态码: " + statusCode);
                Log.d("RegisterActivity", "响应头: " + responseHeaders);
                Log.d("RegisterActivity", "响应体: " + responseBody);

                if (statusCode == 200) {
                    // 注册成功，跳转或提示
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        // 跳转到其他页面，或者清空字段等
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                    });
                } else {
                    // 注册失败，提示错误信息
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "注册失败: " + responseBody, Toast.LENGTH_SHORT).show();
                    });
                }
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resetRegisteringFlag != null) {
            handler.removeCallbacks(resetRegisteringFlag); // 防止内存泄漏
        }
    }
}
