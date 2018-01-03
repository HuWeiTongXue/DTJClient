package com.rotai.dtjclient.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.rotai.dtjclient.MainActivity;
import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeightActivity extends BaseActivity {


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
            serviceReceiver = new Messenger(new ServiceReceiver(HeightActivity.this));
            queue.post(new ServiceSender(HeightActivity.this, new Bundle()));
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
        setContentView(R.layout.activity_height);

        queue = new Handler(Looper.myLooper());

        queue.post(connectService);

        file = this.getResources().openRawResourceFd(R.raw.height);

        mediaPlayer = buildMediaPlayer(this, file);

        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playing = false;
            }
        });

        mediaPlayer.start();

        Intent spService = new Intent("com.rotai.app.DTJService");
        spService.setPackage("com.rotai.app.dtjservice");

        LogUtil.e(TAG, "com.rotai.app.DTJService");

        LogUtil.d(TAG, "MAC ADDRESS: " + getMacAddress());

        queue.post(new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("op", "height");
                queue.post(new ServiceSender(HeightActivity.this,data));
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
        queue.post(new ServiceSender(HeightActivity.this,data));
    }

    private static class ServiceReceiver extends Handler {
        HeightActivity ctx;

        ServiceReceiver(HeightActivity heightActivity) {
            ctx = heightActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;
            final float height = (float) data.getDouble("height");
            LogUtil.e(TAG, "data==" + data + ",,,heigth" + height);

            if (height > 100f) {
                if (ctx.jumping) return;
                ctx.jumping = true;
                ctx.queue.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ctx.playing) {
                            ctx.queue.postDelayed(this, 500);
                            return;
                        }
                        ctx.startActivity(new Intent(ctx, WeightActivity.class));
                    }
                });
            }

            Object wakeup = data.get("wakeup");
            if (wakeup != null && !wakeup.equals("")) {
                return;
            }

        }
    }

    private static class ServiceSender implements Runnable {
        HeightActivity ctx;
        Bundle data;

        ServiceSender(HeightActivity ctx, Bundle data) {
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


    private String getMacAddress() {
        try {
            return loadFileAsString("/sys/class/net/eth0/address").toUpperCase(Locale.ENGLISH).substring(0, 17);
        } catch (IOException e) {
            return null;
        }
    }

    private String loadFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }
}
