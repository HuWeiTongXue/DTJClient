package com.rotai.dtjclient.activity;

import android.annotation.SuppressLint;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.fragment.VideoFragment;
import com.rotai.dtjclient.util.EncodingUtils;
import com.rotai.dtjclient.util.LogUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class QRCodeActivity extends BaseActivity {

    /**
     * UI
     */
    private LinearLayout title,qrcode_ll;
    private FrameLayout container;
    private VideoFragment videoFragment;
    private FragmentTransaction fc;
    private ImageView qrcode_iv;


    /**
     * 服务相关
     */
    private AtomicBoolean isServiceBound = new AtomicBoolean(false);
    private Messenger serviceMessenger;
    private Messenger serviceReceiver;
    Handler queue;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.e(TAG, "com.rotai.app.DTJService onServiceConnected");
            isServiceBound.set(true);
            serviceMessenger = new Messenger(service);
            serviceReceiver = new Messenger(new ServiceReceiver(QRCodeActivity.this));
            queue.post(new ServiceSender(QRCodeActivity.this, new Bundle()));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "onServiceDisconnected: !!!");
            isServiceBound.set(false);
            serviceMessenger = null;
            serviceReceiver = null;
            queue.postDelayed(connectService, 1000);
        }
    };

    Runnable connectService = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "connectService");
            Intent service = new Intent("com.rotai.app.DTJService");
            service.setPackage("com.rotai.app.dtjservice");
            bindService(service, serviceConnection, BIND_AUTO_CREATE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        queue = new Handler(Looper.myLooper());
        queue.post(connectService);

        title=findViewById(R.id.activity_title);
        container=findViewById(R.id.activity_container);
        qrcode_ll=findViewById(R.id.activity_qrcode_ll);
        qrcode_iv=findViewById(R.id.activity_qrcode);

        if(videoFragment==null){
            videoFragment=new VideoFragment();
        }

        fc = getSupportFragmentManager().beginTransaction();
        fc.add(R.id.activity_container,videoFragment);
        fc.commit();

        LogUtil.e(TAG, "com.rotai.app.DTJService");

        queue.post(new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("op", "wx_qrcode");
                queue.post(new ServiceSender(QRCodeActivity.this,data));
            }
        });

    }

    @SuppressLint("HandlerLeak")
    private class ServiceReceiver extends Handler {
        QRCodeActivity ctx;

        ServiceReceiver(QRCodeActivity qrCodeActivity) {
            ctx = qrCodeActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;

            if(data.getInt("network")==1) {
                final String qrcode = data.getString("wx_qrcode");

                Log.e(TAG, "wx_qrcode==" + qrcode);

                if (qrcode != null && !qrcode.equals("")) {
                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "显示公众号二维码");
                            //                        ctx.startActivity(new Intent(ctx, QRCodeActivity.class));
                            Bitmap bitmap = EncodingUtils.createQRCode(qrcode, 220, 220, null);
                            ctx.qrcode_iv.setImageBitmap(bitmap);
                        }
                    });
                }

                Object subscribe = data.get("wx_scan");
                if (subscribe != null && !subscribe.equals("")) {
                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "准备自动测量！");
                            ctx.startActivity(new Intent(ctx, ReadyActivity.class));
                        }
                    });
                }

                Object wakeup = data.get("wakeup");
                if (wakeup != null && !wakeup.equals("")) {
                    return;
                }

                int stepdown = data.getInt("stepdown");
                if (stepdown == 1) {
                    Log.e(TAG, "stepdown==" + stepdown);
                    ctx.startActivity(new Intent(QRCodeActivity.this, SplashActivity.class));
                }
            }else if(data.getInt("network")==0){
                startActivity(new Intent(QRCodeActivity.this,SplashActivity.class));
            }
        }
    }

    private static class ServiceSender implements Runnable {
        QRCodeActivity ctx;
        Bundle data;

        ServiceSender(QRCodeActivity ctx, Bundle data) {
            this.ctx = ctx;
            this.data = data;
        }

        @Override
        public void run() {
            if (ctx.serviceMessenger == null) {
                ctx.queue.post(ctx.connectService);
                ctx.queue.postDelayed(this, 1000);
                return;
            }

            Message message = Message.obtain();
            message.setData(data);
            message.replyTo = ctx.serviceReceiver;
            try {
                ctx.serviceMessenger.send(message);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");

        Bundle data = new Bundle();
        data.putString("op", "bye");
        queue.post(new ServiceSender(QRCodeActivity.this, data));
    }
}
