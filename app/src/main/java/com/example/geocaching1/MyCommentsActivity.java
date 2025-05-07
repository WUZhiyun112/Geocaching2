package com.example.geocaching1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geocaching1.adapter.MyCommentsAdapter;
import com.example.geocaching1.model.Comment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyCommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyCommentsAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private Integer userId;
    private String token;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_comments);

        recyclerView = findViewById(R.id.recyclerView_my_comments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyCommentsAdapter(new ArrayList<>()); // 初始化 adapter
        recyclerView.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // 打印 SharedPreferences 中的所有键值对
        Map<String, ?> allPrefs = prefs.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            Log.d("MyCommentsActivity", "SharedPreferences Key: " + entry.getKey() + ", Value: " + entry.getValue().toString());
        }

        userId = prefs.getInt("USER_ID", -1);
        token = prefs.getString("JWT_TOKEN", "");

        Log.d("MyCommentsActivity", "USER_ID: " + userId);
        Log.d("MyCommentsActivity", "JWT_TOKEN: " + token);

        client = new OkHttpClient();

        loadUserComments();
    }


    private void loadUserComments() {
        if (userId == -1) {
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiUrl = "http://192.168.189.72:8080/api/comments/user/" + userId;

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MyCommentsActivity.this, "Network error, please try again!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("MyCommentsActivity", "Response: " + responseBody);
                    if (responseBody == null || responseBody.trim().isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(MyCommentsActivity.this, "No comments found", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    try {
                        JSONArray jsonResponse = new JSONArray(responseBody);
                        List<Comment> comments = new ArrayList<>();

                        for (int i = 0; i < jsonResponse.length(); i++) {
                            JSONObject commentObj = jsonResponse.getJSONObject(i);
                            int commentUserId = commentObj.getInt("userId");
                            String geocacheCode = commentObj.getString("geocacheCode");
                            String content = commentObj.getString("content");
                            int rating = commentObj.getInt("rating");
                            String commentTime = commentObj.getString("commentTime");

                            Comment comment = new Comment(commentUserId, commentObj.getString("username"), geocacheCode, rating, content);
                            comment.setCommentTime(parseCommentTime(commentTime));

                            comments.add(comment);
                        }

                        runOnUiThread(() -> {
                            adapter.setComments(comments); // 直接更新 adapter 的数据
                        });


                    } catch (JSONException e) {
                        Log.d("MyCommentsActivity", "Response failed with status code: " + response.code());
                        runOnUiThread(() -> Toast.makeText(MyCommentsActivity.this, "Failed to parse data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.d("MyCommentsActivity", "Response failed with status code: " + response.code());
                    runOnUiThread(() -> Toast.makeText(MyCommentsActivity.this, "Failed to retrieve comments", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private Date parseCommentTime(String commentTime) {
        // Adjust the date format to match the ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            return formatter.parse(commentTime);  // This should now work with the correct format
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();  // Return current date if parsing fails
        }
    }
}

