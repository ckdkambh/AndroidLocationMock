package com.example.locationmock;

import android.app.Application;

public class PositonCalculte {
	private double mXpos = 0;
	private double mYpos = 0;
	private double stepLength = 0.00007;
	private double stepOnce = 0.000005;
	private double curBearing = 180;
	
	public PositonCalculte(double x, double y) {
		mXpos = x;
		mYpos = y;
	}
	
	public void CalculteNewPosition(double startX, double startY, double endX, double endY){
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
	public void CalculteNewPositionNonCover(double startX, double startY, double endX, double endY){
		//mCurPosition.setText("当前经度:" + mXpos + ",维度:" + mYpos);
		double dy = startY - endY;
		double dx = startX - endX;
		if (GlobalValue.compareDouble(startY, endY, 0.0001) ||
			GlobalValue.compareDouble(startX, endX, 0.0001)) {
			dy = startY*1000000000 - endY*1000000000;
			dx = startX*1000000000 - endX*1000000000;
		}
		try {
			double dLocalY;
			double dLocalX;
			double length = Math.sqrt(dx * dx + dy * dy);		
			
			dLocalY = dy / length * stepLength;
			dLocalX = dx / length * stepLength;
			curBearing = Math.toDegrees(Math.atan(dLocalY / dLocalX));
			mYpos += dLocalY;
			mXpos += dLocalX;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double getmXpos() {
		return mXpos;
	}

	public void setmXpos(double mXpos) {
		this.mXpos = mXpos;
	}

	public double getmYpos() {
		return mYpos;
	}

	public void setmYpos(double mYpos) {
		this.mYpos = mYpos;
	}

	public double getStepLength() {
		return stepLength;
	}

	public void setStepLength(double stepLength) {
		this.stepLength = stepLength;
	}

	public double getStepOnce() {
		return stepOnce;
	}

	public void setStepOnce(double stepOnce) {
		this.stepOnce = stepOnce;
	}

	public double getCurBearing() {
		return curBearing;
	}

	public void setCurBearing(double curBearing) {
		this.curBearing = curBearing;
	}
}
