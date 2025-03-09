package com.example.geocaching1;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

public class GeocacheFetcher {
    private static final String API_KEY = "baxewkyrs3UzEBF64PY3"; // Your API Key
    private static final String SECRET_KEY = "yV7kv9ESQbW7TzWsm9B2BmcUQwzMvBMZ898ZkxCk";
    private static final String API_URL = "https://www.opencaching.de/okapi/services/caches/search/nearest";

    // 原有的 fetchGeocaches 方法
    public static String fetchGeocaches(double latitude, double longitude) {
        HttpURLConnection conn = null;
        BufferedReader in = null;

        try {
            // 构建请求 URL
            String requestUrl = API_URL + "?consumer_key=" + API_KEY +
                    "&center=" + latitude + "|" + longitude +
                    "&limit=50";

            Log.d("GeocacheFetcher", "Request URL: " + requestUrl);

            // 打开连接
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 设置连接超时（毫秒）
            conn.setReadTimeout(5000);    // 设置读取超时（毫秒）

            // 获取响应码
            int responseCode = conn.getResponseCode();

            // 检查响应是否成功
            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                // 读取响应
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                Log.d("GeocacheFetcher", "API Response: " + response);
                return response.toString(); // 返回响应字符串
            } else {
                Log.e("GeocacheFetcher", "Error: HTTP Response Code " + responseCode);
                return "Error: HTTP Response Code " + responseCode;
            }
        } catch (Exception e) {
            Log.e("GeocacheFetcher", "Exception occurred: " + e.getMessage(), e);
            return "Exception: " + e.getMessage();
        } finally {
            // 确保资源关闭
            try {
                if (in != null) {
                    in.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                Log.e("GeocacheFetcher", "Error closing resources: " + e.getMessage(), e);
            }
        }
    }


    // 继续保留原来获取 Geocache 详情的方法
    public static Geocache fetchGeocacheDetails(String cacheCode) {
        try {
            String requestUrl = "https://www.opencaching.de/okapi/services/caches/geocache" +
                    "?consumer_key=" + API_KEY +
                    "&cache_code=" + cacheCode +
                    "&fields=code|name|location|type|status|difficulty|size|description" +
                    "&format=json";

            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Log.d("GeocacheDetails", "Details Response: " + response);

                // Parse JSON response into a Geocache object
                return parseGeocacheDetails(response.toString());
            } else {
                Log.e("GeocacheDetails", "Error Response Code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e("GeocacheDetails", "Exception: " + e.getMessage(), e);
            return null;
        }
    }

    public static Geocache parseGeocacheDetails(String jsonResponse) {
        try {
            JSONObject cacheObject = new JSONObject(jsonResponse);

            if (cacheObject.has("code")) {
                Log.d("GeocacheFetcher", "Parsed JSON: " + jsonResponse);

                String code = cacheObject.getString("code");
                String name = cacheObject.getString("name");
                String status = cacheObject.getString("status");
                String type = cacheObject.getString("type");

                String[] location = cacheObject.getString("location").split("\\|");
                BigDecimal latitude = new BigDecimal(location[0]);
                BigDecimal longitude = new BigDecimal(location[1]);

                // 获取新的字段
                String description = cacheObject.getString("description");
                String size = cacheObject.getString("size");
                String difficulty = cacheObject.getString("difficulty");

                // 返回包含新字段的 Geocache 对象
                return new Geocache(code, name, latitude, longitude, status, type, new Date(), description, size, difficulty);
            }
        } catch (Exception e) {
            Log.e("GeocacheFetcher", "JSON Parsing Error: ", e);
        }
        return null;
    }


    public static List<Geocache> parseGeocaches(String jsonResponse) {
        List<Geocache> geocacheList = new ArrayList<>();
        try {
            // 将 JSON 字符串解析为 JSON 对象
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // 获取缓存列表数组
            JSONArray cachesArray = jsonObject.getJSONArray("results");

            // 创建日期解析格式
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // 解析每个缓存数据
            for (int i = 0; i < cachesArray.length(); i++) {
                JSONObject cacheObject = cachesArray.getJSONObject(i);
                String code = cacheObject.getString("code");
                String name = cacheObject.getString("name");

                // 解析经纬度值，将字符串转换为 BigDecimal
                BigDecimal longitude = new BigDecimal(cacheObject.getString("longitude"));
                BigDecimal latitude = new BigDecimal(cacheObject.getString("latitude"));

                String type = cacheObject.getString("type");
                String status = cacheObject.getString("status");

                // 获取新的字段
                String description = cacheObject.getString("description");
                String size = cacheObject.getString("size");
                String difficulty = cacheObject.getString("difficulty");

                // 解析 foundAt 时间
                String foundAtString = cacheObject.getString("foundAt");
                Date foundAt = null;
                try {
                    foundAt = formatter.parse(foundAtString); // 解析字符串为 Date
                } catch (Exception e) {
                    Log.e("GeocacheFetcher", "Date parsing error: " + e.getMessage());
                }

                // 创建 Geocache 对象
                Geocache geocache = new Geocache(code, name, latitude, longitude, status, type, foundAt, description, size, difficulty);

                geocacheList.add(geocache);
            }
        } catch (Exception e) {
            Log.e("GeocacheFetcher", "Error parsing JSON response: " + e.getMessage());
        }
        return geocacheList;
    }





}
