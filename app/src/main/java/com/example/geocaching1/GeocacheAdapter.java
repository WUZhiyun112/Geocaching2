package com.example.geocaching1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GeocacheAdapter extends RecyclerView.Adapter<GeocacheAdapter.GeocacheViewHolder> {
    private List<Geocache> geocacheList;
    private Context context;

    public GeocacheAdapter(Context context, List<Geocache> geocacheList) {
        this.context = context;
        this.geocacheList = geocacheList;
    }

    @Override
    public GeocacheViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.geocache_item, parent, false);
        return new GeocacheViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GeocacheViewHolder holder, int position) {
        Geocache geocache = geocacheList.get(position);
        if (geocache != null) {
            holder.nameTextView.setText(geocache.getName());
            holder.typeTextView.setText(geocache.getType());
            holder.statusTextView.setText(geocache.getStatus());
            holder.locationTextView.setText(geocache.getLocation());


            // 点击每个 item 进入详情页
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, GeocacheDetailActivity.class);
                intent.putExtra("geocache", geocache); // 传递整个 Geocache 对象
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return geocacheList.size();
    }

    // 更新数据的方法
    public void updateData(List<Geocache> newGeocacheList) {
        geocacheList.clear();
        geocacheList.addAll(newGeocacheList);
        notifyDataSetChanged(); // 通知 Adapter 数据已更新
    }

    public static class GeocacheViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, typeTextView, statusTextView,locationTextView;

        public GeocacheViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
        }
    }
}