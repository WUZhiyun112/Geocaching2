package com.example.geocaching1;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.view.View;
import android.widget.Button;
import com.example.geocaching1.R;


public class EnterActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;

    private Button mainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        // 绑定 Login 按钮
        loginButton = findViewById(R.id.button_login);
        registerButton = findViewById(R.id.button_register);
        mainButton = findViewById(R.id.button_main);

        // 设置 Login 按钮的点击事件
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 LoginActivity
                Intent intent = new Intent(EnterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 RegisterActivity
                Intent intent = new Intent(EnterActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 LoginActivity
                Intent intent = new Intent(EnterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}