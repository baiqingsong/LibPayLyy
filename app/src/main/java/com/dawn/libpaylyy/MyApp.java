package com.dawn.libpaylyy;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Constant.deviceId = SystemUtil.getDeviceId();
//        Constant.deviceId = "0C65FA01C98300000000";

    }
    public static Context getContext(){
        return context;
    }
}
