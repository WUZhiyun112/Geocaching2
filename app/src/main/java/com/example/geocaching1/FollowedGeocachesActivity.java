package com.example.geocaching1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.geocaching1.adapter.FollowedGeocacheAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FollowedGeocachesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FollowedGeocacheAdapter adapter;
    private ProgressBar progressBar;
    private List<Geocache> followedGeocaches = new ArrayList<>();

    private static final String TAG = "FollowedGeocachesActivity";  // Log tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed_geocaches);

        Log.d(TAG, "FollowedGeocachesActivity - onCreate() called");

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // 初始化适配器
        adapter = new FollowedGeocacheAdapter(followedGeocaches);

        // 获取 SharedPreferences 中保存的用户 ID
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("USER_ID", -1);  // 获取保存的 userId

        // 如果用户未登录，提示并关闭当前 Activity
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();  // 结束当前 Activity
            return;
        }

        // 设置 RecyclerView 布局管理器
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 加载关注的 geocache 数据
        loadFollowedGeocaches(userId);  // 传递 userId 给加载方法
    }

    private void loadFollowedGeocaches(int userId) {
        Log.d(TAG, "FollowedGeocachesActivity - loadFollowedGeocaches() called");

        // 显示进度条
        progressBar.setVisibility(View.VISIBLE);

        // 构造 API URL
        String url = "http://192.168.226.72:8080/api/follow/list?userId=" + userId;
        Log.d(TAG, "FollowedGeocachesActivity - API URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);

        // 创建请求
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // 隐藏进度条
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "FollowedGeocachesActivity - Response received: " + response.toString());

                        // 清空原始数据
                        followedGeocaches.clear();

                        // 解析数据并填充到列表
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject geocache = response.getJSONObject(i);
                                String geocacheCode = geocache.getString("geocacheCode");
                                String userName = geocache.getJSONObject("user").getString("username");
                                String difficulty = geocache.getString("difficulty");

                                // 将获取的 geocache 数据添加到列表
                                followedGeocaches.add(new Geocache(geocacheCode, userName, difficulty));
                                Log.d(TAG, "FollowedGeocachesActivity - Geocache added: " + geocacheCode);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "FollowedGeocachesActivity - Error parsing geocache data: " + e.getMessage());
                            }
                        }

                        // 刷新适配器，通知数据已更新
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 隐藏进度条并显示错误消息
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "FollowedGeocachesActivity - Error response: " + error.toString());
                        Toast.makeText(FollowedGeocachesActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });

        // 将请求添加到队列
        queue.add(request);
    }
}
