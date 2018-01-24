package com.rotai.dtjclient.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.View;
import android.widget.TextView;

import com.cunoraz.gifview.library.GifView;
import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeightActivity extends BaseActivity {

    /**
     * dialog
     */
    private Handler mOffHandler;
    private Timer mOffTime;
    private Dialog mDialog;
    private TextView timer;
    private AlertDialog.Builder builder;


    /**
     * data
     */
    private AssetFileDescriptor file;
    private MediaPlayer mediaPlayer;
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

        //gif
        GifView gifView1 = (GifView) findViewById(R.id.gif);
        gifView1.setVisibility(View.VISIBLE);
        gifView1.play();

        queue = new Handler(Looper.myLooper());

        queue.post(connectService);

        file = this.getResources().openRawResourceFd(R.raw.height);

        mediaPlayer = buildMediaPlayer(this, file);

        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playing = false;
                startActivity(new Intent(HeightActivity.this, WeightActivity.class));
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
                queue.post(new ServiceSender(HeightActivity.this, data));
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        //        mediaPlayer.release();
        //        mediaPlayer = null;

        //        mOffTime.cancel();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Bundle data = new Bundle();
        data.putString("op", "bye");
        queue.post(new ServiceSender(HeightActivity.this, data));

        if(mediaPlayer!=null){
            mediaPlayer.release();
        }
        finish();
    }


    private class ServiceReceiver extends Handler {
        HeightActivity ctx;

        ServiceReceiver(HeightActivity heightActivity) {
            ctx = heightActivity;
        }

        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;
            if (data.getInt("network") == 1) {
                final float height = (float) data.getDouble("height");
                LogUtil.e(TAG, "data==" + data + ",,,heigth" + height);

                //                if ((height > 0) && (!ctx.mDialog.isShowing())) {
                //
                //                    Log.e(TAG, "身高跳转到体重");
                //                    if (ctx.jumping)
                //                        return;
                //                    ctx.jumping = true;
                //                    ctx.queue.post(new Runnable() {
                //                        @Override
                //                        public void run() {
                //                            if (ctx.playing) {
                //                                ctx.queue.postDelayed(this, 500);
                //                                return;
                //                            }
                ////                            ctx.startActivity(new Intent(ctx, WeightActivity.class));
                //                        }
                //                    });
                //                }

                int wakeup = data.getInt("wakeup");
                if (wakeup == 1) {
                    Log.e(TAG, "身高被唤醒");
                    if (mDialog != null && mDialog.isShowing()) {
                        mOffTime.cancel();
                        mDialog.dismiss();
                        mDialog = null;
                        //                        if (!mediaPlayer.isPlaying()) {
                        //                            mediaPlayer.start();
                        //                        }

                        file = ctx.getResources().openRawResourceFd(R.raw.height);

                        mediaPlayer = buildMediaPlayer(ctx, file);

                        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                playing = false;
                                startActivity(new Intent(HeightActivity.this, WeightActivity.class));
                                mediaPlayer.release();
                            }
                        });

                        mediaPlayer.start();

                    }
                    return;
                }

                int stepdown = data.getInt("stepdown");
                if (stepdown == 1) {
                    Log.e(TAG, "下秤了。。");
                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialog();
                        }
                    });
                }
            } else {
                ctx.startActivity(new Intent(HeightActivity.this, SplashActivity.class));
            }
        }
    }

    @SuppressLint("HandlerLeak")
    void showDialog() {

        //dialog
        builder = new AlertDialog.Builder(HeightActivity.this);
        View view = View.inflate(HeightActivity.this, R.layout.dialog_main_notice, null);
        builder.setView(view);
        builder.setCancelable(true);
        TextView title = view.findViewById(R.id.dialog_title);
        TextView content = view.findViewById(R.id.dialog_content);
        timer = view.findViewById(R.id.dialog_timer);
        mDialog = builder.create();
        mDialog.show();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        Log.e(TAG, "show=====");
        mDialog.setCanceledOnTouchOutside(false);

        mOffHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.e(TAG, "倒计时中===========");

                if (msg.what > 0) {
                    Log.e(TAG, "msg.what==111,,,," + msg.what);
                    ////动态显示倒计时
                    timer.setText("倒计时：" + msg.what + "S");

                } else {
                    ////倒计时结束自动关闭
                    Log.e(TAG, "msg.what======0000,,,," + msg.what);
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(HeightActivity.this, SplashActivity.class));
                            }
                        });
                    }

                    mOffTime.cancel();
                }
                super.handleMessage(msg);
            }

        };

        ////倒计时

        mOffTime = new Timer(true);
        TimerTask tt = new TimerTask() {
            int countTime = 5;

            public void run() {
                if (countTime > 0) {
                    countTime--;
                }
                Message msg = new Message();
                msg.what = countTime;
                mOffHandler.sendMessage(msg);
            }
        };
        mOffTime.schedule(tt, 1000, 1000);
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
