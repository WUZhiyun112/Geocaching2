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
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;

public class DashboardActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initFilterBar();
        initRecyclerView();
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

    private void initFilterBar() {
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

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        geocacheAdapter = new MarkedGeocacheAdapter.GeocacheAdapter(this, filteredGeocacheList);
        recyclerView.setAdapter(geocacheAdapter);
    }

    private void loadGeocacheData() {
        if (getIntent() != null && getIntent().hasExtra("geocacheList")) {
            geocacheList.clear();
            List<Geocache> newList = getIntent().getParcelableArrayListExtra("geocacheList");
            if (newList != null) {
                geocacheList.addAll(newList);
                Log.d("DataLoad", "Loaded items: " + geocacheList.size());
            }
            filterData();
        } else {
            Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterData() {
        if (isFiltering) return;
        isFiltering = true;

        List<Geocache> tempFilteredList = new ArrayList<>();
        double maxDistanceMeters = -1;
        if (selectedDistance != null && !selectedDistance.equals("All distances")) {
            try {
                String distanceValue = selectedDistance.replace("km", "").trim();
                maxDistanceMeters = Double.parseDouble(distanceValue) * 1000;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        for (Geocache geocache : geocacheList) {
            if (selectedType != null && !geocache.getType().equalsIgnoreCase(selectedType)) continue;
            if (selectedStatus != null && !geocache.getStatus().equalsIgnoreCase(selectedStatus)) continue;

            if (currentLatitude != 0 && currentLongitude != 0) {
                double lat2 = geocache.getLatitude().doubleValue();
                double lon2 = geocache.getLongitude().doubleValue();
                double distance = calculateDistance(currentLatitude, currentLongitude, lat2, lon2);

                if (maxDistanceMeters > 0 && distance > maxDistanceMeters) continue;
                geocache.setDistanceInMeters(distance);
            }

            tempFilteredList.add(geocache);
        }

        if (currentLatitude != 0 && currentLongitude != 0) {
            Collections.sort(tempFilteredList, Comparator.comparingDouble(Geocache::getDistanceInMeters));
        }

        filteredGeocacheList.clear();
        filteredGeocacheList.addAll(tempFilteredList);
        geocacheAdapter.notifyDataSetChanged();

        isFiltering = false;
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

    private int getTypeIndex(String type) {
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(type)) return i;
        }
        return -1;
    }

    private int getStatusIndex(String status) {
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(status)) return i;
        }
        return -1;
    }

    private int getDistanceIndex(String distance) {
        for (int i = 0; i < distances.length; i++) {
            if (distances[i].equalsIgnoreCase(distance)) return i;
        }
        return -1;
    }

    private boolean[] getSelectedTypes() {
        boolean[] selected = new boolean[types.length];
        int index = getTypeIndex(selectedType);
        if (index != -1) selected[index] = true;
        return selected;
    }

    private boolean[] getSelectedStatuses() {
        boolean[] selected = new boolean[statuses.length];
        int index = getStatusIndex(selectedStatus);
        if (index != -1) selected[index] = true;
        return selected;
    }

    private boolean[] getSelectedDistances() {
        boolean[] selected = new boolean[distances.length];
        int index = getDistanceIndex(selectedDistance);
        if (index != -1) selected[index] = true;
        return selected;
    }
}
