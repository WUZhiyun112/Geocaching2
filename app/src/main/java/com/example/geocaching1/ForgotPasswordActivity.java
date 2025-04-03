package com.example.geocaching1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText;
    private View submitButton, updatePasswordButton;
    private TextInputLayout newPasswordLayout, confirmNewPasswordLayout;
    private TextInputEditText newPasswordEditText, confirmNewPasswordEditText; // Add these

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String VERIFY_URL = "http://192.168.72.72:8080/api/users/verify";
    private static final String UPDATE_PASSWORD_URL = "http://192.168.72.72:8080/api/users/forgot-password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);

        // Correctly initialize both TextInputLayout and TextInputEditText
        newPasswordLayout = findViewById(R.id.newPasswordLayout); // This should be the layout
        confirmNewPasswordLayout = findViewById(R.id.confirmNewPasswordLayout); // This should be the layout
        newPasswordEditText = findViewById(R.id.new_password); // This is the EditText
        confirmNewPasswordEditText = findViewById(R.id.confirm_new_password); // This is the EditText

        submitButton = findViewById(R.id.button_submit);
        updatePasswordButton = findViewById(R.id.button_update_password);

        // Initially hide password fields
        newPasswordLayout.setVisibility(View.GONE);
        confirmNewPasswordLayout.setVisibility(View.GONE);
        updatePasswordButton.setVisibility(View.GONE);

        submitButton.setOnClickListener(v -> onSubmitClicked());
        updatePasswordButton.setOnClickListener(v -> onUpdatePasswordClicked());
    }

    private void onUpdatePasswordClicked() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter and confirm your new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        updatePassword(newPassword);
    }

    private void onSubmitClicked() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please enter username and email", Toast.LENGTH_SHORT).show();
            return;
        }

        verifyUserCredentials(username, email);
    }

    private void verifyUserCredentials(String username, String email) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder().url(VERIFY_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Network Error", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Make password fields visible only after successful verification
                    runOnUiThread(() -> {
                        confirmNewPasswordLayout.setVisibility(View.VISIBLE);
                        newPasswordLayout.setVisibility(View.VISIBLE);
                        updatePasswordButton.setVisibility(View.VISIBLE);

                        newPasswordLayout.requestLayout();
                        confirmNewPasswordLayout.requestLayout();
                        updatePasswordButton.requestLayout();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updatePassword(String newPassword) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("newPassword", newPassword);
            json.put("username", usernameEditText.getText().toString());
            json.put("email", emailEditText.getText().toString()); // 添加email字段
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(UPDATE_PASSWORD_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ForgotPasswordActivity.this,
                                "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "密码更新成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                        finish();
                    });
                } else {
                    String errorMsg = response.body() != null ?
                            response.body().string() : "未知错误";
                    runOnUiThread(() ->
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "密码更新失败: " + errorMsg, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}
