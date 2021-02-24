package com.example.locationmock;

import android.app.Application;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

public class PositonCtrlFlowView {
	private WindowManager.LayoutParams wmParams;
	private WindowManager mWindowManager;
	private RelativeLayout mFloatLayout;
	
	private static final String TAG = "FlowView";
	
	public PositonCtrlFlowView(Application  app, final PositonCalculte posCal) {
		wmParams = new WindowManager.LayoutParams();
		// 获取WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) app.getSystemService(
				app.WINDOW_SERVICE);
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

		LayoutInflater inflater = LayoutInflater.from(app);
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
					mFloatLayout.setVisibility(View.VISIBLE);
					break;
				case MotionEvent.ACTION_MOVE:
					posCal.CalculteNewPosition(event.getRawX(), event.getRawY(),
						GlobalValue.sWinW / 2, GlobalValue.sWinH / 2);
					break;
				case MotionEvent.ACTION_DOWN:
					//mFloatLayout.setBackgroundColor(0xFFFFFFFF);
					mFloatLayout.setVisibility(View.INVISIBLE);
					//walkTick = 0;// 防止闪屏
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

}
