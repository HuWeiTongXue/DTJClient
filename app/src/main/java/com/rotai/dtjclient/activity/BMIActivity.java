package com.rotai.dtjclient.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class BMIActivity extends BaseActivity {

    /**
     * 服务相关
     */
    private AtomicBoolean isSerialPortBound = new AtomicBoolean(false);
    private Messenger serialPortMessenger;
    private Messenger serialPortReceiver;
    ServiceConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);
        Intent spService = new Intent("com.rotai.app.DTJService");
        spService.setPackage("com.rotai.app.dtjservice");

        LogUtil.e(TAG, "com.rotai.app.DTJService");


        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.e(TAG, "com.rotai.app.DTJService onServiceConnected");
                isSerialPortBound.set(true);
                serialPortMessenger = new Messenger(service);
                serialPortReceiver = new Messenger(new SerialPortReceiverHandler(BMIActivity.this));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtil.d(TAG, "onServiceDisconnected: !!!");
                isSerialPortBound.set(false);
                serialPortMessenger = null;

            }
        };
        bindService(spService, conn, BIND_AUTO_CREATE);

        final Handler queue = new Handler(Looper.myLooper());
        queue.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isSerialPortBound.get()) {
                    queue.postDelayed(this, 1000);
                    return;
                }
                Message message = Message.obtain();
                Bundle data = new Bundle();
                data.putString("op", "bmi");
                message.setData(data);
                message.replyTo = serialPortReceiver;
                try {
                    serialPortMessenger.send(message);
                } catch (RemoteException e) {
                    LogUtil.e(TAG, e.getMessage(), e);
                }
                //                queue.postDelayed(this, 1000);

            }
        },10000);

    }


    private static class SerialPortReceiverHandler extends Handler {
        BMIActivity ctx;

        SerialPortReceiverHandler(BMIActivity bmiActivity) {
            ctx = bmiActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;
            float bmi = (float) data.getDouble("bmi");
            LogUtil.e(TAG, "data=="+data+",,,bmi=="+bmi );
            if(bmi>15.0f){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ctx.startActivity(new Intent(ctx, FaceActivity.class));
                    }
                });
            }
        }
    }
}
