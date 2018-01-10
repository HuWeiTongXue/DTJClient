package com.rotai.dtjclient.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.Application;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReadyActivity extends BaseActivity {

    private TextView time;


    Handler queue=new Handler(Looper.myLooper());

    /**
     * data
     */
    AssetFileDescriptor file1,file2;
    MediaPlayer mediaPlayer1,mediaPlayer2;

    /**
     * 服务相关
     */
    private AtomicBoolean isServiceBound = new AtomicBoolean(false);
    private Messenger serviceMessenger;
    private Messenger serviceReceiver;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.e(TAG, "com.rotai.app.DTJService onServiceConnected");
            isServiceBound.set(true);
            serviceMessenger = new Messenger(service);
            serviceReceiver = new Messenger(new ServiceReceiver(ReadyActivity.this));
            queue.post(new ServiceSender(ReadyActivity.this, new Bundle()));
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

    private class ServiceReceiver extends Handler {
        ReadyActivity ctx;

        ServiceReceiver(ReadyActivity readyActivity) {
            ctx = readyActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;

            Object wakeup = data.get("wakeup");
            if (wakeup != null && !wakeup.equals("")) {
                return;
            }

            int stepdown  = (int) data.get("stepdown");
            if (stepdown==1) {
                Log.e(TAG, "stepdown=="+stepdown );
                ctx.startActivity(new Intent(ReadyActivity.this,SplashActivity.class));
            }
        }
    }

    private static class ServiceSender implements Runnable {
        ReadyActivity ctx;
        Bundle data;

        ServiceSender(ReadyActivity ctx, Bundle data) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);
        time=findViewById(R.id.time);

        file1 = this.getResources().openRawResourceFd(R.raw.ready);
        mediaPlayer1 = buildMediaPlayer(this, file1);
        //        file2 = this.getResources().openRawResourceFd(R.raw.ready2);

//        mediaPlayer2 = buildMediaPlayer(this, file2);

        mediaPlayer1.start();

        final CountDownTimer downTimer = new CountDownTimer(5 * 1000+1050, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                time.setText((millisUntilFinished / 1000-1) + "");
            }

            @Override
            public void onFinish() {
                startActivity(new Intent(ReadyActivity.this, QRCodeActivity.class));
//                mediaPlayer2.start();
//                mediaPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        startActivity(new Intent(ReadyActivity.this, QRCodeActivity.class));
//                    }
//                });

            }
        };

        mediaPlayer1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                downTimer.start();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer1.release();
//        mediaPlayer2.release();
        mediaPlayer1=null;
//        mediaPlayer2=null;
        finish();
    }
}
