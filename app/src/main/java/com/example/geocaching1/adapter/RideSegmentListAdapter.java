package com.example.geocaching1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.route.RideStep;
import com.amap.api.services.route.WalkStep;
import com.example.geocaching1.R;
import com.example.geocaching1.databinding.ItemSegmentBinding;
import com.example.geocaching1.utils.MapUtil;

import java.util.List;

/**
 * 骑行段列表适配器
 */
public class RideSegmentListAdapter extends RecyclerView.Adapter<RideSegmentListAdapter.ViewHolder> {

    private List<RideStep> mItemList;

    public RideSegmentListAdapter(List<RideStep> data) {
        this.mItemList = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSegmentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideStep item = mItemList.get(position);
        if (position == 0) {
            holder.binding.busDirIcon.setImageResource(R.drawable.dir_start);
            holder.binding.busLineName.setText("出发");
            holder.binding.busDirIconUp.setVisibility(View.INVISIBLE);
            holder.binding.busDirIconDown.setVisibility(View.VISIBLE);
            holder.binding.busSegSplitLine.setVisibility(View.INVISIBLE);
        } else if (position == mItemList.size() - 1) {
            holder.binding.busDirIcon.setImageResource(R.drawable.dir_end);
            holder.binding.busLineName.setText("到达终点");
            holder.binding.busDirIconUp.setVisibility(View.VISIBLE);
            holder.binding.busDirIconDown.setVisibility(View.INVISIBLE);
        } else {
            holder.binding.busSegSplitLine.setVisibility(View.VISIBLE);
            holder.binding.busDirIconUp.setVisibility(View.VISIBLE);
            holder.binding.busDirIconDown.setVisibility(View.VISIBLE);
            String actionName = item.getAction();
            int resID = MapUtil.getWalkActionID(actionName);
            holder.binding.busDirIcon.setImageResource(resID);
            holder.binding.busLineName.setText(item.getInstruction());
        }
    }

    @Override
    public int getItemCount() {
        return mItemList == null || mItemList.isEmpty() ? 0 : mItemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemSegmentBinding binding;

        public ViewHolder(ItemSegmentBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
