package com.example.geocaching1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.geocaching1.Geocache;
import com.example.geocaching1.R;

import java.util.List;

public class FollowedGeocacheAdapter extends RecyclerView.Adapter<FollowedGeocacheAdapter.ViewHolder> {
    private List<Geocache> followedGeocaches;

    public FollowedGeocacheAdapter(List<Geocache> followedGeocaches) {
        this.followedGeocaches = followedGeocaches;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_geocache, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the geocache item at the given position
        Geocache geocache = followedGeocaches.get(position);

        // Only set the geocache code text
        holder.geocacheCode.setText(geocache.getCode());
    }

    @Override
    public int getItemCount() {
        return followedGeocaches.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Only keep the geocacheCode TextView
        TextView geocacheCode;

        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize the geocacheCode TextView
            geocacheCode = itemView.findViewById(R.id.geocache_code);
        }
    }
}
