package kr.owens.smartBike.util;

import android.widget.Toast;

import kr.owens.smartBike.BaseApplication;

public class ToastUtil {

    public static void show(String message) {
        Toast.makeText(BaseApplication.getContext(), message, Toast.LENGTH_LONG).show();
    }
}
