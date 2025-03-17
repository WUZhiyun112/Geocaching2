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

import java.util.List;

public class GeocacheAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private List<Geocache> geocacheList;
    private Context context;
    private boolean hasMoreData = true; // 是否还有更多数据可以加载

    public GeocacheAdapter(Context context, List<Geocache> geocacheList) {
        this.context = context;
        this.geocacheList = geocacheList;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < geocacheList.size()) ? VIEW_TYPE_ITEM : VIEW_TYPE_LOADING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
            return new GeocacheViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GeocacheViewHolder) {
            Geocache geocache = geocacheList.get(position);
            ((GeocacheViewHolder) holder).bind(geocache);
        }
    }

    @Override
    public int getItemCount() {
        return geocacheList.size() + (hasMoreData ? 1 : 0); // 如果有更多数据，就添加加载视图
    }

    public void updateData(List<Geocache> newGeocacheList, boolean isFirstPage) {
        if (isFirstPage) {
            geocacheList.clear(); // 只有第一页才清空
        }
        geocacheList.addAll(newGeocacheList);
        notifyItemRangeInserted(geocacheList.size() - newGeocacheList.size(), newGeocacheList.size());
    }

    public void setHasMoreData(boolean hasMoreData) {
        this.hasMoreData = hasMoreData;
        notifyItemChanged(geocacheList.size() - 1); // 当数据加载完成时，更新加载视图
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
            tvFoundAt.setText("Found At: " + (geocache.getFoundAt() != null ? geocache.getFoundAt() : "N/A"));
            tvStatus.setText("Status: " + geocache.getStatus());

            // 点击事件，跳转到详情页
            itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, GeocacheDetailActivity.class);
                intent.putExtra("geocache", geocache); // 传递数据
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

    // MarkedGeocacheAdapter 可以继承 GeocacheAdapter 来避免重复代码
    public static class MarkedGeocacheAdapter extends GeocacheAdapter {
        public MarkedGeocacheAdapter(Context context, List<Geocache> geocacheList) {
            super(context, geocacheList);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);

            if (holder instanceof GeocacheViewHolder) {
                // 在此处你可以为 MarkedGeocacheAdapter 做自定义的修改，若有需要
                // 例如改变点击事件，或显示额外的字段
            }
        }
    }
}
