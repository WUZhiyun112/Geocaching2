package com.example.geocaching1.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geocaching1.R;
import com.example.geocaching1.adapter.GeocacheAdapter;
import com.example.geocaching1.model.Geocache;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchedNotFoundGeocachesFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_JWT_TOKEN = "jwt_token";

    private String jwtToken;
    private int userId;
    private RecyclerView recyclerView;
    private TextView tvNoGeocache;
    private GeocacheAdapter adapter;

    public static SearchedNotFoundGeocachesFragment newInstance(int userId, String jwtToken) {
        SearchedNotFoundGeocachesFragment fragment = new SearchedNotFoundGeocachesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putString(ARG_JWT_TOKEN, jwtToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geocache_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvNoGeocache = view.findViewById(R.id.tv_no_geocache);

        recyclerView.setVisibility(View.GONE);
        tvNoGeocache.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        userId = sharedPreferences.getInt("USER_ID", -1); // 赋值给成员变量
        jwtToken = sharedPreferences.getString("JWT_TOKEN", null); // 赋值给成员变量

        if (userId == -1 || jwtToken == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
            getActivity().finish(); // 结束当前 Activity
            return null;  // return null, 避免继续执行后面的代码
        }

        Log.d("SearchedNotFoundGeocachesFragment", "JWT Token: " + jwtToken);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GeocacheAdapter(requireContext());

        // 设置长按监听器
        adapter.setOnItemLongClickListener((position, geocache) -> {
            showStatusChangeDialog(geocache);
            return true;
        });

        recyclerView.setAdapter(adapter);

        return view;
    }
    private void showStatusChangeDialog(Geocache geocache) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Change Status")
                .setMessage("Change the status from 'Searched but not found' to 'Found it'?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    updateStatusToFound(geocache);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateStatusToFound(Geocache geocache) {
        // 修改为PUT请求
        String apiUrl = "http://192.168.147.72:8080/api/foundstatus/set";  // 如果需要更改为 PUT 方法，后端支持 PUT 请求

        // 获取当前时间作为foundAt
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String foundAt = sdf.format(new Date());

        // 创建请求参数
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("userId", String.valueOf(userId))
                .add("geocacheCode", geocache.getCode())
                .add("geocacheName", geocache.getName())
                .add("geocacheType", geocache.getType())
                .add("location", geocache.getLocation())
                .add("myStatus", "Found it")  // 更新状态为 "Found it"
                .add("foundAt", foundAt)  // 设置 foundAt 时间
                .build();

        // 使用PUT方法
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + jwtToken)
                .put(formBody)  // 使用PUT方法
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "更新失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "状态已更新", Toast.LENGTH_SHORT).show();
                        loadData(); // 刷新数据
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "更新失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private List<Geocache> parseResponse(String responseData) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Geocache>>() {}.getType();
        return gson.fromJson(responseData, listType);
    }
    private void loadData() {
        // 重新加载数据的方法
        String apiUrl = "http://192.168.147.72:8080/api/foundstatus/list?userId=" + userId;

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("loadData", "Error loading data", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    List<Geocache> geocaches = parseResponse(responseData); // 解析 JSON
                    requireActivity().runOnUiThread(() -> updateGeocaches(geocaches));
                }
            }

        });
    }
    public void updateGeocaches(List<Geocache> newGeocaches) {
        if (newGeocaches != null && !newGeocaches.isEmpty()) {
            Log.d("NotFoundFragment", "Updating with " + newGeocaches.size() + " items");

            // 创建反转后的列表
            List<Geocache> reversedList = new ArrayList<>(newGeocaches);
            Collections.reverse(reversedList);

            // 使用适配器的setData方法更新数据（使用反转后的列表）
            adapter.setData(reversedList);

            // 更新UI状态
            recyclerView.setVisibility(View.VISIBLE);
            tvNoGeocache.setVisibility(View.GONE);
        } else {
            Log.d("NotFoundFragment", "No data available");

            // 清空数据并显示无数据提示
            adapter.setData(new ArrayList<>());
            recyclerView.setVisibility(View.GONE);
            tvNoGeocache.setVisibility(View.VISIBLE);
        }
    }
}