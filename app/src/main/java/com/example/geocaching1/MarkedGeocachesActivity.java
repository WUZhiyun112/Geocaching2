package com.example.geocaching1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.geocaching1.adapter.MarkedGeocacheAdapter;
import com.example.geocaching1.model.Geocache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkedGeocachesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MarkedGeocacheAdapter adapter;
    private ProgressBar progressBar;
    private List<Geocache> markedGeocaches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marked_geocaches);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // 初始化适配器
        adapter = new MarkedGeocacheAdapter(markedGeocaches, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);

        // 获取用户信息
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("USER_ID", -1);
        String jwtToken = sharedPreferences.getString("JWT_TOKEN", null);

        if (userId == -1 || jwtToken == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish(); // 结束当前 Activity
            return;
        }

        // 加载关注的 Geocache 数据
        loadMarkedGeocaches(userId, jwtToken);
    }

    private void loadMarkedGeocaches(int userId, String jwtToken) {
        progressBar.setVisibility(View.GONE);

        String url = "http://192.168.98.72:8080/api/mark/list?userId=" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    markedGeocaches.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject geocacheJson = response.getJSONObject(i);
                            markedGeocaches.add(new Geocache(
                                    geocacheJson.getString("geocacheCode"),
                                    geocacheJson.optString("geocacheName", "N/A"),
                                    geocacheJson.optString("geocacheType", "N/A"),
                                    geocacheJson.optString("location", "N/A")
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "加载数据失败", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + jwtToken);
                return headers;
            }
        };

        queue.add(request);
    }
}