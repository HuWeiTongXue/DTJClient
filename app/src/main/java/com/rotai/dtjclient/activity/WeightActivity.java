package com.rotai.dtjclient.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class WeightActivity extends BaseActivity {

    /**
     * data
     */
    AssetFileDescriptor file;
    MediaPlayer mediaPlayer;

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
            serviceReceiver = new Messenger(new ServiceReceiver(WeightActivity.this));
            queue.post(new ServiceSender(WeightActivity.this, new Bundle()));
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
        setContentView(R.layout.activity_weight);

        queue = new Handler(Looper.myLooper());

        queue.post(connectService);

        file = this.getResources().openRawResourceFd(R.raw.weight);

        mediaPlayer = buildMediaPlayer(this, file);

        mediaPlayer.start();

        LogUtil.e(TAG, "com.rotai.app.DTJService");

        queue.post(new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("op", "weight");
                queue.post(new ServiceSender(WeightActivity.this,data));
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
        mediaPlayer=null;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");

        Bundle data = new Bundle();
        data.putString("op", "bye");
        queue.post(new ServiceSender(WeightActivity.this,data));
    }

    private static class ServiceReceiver extends Handler {
        WeightActivity ctx;

        ServiceReceiver(WeightActivity weightActivity) {
            ctx = weightActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;
            final float weight = (float) data.getDouble("weight");
            LogUtil.e(TAG, "data=="+data+",,,weigth"+weight );
            ctx.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(weight>20.0f){
                        ctx.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ctx.startActivity(new Intent(ctx, BFPActivity.class));
                            }
                        });
                    }
                }
            });


            Object wakeup = data.get("wakeup");
            if (wakeup != null && !wakeup.equals("")) {
                return;
            }

        }
    }

    private static class ServiceSender implements Runnable {
        WeightActivity ctx;
        Bundle data;

        ServiceSender(WeightActivity ctx, Bundle data) {
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
    };
}
