package com.example.geocaching1.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.example.geocaching1.SimpleDetailActivity;

import java.util.List;
import java.util.Locale;

public class MarkedGeocacheAdapter extends RecyclerView.Adapter<MarkedGeocacheAdapter.MarkedGeocacheViewHolder> {
    private List<Geocache> geocacheList;
    private Context context;

    public MarkedGeocacheAdapter(List<Geocache> geocacheList, Context context) {
        this.geocacheList = geocacheList;
        this.context = context;
    }

    @NonNull
    @Override
    public MarkedGeocacheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_geocache, parent, false);
        return new MarkedGeocacheViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MarkedGeocacheViewHolder holder, int position) {
        Geocache geocache = geocacheList.get(position);
        holder.nameTextView.setText(geocache.getName());
        holder.codeTextView.setText((geocache.getCode()));
        holder.locationTextView.setText((geocache.getLocation()));
        holder.typeTextView.setText((geocache.getType()));
        // 填充其他字段...


        holder.itemView.setOnClickListener(v -> {
            if (geocache == null) {
                Log.e("MarkedGeocacheAdapter1111", "Geocache is null");
                return;
            }
            Log.d("MarkedGeocacheAdapter", "Clicked Geocache: " + geocache.getName() + " | Code: " + geocache.getCode());
            Log.d("MarkedGeocacheAdapter1111", "Clicked Geocache: " + geocache.toString());
            Log.d("MarkedGeocacheAdapter1111", "点击的 Geocache: " + geocache.getName());
            Context context = v.getContext();
            if (context == null) {
                Log.e("MarkedGeocacheAdapter1111", "Context is null");
                return;
            }
            Log.d("MarkedGeocacheAdapter1111", "Context is valid: " + context.getClass().getSimpleName());
            Intent intent = new Intent(context, SimpleDetailActivity.class);
            intent.putExtra("geocache", geocache); // 传递 Geocache 对象
            Log.d("MarkedGeocacheAdapte1111r", "Starting GeocacheDetailActivity");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return geocacheList.size();
    }

    public static class MarkedGeocacheViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, codeTextView, typeTextView, locationTextView;
        // 其他视图组件

        public MarkedGeocacheViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.geocache_name);
            codeTextView = itemView.findViewById(R.id.geocache_code);
            typeTextView = itemView.findViewById(R.id.geocache_type);
            locationTextView = itemView.findViewById(R.id.geocache_location);
            // 初始化其他视图组件
        }

        public void bind(Geocache geocache) {
            nameTextView.setText(geocache.getName());
            codeTextView.setText(geocache.getCode());
            typeTextView.setText(geocache.getType());
            locationTextView.setText(geocache.getLocation());

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (geocache == null) {
                    Log.e("MarkedGeocacheAdapter", "Geocache is null");
                    return;
                }
                Log.d("MarkedGeocacheAdapter", "Geocache clicked: " + geocache.getName());
                Log.d("MarkedGeocacheAdapter222", "Clicked Geocache: " + geocache.toString());
                Context context = v.getContext();
                if (context == null) {
                    Log.e("MarkedGeocacheAdapter", "Context is null");
                    return;
                }
                Log.d("MarkedGeocacheAdapter", "Context is valid: " + context.getClass().getSimpleName());
                Intent intent = new Intent(context, GeocacheDetailActivity.class);
                intent.putExtra("geocache", geocache); // 传递 Geocache 对象
                Log.d("MarkedGeocacheAdapter", "Starting GeocacheDetailActivity");
                context.startActivity(intent);
            });
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
            try {
                if (holder instanceof GeocacheViewHolder) {
                    Geocache geocache = geocacheList.get(position); // 可能抛出IndexOutOfBounds
                    ((GeocacheViewHolder) holder).bind(geocache);
                }
            } catch (IndexOutOfBoundsException e) {
                Log.e("Adapter", "Position error: " + position, e);
            }
        }

        @Override
        public int getItemCount() {
            return geocacheList.size() + (hasMoreData ? 1 : 0); // 如果还有更多数据，就添加加载视图
        }


        public void updateData(List<Geocache> newData, boolean isFirstPage) {
            if (isFirstPage) {
                int oldSize = geocacheList.size();
                geocacheList.clear();
                notifyItemRangeRemoved(0, oldSize); // 先通知移除旧数据
            }

            int startPos = geocacheList.size();
            geocacheList.addAll(newData);
            notifyItemRangeInserted(startPos, newData.size()); // 再通知新增数据
        }

        public void setHasMoreData(boolean hasMoreData) {
            this.hasMoreData = hasMoreData;
            notifyItemChanged(geocacheList.size() - 1); // 当数据加载完成时，更新加载视图
        }

        public static class GeocacheViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, typeTextView, statusTextView, locationTextView, distanceTextView;

            public GeocacheViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                typeTextView = itemView.findViewById(R.id.typeTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
                locationTextView = itemView.findViewById(R.id.locationTextView);
                distanceTextView = itemView.findViewById(R.id.geocache_distance);

            }

            public void bind(Geocache geocache) {
                nameTextView.setText(geocache.getName());
                typeTextView.setText(geocache.getType());
                statusTextView.setText(geocache.getStatus());
                locationTextView.setText(geocache.getLocation());

                // 点击事件，跳转到详情页
                itemView.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Log.d("MarkedGeocacheAdapter333", "Clicked Geocache: " + geocache.toString());
                    Intent intent = new Intent(context,GeocacheDetailActivity.class);
                    intent.putExtra("geocache", geocache); // 传递数据
                    context.startActivity(intent);
                });
                if (geocache.getDistanceInMeters() != null) {
                    distanceTextView.setText(formatDistance(geocache.getDistanceInMeters()));
                } else {
                    distanceTextView.setText("Unknown distance");
                }

            }
        }
        public static String formatDistance(double distanceInMeters) {
            if (distanceInMeters < 1000) {
                return String.format(Locale.getDefault(), "%.0f m", distanceInMeters);
            } else {
                return String.format(Locale.getDefault(), "%.2f km", distanceInMeters / 1000);
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