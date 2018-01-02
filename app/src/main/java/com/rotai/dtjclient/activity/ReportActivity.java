package com.rotai.dtjclient.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.util.LogUtil;
import com.rotai.dtjclient.view.WaveProgress;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 生成报告界面
 */
public class ReportActivity extends BaseActivity {


    /**
     * UI
     */
    private WaveProgress mWaveProgress;
    private TextView reporting;
    private ImageView report_status_iv;
    private TextView report_status_tv;
    private Random mRandom;
    private float mCurrent=5.0f; //当前进度值
    private TextView waitReport;

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
            serviceReceiver = new Messenger(new ServiceReceiver(ReportActivity.this));
            queue.post(new ServiceSender(ReportActivity.this, new Bundle()));
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
        ReportActivity ctx;

        ServiceReceiver(ReportActivity reportActivity) {
            ctx = reportActivity;
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

            Object report = data.get("report");  //报告生成成功
            if (report != null && !report.equals("")) {
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ctx.waitReport.setVisibility(View.GONE);
                        ctx.mWaveProgress.setVisibility(View.VISIBLE);
                        ctx.reporting.setVisibility(View.VISIBLE);
                        ctx.report_status_iv.setVisibility(View.GONE);
                        ctx.report_status_tv.setVisibility(View.GONE);
                        ctx.queue.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(ctx.mCurrent<100.0f){
                                    ctx.mCurrent+=Math.random()*10;
                                    ctx.queue.postDelayed(this,1000);
                                }if(ctx.mCurrent>=100.0f){
                                    ctx.mCurrent=100.0f;
                                    ctx.mWaveProgress.setVisibility(View.GONE);
                                    ctx.report_status_iv.setVisibility(View.VISIBLE);
                                    ctx.report_status_iv.setImageResource(R.mipmap.report);
                                    ctx.reporting.setText("报告生成成功，请在微信公众号中查看您的报告！\n10秒后结束本次测量");
                                    ctx.mCurrent=0;
                                    ctx.queue.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            ctx.startActivity(new Intent(ReportActivity.this,SplashActivity.class));
                                        }
                                    },10000);

                                }
                                ctx.mWaveProgress.setValue(ctx.mCurrent);

                            }
                        },1000);
                    }
                });
            }

            Object report_failed = data.get("report_failed");
            if(report_failed!=null && !report_failed.equals("")){
                ctx.mWaveProgress.setVisibility(View.GONE);
                ctx.reporting.setVisibility(View.GONE);
                ctx.waitReport.setVisibility(View.GONE);
                ctx.report_status_iv.setVisibility(View.VISIBLE);
                ctx.report_status_tv.setVisibility(View.VISIBLE);
                ctx.report_status_iv.setImageResource(R.mipmap.reportfailed);
                ctx.report_status_tv.setText("报告生成失败，请再测一次！");
            }

        }
    }

    private static class ServiceSender implements Runnable {
        ReportActivity ctx;
        Bundle data;

        ServiceSender(ReportActivity ctx, Bundle data) {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        queue = new Handler(Looper.myLooper());

        queue.post(connectService);

        mWaveProgress = (WaveProgress) findViewById(R.id.wave_progress_bar);
        reporting=(TextView) findViewById(R.id.reporting);
        report_status_iv=findViewById(R.id.report_status_iv);
        report_status_tv=findViewById(R.id.report_status_tv);
        waitReport=findViewById(R.id.waitReport);
        mRandom = new Random();

    }
}
