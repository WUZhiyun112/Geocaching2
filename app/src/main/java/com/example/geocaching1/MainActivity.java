package com.example.geocaching1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.ref.WeakReference;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;



import android.content.Context;
import android.content.Intent;

import android.text.SpannableString;

import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.geocaching1.adapter.MarkedGeocacheAdapter;
import com.example.geocaching1.databinding.ActivityMainBinding;
import com.example.geocaching1.model.Geocache;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements AMapLocationListener, LocationSource,
        PoiSearch.OnPoiSearchListener, AMap.OnMapClickListener, AMap.OnMapLongClickListener,
        GeocodeSearch.OnGeocodeSearchListener, AMap.OnMarkerClickListener, AMap.OnMarkerDragListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    // 请求权限意图
    private ActivityResultLauncher<String> requestPermission;
    // 声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    // 声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private MarkedGeocacheAdapter.GeocacheAdapter geocacheAdapter;
    // 声明地图控制器
    private AMap aMap = null;
    // 声明地图定位监听
    private LocationSource.OnLocationChangedListener mListener = null;
    //POI查询对象
    private PoiSearch.Query query;
    //POI搜索对象
    private PoiSearch poiSearch;
    //城市码
    private String cityCode = null;
    //地理编码搜索
    private GeocodeSearch geocodeSearch;
    //解析成功标识码
    private static final int PARSE_SUCCESS_CODE = 1000;
    //城市
    private String city;
    // Geocache 数据列表
    private List<Geocache> geocacheList = new ArrayList<>();

    // 加载自定义图标（确保 geo_star.png 放在 res/drawable 文件夹下）
    BitmapDescriptor customIcon;
    //标点列表
    private final List<Marker> markerList = new ArrayList<>();

    private Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            // 权限申请结果
            showMsg(result ? "已获取到权限" : "权限申请失败");
        });
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FloatingActionButton fabList = findViewById(R.id.fab_list);
        fabList.setOnClickListener(v -> {
            // 确保 geocacheList 已填充数据
            if (geocacheList.isEmpty()) {
                Toast.makeText(this, "No geocache data available", Toast.LENGTH_SHORT).show();
                return;
            }

            // 跳转到 DashboardActivity
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putParcelableArrayListExtra("geocacheList", new ArrayList<>(geocacheList)); // 传递 geocacheList
            startActivity(intent);
        });
        FloatingActionButton fabProfile = findViewById(R.id.fab_profile);

        fabProfile.setOnClickListener(v -> {
            // 跳转到个人资料页面
            Intent intent = new Intent(MainActivity.this, SelfActivity.class);
            startActivity(intent);
        });
        // 初始化定位
        initLocation();
        // 绑定生命周期 onCreate
        binding.mapView.onCreate(savedInstanceState);
        // 初始化地图
        initMap();
        // 初始化搜索
        initSearch();
        // 初始化控件
        initView();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

// Set a LinearLayoutManager (vertical list)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// Set the adapter
        geocacheAdapter = new MarkedGeocacheAdapter.GeocacheAdapter(MainActivity.this, geocacheList);
        recyclerView.setAdapter(geocacheAdapter);



        customIcon = BitmapDescriptorFactory.fromResource(R.drawable.geo_star);
    }

    /**
     * 初始化搜索
     */
    private void initSearch() {
        // 构造 GeocodeSearch 对象
        try {
            geocodeSearch = new GeocodeSearch(this);
            // 设置监听
            geocodeSearch.setOnGeocodeSearchListener(this);
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        // Poi搜索按钮点击事件
//        binding.fabPoi.setOnClickListener(v -> {
//            //构造query对象
//            query = new PoiSearch.Query("购物", "", cityCode);
//            // 设置每页最多返回多少条poiItem
//            query.setPageSize(10);
//            //设置查询页码
//            query.setPageNum(1);
//            //构造 PoiSearch 对象
//            try {
//                poiSearch = new PoiSearch(this, query);
//                //设置搜索回调监听
//                poiSearch.setOnPoiSearchListener(this);
//                //发起搜索附近POI异步请求
//                poiSearch.searchPOIAsyn();
//            } catch (AMapException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        // 清除标点按钮点击事件
//        binding.fabClearMarker.setOnClickListener(v -> clearAllMarker());
        // 路线按钮点击事件
        binding.fabRoute.setOnClickListener(v -> startActivity(new Intent(this, RouteActivity.class)));
        // 键盘按键监听
        binding.etAddress.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                //获取输入框的值
                String address = binding.etAddress.getText().toString().trim();
                if (address.isEmpty()) {
                    showMsg("请输入地址");
                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    //隐藏软键盘
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                    // name表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode
                    GeocodeQuery query = new GeocodeQuery(address, null);
                    geocodeSearch.getFromLocationNameAsyn(query);
                }
                return true;
            }
            return false;
        });

    }

    /**
     * 初始化地图
     */
    private void initMap() {
        if (aMap == null) {
            aMap = binding.mapView.getMap();
            // 创建定位蓝点的样式
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            // 自定义定位蓝点图标
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
            // 自定义精度范围的圆形边框颜色  都为0则透明
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            // 自定义精度范围的圆形边框宽度  0 无宽度
            myLocationStyle.strokeWidth(0);
            // 设置圆形的填充颜色  都为0则透明
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
            // 设置定位蓝点的样式
            aMap.setMyLocationStyle(myLocationStyle);
            // 设置定位监听
            aMap.setLocationSource(this);
            // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
            aMap.setMyLocationEnabled(true);
            //设置最小缩放等级为12 ，缩放级别范围为[3, 20]
            aMap.setMinZoomLevel(5);
            // 开启室内地图
            aMap.showIndoorMap(true);
//            // 设置地图点击事件
//            aMap.setOnMapClickListener(this);
            // 设置地图长按事件
            aMap.setOnMapLongClickListener(this);
//            // 设置地图标点点击事件
//            aMap.setOnMarkerClickListener(this);
            // 设置地图标点拖拽事件
            aMap.setOnMapClickListener(latLng -> {
                if (selectedMarker != null && selectedMarker.isInfoWindowShown()) {
                    selectedMarker.hideInfoWindow();
                    selectedMarker = null; // 清空记录
                }
            });

            // 设置地图标点点击事件
            aMap.setOnMarkerClickListener(marker -> {
                if (!marker.isInfoWindowShown()) {
                    marker.showInfoWindow();
                    selectedMarker = marker; // 记录当前打开的 Marker
                } else {
                    marker.hideInfoWindow();
                    selectedMarker = null; // 关闭时清空记录
                }
                return true; // 事件已处理
            });
            aMap.setOnMarkerDragListener(this);
            // 设置InfoWindowAdapter监听
            aMap.setInfoWindowAdapter(this);
            // 设置InfoWindow点击事件
            aMap.setOnInfoWindowClickListener(this);
            // 地图控件设置
            UiSettings uiSettings = aMap.getUiSettings();
            // 隐藏缩放按钮
            uiSettings.setZoomControlsEnabled(false);
            // 显示比例尺，默认不显示
            uiSettings.setScaleControlsEnabled(true);
        }
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        try {
            //初始化定位
            mLocationClient = new AMapLocationClient(getApplicationContext());
            //设置定位回调监听
            mLocationClient.setLocationListener(this);
            //初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //获取最近3s内精度最高的一次定位结果
            mLocationOption.setOnceLocationLatest(true);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置定位超时时间，单位是毫秒
            mLocationOption.setHttpTimeOut(6000);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        if (mLocationClient != null) mLocationClient.startLocation();
    }

    /**
     * 停止定位
     */
    private void stopLocation() {
        if (mLocationClient != null) mLocationClient.stopLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 绑定生命周期 onResume
        binding.mapView.onResume();
        // 检查是否已经获取到定位权限
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 获取到权限
            startLocation();
        } else {
            // 请求定位权限
            requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 绑定生命周期 onPause
        binding.mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 绑定生命周期 onSaveInstanceState
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 绑定生命周期 onDestroy
        binding.mapView.onDestroy();
    }

    private void showMsg(CharSequence llw) {
        Toast.makeText(this, llw, Toast.LENGTH_SHORT).show();
    }

    /**
     * 定位回调结果
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation == null) {
            showMsg("定位失败，aMapLocation 为空");
            return;
        }
        // 获取定位结果
        if (aMapLocation.getErrorCode() == 0) {
            // 定位成功
            showMsg("定位成功");
//            aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
//            aMapLocation.getLatitude();//获取纬度
//            aMapLocation.getLongitude();//获取经度
//            aMapLocation.getAccuracy();//获取精度信息
//            aMapLocation.getAddress();//详细地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
//            aMapLocation.getCountry();//国家信息
//            aMapLocation.getProvince();//省信息
//            aMapLocation.getCity();//城市信息
            String result = aMapLocation.getDistrict();//城区信息
//            aMapLocation.getStreet();//街道信息
//            aMapLocation.getStreetNum();//街道门牌号信息
//            aMapLocation.getCityCode();//城市编码
//            aMapLocation.getAdCode();//地区编码
//            aMapLocation.getAoiName();//获取当前定位点的AOI信息
//            aMapLocation.getBuildingId();//获取当前室内定位的建筑物Id
//            aMapLocation.getFloor();//获取当前室内定位的楼层
//            aMapLocation.getGpsAccuracyStatus();//获取GPS的当前状态

            // 停止定位
            stopLocation();
            // 显示地图定位结果
            if (mListener != null) {
                mListener.onLocationChanged(aMapLocation);
            }
            // 显示浮动按钮
//            binding.fabPoi.show();
            // 城市编码赋值
            cityCode = aMapLocation.getCityCode();
            // 城市
            city = aMapLocation.getCity();

            fetchGeocacheData(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        } else {
            // 定位失败
            showMsg("定位失败，错误：" + aMapLocation.getErrorInfo());
            Log.e(TAG, "location Error, ErrCode:"
                    + aMapLocation.getErrorCode() + ", errInfo:"
                    + aMapLocation.getErrorInfo());
        }
    }


    private void fetchGeocacheData(double latitude, double longitude) {
        new FetchGeocachesTask(this, latitude, longitude, new GeocacheDataCallback() {
            @Override
            public void onDataLoaded(List<Geocache> geocacheList) {
                // 数据加载完成后的逻辑
                Log.d("MainActivity", "Geocache data loaded: " + geocacheList.size());
            }
        }).execute();
    }
    /**
     * 异步任务：获取 Geocache 数据
     */
    private static class FetchGeocachesTask extends AsyncTask<Void, Void, String> {
        private WeakReference<MainActivity> activityReference;
        private double latitude;
        private double longitude;
        private GeocacheDataCallback callback; // 回调接口

        FetchGeocachesTask(MainActivity activity, double latitude, double longitude, GeocacheDataCallback callback) {
            activityReference = new WeakReference<>(activity);
            this.latitude = latitude;
            this.longitude = longitude;
            this.callback = callback; // 初始化回调
        }

        @Override
        protected String doInBackground(Void... voids) {
            // 执行网络请求，使用传入的经纬度
            return GeocacheFetcher.fetchGeocaches(latitude, longitude);
        }

        @Override
        protected void onPostExecute(String result) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.e("FetchGeocachesTask", "Activity is null or finishing, cannot update UI");
                return;
            }

            Log.d("FetchGeocachesTask", "Received result: " + result);

            if (result != null && !result.isEmpty()) {
                if (result.startsWith("Error") || result.startsWith("Exception")) {
                    // 如果结果是错误信息，显示 Toast
                    Toast.makeText(activity, "Error fetching geocaches: " + result, Toast.LENGTH_SHORT).show();
                    if (callback != null) {
                        callback.onDataLoaded(null); // 回调通知加载失败
                    }
                } else {
                    try {
                        // 解析并显示 Geocache 数据
                        List<Geocache> geocacheList = activity.parseAndShowGeocaches(result);
                        if (callback != null) {
                            callback.onDataLoaded(geocacheList); // 回调通知数据加载完成
                        }
                    } catch (Exception e) {
                        Log.e("FetchGeocachesTask", "Error parsing geocaches", e);
                        Toast.makeText(activity, "Error parsing geocache data", Toast.LENGTH_SHORT).show();
                        if (callback != null) {
                            callback.onDataLoaded(null); // 回调通知加载失败
                        }
                    }
                }
            } else {
                // 如果结果为空，显示 Toast
                Toast.makeText(activity, "Failed to load geocaches", Toast.LENGTH_SHORT).show();
                if (callback != null) {
                    callback.onDataLoaded(null); // 回调通知加载失败
                }
            }
        }
    }
    /**
     * 解析并显示 Geocache 数据
     */
    private List<Geocache> parseAndShowGeocaches(String json) {
        List<Geocache> geocacheList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray results = jsonObject.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                // 获取 Geocache ID
                String geocacheId = results.getString(i);

                // 获取 Geocache 详情
                new FetchGeocacheDetailsTask(geocacheAdapter).execute(geocacheId);
            }

            // 手动添加一个标记（可选）
//            LatLng haidianLatLng = new LatLng(39.95933, 116.29845); // 北京海淀区
//            aMap.addMarker(new MarkerOptions()
//                    .position(haidianLatLng)
//                    .title("Haidian District")
//                    .snippet("Manually added point")
//                    .icon(customIcon)); // 使用自定义图标

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ParseGeocaches", "Error parsing geocache data: " + e.getMessage());
            Toast.makeText(this, "Error parsing geocache data", Toast.LENGTH_SHORT).show();
        }
        return geocacheList;
    }

    private class FetchGeocacheDetailsTask extends AsyncTask<String, Void, Geocache> {
        private MarkedGeocacheAdapter.GeocacheAdapter geocacheAdapter;

        FetchGeocacheDetailsTask(MarkedGeocacheAdapter.GeocacheAdapter geocacheAdapter) {
            this.geocacheAdapter = geocacheAdapter;
        }

        @Override
        protected Geocache doInBackground(String... params) {
            String geocacheId = params[0];
            return GeocacheFetcher.fetchGeocacheDetails(geocacheId); // 在后台获取 Geocache 详情
        }

        @Override
        protected void onPostExecute(Geocache geocache) {
            if (geocache != null) {
                // 将获取的 Geocache 添加到列表
                geocacheList.add(geocache);
                geocacheAdapter.notifyDataSetChanged();

                // 解析 location 字段
                String[] latLng = geocache.getLocation().split("\\|");
                if (latLng.length == 2) {
                    try {
                        BigDecimal latitude = new BigDecimal(latLng[0]);
                        BigDecimal longitude = new BigDecimal(latLng[1]);

                        // 创建 LatLng 对象
                        LatLng position = new LatLng(latitude.doubleValue(), longitude.doubleValue());

                        // 创建 MarkerOptions
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(position)
                                .title(geocache.getName()) // 标题
                                .snippet(geocache.getType()) // 片段
                                .icon(customIcon); // 自定义图标

                        // 检查 AMap 是否初始化
                        if (aMap != null) {
                            // 添加 Marker 到地图
                            Marker marker = aMap.addMarker(markerOptions);
                            // 将 Geocache 对象与 Marker 关联
                            marker.setObject(geocache);
                            Log.d("FetchGeocacheDetailsTask", "Marker added for Geocache: " + geocache.getName());
                        } else {
                            Log.e("FetchGeocacheDetailsTask", "AMap is not initialized");
                        }
                    } catch (NumberFormatException e) {
                        Log.e("FetchGeocacheDetailsTask", "Error parsing location: " + geocache.getLocation(), e);
                    }
                } else {
                    Log.e("FetchGeocacheDetailsTask", "Invalid location format: " + geocache.getLocation());
                }
            } else {
                Log.e("FetchGeocacheDetailsTask", "Failed to fetch details for geocache");
                Toast.makeText(MainActivity.this, "Failed to fetch geocache details", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public interface GeocacheDataCallback {
        void onDataLoaded(List<Geocache> geocacheList);
    }
    /**
     * 激活定位
     *
     * @param onLocationChangedListener
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        if (mListener == null) {
            mListener = onLocationChangedListener;
        }
        startLocation();
    }

    /**
     * 禁用
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * POI搜索返回
     *
     * @param poiResult POI所有数据
     * @param i
     */
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        //解析result获取POI信息

        //获取POI组数列表
        ArrayList<PoiItem> poiItems = poiResult.getPois();
        for (PoiItem poiItem : poiItems) {
            Log.d("MainActivity", " Title：" + poiItem.getTitle() + " Snippet：" + poiItem.getSnippet());
        }
    }

    /**
     * POI中的项目搜索返回
     *
     * @param poiItem 获取POI item
     * @param i
     */
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * 通过经纬度获取地址
     *
     * @param latLng
     */
    private void latLonToAddress(LatLng latLng) {
        //位置点  通过经纬度进行构建
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        //逆编码查询  第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 20, GeocodeSearch.AMAP);
        //异步获取地址信息
        geocodeSearch.getFromLocationAsyn(query);
    }

    /**
     * 添加地图标点
     *
     * @param latLng
     */
    private void addMarker(LatLng latLng) {
        //显示浮动按钮
//        binding.fabClearMarker.show();
        //添加标点
        Marker marker = aMap.addMarker(new MarkerOptions()
                .draggable(true)
                .position(latLng)
                .title("标题")
                .snippet("详细内容"));
        // 显示InfoWindow
        marker.showInfoWindow();
        //设置标点的绘制动画效果
//        Animation animation = new RotateAnimation(marker.getRotateAngle(), marker.getRotateAngle() + 180, 0, 0, 0);
//        long duration = 1000L;
//        animation.setDuration(duration);
//        animation.setInterpolator(new LinearInterpolator());
//        marker.setAnimation(animation);
//        marker.startAnimation();

        markerList.add(marker);
    }

    /**
     * 清空地图Marker
     */
    public void clearAllMarker() {
        if (markerList != null && !markerList.isEmpty()) {
            for (Marker markerItem : markerList) {
                markerItem.remove();
            }
        }
//        binding.fabClearMarker.hide();
    }

    /**
     * 改变地图中心位置
     * @param latLng 位置
     */
    private void updateMapCenter(LatLng latLng) {
        // CameraPosition 第一个参数： 目标位置的屏幕中心点经纬度坐标。
        // CameraPosition 第二个参数： 目标可视区域的缩放级别
        // CameraPosition 第三个参数： 目标可视区域的倾斜度，以角度为单位。
        // CameraPosition 第四个参数： 可视区域指向的方向，以角度为单位，从正北向顺时针方向计算，从0度到360度
        CameraPosition cameraPosition = new CameraPosition(latLng, 16, 30, 0);
        //位置变更
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        //改变位置（使用动画）
        aMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // 通过经纬度获取地址
        latLonToAddress(latLng);
        // 点击地图时添加标点
//        addMarker(latLng);
        // 改变地图中心点
        updateMapCenter(latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // 通过经纬度获取地址
        latLonToAddress(latLng);
    }

    /**
     * 坐标转地址
     *
     * @param regeocodeResult
     * @param rCode
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        //解析result获取地址描述信息
        if (rCode == PARSE_SUCCESS_CODE) {
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            //显示解析后的地址
            showMsg("地址：" + regeocodeAddress.getFormatAddress());
        } else {
            showMsg("获取地址失败");
        }
    }

    /**
     * 地址转坐标
     *
     * @param geocodeResult
     * @param rCode
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
        if (rCode != PARSE_SUCCESS_CODE) {
            showMsg("获取坐标失败");
            return;
        }
        List<GeocodeAddress> geocodeAddressList = geocodeResult.getGeocodeAddressList();
        if (geocodeAddressList != null && !geocodeAddressList.isEmpty()) {
            LatLonPoint latLonPoint = geocodeAddressList.get(0).getLatLonPoint();
            //显示解析后的坐标
            showMsg("坐标：" + latLonPoint.getLongitude() + "，" + latLonPoint.getLatitude());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // 显示或隐藏 InfoWindow
        if (!marker.isInfoWindowShown()) { // 如果 InfoWindow 没有显示
            marker.showInfoWindow(); // 显示 InfoWindow
            selectedMarker = marker;
        } else { // 如果 InfoWindow 已经显示
            marker.hideInfoWindow(); // 隐藏 InfoWindow
            selectedMarker = null;
        }

        // 获取 Marker 关联的 Geocache 对象
        Geocache geocache = (Geocache) marker.getObject();
        if (geocache != null) {
            Log.d("GeocacheDetails", "Name: " + geocache.getName() +
                    ", Location: " + geocache.getLatitude() + ", " + geocache.getLongitude());
        } else {
            Log.d("GeocacheDetails", "Geocache is null"); // 添加空值检查日志
        }

        return true; // 返回 true 表示事件已处理
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(TAG, "开始拖拽");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(TAG, "拖拽中...");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        showMsg("拖拽完成");
    }

    /**
     * 修改内容
     *
     * @param marker
     * @return
     */
    @Override
    public View getInfoContents(Marker marker) {
        View infoContent = getLayoutInflater().inflate(
                R.layout.custom_info_contents, null);
        render(marker, infoContent);
        return infoContent;
    }

    /**
     * 修改背景
     *
     * @param marker
     */
    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = getLayoutInflater().inflate(
                R.layout.custom_info_window, null);
        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * 渲染
     *
     * @param marker
     * @param view
     */
    private void render(Marker marker, View view) {
        // 设置徽章图片
        ((ImageView) view.findViewById(R.id.badge))
                .setImageResource(R.mipmap.ic_location);

        // 修改InfoWindow标题内容和样式
        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (title != null) {
            SpannableString titleText = new SpannableString(title);
            titleUi.setText(titleText);
        } else {
            titleUi.setText("");
        }

        // 修改InfoWindow片段内容和样式
        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }

        // 设置“导航”按钮的点击监听器
        Button navigateButton = view.findViewById(R.id.btn_navigate);
        navigateButton.setOnClickListener(v -> {
            // 获取经纬度信息
            double latitude = marker.getPosition().latitude;
            double longitude = marker.getPosition().longitude;

            Log.d("NavigationButton", "Latitude: " + latitude + ", Longitude: " + longitude);
            Toast.makeText(view.getContext(), "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();

            // 创建Intent对象，跳转到RouteActivity
            Intent intent = new Intent(view.getContext(), RouteActivity.class);
            // 将经纬度信息作为Extra传递
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            // 启动RouteActivity
            view.getContext().startActivity(intent);
        });

        // 设置“详情”按钮的点击监听器
        Button detailsButton = view.findViewById(R.id.btn_details);
        detailsButton.setOnClickListener(v -> {
            // 获取 Marker 关联的 Geocache 对象
            Geocache geocache = (Geocache) marker.getObject();
            if (geocache == null) {
                Toast.makeText(view.getContext(), "Geocache 数据为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (geocache != null) {
                // 创建 Intent，跳转到 GeocacheDetailActivity
                Intent intent = new Intent(view.getContext(), GeocacheDetailActivity.class);
                // 传递 Geocache 对象
                intent.putExtra("geocache", geocache);
                // 启动 GeocacheDetailActivity
                view.getContext().startActivity(intent);
            } else {
                Toast.makeText(view.getContext(), "Geocache 数据为空", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * InfoWindow点击事件
     *
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        showMsg("Window Content: name：" + marker.getTitle() + "\ntype：" + marker.getSnippet());
    }

}