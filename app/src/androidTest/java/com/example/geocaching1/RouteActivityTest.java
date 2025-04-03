package com.example.geocaching1;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class RouteActivityTest {

    @Mock private AMap mockAMap;
    @Mock private RouteSearch mockRouteSearch;
    @Mock private GeocodeSearch mockGeocodeSearch;
    @Mock private InputMethodManager mockInputMethodManager;
    @Mock private LocationSource.OnLocationChangedListener mockLocationListener;

    private ActivityScenario<RouteActivity> scenario;

    @Before
    public void setUp() {
        // 初始化Mockito
        MockitoAnnotations.openMocks(this);

        // 模拟系统服务
        Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                .thenReturn(mockInputMethodManager);

        // 启动Activity并注入mock对象
        scenario = ActivityScenario.launch(RouteActivity.class);
        scenario.onActivity(activity -> {
            activity.setAMapForTesting(mockAMap);
            activity.setRouteSearchForTesting(mockRouteSearch);
            activity.setGeocodeSearchForTesting(mockGeocodeSearch);
        });
    }

    @Test
    public void testInitializationAndMapSetup() {
        scenario.onActivity(activity -> {
            // 验证地图基本设置
            verify(mockAMap).setMinZoomLevel(5);
            verify(mockAMap).showIndoorMap(true);
            verify(mockAMap).setLocationSource(any(LocationSource.class));
            verify(mockAMap).setMyLocationEnabled(true);

            // 验证路线搜索监听器设置
            verify(mockRouteSearch).setRouteSearchListener(activity);
        });
    }

    @Test
    public void testAddressInputAndRoutePlanning() {
        scenario.onActivity(activity -> {
            // 模拟输入起点和终点
            activity.binding.etStartAddress.setText("北京西站");
            activity.binding.etEndAddress.setText("北京南站");

            // 使用Espresso模拟输入
            onView(withId(R.id.et_end_address))
                    .perform(typeText("北京南站"), closeSoftKeyboard());  // 输入文本并关闭键盘

            // 模拟按下回车键
            KeyEvent enterKeyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
            activity.onKey(activity.binding.etEndAddress, KeyEvent.KEYCODE_ENTER, enterKeyEvent);

            // 验证软键盘隐藏
            verify(mockInputMethodManager)
                    .hideSoftInputFromWindow(any(), any());

            // 验证地理编码查询
            verify(mockGeocodeSearch).getFromLocationNameAsyn(any());
        });
    }

    @Test
    public void testTravelModeSelection() {
        scenario.onActivity(activity -> {
            // 设置起点和终点，通过 setter 方法
            activity.setStartPoint(new LatLonPoint(39.90469, 116.40717)); // 北京
            activity.setEndPoint(new LatLonPoint(31.23037, 121.47370));   // 上海

            // 测试步行模式，通过 setter 方法设置 TRAVEL_MODE
            activity.setTravelMode(0);
            activity.startRouteSearch();
            verify(mockRouteSearch).calculateWalkRouteAsyn(any());

            // 测试骑行模式
            activity.setTravelMode(1);
            activity.startRouteSearch();
            verify(mockRouteSearch).calculateRideRouteAsyn(any());

            // 测试驾车模式
            activity.setTravelMode(2);
            activity.startRouteSearch();
            verify(mockRouteSearch).calculateDriveRouteAsyn(any());

            // 测试公交模式
            activity.setTravelMode(3);
            activity.startRouteSearch();
            verify(mockRouteSearch).calculateBusRouteAsyn(any());
        });
    }

    @Test
    public void testRouteResultHandling() {
        scenario.onActivity(activity -> {
            // 模拟步行路线结果
            WalkRouteResult walkResult = mock(WalkRouteResult.class);
            activity.onWalkRouteSearched(walkResult, 1000);

            // 验证地图清理和UI更新
            verify(mockAMap).clear();
            // 可以添加更多验证

            // 模拟驾车路线结果
            DriveRouteResult driveResult = mock(DriveRouteResult.class);
            activity.onDriveRouteSearched(driveResult, 1000);

            // 验证地图清理和UI更新
            verify(mockAMap).clear();
        });
    }

    @Test
    public void testInvalidInputHandling() {
        scenario.onActivity(activity -> {
            // 测试空起点
            activity.binding.etStartAddress.setText("");
            activity.binding.etEndAddress.setText("北京南站");

            KeyEvent enterKeyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
            boolean handled = activity.onKey(activity.binding.etEndAddress, KeyEvent.KEYCODE_ENTER, enterKeyEvent);

            assertTrue(handled);
            // 可以验证Toast显示

            // 测试空终点
            activity.binding.etStartAddress.setText("北京西站");
            activity.binding.etEndAddress.setText("");

            handled = activity.onKey(activity.binding.etEndAddress, KeyEvent.KEYCODE_ENTER, enterKeyEvent);

            assertTrue(handled);
            // 可以验证Toast显示
        });
    }
}

