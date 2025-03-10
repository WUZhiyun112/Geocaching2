package com.example.geocaching1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SelfActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private TextView emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self);

        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);

        updateUIFromPreferences();

        findViewById(R.id.follows_item).setOnClickListener(v -> {
            Intent intent = new Intent(SelfActivity.this, FollowedGeocachesActivity.class);
            startActivity(intent);
        });
        // 设置点击事件
        findViewById(R.id.finds_item).setOnClickListener(v -> {
//            startActivity(new Intent(SelfActivity.this, FindsActivity.class));
        });

        findViewById(R.id.settings_item).setOnClickListener(v -> {
//            startActivity(new Intent(SelfActivity.this, SettingsActivity.class));
        });

        findViewById(R.id.logout_item).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().remove("JWT_TOKEN").apply();
            Toast.makeText(SelfActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SelfActivity.this, LoginActivity.class));
            finish();
        });

    }

    private void updateUIFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // 获取存储的用户信息
        String username = prefs.getString("USERNAME", "N/A");
        String email = prefs.getString("EMAIL", "N/A");
        int userId = prefs.getInt("USER_ID", -1);  // 获取 userId，如果没有存储则默认为 -1

        // 打印调试信息
        Log.d("SelfActivity", "UserId: " + userId);
        Log.d("SelfActivity", "Username: " + username);
        Log.d("SelfActivity", "Email: " + email);

        // 更新 UI
        usernameTextView.setText(username);
        emailTextView.setText(email);
    }

}
