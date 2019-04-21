package com.example.locationmock;

public class GlobalValue {
	//全局变量
	public static double sWinH = 0;
	public static double sWinW = 0;
	
	public static double defaultLongitude = 116.395636;
	public static double defaultLatitude = 39.929983;
	
	public enum TypeOfDirection {
		FROM_START_TO_END,
		FROM_END_TO_START
	}
	
	public static boolean compareDouble(double num1, double num2, double accuracy) {
		if (Math.abs(num1 - num2) < accuracy) {
			return true;
		}
		return false;
	}
}

