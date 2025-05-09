package com.example.geocaching1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.WalkPath;
import com.example.geocaching1.adapter.BusSegmentListAdapter;
import com.example.geocaching1.adapter.DriveSegmentListAdapter;
import com.example.geocaching1.adapter.RideSegmentListAdapter;
import com.example.geocaching1.adapter.WalkSegmentListAdapter;
import com.example.geocaching1.databinding.ActivityRouteDetailBinding;
import com.example.geocaching1.utils.MapUtil;
import com.example.geocaching1.utils.SchemeBusStep;

import java.util.ArrayList;
import java.util.List;

/**
 * 路线规划详情页面
 */
public class RouteDetailActivity extends AppCompatActivity {

    private ActivityRouteDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRouteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        switch (intent.getIntExtra("type", 0)) {
            case 0://步行
                walkDetail(intent);
                break;
            case 1://骑行
                rideDetail(intent);
                break;
            case 2://驾车
                driveDetail(intent);
                break;
            case 3://公交
                busDetail(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 公交详情
     * @param intent
     */
    private void busDetail(Intent intent) {
        BusPath busPath = RouteActivity.sCachedBusPath; // 从静态变量获取
        RouteActivity.sCachedBusPath = null; // 及时清除

        if (busPath == null) {
            Toast.makeText(this, "Route data expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.toolbar.setTitle("Public Transport Route Planning");
//        BusPath busPath = intent.getParcelableExtra("path");
        String dur = MapUtil.getFriendlyTime((int) busPath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) busPath.getDistance());
        binding.tvTime.setText(dur + "(" + dis + ")");
        binding.rvRouteDetail.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRouteDetail.setAdapter(new BusSegmentListAdapter(getBusSteps(busPath.getSteps())));
    }

    /**
     * 公交方案数据组装
     * @param list
     * @return
     */
    private List<SchemeBusStep> getBusSteps(List<BusStep> list) {
        List<SchemeBusStep> busStepList = new ArrayList<>();
        SchemeBusStep start = new SchemeBusStep(null);
        start.setStart(true);
        busStepList.add(start);
        for (BusStep busStep : list) {
            if (busStep.getWalk() != null && busStep.getWalk().getDistance() > 0) {
                SchemeBusStep walk = new SchemeBusStep(busStep);
                walk.setWalk(true);
                busStepList.add(walk);
            }
            if (busStep.getBusLine() != null) {
                SchemeBusStep bus = new SchemeBusStep(busStep);
                bus.setBus(true);
                busStepList.add(bus);
            }
            if (busStep.getRailway() != null) {
                SchemeBusStep railway = new SchemeBusStep(busStep);
                railway.setRailway(true);
                busStepList.add(railway);
            }

            if (busStep.getTaxi() != null) {
                SchemeBusStep taxi = new SchemeBusStep(busStep);
                taxi.setTaxi(true);
                busStepList.add(taxi);
            }
        }
        SchemeBusStep end = new SchemeBusStep(null);
        end.setEnd(true);
        busStepList.add(end);
        return busStepList;
    }

    /**
     * 驾车详情
     * @param intent
     */
    private void driveDetail(Intent intent) {
        DrivePath drivePath = RouteActivity.sCachedDrivePath; // 从静态变量获取
        RouteActivity.sCachedBusPath = null; // 及时清除

        if (drivePath == null) {
            Toast.makeText(this, "Route data expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.toolbar.setTitle("Driving Route Planning");
//        DrivePath drivePath = intent.getParcelableExtra("path");
        String dur = MapUtil.getFriendlyTime((int) drivePath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) drivePath.getDistance());
        binding.tvTime.setText(dur + "(" + dis + ")");
        binding.rvRouteDetail.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRouteDetail.setAdapter(new DriveSegmentListAdapter(drivePath.getSteps()));
    }

    /**
     * 骑行详情
     * @param intent
     */
    private void rideDetail(Intent intent) {
        RidePath ridePath = RouteActivity.sCachedRidePath; // 从静态变量获取
        RouteActivity.sCachedBusPath = null; // 及时清除

        if (ridePath == null) {
            Toast.makeText(this, "Route data expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.toolbar.setTitle("Cycling Route Planning");
//        RidePath ridePath = intent.getParcelableExtra("path");
        String dur = MapUtil.getFriendlyTime((int) ridePath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) ridePath.getDistance());
        binding.tvTime.setText(dur + "(" + dis + ")");
        binding.rvRouteDetail.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRouteDetail.setAdapter(new RideSegmentListAdapter(ridePath.getSteps()));
    }

    /**
     * 步行详情
     * @param intent
     */
    private void walkDetail(Intent intent) {
        WalkPath walkPath = RouteActivity.sCachedWalkPath; // 从静态变量获取
        RouteActivity.sCachedBusPath = null; // 及时清除

        if (walkPath == null) {
            Toast.makeText(this, "Route data expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.toolbar.setTitle("Walking Route Planning");
//        WalkPath walkPath = intent.getParcelableExtra("path");
        String dur = MapUtil.getFriendlyTime((int) walkPath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) walkPath.getDistance());
        binding.tvTime.setText(dur + "(" + dis + ")");
        binding.rvRouteDetail.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRouteDetail.setAdapter(new WalkSegmentListAdapter(walkPath.getSteps()));
    }
}