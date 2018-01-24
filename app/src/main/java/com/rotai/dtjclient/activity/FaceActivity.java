package com.rotai.dtjclient.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.View;
import android.widget.TextView;

import com.cunoraz.gifview.library.GifView;
import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceActivity extends BaseActivity {

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
            serviceReceiver = new Messenger(new ServiceReceiver(FaceActivity.this));
            queue.post(new ServiceSender(FaceActivity.this, new Bundle()));
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
        setContentView(R.layout.activity_face);

        GifView gifView1 = (GifView) findViewById(R.id.gif);
        gifView1.setVisibility(View.VISIBLE);
        gifView1.play();

        queue = new Handler(Looper.myLooper());

        queue.post(connectService);

        file = this.getResources().openRawResourceFd(R.raw.face);

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
                data.putString("op", "camera_on");
                queue.post(new ServiceSender(FaceActivity.this, data));
            }
        });

        //        queue.postDelayed(new Runnable() {
        //            @Override
        //            public void run() {
        //                Bundle data = new Bundle();
        //                data.putString("op", "camera_off");
        //                queue.post(new ServiceSender(FaceActivity.this,data));
        //            }
        //        },15000);

                queue.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(FaceActivity.this,ReportActivity.class));
                    }
                },18000);

    }

    private class ServiceReceiver extends Handler {
        FaceActivity ctx;

        ServiceReceiver(FaceActivity faceActivity) {
            ctx = faceActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;

            if (data.getInt("network") == 1) {
                int wakeup = data.getInt("wakeup");
                if (wakeup == 1) {
                    if (mDialog != null && mDialog.isShowing()) {
                        mOffTime.cancel();
                        mDialog.dismiss();
                        mDialog = null;

                        file = ctx.getResources().openRawResourceFd(R.raw.face);

                        mediaPlayer = buildMediaPlayer(ctx, file);

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                playing = false;
                                startActivity(new Intent(FaceActivity.this, ReportActivity.class));
                                mediaPlayer.release();
                            }
                        });

                        mediaPlayer.start();

                    }
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

                final int age = data.getInt("age");
                LogUtil.e(TAG, "data==" + data + ",,,age" + age);

                //                if (age ==0) {
                //                    ctx.runOnUiThread(new Runnable() {
                //                        @Override
                //                        public void run() {
                //                            ctx.startActivity(new Intent(ctx, ReportActivity.class));
                //                        }
                //                    });
                //                }
            } else {
                startActivity(new Intent(FaceActivity.this, SplashActivity.class));
            }
        }
    }


    @SuppressLint("HandlerLeak")
    void showDialog() {

        //dialog
        builder = new AlertDialog.Builder(FaceActivity.this);
        View view = View.inflate(FaceActivity.this, R.layout.dialog_main_notice, null);
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
                                startActivity(new Intent(FaceActivity.this, SplashActivity.class));
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
        FaceActivity ctx;
        Bundle data;

        ServiceSender(FaceActivity ctx, Bundle data) {
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

    ;

    @Override
    protected void onPause() {
        super.onPause();

        //        mediaPlayer.release();
        //        mediaPlayer = null;
        queue.post(new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("op", "camera_off");
                queue.post(new ServiceSender(FaceActivity.this, data));
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Bundle data = new Bundle();
        data.putString("op", "bye");
        queue.post(new ServiceSender(FaceActivity.this, data));

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        finish();
    }

}
