package com.example.locationmock;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "SetLocation";
	private LocationManager mLocationManager;
	private Context mContext;
	private String mMockProviderName = LocationManager.GPS_PROVIDER;
	private double mLongitude = 116.395636;
	private double mLatitude = 39.929983;
	private Button mSetBtn;
	private Button mUpBtn;
	private Button mDownBtn;
	private EditText mLongitudeEdit;
	private EditText mLatitudeEdit;
	private TextView mStepTxt;
	private RelativeLayout mFloatLayout;
	private WindowManager.LayoutParams wmParams;
	private WindowManager mWindowManager;
	private double stepLength = 0.000007;
	private double stepOnce = 0.0000005;
	private boolean isStart = false;
	private double curBearing = 180;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		mLocationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);

		mLongitudeEdit = (EditText) findViewById(R.id.txt_id_Longitude);
		mLatitudeEdit = (EditText) findViewById(R.id.txt_id_Latitude);
		mSetBtn = (Button) findViewById(R.id.btn_id_set);
		mUpBtn = (Button) findViewById(R.id.btn_id_upstep);
		mDownBtn = (Button) findViewById(R.id.btn_id_downstep);
		mStepTxt = (TextView) findViewById(R.id.txt_id_stepLength);
		
		DisplayMetrics dm2 = getResources().getDisplayMetrics();

		GlobalValue.sWinH = dm2.heightPixels;
		GlobalValue.sWinW = dm2.widthPixels;

		try {
			String providerStr = LocationManager.GPS_PROVIDER;
			LocationProvider provider = mLocationManager
					.getProvider(providerStr);
			Log.e(TAG, "1");
			if (provider != null) {
				Log.e(TAG, "2");
				mLocationManager.addTestProvider(provider.getName(),
						provider.requiresNetwork(),
						provider.requiresSatellite(), provider.requiresCell(),
						provider.hasMonetaryCost(),
						provider.supportsAltitude(), provider.supportsSpeed(),
						provider.supportsBearing(),
						provider.getPowerRequirement(), provider.getAccuracy());
			} else {
				Log.e(TAG, "3");
				mLocationManager.addTestProvider(providerStr, true, true,
						false, false, true, true, true, Criteria.POWER_HIGH,
						Criteria.ACCURACY_FINE);
			}
			mLocationManager.setTestProviderEnabled(providerStr, true);
			mLocationManager.setTestProviderStatus(providerStr,
					LocationProvider.AVAILABLE, null,
					System.currentTimeMillis());

		} catch (SecurityException e) {
			Log.e(TAG, "error");
		}

		// mStartBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// Log.i(TAG, "mStartBtn onClick");
		// new Thread(new RunnableMockLocation()).start();
		// mStartBtn.setClickable(false);
		//
		// }
		// });
		createFloatView();
		updateLocation();
		mSetBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i(TAG, "mStartBtn onClick");
				try {
					mLongitude = Double.valueOf(mLongitudeEdit.getText()
							.toString());
					mLatitude = Double.valueOf(mLatitudeEdit.getText()
							.toString());
					Toast.makeText(getApplicationContext(),
							"经度:" + mLongitude + ",维度:" + mLatitude,
							Toast.LENGTH_SHORT).show();

				} catch (NumberFormatException e) {
				}
			}
		});
		
		mUpBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stepLength += stepOnce;
				mStepTxt.setText("当前步长:"+(int)(stepLength/stepOnce));
			}
		});
		
		mDownBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stepLength -= stepOnce;
				stepLength = stepLength < stepOnce ? stepOnce :stepLength;
				mStepTxt.setText("当前步长:"+(int)(stepLength/stepOnce));
			}
		});
	}

	private class RunnableMockLocation implements Runnable {

		@SuppressLint("NewApi") @Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(50);

					if (isStart == false)
						break;
					updateLocation();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 停止模拟位置，以免启用模拟数据后无法还原使用系统位置 若模拟位置未开启，则removeTestProvider将会抛出异常；
	 * 若已addTestProvider后，关闭模拟位置，未removeTestProvider将导致系统GPS无数据更新；
	 */
	public void stopMockLocation() {
		try {
			mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			// 若未成功addTestProvider，或者系统模拟位置已关闭则必然会出错
		}
	}

	private void createFloatView() {
		wmParams = new WindowManager.LayoutParams();
		// 获取WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) getApplication().getSystemService(
				getApplication().WINDOW_SERVICE);
		// 设置window type
		wmParams.type = LayoutParams.TYPE_PHONE;
		// 设置图片格式，效果为背景透明
		wmParams.format = PixelFormat.RGB_888;
		// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
		wmParams.flags =
		// LayoutParams.FLAG_NOT_TOUCH_MODAL |
		LayoutParams.FLAG_NOT_FOCUSABLE
		// LayoutParams.FLAG_NOT_TOUCHABLE
		;

		// 调整悬浮窗显示的停靠位置为中央
		wmParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

		// 以屏幕左上角为原点，设置x、y初始值
		wmParams.x = 0;
		wmParams.y = 0;

		// 设置悬浮窗口长宽数据
		// wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		// wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		int sqrLength = Math.min((int)GlobalValue.sWinH / 15, (int)GlobalValue.sWinH / 15);
		wmParams.width = sqrLength;
		wmParams.height = sqrLength;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// 获取浮动窗口视图所在布局
		try {
			mFloatLayout = (RelativeLayout) inflater.inflate(
					R.layout.cam_for_server, null);
			// 添加mFloatLayout
			mWindowManager.addView(mFloatLayout, wmParams);
		} catch (java.lang.RuntimeException e) {
			return;
		}

		mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		// 设置监听浮动窗口的触摸移动
		mFloatLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i(TAG, "onTouch");
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_UP:
					isStart = false;
					mFloatLayout.setBackgroundColor(Color.BLACK);
					break;
				case MotionEvent.ACTION_MOVE:
					double dy = event.getRawY() - GlobalValue.sWinH / 2;
					double dx = event.getRawX() - GlobalValue.sWinW / 2;
					double length = Math.sqrt(dx * dx + dy * dy);

					double dLocalY = -stepLength / length * dy;
					double dLocalX = stepLength / length * dx;

					curBearing = Math.toDegrees(Math.atan(-dLocalX / dLocalY));

					mLatitude += dLocalY;
					mLongitude += dLocalX;

					Log.i(TAG, "event.getRawY()=" + event.getRawY()
							+ ",event.getRawX()=" + event.getRawX()
							+ ",GlobalValue.sWinH / 2=" + GlobalValue.sWinH / 2
							+ ",GlobalValue.sWinW / 2=" + GlobalValue.sWinW / 2
							+ ",length=" + length + ",mLatitude=" + mLatitude
							+ ",mLongitude" + mLongitude + ",dLocalY="
							+ dLocalY + ",dLocalX=" + dLocalX + ",curBearing"
							+ curBearing);

					break;
				case MotionEvent.ACTION_DOWN:
					isStart = true;
					new Thread(new RunnableMockLocation()).start();
					mFloatLayout.setBackgroundColor(Color.WHITE);
					break;
				default:
					break;
				}

				return false;
			}
		});

		mFloatLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onClick");
			}
		});
	}
	
	@SuppressLint("NewApi") private void updateLocation()
	{
		try {
			// 模拟位置（addTestProvider成功的前提下）
			String providerStr = LocationManager.GPS_PROVIDER;
			Location mockLocation = new Location(providerStr);
			mockLocation.setLatitude(mLatitude); // 维度（度）
			mockLocation.setLongitude(mLongitude); // 经度（度）
			mockLocation.setAltitude(30); // 高程（米）
			mockLocation.setBearing((float) curBearing); // 方向（度）
			mockLocation.setSpeed(5); // 速度（米/秒）
			mockLocation.setAccuracy(0.1f); // 精度（米）
			mockLocation.setTime(new Date().getTime()); // 本地时间
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				mockLocation.setElapsedRealtimeNanos(SystemClock
						.elapsedRealtimeNanos());
			}
			mLocationManager.setTestProviderLocation(providerStr,
					mockLocation);
		} catch (Exception e) {
			// 防止用户在软件运行过程中关闭模拟位置或选择其他应用
			stopMockLocation();
		}
	}

}
