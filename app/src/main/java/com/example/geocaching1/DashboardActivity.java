package com.example.geocaching1;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geocaching1.model.Geocache;
import com.example.geocaching1.R;
import com.example.geocaching1.adapter.MarkedGeocacheAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MarkedGeocacheAdapter.GeocacheAdapter geocacheAdapter;
    private List<Geocache> geocacheList = new ArrayList<>();
    private List<Geocache> filteredGeocacheList = new ArrayList<>();  // 存储筛选后的数据

    private Spinner typeSpinner;
    private Spinner statusSpinner;

    private int currentPage = 0; // 当前页码
    private int pageSize = 10;   // 每页加载的数据量
    private boolean isLoading = false; // 是否正在加载数据
    private boolean hasMoreData = true; // 是否还有更多数据可以加载

    // 假设的筛选条件
    private String selectedType = null;
    private String selectedStatus = null;

    // 获取筛选条件数组
    private String[] types;
    private String[] statuses;

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

    }



    /**
     * 初始化筛选条
     */
    private void initFilterBar() {
        // 初始化 MaterialAutoCompleteTextView
        MaterialAutoCompleteTextView typeSpinner = findViewById(R.id.type_spinner);
        MaterialAutoCompleteTextView statusSpinner = findViewById(R.id.status_spinner);

        // 初始化数组
        types = getResources().getStringArray(R.array.type_array);
        statuses = getResources().getStringArray(R.array.status_array);

        // 如果数组为空，设置默认值
        if (types == null || statuses == null) {
            Log.e("DashboardActivity", "Types or statuses array is null");
            types = new String[]{"所有类型", "Traditional", "Virtual"};
            statuses = new String[]{"所有状态", "Available", "Unavailable"};
        }

        // 初始化适配器
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, types);
        typeAdapter.setDropDownViewResource(R.layout.dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, statuses);
        statusAdapter.setDropDownViewResource(R.layout.dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // 设置默认选中项
        typeSpinner.setText(types[0], false);
        statusSpinner.setText(statuses[0], false);

        // 设置监听器
        typeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedType = position == 0 ? null : types[position];
            filterData();
        });

        statusSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatus = position == 0 ? null : statuses[position];
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

    private void filterData() {
        filteredGeocacheList.clear();

        for (Geocache geocache : geocacheList) {
            boolean match = true;

            if (selectedType != null && !geocache.getType().equalsIgnoreCase(selectedType)) {
                match = false;
            }

            if (selectedStatus != null && !geocache.getStatus().equalsIgnoreCase(selectedStatus)) {
                match = false;
            }

            if (match) {
                filteredGeocacheList.add(geocache);
            }
        }

        // 更新 RecyclerView
        geocacheAdapter.notifyDataSetChanged();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源（如果有）
    }
} 