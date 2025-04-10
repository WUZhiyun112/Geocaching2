package com.example.geocaching1;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.Manifest;
import android.content.pm.PackageManager;
public class DashboardActivity extends AppCompatActivity {

    private static final long FILTER_DEBOUNCE_DELAY = 300; // 防抖延迟300毫秒

    private RecyclerView recyclerView;
    private MarkedGeocacheAdapter.GeocacheAdapter geocacheAdapter;
    private List<Geocache> geocacheList = new ArrayList<>();
    private List<Geocache> filteredGeocacheList = new ArrayList<>();

    private String selectedType = null;
    private String selectedStatus = null;
    private String selectedDistance = null;

    private String[] types;
    private String[] statuses;
    private String[] distances;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private double distanceInMeters;
    private boolean isFiltering = false;
    private boolean isDataLoaded = false; // 新增标志位，防止重复加载

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Log.d("DashboardActivity12321", "onCreate called");  // Log when the activity is created

        initFilterBar();
        initRecyclerView();

        // 只有在数据未加载时才加载数据
        if (!isDataLoaded) {
            loadGeocacheData();
            isDataLoaded = true;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("DashboardActivity12321", "Location permissions not granted");
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                Log.d("Location", "Lat: " + currentLatitude + ", Lon: " + currentLongitude);
                debouncedFilterData();
            } else {
                Log.w("DashboardActivity12321", "Location is null");
            }
        });
    }

    private void initFilterBar() {
        Log.d("DashboardActivity12321", "Initializing filter bar");  // Log filter bar initialization

        MaterialAutoCompleteTextView typeSpinner = findViewById(R.id.type_spinner);
        MaterialAutoCompleteTextView statusSpinner = findViewById(R.id.status_spinner);
        MaterialAutoCompleteTextView distanceSpinner = findViewById(R.id.distance_spinner);

        types = getResources().getStringArray(R.array.type_array);
        statuses = getResources().getStringArray(R.array.status_array);
        distances = getResources().getStringArray(R.array.distance_array);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, types);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, statuses);
        statusSpinner.setAdapter(statusAdapter);

        ArrayAdapter<String> distanceAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, distances);
        distanceSpinner.setAdapter(distanceAdapter);

        typeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedType = position == 0 ? null : types[position];
            Log.d("Filter", "Selected Type: " + selectedType);  // Log the selected type
            debouncedFilterData();
        });

        statusSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatus = position == 0 ? null : statuses[position];
            Log.d("Filter", "Selected Status: " + selectedStatus);  // Log the selected status
            debouncedFilterData();
        });

        distanceSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedDistance = position == 0 ? null : distances[position];
            Log.d("Filter", "Selected Distance: " + selectedDistance);  // Log the selected distance
            debouncedFilterData();
        });
    }

    private void initRecyclerView() {
        Log.d("DashboardActivity12321", "Initializing RecyclerView");  // Log RecyclerView initialization

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        geocacheAdapter = new MarkedGeocacheAdapter.GeocacheAdapter(this, filteredGeocacheList);
        recyclerView.setAdapter(geocacheAdapter);
    }

    private void loadGeocacheData() {
        Log.d("DashboardActivity12321", "Loading geocache data");

        if (getIntent() != null && getIntent().hasExtra("geocacheList")) {
            List<Geocache> newList = getIntent().getParcelableArrayListExtra("geocacheList");

            if (newList != null) {
                // 使用 HashSet 去重
                Set<Geocache> uniqueGeocaches = new HashSet<>(newList);

                // 更新 geocacheList
                geocacheList.clear();
                geocacheList.addAll(uniqueGeocaches); // 无重复数据

                Log.d("DataLoad", "Loaded unique items: " + geocacheList.size());
                debouncedFilterData();
            } else {
                Toast.makeText(this, "No data received", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
        }
    }


    private void debouncedFilterData() {
        Log.d("Filter", "Debounced filter triggered");  // Log when filtering is triggered

        recyclerView.removeCallbacks(filterRunnable);  // Make sure to cancel previous runs
        recyclerView.postDelayed(filterRunnable, FILTER_DEBOUNCE_DELAY);
    }

    private final Runnable filterRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("Filter", "Running filter data method");  // Log when filter data method runs
            filterData();
        }
    };

    private void filterData() {
        Log.d("Filter", "Filtering data...");  // Log when filterData is called

        if (isFiltering) {
            Log.d("Filter", "Already filtering, skipping");  // Log if already filtering
            return;
        }
        isFiltering = true;

        List<Geocache> tempFilteredList = new ArrayList<>();
        double maxDistanceMeters = -1;

        try {
            if (selectedDistance != null && !selectedDistance.equals("All distances")) {
                String distanceValue = selectedDistance.replace("km", "").trim();
                maxDistanceMeters = Double.parseDouble(distanceValue) * 1000;
            }
        } catch (NumberFormatException e) {
            Log.e("FilterError", "Invalid distance format", e);
            maxDistanceMeters = -1;
        }

        for (Geocache geocache : geocacheList) {
            Log.d("Filter", "Checking geocache: " + geocache.getName());  // Log each geocache being checked

            // Type filtering
            if (selectedType != null && !geocache.getType().equalsIgnoreCase(selectedType)) {
                continue;
            }

            // Status filtering
            if (selectedStatus != null && !geocache.getStatus().equalsIgnoreCase(selectedStatus)) {
                continue;
            }

            // Distance filtering
            if (currentLatitude != 0 && currentLongitude != 0) {
                double lat2 = geocache.getLatitude().doubleValue();
                double lon2 = geocache.getLongitude().doubleValue();
                double distance = calculateDistance(currentLatitude, currentLongitude, lat2, lon2);

                if (maxDistanceMeters > 0 && distance > maxDistanceMeters) {
                    continue;
                }
                geocache.setDistanceInMeters(distance);
            }

            if (!tempFilteredList.contains(geocache)) {
                tempFilteredList.add(geocache);
            }
        }

        // Sort by distance
        if (currentLatitude != 0 && currentLongitude != 0) {
            Collections.sort(tempFilteredList, Comparator.comparingDouble(Geocache::getDistanceInMeters));
        }

        filteredGeocacheList.clear();  // Clear old filtered list before updating
        filteredGeocacheList.addAll(tempFilteredList);
        geocacheAdapter.notifyDataSetChanged();  // Update RecyclerView

        isFiltering = false;
        Log.d("FilterDebug", "Filter completed. Items: " + filteredGeocacheList.size());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, result);
        return result[0];
    }

    public double getDistanceInMeters() {
        return distanceInMeters;
    }

    public void setDistanceInMeters(double distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }
}
