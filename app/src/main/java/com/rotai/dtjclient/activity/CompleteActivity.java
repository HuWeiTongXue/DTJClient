package com.rotai.dtjclient.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private AtomicBoolean isSerialPortBound = new AtomicBoolean(false);
    private Messenger serialPortMessenger;
    private Messenger serialPortReceiver;
    ServiceConnection conn;

    /**
     * data
     */
    AssetFileDescriptor file,file2;
    MediaPlayer mediaPlayer,mediaPlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);

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

        Intent spService = new Intent("com.rotai.app.DTJService");
        spService.setPackage("com.rotai.app.dtjservice");

        LogUtil.e(TAG, "com.rotai.app.DTJService");

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.e(TAG, "com.rotai.app.DTJService onServiceConnected");
                isSerialPortBound.set(true);
                serialPortMessenger = new Messenger(service);
                serialPortReceiver = new Messenger(new SerialPortReceiverHandler(CompleteActivity.this));
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
        queue.post(new Runnable() {
            @Override
            public void run() {
                if (!isSerialPortBound.get()) {
                    queue.postDelayed(this, 1000);
                    return;
                }
                Message message = Message.obtain();
                Bundle data = new Bundle();
                data.putString("op", "qrcode");
                message.setData(data);
                message.replyTo = serialPortReceiver;
                try {
                    serialPortMessenger.send(message);
                } catch (RemoteException e) {
                    LogUtil.e(TAG, e.getMessage(), e);
                }

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
        mediaPlayer2.release();
    }

    private static class SerialPortReceiverHandler extends Handler {
        CompleteActivity ctx;

        SerialPortReceiverHandler(CompleteActivity completeActivity) {
            ctx = completeActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;



        }
    }
}
