package com.example.geocaching1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GeocacheAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Geocache> geocacheList;
    private Context context;
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;
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
            View view = LayoutInflater.from(context).inflate(R.layout.geocache_item, parent, false);
            return new GeocacheViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
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
        return geocacheList.size() + (hasMoreData ? 1 : 0);
    }

    public void updateData(List<Geocache> newGeocacheList, boolean isFirstPage) {
        if (isFirstPage) {
            geocacheList.clear(); // 只有第一页才清空
        }
        geocacheList.addAll(newGeocacheList);
        notifyDataSetChanged();
    }

    public static class GeocacheViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, typeTextView, statusTextView, locationTextView;

        public GeocacheViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
        }

        public void bind(Geocache geocache) {
            nameTextView.setText(geocache.getName());
            typeTextView.setText(geocache.getType());
            statusTextView.setText(geocache.getStatus());
            locationTextView.setText(geocache.getLocation());

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
}
