package com.example.geocaching1;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private GeocacheAdapter geocacheAdapter;
    private List<Geocache> geocacheList = new ArrayList<>();
    private List<Geocache> filteredGeocacheList = new ArrayList<>();  // 存储筛选后的数据

    private String selectedType = null;
    private String selectedStatus = null;

    private String[] types;
    private String[] statuses;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // 初始化筛选条
        initFilterBar(view);

        // 初始化 RecyclerView
        initRecyclerView(view);

        // 加载数据
        loadGeocacheData();

        return view;
    }




    /**
     * 初始化筛选条
     */
    private void initFilterBar(View view) {
        MaterialAutoCompleteTextView typeSpinner = view.findViewById(R.id.type_spinner);
        MaterialAutoCompleteTextView statusSpinner = view.findViewById(R.id.status_spinner);

        types = getResources().getStringArray(R.array.type_array);
        statuses = getResources().getStringArray(R.array.status_array);

        if (types == null || statuses == null) {
            Log.e("DashboardFragment", "Types or statuses array is null");
            types = new String[]{"所有类型", "Traditional", "Virtual"};
            statuses = new String[]{"所有状态", "Available", "Unavailable"};
        }

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, types);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, statuses);
        statusSpinner.setAdapter(statusAdapter);

        typeSpinner.setText(types[0], false);
        statusSpinner.setText(statuses[0], false);

        typeSpinner.setOnItemClickListener((parent, view1, position, id) -> {
            selectedType = position == 0 ? null : types[position];
            filterData();
        });

        statusSpinner.setOnItemClickListener((parent, view1, position, id) -> {
            selectedStatus = position == 0 ? null : statuses[position];
            filterData();
        });
    }

    private void initRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);  // 增强性能

        geocacheAdapter = new GeocacheAdapter(requireContext(), filteredGeocacheList);
        recyclerView.setAdapter(geocacheAdapter);
    }

    /**
     * 从 Intent 加载数据
     */
    private void loadGeocacheData() {
        if (getArguments() != null && getArguments().containsKey("geocacheList")) {
            geocacheList = getArguments().getParcelableArrayList("geocacheList");
            if (geocacheList != null && !geocacheList.isEmpty()) {
                filteredGeocacheList.addAll(geocacheList);
                geocacheAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(requireContext(), "No geocache data available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Failed to load geocache data", Toast.LENGTH_SHORT).show();
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
    public void onDestroy() {
        super.onDestroy();
        // Clean up any resources or references here
    }

}
