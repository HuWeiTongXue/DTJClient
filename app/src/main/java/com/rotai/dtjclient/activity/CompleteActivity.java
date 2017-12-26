package com.rotai.dtjclient.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;
import com.rotai.dtjclient.util.QrCodeUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class CompleteActivity extends BaseActivity {

    /**
     * UI
     */
    private ImageView complete_qrcode;
    private TextView time;
    CountDownTimer downTimer;

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
            serviceReceiver = new Messenger(new ServiceReceiver(CompleteActivity.this));
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

    /**
     * data
     */
    AssetFileDescriptor file,file2;
    MediaPlayer mediaPlayer,mediaPlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);

        queue = new Handler(Looper.myLooper());

        queue.post(connectService);

        complete_qrcode=findViewById(R.id.complete_qrcode);
        time=findViewById(R.id.time_tv);

        file = this.getResources().openRawResourceFd(R.raw.done);
        file2 = this.getResources().openRawResourceFd(R.raw.done);

        mediaPlayer = buildMediaPlayer(this, file);
        mediaPlayer2 = buildMediaPlayer(this, file2);

        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlayer2.start();
                    }
                },5000);
            }
        });

        downTimer= new CountDownTimer(60 * 1000+1050, 1000){

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                time.setText("付款码有效时间："+(millisUntilFinished / 1000-1) + "s");
            }

            @Override
            public void onFinish() {
                startActivity(new Intent(getApplicationContext(),SplashActivity.class));
            }
        };

        downTimer.start();

        LogUtil.e(TAG, "com.rotai.app.DTJService");

        queue.post(new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("op", "pay");
                queue.post(new ServiceSender(CompleteActivity.this,data));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
        mediaPlayer2.release();
    }

    private static class ServiceReceiver extends Handler {
        CompleteActivity ctx;

        ServiceReceiver(CompleteActivity completeActivity) {
            ctx = completeActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;
            Object paid = data.get("paid");
            if (paid != null && !paid.equals("")) {
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ctx.startActivity(new Intent(ctx, SplashActivity.class));
                    }
                });
            }

            final String pay = data.getString("pay");
            if(pay!=null && !pay.equals("")){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "显示付费二维码" );
                        Bitmap bitmap = QrCodeUtil.generateBitmap(pay, 220, 220);
                        ctx.complete_qrcode.setImageBitmap(bitmap);
                    }
                });
            }

        }
    }

    private static class ServiceSender implements Runnable {
        CompleteActivity ctx;
        Bundle data;

        ServiceSender(CompleteActivity ctx, Bundle data) {
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
}
