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

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeightActivity extends BaseActivity {

    /**
     * 服务相关
     */
    private AtomicBoolean isSerialPortBound = new AtomicBoolean(false);
    private Messenger serialPortMessenger;
    private Messenger serialPortReceiver;
    ServiceConnection conn;

    /**
     * data
     */
    AssetFileDescriptor file;
    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height);

        file = this.getResources().openRawResourceFd(R.raw.height);

        mediaPlayer = buildMediaPlayer(this, file);

        mediaPlayer.start();

        Intent spService = new Intent("com.rotai.app.DTJService");
        spService.setPackage("com.rotai.app.dtjservice");

        Log.e(TAG, "com.rotai.app.DTJService");

        Log.d(TAG, "MAC ADDRESS: " + getMacAddress());

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "com.rotai.app.DTJService onServiceConnected");
                isSerialPortBound.set(true);
                serialPortMessenger = new Messenger(service);
                serialPortReceiver = new Messenger(new SerialPortReceiverHandler(HeightActivity.this));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: !!!");
                isSerialPortBound.set(false);
                serialPortMessenger = null;

            }
        };
        bindService(spService, conn, BIND_AUTO_CREATE);

        final Handler queue = new Handler(Looper.myLooper());
        queue.post(new Runnable() {
            @Override
            public void run() {
                if (!isSerialPortBound.get()) {
                    queue.postDelayed(this, 1000);
                    return;
                }
                Message message = Message.obtain();
                Bundle data = new Bundle();
                data.putString("op", "height");
                message.setData(data);
                message.replyTo = serialPortReceiver;
                try {
                    serialPortMessenger.send(message);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                //                queue.postDelayed(this, 1000);

            }
        });

    }


    private static class SerialPortReceiverHandler extends Handler {
        HeightActivity ctx;

        SerialPortReceiverHandler(HeightActivity heightActivity) {
            ctx = heightActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;
            final float height = (float) data.getDouble("height");
            Log.e(TAG, "data==" + data + ",,,heigth" + height);

            ctx.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (height > 100.0f) {
                        ctx.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ctx.startActivity(new Intent(ctx, WeightActivity.class));
                            }
                        });
                    }
                }
            });


        }
    }


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
