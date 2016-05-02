package cn.demo.gpsdemo;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //分别显示GPS的事件和位置信息；
    EditText mEditTextEvent,mEditTextLocation;
    //GPS的位置服务
    LocationManager mLocationManager;
    //GPS状态的监听器
    GpsStatus.Listener  mStatusListener;
    //GPS位置服务的监听器
    LocationListener mLocationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initGPS();
    }

    private void initGPS() {
        // 创建LocationManager对象
        mLocationManager = (LocationManager) getSystemService
                (Context.LOCATION_SERVICE);
        // 从GPS获取最近的最近的定位信息
        Location location;
        //targetSdkVersion >=23 时 ，在AS中需要调用下面语句，否则部分编译不能通过
        //if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
        // != PackageManager.PERMISSION_GRANTED &&
        // ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        // != PackageManager.PERMISSION_GRANTED) {    //......
        // }
        // 本次 minSdkVersion 15,targetSdkVersion 23，不提示需要检查权限问题，故if语句忽略不计
        if(android.os.Build.VERSION.SDK_INT >= 23){
        }else{
        }

        //检查当前可以使用的位置
        browseCriteria();

        //创建一个GPS状态的监听器
        mStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                switch (event){
                    // 第一次定位
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                       displayGPSEvent("第一次定位");
                        break;
                    // 卫星状态改变
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        Log.i(TAG, "卫星状态改变");
                        displayGPSEvent("卫星状态改变");
                        // 获取当前状态
                        GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                        // 获取卫星颗数的默认最大值
                        int maxSatellites = gpsStatus.getMaxSatellites();
                        // 创建一个迭代器保存所有卫星
                        Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
                                .iterator();
                        int count = 0;
                        while (iters.hasNext() && count <= maxSatellites) {
                            GpsSatellite s = iters.next();
                            count++;
                        }
                        Log.d(TAG, "搜索到：" + count + "颗卫星");
                        displayGPSEvent( "搜索到：" + count + "颗卫星");
                        break;
                    // 定位启动
                    case GpsStatus.GPS_EVENT_STARTED:
                        Log.i(TAG, "定位启动");
                        displayGPSEvent( "定位启动");
                        break;
                    // 定位结束
                    case GpsStatus.GPS_EVENT_STOPPED:
                        Log.i(TAG, "定位结束");
                        displayGPSEvent( "定位结束");
                        break;
                }
            };
        };

        //创建一个GPS位置服务的监听器
        mLocationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // 当GPS定位信息发生改变时，更新位置
                diaplayLocationView(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                // 当GPS LocationProvider可用时，更新位置
                diaplayLocationView(mLocationManager.getLastKnownLocation(provider));
            }

            @Override
            public void onProviderDisabled(String provider) {
                diaplayLocationView(null);
            }
        };

        //获取位置服务中的的GPS服务的提供者（GPS服务提供者、网络方式提供、混合方式的APGS等方式）
        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = mLocationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER);
            //如果启用了GPS方式
            //监听位置变化
            // 绑定监听，有4个参数
            // 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
            // 参数2，位置信息更新周期，单位毫秒
            // 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
            // 参数4，监听
            // 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

            // 1秒更新一次，或最小位移变化超过1米更新一次；
            // 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 8, mLocationListener);
        }
        else{
            location = mLocationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER);
            //如果启用了网络方式
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 8, mLocationListener);
        }

        // 使用location来更新EditText的显示
        diaplayLocationView(location);
    }

    private void initView() {
        mEditTextEvent= (EditText) findViewById(R.id.edittextEvent);
        mEditTextEvent.setText("GPS定位事件：......");
        mEditTextLocation= (EditText) findViewById(R.id.edittextLocation);
        mEditTextLocation.setText("实时的位置信息：......");
    }
    public void displayGPSEvent(String text){
        String newText=mEditTextEvent.getText().toString()+"\n"+text;
        mEditTextEvent.setText(newText);
    }

    // 显示指定位置实例的内容
    public void diaplayLocationView(Location newLocation) {
        if (newLocation != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("实时的位置信息：\n");
            sb.append("经度：");
            sb.append(newLocation.getLongitude());
            sb.append("\n纬度：");
            sb.append(newLocation.getLatitude());
            sb.append("\n高度：");
            sb.append(newLocation.getAltitude());
            sb.append("\n速度：");
            sb.append(newLocation.getSpeed());
            sb.append("\n方向：");
            sb.append(newLocation.getBearing());
            mEditTextLocation.setText(sb.toString());
        } else {
            // 如果传入的Location对象为空则清空EditText
            mEditTextLocation.setText("实时的位置信息：......");
        }
    }
        /**
         * 返回当前检测到的GPS服务提供者
         *
         */
    private void  browseCriteria() {
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // 创建一个LocationProvider的过滤条件
        Criteria cri = new Criteria();
        // 设置要求LocationProvider必须是免费的。
        cri.setCostAllowed(false);
        // 设置要求LocationProvider能提供高度信息
        cri.setAltitudeRequired(true);
        // 设置要求LocationProvider能提供方向信息
        cri.setBearingRequired(true);
        // 获取系统所有复合条件的LocationProvider的名称
        List<String> providerNames = mLocationManager.getProviders(cri , false);
        StringBuilder sb=new StringBuilder();
        sb.append("当前检测的到GPS服务有:\n");
        for(String item : providerNames){
            sb.append(item+"\n");
        }
        displayGPSEvent(sb.toString());

    }
}
