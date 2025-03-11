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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FollowedGeocachesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FollowedGeocacheAdapter adapter;
    private ProgressBar progressBar;
    private List<Geocache> followedGeocaches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed_geocaches);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Initialize the adapter
        adapter = new FollowedGeocacheAdapter(followedGeocaches, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Get user info from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("USER_ID", -1);
        String jwtToken = sharedPreferences.getString("JWT_TOKEN", null);

        if (userId == -1 || jwtToken == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        // Load followed geocaches
        loadFollowedGeocaches(userId, jwtToken);
    }

    private void loadFollowedGeocaches(int userId, String jwtToken) {
        progressBar.setVisibility(View.VISIBLE);

        String url = "http://192.168.226.72:8080/api/follow/list?userId=" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    followedGeocaches.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject geocache = response.getJSONObject(i);
                            followedGeocaches.add(new Geocache(
                                    geocache.getString("geocacheCode"),
                                    geocache.optString("geocacheName", "N/A"),
                                    geocache.optString("geocacheType", "N/A"),
                                    geocache.optString("location", "N/A")
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
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
