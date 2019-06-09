package kr.owens.smartBike;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {
    private static Context applicationContext = null;

    public static Context getContext() {
        return applicationContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        applicationContext = base;
    }
}
