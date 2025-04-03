package com.example.geocaching1;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geocaching1.adapter.CommentAdapter;
import com.example.geocaching1.model.Comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewComments;
    private Button btnAddComment;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private String geocacheCode;
    private Integer userId;
    private String username;
    private String token; // 存储用户 JWT 令牌
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        recyclerViewComments = findViewById(R.id.recyclerView_comments);
        btnAddComment = findViewById(R.id.btn_add_comment);

        // 获取 geocacheCode
        geocacheCode = getIntent().getStringExtra("geocacheCode");

        // 初始化 RecyclerView
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setAdapter(commentAdapter);

        // 获取用户信息
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);  // 假设你将用户ID保存在 SharedPreferences 中
        token = prefs.getString("JWT_TOKEN", "");  // 获取JWT令牌
        username = prefs.getString("USERNAME", "N/A");

        Log.d("DetailActivity Oncreate", "userId: " + userId);  // 打印用户ID
        Log.d("DetailActivity Oncreate", "JWT Token: " + token);  // 打印JWT令牌
        Log.d("DetailActivity Oncreate", "usernane: " + username);  // 打印JWT令牌

        client = new OkHttpClient(); // 初始化 OkHttpClient

        // 加载评论
        loadComments();

        // 添加评论按钮点击事件
        btnAddComment.setOnClickListener(v -> showAddCommentDialog());
    }

    private void loadComments() {
        if (geocacheCode == null) {
            Toast.makeText(this, "GeocacheCode is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiUrl = "http://192.168.72.72:8080/api/comments/geocache/" + geocacheCode;

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + token)
                .get()
                .build();

        Log.d("API Request", "URL: " + apiUrl);
        Log.d("API Request", "Token: " + token);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CommentsActivity.this, "网络错误，请重试！", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if (responseBody == null || responseBody.trim().isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(CommentsActivity.this, "暂无评论", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    try {
                        JSONArray jsonResponse = new JSONArray(responseBody);
                        List<Comment> comments = new ArrayList<>();

                        for (int i = 0; i < jsonResponse.length(); i++) {
                            JSONObject commentObj = jsonResponse.getJSONObject(i);
                            int commentUserId = commentObj.getInt("userId");
                            String commentGeocacheCode = commentObj.getString("geocacheCode");
                            String commentContent = commentObj.getString("content");
                            int rating = commentObj.getInt("rating");
                            String commentTime = commentObj.getString("commentTime");

                            // Get username from JSON
                            String username = commentObj.getString("username");

                            // Create Comment object
                            Comment comment = new Comment(commentUserId, username, commentGeocacheCode, rating, commentContent);

                            // Parse the commentTime to Date
                            comment.setCommentTime(parseCommentTime(commentTime));

                            comments.add(comment);
                        }

                        runOnUiThread(() -> {
                            commentList.clear();
                            commentList.addAll(comments);
                            commentAdapter.notifyDataSetChanged();
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(CommentsActivity.this, "数据解析失败", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(CommentsActivity.this, "获取评论失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void showAddCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Comment");

        // 加载布局文件
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_comment, null);
        EditText etCommentContent = dialogView.findViewById(R.id.et_comment_content);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);

        builder.setView(dialogView);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String commentContent = etCommentContent.getText().toString().trim();
            int rating = (int) ratingBar.getRating();

            if (commentContent.isEmpty()) {
                Toast.makeText(this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建评论对象
            Comment comment = new Comment(userId, username, geocacheCode, rating, commentContent);

            // 保存评论
            saveComment(comment);
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }


    private Date parseCommentTime(String commentTime) {
        if (commentTime == null || commentTime.isEmpty()) {
            return new Date();
        }

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss",    // ISO 8601
                "yyyy-MM-dd HH:mm:ss",      // Standard format
                "yyyy-MM-dd HH:mm",         // Without seconds
                "yyyy-MM-dd"                // Date only
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
                return sdf.parse(commentTime);
            } catch (ParseException e) {
                // Try next format
            }
        }
        return new Date();
    }
    private void saveComment(Comment comment) {
        String apiUrl = "http://192.168.72.72:8080/api/comments/add";


        JSONObject json = new JSONObject();
        try {
            json.put("userId", comment.getUserId());
            json.put("geocacheCode", comment.getGeocacheCode());
            json.put("rating", comment.getRating());
            json.put("content", comment.getContent());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("saveComment", "Sending JSON: " + json.toString());
        Log.d("saveComment", "Token: " + token);
        RequestBody requestBody = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CommentsActivity.this, "提交失败，请重试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("saveComment", "Response Code: " + response.code());  // 打印响应码
                Log.d("saveComment", "Response Body: " + responseBody);  // 打印响应体内容

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(CommentsActivity.this, "评论提交成功！", Toast.LENGTH_SHORT).show();
                        loadComments(); // 重新加载评论
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(CommentsActivity.this, "评论提交失败: " + responseBody, Toast.LENGTH_SHORT).show());
                }
            }

        });
    }
}
