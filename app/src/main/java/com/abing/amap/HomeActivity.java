package com.abing.amap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.abing.amap.utils.AutoStartService;
import com.abing.amap.utils.Const;
import com.abing.amap.utils.FileUtil;
import com.abing.amap.utils.GPSUtil;
import com.abing.amap.utils.SensorEventHelper;
import com.abing.amap.utils.TextSpeechUtil;
import com.abing.amap.utils.nicedialog.BaseNiceDialog;
import com.abing.amap.utils.nicedialog.NiceDialog;
import com.abing.amap.utils.nicedialog.ViewConvertListener;
import com.abing.amap.utils.nicedialog.ViewHolder;
import com.abing.amap.utils.updatemanager.UpdateManager;
import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 定位图标箭头指向手机朝向
 */
public class HomeActivity extends AppCompatActivity implements LocationSource,
        AMapLocationListener, AMap.OnMapClickListener, View.OnClickListener, GeoFenceListener {
    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;

    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private boolean mFirstFix = false;
    private Marker mLocMarker;
    private SensorEventHelper mSensorHelper;
    private Circle mCircle;
    public static final String LOCATION_MARKER_FLAG = "mylocation";


    // 多边形围栏的边界点
    private List<LatLng> polygonPoints = new ArrayList<LatLng>();

    private List<Marker> markerList = new ArrayList<Marker>();

    // 当前的坐标点集合，主要用于进行地图的可视区域的缩放
    private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

    private BitmapDescriptor bitmap = null;
    private MarkerOptions markerOption = null;

    // 地理围栏客户端
    private GeoFenceClient fenceClient = null;

    // 触发地理围栏的行为，默认为进入提醒
    private int activatesAction = GeoFenceClient.GEOFENCE_IN;
    // 地理围栏的广播action
    private static final String GEOFENCE_BROADCAST_ACTION = "com.example.geofence.polygon";

    // 记录已经添加成功的围栏
    private HashMap<String, GeoFence> fenceMap = new HashMap<String, GeoFence>();
    List<GeoFence> fenceList = new ArrayList<GeoFence>();
    private Button but_addFence;
    private Button but_clearFence;
    private Button but_offLine;
    private Button but_videoDialog;
    private Button but_heZhunZheng;
    private Button but_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
        setContentView(R.layout.activity_home);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        fenceClient = new GeoFenceClient(getApplicationContext());
        bitmap = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        markerOption = new MarkerOptions().icon(bitmap).draggable(true);

        init();
//        initSpeech();
    }

    /**
     * 初始化
     */
    private void init() {


        Intent intent = new Intent(HomeActivity.this, AutoStartService.class);
        startService(intent);
        but_addFence = (Button) findViewById(R.id.but_addFence);
        but_clearFence = (Button) findViewById(R.id.but_clearFence);
        but_offLine = (Button) findViewById(R.id.but_offLine);
        but_videoDialog = (Button) findViewById(R.id.but_videoDialog);
        but_heZhunZheng = (Button) findViewById(R.id.but_heZhunZheng);
        but_setting = (Button) findViewById(R.id.but_setting);
        but_addFence.setOnClickListener(this);
        but_clearFence.setOnClickListener(this);
        but_offLine.setOnClickListener(this);
        but_videoDialog.setOnClickListener(this);
        but_heZhunZheng.setOnClickListener(this);
        but_setting.setOnClickListener(this);


        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.getUiSettings().setRotateGesturesEnabled(true);
            aMap.getUiSettings().setLogoBottomMargin(-100);
            aMap.moveCamera(CameraUpdateFactory.zoomBy(6));
            aMap.setLocationSource(this);// 设置定位监听
            aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        }
        polygonPoints = new ArrayList<LatLng>();
        mSensorHelper = new SensorEventHelper(this);
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }


        aMap.setOnMapClickListener(this);


        IntentFilter fliter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        fliter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, fliter);
        /**
         * 创建pendingIntent
         */
        fenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        fenceClient.setGeoFenceListener(this);
        /**
         * 设置地理围栏的触发行为,默认为进入
         */
        fenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN);



        UpdateManager updateManager = new UpdateManager();
        updateManager.checkAppUpdate(HomeActivity.this, false, FileUtil.getSDPath()+"/aaacheckupdate.xml");
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        mapView.onPause();
        deactivate();
        mFirstFix = false;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocMarker != null) {
            mLocMarker.destroy();
        }
        mapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }

        try {
            unregisterReceiver(mGeoFenceReceiver);
        } catch (Throwable e) {
        }
        if (null != fenceClient) {
            fenceClient.removeGeoFence();
        }


    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {

                LatLng location = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
//                LatLng location1 = new LatLng(32.136706,118.687833);
                if (!mFirstFix) {
                    mFirstFix = true;
                    addCircle(location, amapLocation.getAccuracy() / 2);//添加定位精度圆
                    addMarker(location);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
                } else {
                    mCircle.setCenter(location);
                    mCircle.setRadius(amapLocation.getAccuracy());
                    mLocMarker.setPosition(location);
//                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(location));
                }
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
//                Toast.makeText(HomeActivity.this, errText, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            mLocationOption.setGpsFirst(true);
//            mLocationOption.setWifiScan(false);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addMarker(LatLng latlng) {
        if (mLocMarker != null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.navi_map_gps_locked)));
        options.anchor(0.5f, 0.5f);
        options.position(latlng);
        mLocMarker = aMap.addMarker(options);
//        mLocMarker.setTitle(LOCATION_MARKER_FLAG);
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        if (null == polygonPoints) {
//            polygonPoints = new ArrayList<LatLng>();
//        }
//        polygonPoints.add(latLng);
//        addPolygonMarker(latLng);
//        Toast.makeText(getApplicationContext(), "已选择" + polygonPoints.size() + "个点", Toast.LENGTH_LONG)
//                .show();
    }

    // 添加多边形的边界点marker
    private void addPolygonMarker(LatLng latlng) {
        markerOption.position(latlng);
        Marker marker = aMap.addMarker(markerOption);
        markerList.add(marker);
    }

    private void removeMarkers() {
        if (null != markerList && markerList.size() > 0) {
            for (Marker marker : markerList) {
                marker.remove();
            }
            markerList.clear();
        }
    }

    /**
     * 接收触发围栏后的广播,当添加围栏成功之后，会立即对所有围栏状态进行一次侦测，如果当前状态与用户设置的触发行为相符将会立即触发一次围栏广播；
     * 只有当触发围栏之后才会收到广播,对于同一触发行为只会发送一次广播不会重复发送，除非位置和围栏的关系再次发生了改变。
     */
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收广播
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
                String customId = bundle
                        .getString(GeoFence.BUNDLE_KEY_CUSTOMID);
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                //status标识的是当前的围栏状态，不是围栏行为
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                StringBuffer sb = new StringBuffer();
                switch (status) {
                    case GeoFence.STATUS_LOCFAIL:
                        sb.append("定位失败");
                        break;
                    case GeoFence.STATUS_IN:
                        sb.append("进入围栏 ");
                        break;
                    case GeoFence.STATUS_OUT:
                        sb.append("离开围栏 ");
                        break;
                    case GeoFence.STATUS_STAYED:
                        sb.append("停留在围栏内 ");
                        break;
                    default:
                        break;
                }
                if (status != GeoFence.STATUS_LOCFAIL) {
                    if (!TextUtils.isEmpty(customId)) {
                        sb.append(" customId: " + customId);
                    }
                    sb.append(" fenceId: " + fenceId);
                }
                String str = sb.toString();
                Message msg = Message.obtain();
                msg.obj = str;
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    StringBuffer sb = new StringBuffer();
                    sb.append("添加围栏成功");
                    String customId = (String) msg.obj;
                    if (!TextUtils.isEmpty(customId)) {
                        sb.append("customId: ").append(customId);
                    }
                    Toast.makeText(getApplicationContext(), sb.toString(),
                            Toast.LENGTH_SHORT).show();
                    drawFence2Map();
                    break;
                case 1:
                    int errorCode = msg.arg1;
                    Toast.makeText(getApplicationContext(),
                            "添加围栏失败 " + errorCode, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    String statusStr = (String) msg.obj;
                    Toast.makeText(getApplicationContext(), statusStr, Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
            }
        }
    };
    Object lock = new Object();

    void drawFence2Map() {
        new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        if (null == fenceList || fenceList.isEmpty()) {
                            return;
                        }
                        for (GeoFence fence : fenceList) {
                            if (fenceMap.containsKey(fence.getFenceId())) {
                                continue;
                            }
                            drawFence(fence);
                            fenceMap.put(fence.getFenceId(), fence);
                        }
                    }
                } catch (Throwable e) {

                }
            }
        }.start();
    }

    private void drawFence(GeoFence fence) {
        switch (fence.getType()) {
            case GeoFence.TYPE_ROUND:
            case GeoFence.TYPE_AMAPPOI:
                drawCircle(fence);
                break;
            case GeoFence.TYPE_POLYGON:
            case GeoFence.TYPE_DISTRICT:
                drawPolygon(fence);
                break;
            default:
                break;
        }

        // 设置所有maker显示在当前可视区域地图中
        LatLngBounds bounds = boundsBuilder.build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        polygonPoints.clear();
        removeMarkers();
    }

    private void drawCircle(GeoFence fence) {
        LatLng center = new LatLng(fence.getCenter().getLatitude(),
                fence.getCenter().getLongitude());
        // 绘制一个圆形
        aMap.addCircle(new CircleOptions().center(center)
                .radius(fence.getRadius()).strokeColor(Const.STROKE_COLOR)
                .fillColor(Const.FILL_COLOR).strokeWidth(Const.STROKE_WIDTH));
        boundsBuilder.include(center);
    }

    private void drawPolygon(GeoFence fence) {
        final List<List<DPoint>> pointList = fence.getPointList();
        if (null == pointList || pointList.isEmpty()) {
            return;
        }
        for (List<DPoint> subList : pointList) {
            List<LatLng> lst = new ArrayList<LatLng>();

            PolygonOptions polygonOption = new PolygonOptions();
            for (DPoint point : subList) {
                lst.add(new LatLng(point.getLatitude(), point.getLongitude()));
                boundsBuilder.include(
                        new LatLng(point.getLatitude(), point.getLongitude()));
            }
            polygonOption.addAll(lst);

            polygonOption.strokeColor(Const.STROKE_COLOR)
                    .fillColor(Const.FILL_COLOR).strokeWidth(Const.STROKE_WIDTH);
            aMap.addPolygon(polygonOption);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_offLine:
                //在Activity页面调用startActvity启动离线地图组件
                startActivity(new Intent(HomeActivity.this,
                        com.amap.api.maps.offlinemap.OfflineMapActivity.class));
                break;
            case R.id.but_addFence:
                addFence();
                break;
            case R.id.but_clearFence:
                //会清除所有围栏
//                if (null != fenceClient) {
//                    fenceClient.removeGeoFence();
//                Toast.makeText(HomeActivity.this, "清除围栏", Toast.LENGTH_LONG).show();
////                }
//
////                fenceMap.clear();
//                Log.d("fenceList", fenceList.toString());
//                for (GeoFence fence : fenceList) {
//                    if (fenceMap.containsKey(fence.getFenceId())) {
//                        fenceClient.removeGeoFence(fence);
//                    }
//                }
//                mMapView.invalidate();
//                mMapView.postInvalidate();
//                mMapView.requestLayout();
//                mAMap.clear();//刷新地图
                break;
            case R.id.but_heZhunZheng:
                Toast.makeText(HomeActivity.this, "敬请期待", Toast.LENGTH_LONG).show();
                break;
            case R.id.but_videoDialog:
                videoDialog();
                break;
            case R.id.but_setting:
                Toast.makeText(HomeActivity.this, "敬请期待", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onGeoFenceCreateFinished(final List<GeoFence> geoFenceList,
                                         int errorCode, String customId) {
        Message msg = Message.obtain();
        if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
            fenceList = geoFenceList;
            msg.obj = customId;
            msg.what = 0;
        } else {
            msg.arg1 = errorCode;
            msg.what = 1;
        }
        handler.sendMessage(msg);
    }

    /**
     * 添加围栏
     *
     * @author hongming.wang
     * @since 3.2.0
     */
    private void addFence() {
        addPolygonFence();
    }


    /**
     * 添加多边形围栏
     *
     * @author hongming.wang
     * @since 3.2.0
     */
    private void addPolygonFence() {
        String customId = "";
        if (null == polygonPoints || polygonPoints.size() < 3) {
            Toast.makeText(getApplicationContext(), "参数不全,至少3个点", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        List<DPoint> pointList = new ArrayList<DPoint>();
        for (LatLng latLng : polygonPoints) {
            pointList.add(new DPoint(latLng.latitude, latLng.longitude));
        }
        fenceClient.addGeoFence(pointList, customId);
    }

    /**
     * 点击行车视频弹框
     */
    private void videoDialog() {
        NiceDialog.init()
                .setLayoutId(R.layout.dialog_carvideo)
                .setConvertListener(new ViewConvertListener() {
                    @Override
                    public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
                        holder.getView(R.id.image_close).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        holder.getView(R.id.but_xingChe).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                startActivityForResult(new Intent(HomeActivity.this, CameraActivity.class), Const.REQUEST_CAMERA);
                            }
                        });
                        holder.getView(R.id.but_cheXiang).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                startActivityForResult(new Intent(HomeActivity.this, CameraActivity.class), Const.REQUEST_CAMERA);
                            }
                        });
                        holder.getView(R.id.but_youZhuan).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                startActivityForResult(new Intent(HomeActivity.this, CameraActivity.class), Const.REQUEST_CAMERA);
                            }
                        });
                        holder.getView(R.id.but_daoChe).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                startActivityForResult(new Intent(HomeActivity.this, CameraActivity.class), Const.REQUEST_CAMERA);
                            }
                        });

                    }
                })
                .setOutCancel(true)
                .setAnimStyle(R.style.DefaultAnimation)
                .show(getSupportFragmentManager());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Const.REQUEST_CAMERA) {

        }
    }


    /**
     * 初始化语音播报
     */
    private void initSpeech() {

        final TextSpeechUtil textSpeechUtil=new TextSpeechUtil(HomeActivity.this);
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                textSpeechUtil.toSpeech("你好，欢迎来到智能渣土车管控系统！");
            }
        }.start();

    }

}