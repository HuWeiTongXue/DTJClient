package com.rotai.dtjclient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;
import com.rotai.dtjclient.base.Constant;
import com.rotai.dtjclient.util.LogUtil;
import com.rotai.dtjclient.util.SDCardUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.rotai.dtjclient.base.Constant.SDCARD_PATH;
import static com.rotai.dtjclient.base.Constant.fileName_rotai;

/**
 * 检查内存卡中是否有指定的视频，没有就开始下载，下载完成以后跳转到图片广告轮播
 * 检查内存卡中是否有服务，没有就开始下载，下载完成以后跳转到图片广告轮播
 */

public class LoadingActivity extends BaseActivity {

    private List<File> fileList = new ArrayList<>();

    OkHttpClient okHttpClient = new OkHttpClient();

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    startActivity(new Intent(LoadingActivity.this,SplashActivity.class));
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        HandlerThread worker = new HandlerThread(TAG + this.getClass().getSimpleName() + "_Woker");
        worker.start();
        Handler queue = new Handler(worker.getLooper());

        List<String> videoPaths = SDCardUtil.getAllVideoFilePath(fileList, SDCARD_PATH, "mp4");
        List<String> apkPaths = SDCardUtil.getAllAPKFilePath(fileList, SDCARD_PATH, "apk");

        if ((videoPaths == null) || (!videoPaths.contains(SDCARD_PATH + fileName_rotai))) {
            queue.post(new downloadAD());
        } else {
            startActivity(new Intent(LoadingActivity.this, SplashActivity.class));
        }
    }

    /**
     * 下载视频广告
     */
    class downloadAD implements Runnable {
        @Override
        public void run() {

            //构建请求
            Request request = new Request.Builder()
                    .get()
                    .url(Constant.adUrl)
                    .build();
            Call call = okHttpClient.newCall(request);

            //call加入请求队列
            call.enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    LogUtil.e(TAG, "onFailure2: ");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    final InputStream is = response.body().byteStream();

                    long total = response.body().contentLength();
                    int sum = 0;
                    int len;
                    byte[] buf = new byte[1024];
                    File file = new File(SDCARD_PATH, fileName_rotai);
                    FileOutputStream fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        LogUtil.e(TAG, "onResponse: " + sum + "/" + total);
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                    LogUtil.e(TAG, "onResponse: 下载成功");

                    Message message = new Message();
                    message.what=1;
                    handler.sendMessage(message);
                }
            });
        }
    }


}
