package com.rotai.dtjclient.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.fragment.VideoFragment;
import com.rotai.dtjclient.util.LogUtil;
import com.rotai.dtjclient.util.QrCodeUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class QRCodeActivity extends BaseActivity {

    /**
     * UI
     */
    private LinearLayout title,qrcode_ll;
    private FrameLayout container;
    private VideoFragment videoFragment;
    private FragmentTransaction fc;
    private ImageView qrcode_iv;


    /**
     * 服务相关
     */
    private AtomicBoolean isSerialPortBound = new AtomicBoolean(false);
    private Messenger serialPortMessenger;
    private Messenger serialPortReceiver;
    ServiceConnection conn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        title=findViewById(R.id.activity_title);
        container=findViewById(R.id.activity_container);
        qrcode_ll=findViewById(R.id.activity_qrcode_ll);
        qrcode_iv=findViewById(R.id.activity_qrcode);

        if(videoFragment==null){
            videoFragment=new VideoFragment();
        }

        fc = getSupportFragmentManager().beginTransaction();
        fc.add(R.id.activity_container,videoFragment);
        fc.commit();

        Intent spService = new Intent("com.rotai.app.DTJService");
        spService.setPackage("com.rotai.app.dtjservice");

        LogUtil.e(TAG, "com.rotai.app.DTJService");

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.e(TAG, "com.rotai.app.DTJService onServiceConnected");
                isSerialPortBound.set(true);
                serialPortMessenger = new Messenger(service);
                serialPortReceiver = new Messenger(new SerialPortReceiverHandler(QRCodeActivity.this));
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

    private static class SerialPortReceiverHandler extends Handler {
        QRCodeActivity ctx;

        SerialPortReceiverHandler(QRCodeActivity qrCodeActivity) {
            ctx = qrCodeActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;
            final String qrcode = data.getString("qrcode");
            if(qrcode!=null && !qrcode.equals("")){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "显示二维码" );
//                        ctx.startActivity(new Intent(ctx, QRCodeActivity.class));
                        Bitmap bitmap = QrCodeUtil.generateBitmap(qrcode, 220, 220);
                        ctx.qrcode_iv.setImageBitmap(bitmap);
                    }
                });
            }

            Object subscribe = data.get("wx_subscribe");
            if(subscribe!=null && !subscribe.equals("")){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "切换视图 " );
                        ctx.startActivity(new Intent(ctx, HeightActivity.class));
                    }
                });
            }
        }
    }
}
