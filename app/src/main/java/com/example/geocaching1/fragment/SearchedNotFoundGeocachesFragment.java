package com.example.geocaching1.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geocaching1.R;
import com.example.geocaching1.adapter.GeocacheAdapter;
import com.example.geocaching1.model.Geocache;

import java.util.ArrayList;
import java.util.List;

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

        // 初始化视图
        recyclerView = view.findViewById(R.id.recyclerView);
        tvNoGeocache = view.findViewById(R.id.tv_no_geocache);

        // 设置默认状态
        recyclerView.setVisibility(View.GONE);
        tvNoGeocache.setVisibility(View.GONE);

        // 初始化RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GeocacheAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void updateGeocaches(List<Geocache> newGeocaches) {
        if (newGeocaches != null && !newGeocaches.isEmpty()) {
            Log.d("NotFoundFragment", "Updating with " + newGeocaches.size() + " items");

            // 使用适配器的setData方法更新数据
            adapter.setData(newGeocaches);

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