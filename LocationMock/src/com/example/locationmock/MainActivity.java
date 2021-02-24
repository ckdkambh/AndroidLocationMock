package com.example.locationmock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.locationmock.GlobalValue;


public class MainActivity extends Activity {
	private static final String TAG = "SetLocation";
	private LocationManager mLocationManager;
	private Context mContext;
	private String mMockProviderName = LocationManager.GPS_PROVIDER;
	private double mXpos = GlobalValue.defaultLongitude;
	private double mYpos = GlobalValue.defaultLatitude;
	private Button mSetBtn;
	private Button mUpBtn;
	private Button mDownBtn;
	private Button mStoreCurLocBtn;
	private Button mGetStoreLocBtn;
	private Button mEmptyStoreLocBtn;
	private Button mFloatWinSwitchBtn;
	private Button mSetStartPointBtn;
	private Button mSetEndPointBtn;
	private Button mStartAutoWalkBtn;
	private EditText mLongitudeEdit;
	private EditText mLatitudeEdit;
	private TextView mStepTxt;
	private TextView mStartPosition;
	private TextView mEndPosition;
	private TextView mCurPosition;
	private RelativeLayout mFloatLayout;
	private WindowManager.LayoutParams wmParams;
	private WindowManager mWindowManager;
	private double stepLength = 0.00007;
	private double stepOnce = 0.000005;
	private boolean isFloatWinEnable = true;
	private double mStartXpos = 0.0;
	private double mStartYpos = 0.0;
	private double mEndXpos = 0.0;
	private double mEndYpos = 0.0;	
	private boolean dirFromStartToEnd = false;
	private double curBearing = 180;
	private List<String> dataNameList = new ArrayList<String>();
	private SharedPreferences preferences;
	private Editor editor;
	private static boolean isAutoRun = false;
	private static int walkTick = 0;//移位许可，因为定时器过快，不能每个周期都进行位移
	private static int count1 = 0;

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
		mStartPosition = (TextView) findViewById(R.id.txt_id_start_position);
		mEndPosition = (TextView) findViewById(R.id.txt_id_end_position);
		mCurPosition = (TextView) findViewById(R.id.txt_id_cur_position);
		mStoreCurLocBtn = (Button) findViewById(R.id.btn_id_storeCurLocate);
		mGetStoreLocBtn = (Button) findViewById(R.id.btn_id_chooseStoredLocation);
		mEmptyStoreLocBtn = (Button) findViewById(R.id.btn_id_emptyStoredLocation);
		mFloatWinSwitchBtn = (Button) findViewById(R.id.btn_id_floatWinSwitch);
		mSetStartPointBtn = (Button) findViewById(R.id.btn_id_set_start_point);
		mSetEndPointBtn = (Button) findViewById(R.id.btn_id_set_end_point);
		mStartAutoWalkBtn = (Button) findViewById(R.id.btn_id_start_auto_walk);
		
		mSetBtn.setFocusable(true);
		mSetBtn.setFocusableInTouchMode(true);
		mSetBtn.requestFocus();
		mSetBtn.requestFocusFromTouch();
		
		preferences = this.getSharedPreferences("t1", Context.MODE_PRIVATE);
		editor = preferences.edit();
		readNameList();
		if (dataNameList.contains("lastLocation")) {
			mXpos = preferences.getFloat("lastLocationLo", 0);
			mYpos = preferences.getFloat("lastLocationLa", 0);
		}

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

		createFloatView();
		updateLocation();
		mSetBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i(TAG, "mStartBtn onClick");
				try {
					mXpos = Double.valueOf(mLongitudeEdit.getText()
							.toString());
					mYpos = Double.valueOf(mLatitudeEdit.getText()
							.toString());
					Toast.makeText(getApplicationContext(),
							"经度:" + mXpos + ",维度:" + mYpos,
							Toast.LENGTH_SHORT).show();

				} catch (NumberFormatException e) {
				}
			}
		});
		mStepTxt.setText("当前步长:" + (int) (stepLength / stepOnce));
		mUpBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stepLength += stepOnce;
				mStepTxt.setText("当前步长:" + (int) (stepLength / stepOnce));
			}
		});

		mDownBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stepLength -= stepOnce;
				stepLength = stepLength < stepOnce ? stepOnce : stepLength;
				mStepTxt.setText("当前步长:" + (int) (stepLength / stepOnce));
			}
		});

		final Context ctxt = this;

		mStoreCurLocBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final EditText mNameEdit = new EditText(ctxt);
				Dialog alertDialog = new AlertDialog.Builder(ctxt)
						.setTitle("输入收藏当前地点名称")
						.setView(mNameEdit)
						.setIcon(R.drawable.ic_launcher)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										storeCurLocat(mNameEdit.getText()
												.toString());
										storeLastLocat();
										wirteNameList();
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).create();
				alertDialog.show();
			}
		});
		mFloatWinSwitchBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isFloatWinEnable)
				{
					mFloatLayout.setVisibility(View.GONE);
					isFloatWinEnable = false;
					mFloatWinSwitchBtn.setText("开启浮动窗");
				}
				else
				{
					mFloatLayout.setVisibility(View.VISIBLE);
					isFloatWinEnable = true;
					mFloatWinSwitchBtn.setText("关闭浮动窗");
				}
			}
			
		});
		mGetStoreLocBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final String[] strList = new String[dataNameList.size()];

				for (int i = 0; i < dataNameList.size(); i++) {
					strList[i] = dataNameList.get(i);
				}

				Dialog alertDialog = new AlertDialog.Builder(ctxt)
						.setTitle("选择收藏地点")
						.setIcon(R.drawable.ic_launcher)
						.setSingleChoiceItems(strList, 0,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										Log.d(TAG, "选择了"+which);
										mXpos = preferences.getFloat(strList[which]+"Lo", 0);
										mYpos = preferences.getFloat(strList[which]+"La", 0);
										Log.d(TAG, "mXpos:"+mXpos+"mYpos:"+mYpos);
										dialog.dismiss();
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).create();
				alertDialog.show();
			}
		});

		mSetStartPointBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mStartXpos = mXpos;
				mStartYpos = mYpos;
				mStartPosition.setText("记录起点经度:" + mStartXpos + ",维度:" + mStartYpos);
			}
		});		
		
		mSetEndPointBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mEndXpos = mXpos;
				mEndYpos = mYpos;
				mEndPosition.setText("记录终点经度:" + mEndXpos + ",维度:" + mEndYpos);
			}
		});			
		
		mStartAutoWalkBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 参数检查
				if (mStartYpos == 0.0 || mStartXpos == 0.0) {
					Toast.makeText(getApplicationContext(),
							"请正确设置起点位置",
							Toast.LENGTH_SHORT).show();
					isAutoRun = false;
					dirFromStartToEnd = false;
					return;
				}
				
				if (mEndYpos == 0.0 || mEndXpos == 0.0) {
					Toast.makeText(getApplicationContext(),
							"请正确设置终点位置",
							Toast.LENGTH_SHORT).show();
					isAutoRun = false;
					dirFromStartToEnd = false;
					return;
				}
				
				if (GlobalValue.compareDouble(mStartYpos, mEndYpos, stepLength/2) &&
					GlobalValue.compareDouble(mStartXpos, mEndXpos, stepLength/2)) {
					Toast.makeText(getApplicationContext(),
							"起点终点太近请重新设置",
							Toast.LENGTH_SHORT).show();
					isAutoRun = false;
					dirFromStartToEnd = false;
					return;
				}
				
				if (isAutoRun == false) {
					isAutoRun = true;
					walkTick = 0;// 防止闪屏
					mStartAutoWalkBtn.setText("关闭自动行走");
				} else {
					isAutoRun = false;
					dirFromStartToEnd = false;
					mStartAutoWalkBtn.setText("开启自动行走");
				}			
				
			}
		});
		
		mEmptyStoreLocBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Dialog alertDialog = new AlertDialog.Builder(ctxt)
						.setTitle("提示")
						.setMessage("是否清空")
						.setIcon(R.drawable.ic_launcher)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dataNameList.clear();
										storeLastLocat();
										wirteNameList();
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).create();
				alertDialog.show();
			}
		});
		
		new Thread(new RunnableMockLocation()).start();
	}

	private void AutoRuning() {
		double checkXposition = 0.0;
		double checkYposition = 0.0;
		
		if (dirFromStartToEnd == true) {
			checkYposition = mEndYpos;
			checkXposition = mEndXpos;
		} else {
			checkYposition = mStartYpos;
			checkXposition = mStartXpos;
		}
		
		if (GlobalValue.compareDouble(mYpos, checkYposition, stepLength*2) &&
			GlobalValue.compareDouble(mXpos, checkXposition, stepLength*2)) {
			dirFromStartToEnd = !dirFromStartToEnd;
		}
		if (dirFromStartToEnd == true) {
			CalculteNewPositionNonCover(mEndXpos, mEndYpos, mStartXpos, mStartYpos);
		} else {
			CalculteNewPositionNonCover(mStartXpos, mStartYpos, mEndXpos, mEndYpos);
		}
	}
	
	private boolean ApplyRuning(){
		if (walkTick >= 1) {
			walkTick = 0;
			return true;
		}
		return false;
	}
	
	private class RunnableMockLocation implements Runnable {

		@SuppressLint("NewApi")
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
					walkTick++;
					if (isAutoRun == true) {
						AutoRuning();
					}
					storeLastLocat();
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

	private void CalculteNewPosition(double startX, double startY, double endX, double endY){
		count1++;
		if (ApplyRuning() == false) {
			return;
		}
		//mCurPosition.setText("当前经度:" + mXpos + ",维度:" + mYpos);
		double dy = startY - endY;
		double dx = startX - endX;
		if (GlobalValue.compareDouble(startY, endY, 0.0001) ||
			GlobalValue.compareDouble(startX, endX, 0.0001)) {
			dy = startY*10000000 - endY*10000000;
			dx = startX*10000000 - endX*10000000;
		}
		try {
			double length = Math.sqrt(dx * dx + dy * dy);
			double dLocalY = -stepLength / length * dy;
			double dLocalX = stepLength / length * dx;
			curBearing = Math.toDegrees(Math.atan(dLocalY / dLocalX));
			mYpos += dLocalY;
			mXpos += dLocalX;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// 不转换坐标系
	private void CalculteNewPositionNonCover(double startX, double startY, double endX, double endY){
		count1++;
		if (ApplyRuning() == false) {
			return;
		}
		//mCurPosition.setText("当前经度:" + mXpos + ",维度:" + mYpos);
		double dy = startY - endY;
		double dx = startX - endX;
		if (GlobalValue.compareDouble(startY, endY, 0.0001) ||
			GlobalValue.compareDouble(startX, endX, 0.0001)) {
			dy = startY*1000000000 - endY*1000000000;
			dx = startX*1000000000 - endX*1000000000;
		}
		try {
			double length = Math.sqrt(dx * dx + dy * dy);
			double dLocalY = dy / length * stepLength;
			double dLocalX = dx / length * stepLength;
			curBearing = Math.toDegrees(Math.atan(dLocalY / dLocalX));
			mYpos += dLocalY;
			mXpos += dLocalX;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		int sqrLength = Math.min((int) GlobalValue.sWinH / 15,
				(int) GlobalValue.sWinH / 15);
		wmParams.width = sqrLength;
		wmParams.height = sqrLength;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// 获取浮动窗口视图所在布局
		try {
			mFloatLayout = (RelativeLayout) inflater.inflate(
					R.layout.cam_for_server, null);
			// 添加mFloatLayout
			mWindowManager.addView(mFloatLayout, wmParams);
			Log.i(TAG, "mWindowManager addView done");
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
					//mFloatLayout.setBackgroundColor(0xFF000000);
					mFloatLayout.setVisibility(View.VISIBLE);
					break;
				case MotionEvent.ACTION_MOVE:
					CalculteNewPosition(event.getRawX(), event.getRawY(),
						GlobalValue.sWinW / 2, GlobalValue.sWinH / 2);

					//mStepTxt.setText("count1:" + count1);
					break;
				case MotionEvent.ACTION_DOWN:
					//mFloatLayout.setBackgroundColor(0xFFFFFFFF);
					mFloatLayout.setVisibility(View.INVISIBLE);
					walkTick = 0;// 防止闪屏
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

	@SuppressLint("NewApi")
	private void updateLocation() {
		try {
			// 模拟位置（addTestProvider成功的前提下）
			String providerStr = LocationManager.GPS_PROVIDER;
			Location mockLocation = new Location(providerStr);
			mockLocation.setLatitude(mYpos); // 维度（度）
			mockLocation.setLongitude(mXpos); // 经度（度）
			mockLocation.setAltitude(30); // 高程（米）
			mockLocation.setBearing((float) curBearing); // 方向（度）
			mockLocation.setSpeed(5); // 速度（米/秒）
			mockLocation.setAccuracy(0.1f); // 精度（米）
			mockLocation.setTime(new Date().getTime()); // 本地时间
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				mockLocation.setElapsedRealtimeNanos(SystemClock
						.elapsedRealtimeNanos());
			}
			mLocationManager.setTestProviderLocation(providerStr, mockLocation);
		} catch (Exception e) {
			// 防止用户在软件运行过程中关闭模拟位置或选择其他应用
			stopMockLocation();
		}
	}

	private void wirteNameList() {
		editor.putInt("dataListNameLength", dataNameList.size());

		Log.d(TAG, "wirteNameList dataNameList.size():"+dataNameList.size());
		
		for (int i = 0; i < dataNameList.size(); i++) {
			editor.putString("dataListName" + i, dataNameList.get(i));
			Log.d(TAG, "wirteNameList editor.putString:"+"dataListName" + i+","+dataNameList.get(i));
		}
		editor.commit();
	}

	private void readNameList() {
		int length = 0;
		length = preferences.getInt("dataListNameLength", 0);
		Log.d(TAG, "readNameList length:"+length);
		
		for (int i = 0; i < length; i++) {
			dataNameList.add(preferences.getString("dataListName" + i, "none"));
			Log.d(TAG, "readNameList "+"dataListName" + i+","+dataNameList.get(i));
		}
		
	}

	protected void onDestroy() {
		super.onDestroy();
		storeLastLocat();
		wirteNameList();
	}

	private void storeCurLocat(String name) {
		if (dataNameList.contains(name)) {
			Toast.makeText(this, "名称重复", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "名称重复");
			return;
		}
		editor.putFloat(name + "Lo", (float) mXpos);
		editor.putFloat(name + "La", (float) mYpos);
		dataNameList.add(name);
		Log.d(TAG, "storeCurLocat,name:"+name+",mXpos:"+mXpos+",mYpos"+mYpos);
		editor.commit();
	}
	
	private void storeLastLocat() {
		if (!dataNameList.contains("lastLocation")) {
			dataNameList.add("lastLocation");
		}
		editor.putFloat("lastLocationLo", (float) mXpos);
		editor.putFloat("lastLocationLa", (float) mYpos);
		editor.commit();
	}
}
