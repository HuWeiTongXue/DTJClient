package com.rotai.dtjclient.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.Application;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
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
     * UI-人脸预览
     */

    private Handler bgQueue;
    private Camera camera;
    private TextureView previewView;  //实时预览
    private int count=0;

    /**
     * 小图
     */
    private ImageView small1,small2,small3,small4,small5,small6,small7,small8;

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

    final Runnable cameraOn = new Runnable() {
        @Override
        public void run() {
            if (camera != null)
                return;
            try {
                LogUtil.d(TAG, "open camera");
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                FaceActivity.this.camera.setDisplayOrientation(270);
                FaceActivity.this.camera.setPreviewTexture(previewView.getSurfaceTexture());
                FaceActivity.this.camera.startPreview();
            } catch (IOException | RuntimeException e) {
                cameraOff.run();
                bgQueue.postDelayed(this, 1000);
                LogUtil.e(TAG, e.getMessage(), e);
            }
        }
    };

    final Runnable cameraOff = new Runnable() {
        @Override
        public void run() {
            if (camera == null)
                return;
            try {
                LogUtil.d(TAG, "close camera");
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (Exception ignore) {
            }
        }
    };

//    Application ctx;
//
//    public void FaceActivity(Application ctx) {
//        this.ctx = ctx;
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face2);

        initView();

        queue = new Handler(Looper.myLooper());


        HandlerThread worker = new HandlerThread("BackgroundWoker");
        worker.start();
        bgQueue = new Handler(worker.getLooper());

        bgQueue.postDelayed(cameraOn, 500);

        queue.post(connectService);

        queue.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (camera == null)
                        return;
                    Bitmap bitmap = previewView.getBitmap();
                    if (bitmap == null) {
                        LogUtil.d(TAG, "bitmap null");
                        return;
                    }
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 99, bos);
                    final byte[] data = bos.toByteArray();

                    Bundle msg = new Bundle();
                    msg.putString("op", "camera_frame");
                    msg.putByteArray("data", data);
                    bgQueue.post(new FaceActivity.ServiceSender(FaceActivity.this, msg));
                } finally {
                    bgQueue.postDelayed(this, 2000);
                }
            }
        });

        file = this.getResources().openRawResourceFd(R.raw.face);
        mediaPlayer = buildMediaPlayer(this, file);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playing = false;
            }
        });
        mediaPlayer.start();


        Application.getInstance().serviceMessageCallbacks.add(new Application.ServiceMessageCallback() {
            @Override
            public void message(final Bundle msg) {
                count++;
                Log.e(TAG, "count=="+count );
                final byte[] data = msg.getByteArray("camera_frame");
                if (data == null) return;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(count==0){
                            small1.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        }else if(count==1){
                            small2.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        }else if(count==2){
                            small3.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        }else if(count==3){
                            small4.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        }else if(count==4){
                            small5.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        }else if(count==5){
                            small6.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        }else if(count==6){
                            small7.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        }else if(count==7){
                            small8.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        }
                    }
                });

            }
        });

        LogUtil.e(TAG, "com.rotai.app.DTJService");

    }

    private void initView() {
        previewView = findViewById(R.id.camera_view);
        small1=findViewById(R.id.small_img1);
        small2=findViewById(R.id.small_img2);
        small3=findViewById(R.id.small_img3);
        small4=findViewById(R.id.small_img4);
        small5=findViewById(R.id.small_img5);
        small6=findViewById(R.id.small_img6);
        small7=findViewById(R.id.small_img7);
        small8=findViewById(R.id.small_img8);
    }

    @Override
    protected void onResume() {
        super.onResume();

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

            Log.e(TAG, "data============" + bundleToStr(data));
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
                                //                                startActivity(new Intent(FaceActivity.this, ReportActivity.class));
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
                                //                                startActivity(new Intent(FaceActivity.this, SplashActivity.class));
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

    static String bundleToStr(Bundle data) {
        StringBuilder sb = new StringBuilder();
        for (String key : data.keySet())
            sb.append(key).append(": ").append(data.get(key)).append(", ");
        if (sb.length() == 0)
            sb.append("empty");
        else {
            sb.insert(0, "{ ");
            sb.delete(sb.length() - 2, sb.length());
            sb.append(" }");
        }
        return sb.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();

        camera.release();
        Log.e(TAG, "释放相机");

    }

    @Override
    protected void onStop() {
        super.onStop();

        count=0;

        bgQueue.post(cameraOff);

        Bundle data = new Bundle();
        data.putString("op", "bye");
        queue.post(new ServiceSender(FaceActivity.this, data));

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        finish();
    }

}
