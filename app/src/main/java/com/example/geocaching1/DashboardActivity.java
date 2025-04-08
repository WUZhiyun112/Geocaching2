package com.example.geocaching1;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geocaching1.model.Geocache;
import com.example.geocaching1.R;
import com.example.geocaching1.adapter.MarkedGeocacheAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;


public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MarkedGeocacheAdapter.GeocacheAdapter geocacheAdapter;
    private List<Geocache> geocacheList = new ArrayList<>();
    private List<Geocache> filteredGeocacheList = new ArrayList<>();  // 存储筛选后的数据

    private Spinner typeSpinner;
    private Spinner statusSpinner;
    private Spinner distanceSpinner;
    private int currentPage = 0; // 当前页码
    private int pageSize = 10;   // 每页加载的数据量
    private boolean isLoading = false; // 是否正在加载数据
    private boolean hasMoreData = true; // 是否还有更多数据可以加载

    // 假设的筛选条件
    private String selectedType = null;
    private String selectedStatus = null;
    private String selectedDistance = null;

    // 获取筛选条件数组
    private String[] types;
    private String[] statuses;
    private String[] distances;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private double distanceInMeters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 初始化筛选条
        initFilterBar();

        // 初始化 RecyclerView
        initRecyclerView();

        // 从 Intent 加载数据
        loadGeocacheData();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    Log.d("Location", "Lat: " + currentLatitude + ", Lon: " + currentLongitude);
                  filterData();
                }
            });
        }


    }



    /**
     * 初始化筛选条
     */
    private void initFilterBar() {
        // Initialize spinners
        MaterialAutoCompleteTextView typeSpinner = findViewById(R.id.type_spinner);
        MaterialAutoCompleteTextView statusSpinner = findViewById(R.id.status_spinner);
        MaterialAutoCompleteTextView distanceSpinner = findViewById(R.id.distance_spinner);

        // Initialize arrays
        types = getResources().getStringArray(R.array.type_array);
        statuses = getResources().getStringArray(R.array.status_array);
        distances = getResources().getStringArray(R.array.distance_array);

        // Set up adapters
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, types);
        typeAdapter.setDropDownViewResource(R.layout.dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, statuses);
        statusAdapter.setDropDownViewResource(R.layout.dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Add this for distance spinner
        ArrayAdapter<String> distanceAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, distances);
        distanceAdapter.setDropDownViewResource(R.layout.dropdown_item);
        distanceSpinner.setAdapter(distanceAdapter);
//        distanceSpinner.setText(distances[0], false);

        // Set listeners
        typeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedType = position == 0 ? null : types[position];
            filterData();
        });

        statusSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatus = position == 0 ? null : statuses[position];
            filterData();
        });

        distanceSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedDistance = position == 0 ? null : distances[position];
            filterData();
        });
    }



    /**
     * 初始化 RecyclerView
     */
    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化适配器
        geocacheAdapter = new MarkedGeocacheAdapter.GeocacheAdapter(DashboardActivity.this, filteredGeocacheList);
        recyclerView.setAdapter(geocacheAdapter);

        // 添加滚动监听器
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // 判断是否滚动到底部
                    if (!isLoading && hasMoreData) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= pageSize) {
                            loadMoreData(); // 加载更多数据
                        }
                    }
                }
            }
        });
    }

    private void loadMoreData() {
        if (!hasMoreData || isLoading) return;

        isLoading = true; // 标记正在加载
        geocacheAdapter.notifyItemInserted(geocacheList.size()); // 显示加载中

        new Handler().postDelayed(() -> {
            List<Geocache> newData = fetchDataFromSource(currentPage + 1, pageSize);
            if (newData != null && !newData.isEmpty()) {
                currentPage++; // 只有成功获取数据才增加页码
                geocacheAdapter.updateData(newData, false);
            } else {
                hasMoreData = false; // 标记无更多数据
            }
            isLoading = false;
        }, 1000);
    }


    private List<Geocache> fetchDataFromSource(int page, int pageSize) {
        // 这里模拟从数据源中获取数据
        List<Geocache> data = new ArrayList<>();
        int start = page * pageSize;
        int end = Math.min(start + pageSize, geocacheList.size());

        if (start < geocacheList.size()) {
            for (int i = start; i < end; i++) {
                data.add(geocacheList.get(i));
            }
        }

        return data;
    }

    /**
     * 从 Intent 加载数据
     */
    private void loadGeocacheData() {
        if (getIntent() != null && getIntent().hasExtra("geocacheList")) {
            geocacheList = getIntent().getParcelableArrayListExtra("geocacheList");
            if (geocacheList != null && !geocacheList.isEmpty()) {
                // 只加载第一页数据
                filteredGeocacheList.addAll(fetchDataFromSource(currentPage, pageSize));
                geocacheAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "No geocache data available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to load geocache data", Toast.LENGTH_SHORT).show();
        }
    }

//    private void filterData() {
//        filteredGeocacheList.clear();
//
//        for (Geocache geocache : geocacheList) {
//            boolean match = true;
//
//            if (selectedType != null && !geocache.getType().equalsIgnoreCase(selectedType)) {
//                match = false;
//            }
//
//            if (selectedStatus != null && !geocache.getStatus().equalsIgnoreCase(selectedStatus)) {
//                match = false;
//            }
//
//            if (match) {
//                filteredGeocacheList.add(geocache);
//            }
//        }
//
//        // Update distances after filtering
//        for (Geocache geocache : filteredGeocacheList) {
//            double lat2 = geocache.getLatitude().doubleValue(); // Convert BigDecimal to double
//            double lon2 = geocache.getLongitude().doubleValue(); // Convert BigDecimal to double
//
//            geocache.setDistanceInMeters(
//                    calculateDistance(currentLatitude, currentLongitude, lat2, lon2)
//            );
//        }
//
//        // Optional: Sort by distance
//        Collections.sort(filteredGeocacheList, Comparator.comparingDouble(Geocache::getDistanceInMeters));
//
//        geocacheAdapter.notifyDataSetChanged();
//    }
private void filterData() {
    filteredGeocacheList.clear();

    // Pre-calculate max distance in meters if selected
    double maxDistanceMeters = -1;
    if (selectedDistance != null && !selectedDistance.equals("All distances")) {
        try {
            String distanceValue = selectedDistance.replace("km", "").trim();
            maxDistanceMeters = Double.parseDouble(distanceValue) * 1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // Filter and calculate distances in one pass
    for (Geocache geocache : geocacheList) {
        // Type filter
        if (selectedType != null && !geocache.getType().equalsIgnoreCase(selectedType)) {
            continue;
        }

        // Status filter
        if (selectedStatus != null && !geocache.getStatus().equalsIgnoreCase(selectedStatus)) {
            continue;
        }

        // Calculate distance if we have location
        if (currentLatitude != 0 && currentLongitude != 0) {
            double lat2 = geocache.getLatitude().doubleValue();
            double lon2 = geocache.getLongitude().doubleValue();
            double distance = calculateDistance(currentLatitude, currentLongitude, lat2, lon2);

            // Distance filter if applicable
            if (maxDistanceMeters > 0 && distance > maxDistanceMeters) {
                continue;
            }

            geocache.setDistanceInMeters(distance);
        }

        filteredGeocacheList.add(geocache);
    }

    // Sort by distance if we have location
    if (currentLatitude != 0 && currentLongitude != 0) {
        Collections.sort(filteredGeocacheList, Comparator.comparingDouble(Geocache::getDistanceInMeters));
    }

    geocacheAdapter.notifyDataSetChanged();
}

    public double getDistanceInMeters() {
        return distanceInMeters;
    }

    public void setDistanceInMeters(double distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, result);
        return result[0]; // 单位：米
    }


    // 获取选中的类型
    private boolean[] getSelectedTypes() {
        boolean[] selected = new boolean[types.length]; // 根据类型数组的大小来设置

        if (selectedType != null) {
            // 选择特定的类型，设置对应的索引为 true
            int index = getTypeIndex(selectedType);
            if (index != -1) {
                selected[index] = true;
            }
        }
        return selected;
    }

    // 获取选中的状态
    private boolean[] getSelectedStatuses() {
        boolean[] selected = new boolean[statuses.length]; // 根据状态数组的大小来设置

        if (selectedStatus != null) {
            // 选择特定的状态，设置对应的索引为 true
            int index = getStatusIndex(selectedStatus);
            if (index != -1) {
                selected[index] = true;
            }
        }
        return selected;
    }
    private boolean[] getSelectedDistanace() {
        boolean[] selected = new boolean[distances.length]; // 根据类型数组的大小来设置

        if (selectedDistance!= null) {
            // 选择特定的类型，设置对应的索引为 true
            int index = getTypeIndex(selectedDistance);
            if (index != -1) {
                selected[index] = true;
            }
        }
        return selected;
    }

    // 获取类型索引
    private int getTypeIndex(String type) {
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(type)) {
                return i;
            }
        }
        return -1;
    }

    // 获取状态索引
    private int getStatusIndex(String status) {
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(status)) {
                return i;
            }
        }
        return -1;
    }
    private int getDistanceIndex(String distance) {
        for (int i = 0; i < distances.length; i++) {
            if (distances[i].equalsIgnoreCase(distance)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源（如果有）
    }
}