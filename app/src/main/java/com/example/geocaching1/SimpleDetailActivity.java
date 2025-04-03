package com.example.geocaching1;

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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SimpleDetailActivity extends AppCompatActivity {

    private TextView tvName, tvType, tvLocation, tvCode;
    private MapView mapView;
    private AMap aMap;
    private BigDecimal latitude, longitude;
    private Button btnNavigate;
    private Button btnMark;
    private boolean isRegistering = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_detail);

        // 初始化视图
        tvName = findViewById(R.id.tv_name);
        tvType = findViewById(R.id.tv_type);
        tvLocation = findViewById(R.id.tv_location);
        tvCode = findViewById(R.id.tv_code);
        mapView = findViewById(R.id.map_view);

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


// 显示 Geocache 的详细信息
        if (geocache != null) {
            tvName.setText(geocache.getName());
            tvType.setText("Type: " + geocache.getType());
            tvCode.setText("Code: " + geocache.getCode());

            // 获取纬度、经度
            latitude = geocache.getLatitude();
            longitude = geocache.getLongitude();



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

                Intent intent = new Intent(SimpleDetailActivity.this, RouteActivity.class);
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
                Toast.makeText(SimpleDetailActivity.this, "获取关注状态失败", Toast.LENGTH_SHORT).show();
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

        Log.d("ToggleMark", "userId: " + userId);  // 打印用户ID
        Log.d("ToggleMark", "JWT Token: " + token);  // 打印JWT令牌

        // 如果未登录，则提示登录
        if (userId == -1 || token.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SimpleDetailActivity.this, "获取关注状态失败，请稍后重试", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SimpleDetailActivity.this, "网络错误，请重试！", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(SimpleDetailActivity.this, "关注成功", Toast.LENGTH_SHORT).show();
                            } else {
                                btnMark.setText("Mark");
                                btnMark.setBackgroundColor(Color.parseColor("#396034")); // Green background
                                btnMark.setTextColor(Color.WHITE); // White text
                                saveMarkStatus(geocacheCode, false);  // 保存取消关注状态
                                Toast.makeText(SimpleDetailActivity.this, "取消关注成功", Toast.LENGTH_SHORT).show();
                            }
                            isRegistering = false;  // 操作完成
                        });
                    } else {
                        runOnUiThread(() -> {
                            try {
                                String errorMessage = response.body() != null ? response.body().string() : "未知错误";
                                Toast.makeText(SimpleDetailActivity.this, "操作失败: " + errorMessage, Toast.LENGTH_SHORT).show();
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
