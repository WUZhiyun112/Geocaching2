package com.example.geocaching1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geocaching1.GeocacheAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GeocacheAdapter geocacheAdapter;
    private List<Geocache> geocacheList = new ArrayList<>();
    private List<Geocache> filteredGeocacheList = new ArrayList<>();  // 存储筛选后的数据

    private Spinner typeSpinner;
    private Spinner statusSpinner;

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

        // 初始化底部导航栏
        initBottomNavigation();

        // 初始化筛选条
        initFilterBar();

        // 初始化 RecyclerView
        initRecyclerView();

        // 从 Intent 加载数据
        loadGeocacheData();

    }

    private void initBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.navigation_dashboard) {
                return true; // 当前页面，无需跳转
            } else if (itemId == R.id.navigation_self) {
                startActivity(new Intent(DashboardActivity.this, SelfActivity.class));
                return true;
            }
            return false;
        });
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
        geocacheAdapter = new GeocacheAdapter(DashboardActivity.this, filteredGeocacheList);
        recyclerView.setAdapter(geocacheAdapter);
    }

    /**
     * 从 Intent 加载数据
     */
    private void loadGeocacheData() {
        if (getIntent() != null && getIntent().hasExtra("geocacheList")) {
            geocacheList = getIntent().getParcelableArrayListExtra("geocacheList");
            if (geocacheList != null && !geocacheList.isEmpty()) {
                filteredGeocacheList.addAll(geocacheList);
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
