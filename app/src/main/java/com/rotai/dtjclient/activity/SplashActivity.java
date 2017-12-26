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
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rotai.dtjclient.MainActivity;
import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

public class SplashActivity extends BaseActivity {

    private int[] imageId = new int[]{
            R.mipmap.guide1,
            R.mipmap.guide2,
            R.mipmap.guide3};

    private List<ImageView> mImages = new ArrayList<>();

    /**
     * data
     */
    AssetFileDescriptor file;
    MediaPlayer mediaPlayer;

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
            serviceReceiver = new Messenger(new ServiceReceiver(SplashActivity.this));
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
        setContentView(R.layout.activity_splash);

        queue = new Handler(Looper.myLooper());

        queue.post(connectService);

        AutoScrollViewPager pic_viewPager = findViewById(R.id.activity_viewPager);

        startViewPager(pic_viewPager);

        file = this.getResources().openRawResourceFd(R.raw.adtips);

        mediaPlayer = buildMediaPlayer(this, file);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
            }
        });

        LogUtil.e(TAG, "com.rotai.app.DTJService");

        queue.post(new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("op", "hi");
                queue.post(new ServiceSender(SplashActivity.this,data));
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
    }

    private static class ServiceSender implements Runnable {
        SplashActivity ctx;
        Bundle data;

        ServiceSender(SplashActivity ctx, Bundle data) {
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

    private static class ServiceReceiver extends Handler {
        SplashActivity ctx;

        ServiceReceiver(SplashActivity splashActivity) {
            ctx = splashActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if (data == null)
                return;
            Object wakeup = data.get("wakeup");
            if (wakeup != null && !wakeup.equals("")) {
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ctx.startActivity(new Intent(ctx, QRCodeActivity.class));
                    }
                });
            }
        }
    }

    private void startViewPager(AutoScrollViewPager pic_viewPager) {
        for (int anImageId : imageId) {
            ImageView imageView = new ImageView(SplashActivity.this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(anImageId);
            mImages.add(imageView);
        }

        pic_viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mImages.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mImages.get(position));
                return mImages.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mImages.get(position));
            }
        });

        //开启自动滑动 5000毫秒
        pic_viewPager.startAutoScroll(5000);
        //自动切换5000毫秒
        pic_viewPager.setInterval(5000);
        //往左滑 默认right
        pic_viewPager.setDirection(AutoScrollViewPager.RIGHT);
        //是否循环 默认true
        pic_viewPager.setCycle(true);
        pic_viewPager.setBorderAnimation(false);
    }
}
