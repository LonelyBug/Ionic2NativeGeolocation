package com.sx.family;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GPSActivity extends CordovaPlugin {
private static final String STOP_ACTION = "stop";
	private static final String GET_ACTION = "getCurrentPosition";
	public JSONObject jsonObj = new JSONObject();
	public boolean result = false;
	public CallbackContext callbackContext;
	@Override
	public boolean execute(String action, JSONArray args,
  			final CallbackContext callbackContext) {
  		setCallbackContext(callbackContext);
  		if (GET_ACTION.equals(action)) {
  			cordova.getActivity().runOnUiThread(new Runnable() {
  				@Override
  				public void run() {
            startGpsLocation();
  				}

  			});
  			return true;
  		} else if (STOP_ACTION.equals(action)) {

  			callbackContext.success(200);
  			return true;
  		} else {
  			callbackContext
  					.error(PluginResult.Status.INVALID_ACTION.toString());
  		}

  		while (result == false) {
  			try {
  				Thread.sleep(100);
  			} catch (InterruptedException e) {
  				e.printStackTrace();
  			}
  		}
  		return result;
  	}

	public static final String SP_NAME = "config";
	public static final String SP_KEY = "location";

	protected LocationManager locationManager;
	protected MyLoactionListener locationListener; // 单例

	public Location startGpsLocation() {
		String serviceName = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) GPSActivity.this.getSystemService(serviceName);

		checkGPSEnabled(locationManager);

		String provider = getProvider(locationManager);
		Toast.makeText(GPSActivity.this, provider, Toast.LENGTH_LONG).show();
		Log.i(GPSActivity.this.getPackageName(), "=========当前最好提供位置的服务：" + provider);
		if(provider == null){
			Toast.makeText(GPSActivity.this, "无法定位，请在户外打开GPS", Toast.LENGTH_LONG).show();
			return null;
		}
		if(!"gps".equals(provider)){
			Toast.makeText(GPSActivity.this, "GPS 没有开启", Toast.LENGTH_LONG).show();
		}

		Location location = locationManager.getLastKnownLocation(provider);
		//更新位置
		updateWithNewLocation(location);
		 //注册位置更新监听(最小时间间隔为5秒,最小距离间隔为5米)
		locationManager.requestLocationUpdates(provider, 2000, 1,getListener());
		return location;
	}

	/**
	 * 监测GPS是否开启
	 * @return
	 */
	private boolean checkGPSEnabled(LocationManager locationManager){
		boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(!gpsEnabled){
			AlertDialog.Builder builder = new AlertDialog.Builder(GPSActivity.this);
			builder.setTitle("提示").setMessage("GPS未开启，是否马上设置？").setPositiveButton("设置", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();
					intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					GPSActivity.this.startActivity(intent);
				}
			}).setNegativeButton("取消", null);
			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}


		return gpsEnabled;
	}
	/**
	 * @param manager
	 *            位置管理服务
	 * @return 最好的位置提供者
	 */
	private String getProvider(LocationManager manager) {
		// 设置查询条件
		Criteria criteria = new Criteria();
		// 定位精准度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// 对海拔是否敏感
		criteria.setAltitudeRequired(false);
		//不要求方位信息
		criteria.setBearingRequired(false);
		// 对手机耗电性能要求（获取频率）
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		// 对速度变化是否敏感
		criteria.setSpeedRequired(false);
		// 是否运行产生开销（费用）
		criteria.setCostAllowed(true);
		// 返回最合适的符合条件的provider 如果置为ture只会返回当前打开的gps设备
		// 如果置为false如果设备关闭也会返回
		return manager.getBestProvider(criteria, true);
	}

	private void updateWithNewLocation(Location location) {
		if (location == null) {
			return;
		try{
        JSONObject coords = new JSONObject();
        coords.put("latitude", location.getLatitude());
        coords.put("longitude", location.getLongitude());
        jsonObj.put("coords", coords);
        callbackContext.success(jsonObj);
        result = true;
		} catch(Exception e){
        callbackContext.error(e.getMessage());
				result = true;
		}
	}

	private synchronized MyLoactionListener getListener() {
		if (locationListener == null) {
			synchronized (cordova.getActivity()) {
				if (locationListener == null) {
					locationListener = new MyLoactionListener();
				}
			}

		}
		return locationListener;
	}

	private class MyLoactionListener implements LocationListener {

		/**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
        	updateWithNewLocation(location);
//            Log.i(TAG, "时间："+location.getTime());
//            Log.i(TAG, "经度："+location.getLongitude());
//            Log.i(TAG, "纬度："+location.getLatitude());
//            Log.i(TAG, "海拔："+location.getAltitude());
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
            //GPS状态为可见时
            case LocationProvider.AVAILABLE:
//                Log.i(TAG, "当前GPS状态为可见状态");
                break;
            //GPS状态为服务区外时
            case LocationProvider.OUT_OF_SERVICE:
//                Log.i(TAG, "当前GPS状态为服务区外状态");
                break;
            //GPS状态为暂停服务时
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                Log.i(TAG, "当前GPS状态为暂停服务状态");
                break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
        	String serviceName = Context.LOCATION_SERVICE;
        	LocationManager lm = (LocationManager) getSystemService(serviceName);
            Location location=lm.getLastKnownLocation(provider);
            updateWithNewLocation(location);
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
        	updateWithNewLocation(null);
        }


	};
}
