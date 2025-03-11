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

import com.example.geocaching1.Geocache;
import com.example.geocaching1.GeocacheDetailActivity;
import com.example.geocaching1.R;

import java.util.List;
public class FollowedGeocacheAdapter extends RecyclerView.Adapter<FollowedGeocacheAdapter.FollowedGeocacheViewHolder> {
    private List<Geocache> geocacheList;
    private Context context;

    public FollowedGeocacheAdapter(List<Geocache> geocacheList, Context context) {
        this.geocacheList = geocacheList;
        this.context = context;
    }

    @NonNull
    @Override
    public FollowedGeocacheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_geocache, parent, false);
        return new FollowedGeocacheViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowedGeocacheViewHolder holder, int position) {
        Geocache geocache = geocacheList.get(position);
        holder.nameTextView.setText(geocache.getName());
        holder.codeTextView.setText((geocache.getCode()));
        holder.locationTextView.setText((geocache.getLocation()));
        holder.typeTextView.setText((geocache.getType()));
        // 填充其他字段...

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            // 跳转到详情页面
            Intent intent = new Intent(context, GeocacheDetailActivity.class);
            intent.putExtra("geocache", geocache); // 传递 Geocache 对象
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return geocacheList.size();
    }

    public static class FollowedGeocacheViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, codeTextView, typeTextView, locationTextView;
        // 其他视图组件

        public FollowedGeocacheViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.geocache_name);
            codeTextView = itemView.findViewById(R.id.geocache_code);
            typeTextView = itemView.findViewById(R.id.geocache_type);
            locationTextView = itemView.findViewById(R.id.geocache_location);
            // 初始化其他视图组件
        }
    }

    public static class GeocacheAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
            // 只有在该位置是最后一项时才返回加载视图类型
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
}