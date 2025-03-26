package com.example.geocaching1.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geocaching1.model.Geocache;
import com.example.geocaching1.GeocacheDetailActivity;
import com.example.geocaching1.R;

import java.util.ArrayList;
import java.util.List;

public class GeocacheAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private List<Geocache> geocacheList;
    private Context context;
    private boolean hasMoreData = false; // 默认没有更多数据
    private boolean showLoading = false; // 控制是否显示加载视图

    public GeocacheAdapter(Context context) {
        this.context = context;
        this.geocacheList = new ArrayList<>(); // 初始化为空列表
    }

    @Override
    public int getItemViewType(int position) {
        // 只有当有更多数据且是最后一个位置时才显示加载视图
        return (showLoading && position == geocacheList.size()) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
            return new GeocacheViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GeocacheViewHolder && position < geocacheList.size()) {
            Geocache geocache = geocacheList.get(position);
            ((GeocacheViewHolder) holder).bind(geocache);
        }
    }

    @Override
    public int getItemCount() {
        // 只有当有更多数据时才额外计算加载视图
        return geocacheList.size() + (showLoading ? 1 : 0);
    }

    public void setData(List<Geocache> newGeocacheList) {
        geocacheList.clear();
        if (newGeocacheList != null) {
            geocacheList.addAll(newGeocacheList);
        }
        notifyDataSetChanged();
    }

    public void addData(List<Geocache> newGeocacheList) {
        if (newGeocacheList != null && !newGeocacheList.isEmpty()) {
            int startPosition = geocacheList.size();
            geocacheList.addAll(newGeocacheList);
            notifyItemRangeInserted(startPosition, newGeocacheList.size());
        }
    }

    public void setLoading(boolean loading) {
        if (showLoading != loading) {
            showLoading = loading;
            if (loading) {
                notifyItemInserted(geocacheList.size());
            } else {
                notifyItemRemoved(geocacheList.size());
            }
        }
    }

    public void setHasMoreData(boolean hasMoreData) {
        this.hasMoreData = hasMoreData;
    }

    public boolean isEmpty() {
        return geocacheList.isEmpty();
    }

    public static class GeocacheViewHolder extends RecyclerView.ViewHolder {
        TextView tvGeocacheCode, tvGeocacheName, tvGeocacheType, tvLocation, tvFoundAt, tvStatus;

        public GeocacheViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGeocacheCode = itemView.findViewById(R.id.tvGeocacheCode);
            tvGeocacheName = itemView.findViewById(R.id.tvGeocacheName);
            tvGeocacheType = itemView.findViewById(R.id.tvGeocacheType);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvFoundAt = itemView.findViewById(R.id.tvFoundAt);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Geocache geocache) {
            tvGeocacheCode.setText("Geocache Code: " + geocache.getCode());
            tvGeocacheName.setText(geocache.getName());
            tvGeocacheType.setText("Type: " + geocache.getType());
            tvLocation.setText("Location: " + geocache.getLocation());
            tvFoundAt.setText("Found At: " + geocache.getFormattedFoundAt());
            tvStatus.setText("Status: " + geocache.getStatus());

            itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, GeocacheDetailActivity.class);
                intent.putExtra("geocache", geocache);
                context.startActivity(intent);
            });
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}