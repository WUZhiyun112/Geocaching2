package com.example.geocaching1;


import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class GeocacheDetailsActivity extends AppCompatActivity {

    private TextView codeTextView, nameTextView, locationTextView, typeTextView, statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocache_details);

        codeTextView = findViewById(R.id.codeTextView);
        nameTextView = findViewById(R.id.nameTextView);
        locationTextView = findViewById(R.id.locationTextView);
        typeTextView = findViewById(R.id.typeTextView);
        statusTextView = findViewById(R.id.statusTextView);

        String cacheCode = getIntent().getStringExtra("cacheCode");

        // 使用 cacheCode 获取详细信息
        // 假设您从 API 获取数据并解析为 Geocache 对象
        Geocache geocache = fetchGeocacheDetails(cacheCode);

        codeTextView.setText(geocache.getCode());
        nameTextView.setText(geocache.getName());
        locationTextView.setText(geocache.getLocation());
        typeTextView.setText(geocache.getType());
        statusTextView.setText(geocache.getStatus());
    }

    // 假设 fetchGeocacheDetails 方法可以获取详细信息
    private Geocache fetchGeocacheDetails(String cacheCode) {
        return GeocacheFetcher.fetchGeocacheDetails(cacheCode);
    }


}
