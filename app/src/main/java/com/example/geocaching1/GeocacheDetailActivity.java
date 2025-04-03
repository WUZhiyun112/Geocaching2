package com.example.geocaching1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.CameraUpdateFactory;
import com.example.geocaching1.model.Geocache;
import com.example.geocaching1.utils.ApiClient;

import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeocacheDetailActivity extends AppCompatActivity {

    private TextView tvName, tvType, tvLocation, tvDescription, tvDifficulty, tvSize, tvStatus, tvDescriptionTitle, tvFoundStatus;
    private MapView mapView;
    private AMap aMap;
    private BigDecimal latitude, longitude;
    private Button btnNavigate;
    private Button btnMark;
    private boolean isRegistering = false;
    private CountingIdlingResource mIdlingResource = new CountingIdlingResource("GeocacheDetail");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocache_detail);


        // 初始化视图
        tvName = findViewById(R.id.tv_name);
        tvType = findViewById(R.id.tv_type);
        tvLocation = findViewById(R.id.tv_location);
        tvDescription = findViewById(R.id.tv_description);
        tvDifficulty = findViewById(R.id.tv_difficulty);
        tvSize = findViewById(R.id.tv_size);
        tvStatus = findViewById(R.id.tv_status);
        tvDescriptionTitle = findViewById(R.id.tv_description_title);
        mapView = findViewById(R.id.map_view);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);  // 假设你将用户ID保存在 SharedPreferences 中
        String token = prefs.getString("JWT_TOKEN", "");  // 获取JWT令牌
        String username = prefs.getString("USERNAME", "N/A");

        Log.d("DetailActivity Oncreate", "userId: " + userId);  // 打印用户ID
        Log.d("DetailActivity Oncreate", "JWT Token: " + token);  // 打印JWT令牌
        Log.d("DetailActivity Oncreate", "usernane: " + username);  // 打印JWT令牌


        // 地图生命周期管理
        mapView.onCreate(savedInstanceState);
        if (mapView != null) {
            aMap = mapView.getMap();
        }

        btnMark = findViewById(R.id.btn_mark);
        // 获取传递的 Geocache 对象
        Geocache geocache = getIntent().getParcelableExtra("geocache");

        btnMark.setOnClickListener(v -> {
            toggleMark(geocache);  // 调用 toggleMark 方法
        });

        TextView tvChangeFoundStatus = findViewById(R.id.tv_change_found_status);
        TextView tvFoundStatus = findViewById(R.id.tv_found_status);

        TextView btnComment = findViewById(R.id.btn_comment);
        btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(GeocacheDetailActivity.this, CommentsActivity.class);
            intent.putExtra("geocacheCode", geocache.getCode()); // 传递 geocache 的 code
            startActivity(intent);
        });

        tvChangeFoundStatus.setOnClickListener(v -> {
            // 获取 SharedPreferences
//            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//            int userId = prefs.getInt("USER_ID", -1);  // 假设你将用户ID保存在 SharedPreferences 中
//            String token = prefs.getString("JWT_TOKEN", "");  // 获取JWT令牌
//            String username = prefs.getString("USERNAME", "N/A");
//
//            Log.d("DetailActivity Oncreate", "userId: " + userId);  // 打印用户ID
//            Log.d("DetailActivity Oncreate", "JWT Token: " + token);  // 打印JWT令牌
//            Log.d("DetailActivity Oncreate", "usernane: " + username);  // 打印JWT令牌

            // 状态选项
            String[] options = {"Haven’t started", "Found it", "Searched but not found"};

            new AlertDialog.Builder(this)
                    .setTitle("Update Your Search Status")
                    .setItems(options, (dialog, which) -> {
                        String selectedStatus = options[which];

                        // 如果用户选择了 "Haven’t started"，直接 return，不做任何操作
                        if ("Haven’t started".equals(selectedStatus)) {
                            return;
                        }

                        // 更新 UI
                        tvFoundStatus.setText("My Progress: " + selectedStatus);

                        // 调用 API 更新状态
                        updateSearchStatus(selectedStatus);

                        // 保存状态已更新
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("STATUS_UPDATED", true);
                        editor.apply();
                    })
                    .show();
        });

        getFoundStatus();
        // 显示 Geocache 的详细信息
        if (geocache != null) {
            tvName.setText(geocache.getName());
            tvType.setText("Type: " + geocache.getType());

            // 获取纬度、经度
            latitude = geocache.getLatitude();
            longitude = geocache.getLongitude();
//            String locationAccurate = "位置: 纬度 " + latitude.toString() + ", 经度 " + longitude.toString();
//            tvLocation.setText(locationAccurate);

            tvDescriptionTitle.setText("Description");
            tvDescription.setText(Html.fromHtml(geocache.getDescription(), Html.FROM_HTML_MODE_LEGACY));

            tvDifficulty.setText("Difficulty: " + geocache.getDifficulty());
            tvSize.setText("Size: " + geocache.getSize());
            tvStatus.setText("Status: " + geocache.getStatus());

            btnNavigate = findViewById(R.id.btn_navigate);

            // 设置地图上的 geocache 位置
            if (latitude != null && longitude != null) {
                LatLng geocacheLocation = new LatLng(latitude.doubleValue(), longitude.doubleValue());
                aMap.addMarker(new MarkerOptions().position(geocacheLocation).title(geocache.getName()));
                aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(geocacheLocation, 14, 0, 0))); // 设置缩放级别 14
            }

            getAddressFromCoordinates(latitude.doubleValue(), longitude.doubleValue());

            // Set click listener for navigation
            btnNavigate.setOnClickListener(v -> {
                double latitudeValue = latitude.doubleValue();
                double longitudeValue = longitude.doubleValue();

                Intent intent = new Intent(GeocacheDetailActivity.this, RouteActivity.class);
                intent.putExtra("latitude", latitudeValue);
                intent.putExtra("longitude", longitudeValue);
                startActivity(intent);
            });

            // 禁用地图 UI 手势（可选）
            UiSettings uiSettings = aMap.getUiSettings();
            uiSettings.setZoomControlsEnabled(false); // 关闭缩放控件
            uiSettings.setScrollGesturesEnabled(false); // 允许滑动
        }


        checkMarkStatus(geocache, isMarked -> {
            if (isMarked == null) {
                // 如果获取关注状态失败，可以显示错误提示
                runOnUiThread(() ->
                        Toast.makeText(GeocacheDetailActivity.this, "获取关注状态失败", Toast.LENGTH_SHORT).show()
                );
            } else {
                // 根据返回的关注状态更新按钮文本
                if (isMarked) {
                    btnMark.setText("Marked");
                    btnMark.setBackgroundColor(Color.GRAY); // Gray background
                    btnMark.setTextColor(Color.WHITE); // White text
                } else {
                    btnMark.setText("Mark");
                    btnMark.setBackgroundColor(Color.parseColor("#396034")); // Green background
                    btnMark.setTextColor(Color.WHITE); // White text
                }
            }
        });
    }

    private void getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // 使用英文字段
                String addressText = address.getAddressLine(0); // 获取完整地址
                String city = address.getLocality(); // 获取所在城市
                String country = address.getCountryName(); // 获取国家

                // 如果没有城市信息，可以从国家信息中获取
                if (city == null || city.isEmpty()) {
                    city = "N/A"; // 如果没有找到城市信息，显示 N/A
                }
                if (country == null || country.isEmpty()) {
                    country = "N/A"; // 如果没有找到国家信息，显示 N/A
                }

                // 将地址、城市和国家信息转换为英文显示
                String locationText = "Location: " + addressText + "\nCity: " + city + "\nCountry: " + country +
                        "\nLatitude: " + latitude + "\nLongitude: " + longitude;

                tvLocation.setText(locationText);
            } else {
                String locationText = "Address not found\nLatitude: " + latitude + "\nLongitude: " + longitude;
                tvLocation.setText(locationText);
            }
        } catch (IOException e) {
            e.printStackTrace();
            String locationText = "Failed to get address\nLatitude: " + latitude + "\nLongitude: " + longitude;
            tvLocation.setText(locationText);
        }
    }


    private void saveMarkStatus(String geocacheCode, boolean isMarked) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("MARKED_" + geocacheCode, isMarked);  // 保存关注状态
        editor.apply();
    }

    private void toggleMark(Geocache geocache) {
        if (!isNetworkAvailable()) {
            runOnUiThread(() ->
                    Toast.makeText(this, "网络不可用，请检查连接", Toast.LENGTH_SHORT).show());
            return;
        }

        // 防止重复操作
        if (isRegistering) {
            runOnUiThread(() ->
                    Toast.makeText(this, "操作正在进行，请稍后重试！", Toast.LENGTH_SHORT).show());
            return;
        }

        isRegistering = true;  // 标记操作进行中

        // 从SharedPreferences中获取当前用户的ID和JWT令牌
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);  // 假设你将用户ID保存在 SharedPreferences 中
        String token = prefs.getString("JWT_TOKEN", "");  // 获取JWT令牌

        Log.d("ToggleMark", "userId: " + userId);  // 打印用户ID
        Log.d("ToggleMark", "JWT Token: " + token);  // 打印JWT令牌

        // 如果未登录，则提示登录
        if (userId == -1 || token.isEmpty()) {
            runOnUiThread(() ->
                    Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show());
            isRegistering = false;  // 操作完成
            return;
        }

        final String geocacheCode = geocache.getCode();  // 将geocacheCode声明为final
        final String[] geocacheName = {geocache.getName()};  // 将geocacheName声明为final
        final String[] geocacheType = {geocache.getType()};  // 将geocacheType声明为final
        final String[] location = {geocache.getLocation()};  // 将location声明为final

        Log.d("ToggleMark", "geocacheCode: " + geocacheCode);
        Log.d("ToggleMark", "geocacheName: " + geocacheName[0]);
        Log.d("ToggleMark", "geocacheType: " + geocacheType[0]);
        Log.d("ToggleMark", "location: " + location[0]);

// 获取关注状态并执行相应操作
        checkMarkStatus(geocache, isMarked -> {
            if (isMarked == null) {
                // 如果网络请求失败，标记操作完成
                runOnUiThread(() ->
                        Toast.makeText(GeocacheDetailActivity.this, "获取关注状态失败，请稍后重试", Toast.LENGTH_SHORT).show());
                isRegistering = false;
                return;
            }

            // 对 URL 中的特殊字符进行编码
            try {
                geocacheName[0] = URLEncoder.encode(geocacheName[0], "UTF-8");
                geocacheType[0] = URLEncoder.encode(geocacheType[0], "UTF-8");
                location[0] = URLEncoder.encode(location[0], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            String apiUrl = isMarked ?
                    "http://192.168.72.72:8080/api/mark/remove?userId=" + userId + "&geocacheCode=" + geocacheCode :
                    "http://192.168.72.72:8080/api/mark/add?userId=" + userId + "&geocacheCode=" + geocacheCode +
                            "&geocacheName=" + geocacheName[0] + "&geocacheType=" + geocacheType[0] + "&location=" + location[0];

            // 创建 OkHttpClient 实例并发送请求
            OkHttpClient client = ApiClient.getUnsafeOkHttpClient();

            // 根据关注状态选择合适的请求方法（POST 或 DELETE）
            Request request;
            if (isMarked) {
                // 如果已经关注，使用 DELETE 请求
                request = new Request.Builder()
                        .url(apiUrl)
                        .header("Authorization", "Bearer " + token)  // 在请求头中加入 Authorization
                        .delete()  // 直接使用 DELETE 方法
                        .build();
            } else {
                // 如果没有关注，使用 POST 请求并附加请求体
                JSONObject json = new JSONObject();
                try {
                    json.put("userId", userId);
                    json.put("geocacheCode", geocacheCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

                request = new Request.Builder()
                        .url(apiUrl)
                        .header("Authorization", "Bearer " + token)  // 在请求头中加入 Authorization
                        .post(body)  // 使用 POST 方法并附加请求体
                        .build();
            }

            // 发起请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(GeocacheDetailActivity.this, "网络错误，请重试！", Toast.LENGTH_SHORT).show();
                        isRegistering = false;  // 操作完成
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            // 更新按钮文本
                            if (!isMarked) {
                                btnMark.setText("Marked");
                                btnMark.setBackgroundColor(Color.GRAY); // Gray background
                                btnMark.setTextColor(Color.WHITE); // White text
                                saveMarkStatus(geocacheCode, true);  // 保存关注状态
                                runOnUiThread(() ->
                                        Toast.makeText(GeocacheDetailActivity.this, "关注成功", Toast.LENGTH_SHORT).show());
                            } else {
                                btnMark.setText("Mark");
                                btnMark.setBackgroundColor(Color.parseColor("#396034")); // Green background
                                btnMark.setTextColor(Color.WHITE); // White text
                                saveMarkStatus(geocacheCode, false);  // 保存取消关注状态
                                runOnUiThread(() ->
                                        Toast.makeText(GeocacheDetailActivity.this, "取消关注成功", Toast.LENGTH_SHORT).show());
                            }
                            isRegistering = false;  // 操作完成
                        });
                    } else {
                        runOnUiThread(() -> {
                            try {
                                String errorMessage = response.body() != null ? response.body().string() : "未知错误";
                                runOnUiThread(() ->
                                        Toast.makeText(GeocacheDetailActivity.this, "操作失败: " + errorMessage, Toast.LENGTH_SHORT).show());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            isRegistering = false;  // 操作完成
                        });
                    }
                }
            });
        });
    }

    private void checkMarkStatus(Geocache geocache, MarkStatusCallback callback) {
        // 发送请求获取关注状态
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);  // 获取用户ID
        String token = prefs.getString("JWT_TOKEN", "");  // 获取JWT令牌

        String geocacheCode = geocache.getCode();  // 获取 geocache 的 code
        String apiUrl = "http://192.168.72.72:8080/api/mark/list?userId=" + userId;

        OkHttpClient client = ApiClient.getUnsafeOkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + token)  // 在请求头中加入 Authorization
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onResult(null);  // 网络错误时回调
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        if (responseBody.isEmpty()) {
                            // 如果响应体为空，直接返回未关注状态
                            callback.onResult(false);
                            return;
                        }

                        JSONArray markList = new JSONArray(responseBody);
                        boolean isMarked = false;

                        // 遍历关注列表
                        for (int i = 0; i < markList.length(); i++) {
                            JSONObject markItem = markList.getJSONObject(i);
                            String markedGeocacheCode = markItem.getString("geocacheCode");
                            boolean marked = markItem.getBoolean("marked");

                            if (markedGeocacheCode.equals(geocacheCode) && marked) {
                                isMarked = true;
                                break;
                            }
                        }

                        callback.onResult(isMarked);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResult(null);
                    }
                } else {
                    callback.onResult(null);
                }
            }
        });
    }

    interface MarkStatusCallback {
        void onResult(Boolean isMarked);
    }

    private void updateSearchStatus(String status) {
        // 获取用户信息
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);
        String token = prefs.getString("JWT_TOKEN", "");

        // 获取 geocache 相关信息
        Geocache geocache = getIntent().getParcelableExtra("geocache");
        String geocacheCode = geocache.getCode();
        String geocacheName = geocache.getName();
        String geocacheType = geocache.getType();
        String location = geocache.getLocation();

        try {
            // 对可能包含特殊字符的字段进行 URL 编码
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8.toString());
            String encodedGeocacheName = URLEncoder.encode(geocacheName, StandardCharsets.UTF_8.toString());
            String encodedGeocacheType = URLEncoder.encode(geocacheType, StandardCharsets.UTF_8.toString());
            String encodedStatus = URLEncoder.encode(status, StandardCharsets.UTF_8.toString());

            // 仅在用户选择 "Found it" 时记录找到时间
            String foundAtParam = "";
            if ("Found it".equals(status)) {
                // 获取当前时间，使用 Calendar 获取
                Calendar calendar = Calendar.getInstance();
                long currentTimeMillis = calendar.getTimeInMillis();  // 获取当前时间的毫秒值

                // 转换为标准的时间字符串（比如：2025-03-13T15:30:00）
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                String formattedFoundAt = URLEncoder.encode(sdf.format(new Date(currentTimeMillis)), StandardCharsets.UTF_8.toString());
                foundAtParam = "&foundAt=" + formattedFoundAt;
            }

            // 构造请求 URL
            String apiUrl = "http://192.168.72.72:8080/api/foundstatus/set";

            // 创建 POST 请求的请求体
            String requestBody = "userId=" + userId +
                    "&geocacheCode=" + geocacheCode +
                    "&geocacheName=" + encodedGeocacheName +
                    "&geocacheType=" + encodedGeocacheType +
                    "&location=" + encodedLocation +
                    "&myStatus=" + encodedStatus +
                    foundAtParam;  // 只有 "Found it" 时才会添加这个参数

            // 创建 OkHttp 请求
            OkHttpClient client = ApiClient.getUnsafeOkHttpClient();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + token)  // 加入 Authorization 头
                    .post(RequestBody.create(requestBody, MediaType.get("application/x-www-form-urlencoded")))
                    .build();

            // 发起请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(GeocacheDetailActivity.this, "网络错误，请重试！", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(GeocacheDetailActivity.this, "状态更新成功", Toast.LENGTH_SHORT).show();

                            // 更新 UI 上的 "My Progress" 状态
                            TextView tvFoundStatus = findViewById(R.id.tv_found_status);
                            tvFoundStatus.setText("My Progress: " + status);

                            // 如果状态是 "Found it" 或 "Searched but not found"，立即隐藏 change 按钮
                            if ("Found it".equals(status) || "Searched but not found".equals(status)) {
                                findViewById(R.id.tv_change_found_status).setVisibility(View.GONE);
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(GeocacheDetailActivity.this, "状态更新失败，请重试", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(GeocacheDetailActivity.this, "编码错误，请重试", Toast.LENGTH_SHORT).show();
            });
        }
    }


    private void getFoundStatus() {
        // 获取用户信息
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);  // 假设用户 ID 存储在 SharedPreferences 中
        String token = prefs.getString("JWT_TOKEN", "");

        // 获取 geocacheCode（假设是通过 Intent 传递的）
        Geocache geocache = getIntent().getParcelableExtra("geocache");
        String geocacheCode = geocache.getCode();

        // 构造请求 URL
        String apiUrl = "http://192.168.72.72:8080/api/foundstatus/list?userId=" + userId;

        // 创建 OkHttp 请求
        OkHttpClient client = ApiClient.getUnsafeOkHttpClient();

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + token)  // 在请求头中加入 Authorization
                .get()  // 使用 GET 请求
                .build();

        // 发起请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(GeocacheDetailActivity.this, "网络错误，请重试！", Toast.LENGTH_SHORT).show();
                    updateStatusText("Haven't Started");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    if (responseBody == null || responseBody.trim().isEmpty()) {
                        runOnUiThread(() -> updateStatusText("Haven't Started"));
                        return;
                    }

                    try {
                        JSONArray jsonResponse = new JSONArray(responseBody);
                        String foundStatus = "Haven't Started";  // 默认状态
                        boolean shouldDisableChangeButton = false;

                        for (int i = 0; i < jsonResponse.length(); i++) {
                            JSONObject statusObj = jsonResponse.getJSONObject(i);
                            String code = statusObj.getString("geocacheCode");
                            int user = statusObj.getJSONObject("user").getInt("userId");
                            String status = statusObj.getString("status");

                            if (code.equals(geocacheCode) && user == userId) {
                                foundStatus = status;
                                Log.d("GeocacheDetail", "Match found! Status: " + foundStatus);

                                // 如果状态是 "Found it" 或 "Searched but not found"，禁止修改
                                if ("Found it".equals(foundStatus) || "Searched but not found".equals(foundStatus)) {
                                    shouldDisableChangeButton = true;
                                }
                                break;
                            }
                        }

                        boolean finalShouldDisableChangeButton = shouldDisableChangeButton;
                        String finalFoundStatus = foundStatus;

                        runOnUiThread(() -> {
                            updateStatusText(finalFoundStatus);

                            // 如果需要隐藏按钮
                            if (finalShouldDisableChangeButton) {
                                findViewById(R.id.tv_change_found_status).setVisibility(View.GONE);
                            }
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> updateStatusText("Haven't Started"));
                    }
                } else {
                    runOnUiThread(() -> updateStatusText("Haven't Started"));
                }
            }
        });
    }


    private void updateStatusText(String status) {
        TextView statusTextView = findViewById(R.id.tv_found_status);
        Log.d("GeocacheDetail", "Updating status text: " + status);
        statusTextView.setText("My Progress: " + status);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // 处理地图生命周期
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    public CountingIdlingResource getCountingIdlingResource() {
        return mIdlingResource;
    }

    // 在需要等待的操作前调用
    private void beginNetworkRequest() {
        mIdlingResource.increment();
    }

    // 在操作完成后调用
    private void endNetworkRequest() {
        if (!mIdlingResource.isIdleNow()) {
            mIdlingResource.decrement();
        }
    }
}
