package com.example.geocaching1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.CameraUpdateFactory;
import com.example.geocaching1.utils.ApiClient;

import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeocacheDetailActivity extends AppCompatActivity {

    private TextView tvName, tvType, tvLocation, tvDescription, tvDifficulty, tvSize, tvStatus, tvDescriptionTitle;
    private MapView mapView;
    private AMap aMap;
    private BigDecimal latitude, longitude;
    private Button btnNavigate;
    private Button btnFollow;
    private boolean isRegistering = false;


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

        // 地图生命周期管理
        mapView.onCreate(savedInstanceState);
        if (mapView != null) {
            aMap = mapView.getMap();
        }

        btnFollow = findViewById(R.id.btn_follow);
        // 获取传递的 Geocache 对象
        Geocache geocache = getIntent().getParcelableExtra("geocache");

        btnFollow.setOnClickListener(v -> {
            toggleFollow(geocache);  // 调用 toggleFollow 方法
        });


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

        checkFollowStatus(geocache);
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
    private void toggleFollow(Geocache geocache) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "网络不可用，请检查连接", Toast.LENGTH_SHORT).show();
            return;
        }

        // 防止重复操作
        if (isRegistering) {
            Toast.makeText(this, "操作正在进行，请稍后重试！", Toast.LENGTH_SHORT).show();
            return;
        }

        isRegistering = true;  // 标记操作进行中

        // 从SharedPreferences中获取当前用户的ID和JWT令牌
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);  // 假设你将用户ID保存在 SharedPreferences 中
        String token = prefs.getString("JWT_TOKEN", "");  // 获取JWT令牌

        Log.d("ToggleFollow", "userId: " + userId);  // 打印用户ID
        Log.d("ToggleFollow", "JWT Token: " + token);  // 打印JWT令牌

        // 如果未登录，则提示登录
        if (userId == -1 || token.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            isRegistering = false;  // 操作完成
            return;
        }

        String geocacheCode = geocache.getCode();  // 获取 geocache 的 code
        boolean isFollowed = getFollowStatus(geocacheCode);  // 获取关注状态
        Log.d("ToggleFollow", "geocacheCode: " + geocacheCode);
        // 根据关注状态选择API URL
        String apiUrl = isFollowed ?
                "http://192.168.226.72:8080/api/follow/remove?userId=" + userId + "&geocacheCode=" + geocacheCode :
                "http://192.168.226.72:8080/api/follow/add?userId=" + userId + "&geocacheCode=" + geocacheCode;

        // 创建 OkHttpClient 实例并发送请求
        OkHttpClient client = ApiClient.getUnsafeOkHttpClient();

        // 根据关注状态选择合适的请求方法（POST 或 DELETE）
        Request request;
        if (isFollowed) {
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
                        if (!isFollowed) {
                            btnFollow.setText("Unfollow");
                            saveFollowStatus(geocacheCode, true);  // 保存关注状态
                            Toast.makeText(GeocacheDetailActivity.this, "关注成功", Toast.LENGTH_SHORT).show();
                        } else {
                            btnFollow.setText("Follow");
                            saveFollowStatus(geocacheCode, false);  // 保存取消关注状态
                            Toast.makeText(GeocacheDetailActivity.this, "取消关注成功", Toast.LENGTH_SHORT).show();
                        }
                        isRegistering = false;  // 操作完成
                    });
                } else {
                    runOnUiThread(() -> {
                        try {
                            String errorMessage = response.body() != null ? response.body().string() : "未知错误";
                            Toast.makeText(GeocacheDetailActivity.this, "操作失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        isRegistering = false;  // 操作完成
                    });
                }
            }
        });
    }



    private boolean getFollowStatus(String geocacheCode) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("FOLLOWED_" + geocacheCode, false);  // 默认值为false
    }

    private void saveFollowStatus(String geocacheCode, boolean isFollowed) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("FOLLOWED_" + geocacheCode, isFollowed);  // 保存关注状态
        editor.apply();
    }
    private void checkFollowStatus(Geocache geocache) {
        // 从SharedPreferences中获取当前用户的ID和JWT令牌
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);  // 假设你将用户ID保存在 SharedPreferences 中
        String token = prefs.getString("JWT_TOKEN", "");  // 获取JWT令牌

        // 如果未登录，则提示登录
        if (userId == -1 || token.isEmpty()) {
            btnFollow.setText("Follow");
            return;
        }

        String geocacheCode = geocache.getCode();  // 获取 geocache 的 code
        String apiUrl = "http://192.168.226.72:8080/api/follow/list?userId=" + userId;

        // 创建 OkHttpClient 实例并发送请求
        OkHttpClient client = ApiClient.getUnsafeOkHttpClient();

        // 发起 GET 请求检查用户是否已关注该 geocache
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + token)  // 在请求头中加入 Authorization
                .build();

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
                    String responseBody = response.body() != null ? response.body().string() : "";

                    try {
                        // 解析返回的 JSON 数据
                        JSONArray followList = new JSONArray(responseBody);

                        // 使用 AtomicBoolean 替代 boolean 以便在 lambda 表达式中修改
                        AtomicBoolean isFollowed = new AtomicBoolean(false);

                        // 遍历关注的 geocache 列表
                        for (int i = 0; i < followList.length(); i++) {
                            JSONObject followItem = followList.getJSONObject(i);
                            String followedGeocacheCode = followItem.getString("geocacheCode");
                            boolean followed = followItem.getBoolean("followed");

                            // 判断该 geocache 是否已被关注
                            if (followedGeocacheCode.equals(geocacheCode) && followed) {
                                isFollowed.set(true);  // 使用 AtomicBoolean 设置值
                                break;
                            }
                        }

                        // 更新 UI
                        runOnUiThread(() -> {
                            if (isFollowed.get()) {  // 获取 AtomicBoolean 的值
                                btnFollow.setText("Unfollow");
                            } else {
                                btnFollow.setText("Follow");
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(GeocacheDetailActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
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
}
