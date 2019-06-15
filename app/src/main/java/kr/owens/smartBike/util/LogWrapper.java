package kr.owens.smartBike.util;

import android.util.Log;

public class LogWrapper {
    private static final String TAG = "[SmartBike]";

    public static void printLog(String message) {
        Log.e(TAG, message);
    }

    public static void printLog(String formatString, Object... objects) {
        Log.e(TAG, String.format(formatString, objects));
    }
}
