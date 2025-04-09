package com.example.geocaching1.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
    private OnItemLongClickListener longClickListener; // 直接使用自定义接口

    public GeocacheAdapter(Context context) {
        this.context = context;
        this.geocacheList = new ArrayList<>(); // 初始化为空列表
    }
    public interface OnItemLongClickListener {
        boolean onItemLongClick(int position, Geocache geocache);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
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
            Geocache geocache = geocacheList.get(position);  // 确保 geocache 变量存在
            ((GeocacheViewHolder) holder).bind(geocache);

            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    return longClickListener.onItemLongClick(position, geocache);
                }
                return false;
            });
        }
    }


    @Override
    public int getItemCount() {
        // 只在有更多数据且列表不为空时显示加载视图
        return geocacheList.size() + (hasMoreData  ? 1 : 0);
    }

    public void setHasMoreData(boolean hasMoreData) {
        boolean wasShowing = this.hasMoreData;
        this.hasMoreData = hasMoreData;

        if (wasShowing && !hasMoreData) {
            // 移除加载视图
            notifyItemRemoved(geocacheList.size());
        } else if (!wasShowing && hasMoreData) {
            // 添加加载视图
            notifyItemInserted(geocacheList.size());
        }
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
            tvStatus.setText("Status: " + geocache.getStatus());

            // Conditionally show or hide the "Found At" field
            if ("Found it".equals(geocache.getStatus())) {
                tvFoundAt.setVisibility(View.VISIBLE);
                tvFoundAt.setText("Found At: " + geocache.getFormattedFoundAt());
            } else {
                tvFoundAt.setVisibility(View.GONE);  // Hide the "Found At" field if status is not "Found it"
            }
//
//            itemView.setOnClickListener(v -> {
//                Context context = v.getContext();
//                Intent intent = new Intent(context, GeocacheDetailActivity.class);
//                intent.putExtra("geocache", geocache);
//                context.startActivity(intent);
//            });
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