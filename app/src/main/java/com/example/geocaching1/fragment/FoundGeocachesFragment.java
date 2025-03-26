package com.example.geocaching1.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.geocaching1.model.Geocache;
import com.example.geocaching1.R;
import com.example.geocaching1.adapter.GeocacheAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
public class FoundGeocachesFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_JWT_TOKEN = "jwt_token";

    private String jwtToken;
    private int userId;
    private RecyclerView recyclerView;
    private TextView tvNoGeocache;
    private GeocacheAdapter adapter;
    private List<Geocache> geocaches = new ArrayList<>();

    public static FoundGeocachesFragment newInstance(int userId, String jwtToken) {
        FoundGeocachesFragment fragment = new FoundGeocachesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putString(ARG_JWT_TOKEN, jwtToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geocache_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvNoGeocache = view.findViewById(R.id.tv_no_geocache);

        recyclerView.setVisibility(View.GONE);
        tvNoGeocache.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GeocacheAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void updateGeocaches(List<Geocache> newGeocaches) {
        if (newGeocaches != null && !newGeocaches.isEmpty()) {
            // Sort the list by 'foundAt' in descending order
            Collections.sort(newGeocaches, new Comparator<Geocache>() {
                @Override
                public int compare(Geocache g1, Geocache g2) {
                    // Assuming getFoundAt() returns a Date or similar object
                    return g2.getFoundAt().compareTo(g1.getFoundAt()); // Descending order
                }
            });

            Log.d("Fragment_Debug", "First item foundAt: " + newGeocaches.get(0).getFoundAt());

            // Directly use the adapter's setData method to update the data
            adapter.setData(newGeocaches);

            recyclerView.setVisibility(View.VISIBLE);
            tvNoGeocache.setVisibility(View.GONE);
        } else {
            // Clear the adapter data
            adapter.setData(new ArrayList<>());
            recyclerView.setVisibility(View.GONE);
            tvNoGeocache.setVisibility(View.VISIBLE);
        }
    }
}
