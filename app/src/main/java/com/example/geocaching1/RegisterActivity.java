package com.example.geocaching1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private static final String REGISTER_URL = "http://192.168.189.72:8080/api/users/register";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private boolean isRegistering = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable resetRegisteringFlag;
    private CheckBox privacyPolicyCheckBox;
    private boolean privacyPolicyAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化隐私政策复选框
        privacyPolicyCheckBox = findViewById(R.id.privacyPolicyCheckBox);
        if (privacyPolicyCheckBox != null) {
            privacyPolicyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        showPrivacyPolicyDialog();
                    } else {
                        privacyPolicyAccepted = false;
                    }
                }
            });
        }

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

            if (!privacyPolicyAccepted) {
                Toast.makeText(this, "Please read and agree to the Privacy Policy.", Toast.LENGTH_SHORT).show();
                return;
            }

            EditText emailField = findViewById(R.id.editTextEmail);
            EditText usernameField = findViewById(R.id.editTextUsername);
            EditText passwordField = findViewById(R.id.editTextPassword);

            String email = emailField.getText().toString().trim();
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // 输入验证
            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields must be filled out", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(email)) {
                emailField.setError("Please enter a valid email address");
                return;
            }
            if (username.length() < 3) {
                usernameField.setError("Username must be at least 3 characters long");
                return;
            }
            if (password.length() < 8) {
                passwordField.setError("Password must be at least 8 characters long");
                return;
            }

            // 防止重复提交
            isRegistering = true;
            registerUser(email, username, password);

            // 1 秒后允许再次提交
            resetRegisteringFlag = () -> isRegistering = false;
            handler.postDelayed(resetRegisteringFlag, 1000);
        });
    }

    private void showPrivacyPolicyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.privacy_policy_title)
                .setMessage(R.string.privacy_policy_text)
                .setPositiveButton(R.string.privacy_policy_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        privacyPolicyAccepted = true;
                        privacyPolicyCheckBox.setChecked(true);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.privacy_policy_decline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        privacyPolicyAccepted = false;
                        privacyPolicyCheckBox.setChecked(false);
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                    });
                } else {
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
            handler.removeCallbacks(resetRegisteringFlag);
        }
    }
}