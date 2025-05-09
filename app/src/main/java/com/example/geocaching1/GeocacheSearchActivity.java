package com.example.geocaching1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.geocaching1.model.Geocache;
import com.example.geocaching1.R;
import com.example.geocaching1.adapter.GeocacheSearchPagerAdapter;
import com.example.geocaching1.fragment.FoundGeocachesFragment;
import com.example.geocaching1.fragment.SearchedNotFoundGeocachesFragment;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeocacheSearchActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private GeocacheSearchPagerAdapter adapter;
    private TabLayout tabLayout;
    private int userId;
    private String jwtToken;
    private List<Geocache> foundGeocaches = new ArrayList<>();
    private List<Geocache> notFoundGeocaches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocache_search);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("USER_ID", -1); // 赋值给成员变量
        jwtToken = sharedPreferences.getString("JWT_TOKEN", null); // 赋值给成员变量

        if (userId == -1 || jwtToken == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish(); // 结束当前 Activity
            return;
        }
        Log.d("GeocacheSearchActivity", "JWT Token: " + jwtToken);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        adapter = new GeocacheSearchPagerAdapter(getSupportFragmentManager(), userId, jwtToken);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager); // 绑定 ViewPager

        loadGeocachesFromAPI();
    }

    private void loadGeocachesFromAPI() {
        String url = "http://192.168.189.72:8080/api/foundstatus/list?userId=" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    foundGeocaches.clear();
                    notFoundGeocaches.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String foundAtValue = obj.isNull("foundAt") ? "N/A" : obj.getString("foundAt");
                            Log.d("JSON_PARSING", "Code: " + obj.getString("geocacheCode")
                                    + " | Raw foundAt: " + obj.optString("foundAt", "NULL")
                                    + " | Parsed: " + foundAtValue);

                            Geocache geocache = new Geocache(
                                    obj.getString("geocacheCode"),
                                    obj.getString("geocacheName"),
                                    obj.getString("geocacheType"),
                                    obj.getString("location"),
                                    foundAtValue,
                                    obj.getString("status")
                            );

                            String status = obj.getString("status");
                            if ("Found it".equals(status)) {
                                foundGeocaches.add(geocache);
                            } else if ("Searched but not found".equals(status)) {
                                notFoundGeocaches.add(geocache);
                            }
                        } catch (JSONException e) {
                            Log.e("GeocacheSearchActivity", "JSON Parsing Error: " + e.getMessage());
                        }
                    }

                    // Update the fragments with the fetched data
                    updateFragments();
                    Log.d("GeocacheSearchActivity", "API Response: " + response.toString());

                },
                error -> Log.e("GeocacheSearchActivity", "Request Error: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + jwtToken);
                return headers;
            }
        };

        queue.add(request);
    }

    // GeocacheSearchActivity.java
    private void updateFragments() {
        // 使用防御性拷贝
        List<Geocache> safeFoundList = new ArrayList<>(foundGeocaches);
        List<Geocache> safeNotFoundList = new ArrayList<>(notFoundGeocaches);

        // 更新 FoundGeocachesFragment
        FoundGeocachesFragment foundFragment = (FoundGeocachesFragment)
                getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":0");
        if (foundFragment != null) {
            foundFragment.updateGeocaches(safeFoundList);
        }

        // 更新 SearchedNotFoundGeocachesFragment
        SearchedNotFoundGeocachesFragment notFoundFragment = (SearchedNotFoundGeocachesFragment)
                getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":1");
        if (notFoundFragment != null) {
            notFoundFragment.updateGeocaches(safeNotFoundList);
        }
    }

}