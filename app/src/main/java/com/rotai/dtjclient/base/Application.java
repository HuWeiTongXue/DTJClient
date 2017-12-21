package com.rotai.dtjclient.base;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class Application extends android.app.Application {

    public static final String TAG="dtjclient";

    private static Application mApplication=null;
    public static Messenger serviceMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication=this;

        Intent spService = new Intent("com.rotai.app.DTJService");
        spService.setPackage("com.rotai.app.dtjservice");

        Log.e(TAG, "com.rotai.app.DTJService");

        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "com.rotai.app.DTJService onServiceConnected");
                serviceMessenger = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: !!!");
                serviceMessenger = null;

            }
        };
        bindService(spService, conn, BIND_AUTO_CREATE);
    }

    public static Application getInstance() {
        return mApplication;
    }
}