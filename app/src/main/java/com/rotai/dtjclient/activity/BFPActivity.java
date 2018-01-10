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

public class BFPActivity extends BaseActivity {


    /**
     * data
     */
    AssetFileDescriptor file;
    MediaPlayer mediaPlayer;
    boolean playing = true;
    boolean jumping = false;

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
            serviceReceiver = new Messenger(new ServiceReceiver(BFPActivity.this));
            queue.post(new ServiceSender(BFPActivity.this, new Bundle()));
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
        setContentView(R.layout.activity_bmi);

        queue = new Handler(Looper.myLooper());

        queue.post(connectService);

        file = this.getResources().openRawResourceFd(R.raw.bmi);

        mediaPlayer = buildMediaPlayer(this, file);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playing = false;
            }
        });

        mediaPlayer.start();


        LogUtil.e(TAG, "com.rotai.app.DTJService");

        queue.post(new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("op", "bfp");
                queue.post(new ServiceSender(BFPActivity.this,data));
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
        queue.post(new ServiceSender(BFPActivity.this,data));
    }


    private class ServiceReceiver extends Handler {
        BFPActivity ctx;
        ServiceReceiver(BFPActivity BFPActivity) {
            ctx = BFPActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;
            final float bfp = (float) data.getDouble("bfp");
            LogUtil.e(TAG, "data=="+data+",,,bfp=="+bfp );

            if(bfp>0f){
                if (ctx.jumping) return;
                ctx.jumping = true;
                ctx.queue.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ctx.playing) {
                            ctx.queue.postDelayed(this, 500);
                            return;
                        }
                        ctx.startActivity(new Intent(ctx,FaceActivity.class));
                    }
                });
            }


            Object wakeup = data.get("wakeup");
            if (wakeup != null && !wakeup.equals("")) {
                return;
            }

            Object stepdown  = data.get("stepdown ");
            if (stepdown != null && !stepdown.equals("")) {
                ctx.startActivity(new Intent(BFPActivity.this,SplashActivity.class));
            }
        }
    }

    private static class ServiceSender implements Runnable {
        BFPActivity ctx;
        Bundle data;

        ServiceSender(BFPActivity ctx, Bundle data) {
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
